package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import model.Films;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import dao.FilmDAO;
import model.Film;
import java.io.BufferedReader;
import java.io.StringReader;

@WebServlet("/Filmsapi")
public class RESTController extends HttpServlet {

    // sends a single film in the requested format, writing it to the response
	// Defaults to JSON
    private void sendFilmResponse(HttpServletRequest request, HttpServletResponse response, Film film)
            throws Exception {
        String format = request.getParameter("format");
        PrintWriter out = response.getWriter();

        if ("xml".equals(format)) {
            response.setContentType("text/xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Film.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            marshaller.marshal(film, sw);
            out.write(sw.toString());
        } else if ("text".equals(format)) {
            response.setContentType("text/plain");
            out.write(film.toString());
        } else {
            response.setContentType("application/json");
            Gson gson = new Gson();
            out.write(gson.toJson(film));
        }

        out.close();
    }

    // sends a list of films in the requested format
    // Two seperate methods because JAXB requires a wrapper class for arraylists
    private void sendFilmsResponse(HttpServletRequest request, HttpServletResponse response, ArrayList<Film> films)
            throws Exception {
        String format = request.getParameter("format");
        PrintWriter out = response.getWriter();

        if ("xml".equals(format)) {
            response.setContentType("text/xml");
            Films wrapper = new Films();
            wrapper.setFilms(films);
            JAXBContext jaxbContext = JAXBContext.newInstance(Films.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            marshaller.marshal(wrapper, sw);
            out.write(sw.toString());
        } else if ("text".equals(format)) {
            response.setContentType("text/plain");
            for (Film f : films) {
                out.write(f.toString() + "\n");
            }
        } else {
            response.setContentType("application/json");
            Gson gson = new Gson();
            out.write(gson.toJson(films));
        }

        out.close();
    }

    // extracts a film from the request body, supports JSON, XML and form parameters
    private Film extractFilmFromRequest(HttpServletRequest request) throws Exception {
        String contentType = request.getContentType();

        // JSON input
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            if (body.trim().isEmpty()) {
                throw new Exception("Request is empty");
            }
            Gson gson = new Gson();
            Film film = gson.fromJson(body, Film.class);
            return film;
        }

        // XML input
        if (contentType != null && contentType.toLowerCase().contains("xml")) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            if (body.trim().isEmpty()) {
                throw new Exception("Request body is empty");
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(Film.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Film film = (Film) jaxbUnmarshaller.unmarshal(new StringReader(body));
            return film;
        }

        // Form parameters
        String idParam   = request.getParameter("id");
        String title     = request.getParameter("title");
        String yearParam = request.getParameter("year");
        String director  = request.getParameter("director");
        String stars     = request.getParameter("stars");
        String review    = request.getParameter("review");

        if (title == null || yearParam == null || director == null
                || stars == null || review == null) {
            throw new Exception("Missing required film parameters");
        }

        int year;
        try {
            year = Integer.parseInt(yearParam.trim());
        } catch (NumberFormatException e) {
            throw new Exception("Invalid year format");
        }

        int id = 0;
        if (idParam != null && !idParam.trim().isEmpty()) {
            id = Integer.parseInt(idParam.trim());
        }

        return new Film(id, title, year, director, stars, review);
    }

    // Handles GET by ID, GET by title and GET all
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FilmDAO dao = new FilmDAO();
        String idInput    = request.getParameter("id");
        String titleInput = request.getParameter("title");
        PrintWriter out   = response.getWriter();

        try {
            // GET by ID
            if (idInput != null && !idInput.trim().isEmpty()) {
                int id = Integer.parseInt(idInput);
                Film film = dao.getFilmByID(id);

                if (film == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("Film not found");
                    out.close();
                    return;
                }

                sendFilmResponse(request, response, film);

            // GET by title search
            } else if (titleInput != null && !titleInput.trim().isEmpty()) {
                ArrayList<Film> films = dao.searchFilms(titleInput.trim());
                sendFilmsResponse(request, response, films);

            // GET all
            } else {
                ArrayList<Film> films = dao.getAllFilms();
                sendFilmsResponse(request, response, films);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("Server error: " + e.getMessage());
            out.close();
        }
    }

    // Inserts new film into database, 201 on success
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FilmDAO dao = new FilmDAO();
        PrintWriter out = response.getWriter();

        try {
            Film film = extractFilmFromRequest(request);

            if (film.getTitle().trim().isEmpty() || film.getDirector().trim().isEmpty()
                    || film.getStars().trim().isEmpty() || film.getReview().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("All fields are required");
                out.close();
                return;
            }

            if (film.getYear() <= 1900) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("Year must be a valid number");
                out.close();
                return;
            }

            dao.insertFilm(film);
            response.setStatus(HttpServletResponse.SC_CREATED);
            sendFilmResponse(request, response, film);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("Error: " + e.getMessage());
            out.close();
        }
    }

    // Updates film, id must be included in the body to identify which film to update
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FilmDAO dao = new FilmDAO();
        PrintWriter out = response.getWriter();

        try {
            Film film = extractFilmFromRequest(request);

            if (film.getTitle().trim().isEmpty() || film.getDirector().trim().isEmpty()
                    || film.getStars().trim().isEmpty() || film.getReview().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("All fields are required");
                out.close();
                return;
            }

            if (film.getYear() <= 1900) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("Year must be a positive number");
                out.close();
                return;
            }

            dao.updateFilm(film);
            response.setStatus(HttpServletResponse.SC_OK);
            sendFilmResponse(request, response, film);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("Error: " + e.getMessage());
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FilmDAO dao = new FilmDAO();
        String idInput = request.getParameter("id");
        PrintWriter out = response.getWriter();

        if (idInput == null || idInput.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("Film id is required");
            out.close();
            return;
        }

        try {
            int id = Integer.parseInt(idInput);
            dao.deleteFilm(id);
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("Film deleted successfully");
            out.close();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("Server error: " + e.getMessage());
            out.close();
        }
    }
}