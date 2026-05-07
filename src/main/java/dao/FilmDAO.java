package dao;

import java.sql.*;
import java.util.ArrayList;
import model.Film;

public class FilmDAO {
	
	// Centralised database connection
	Connection conn = null;
	private final String user = "";
	private final String password = "";
	private final String url = ""+user;

	public FilmDAO() {}

	// Open a connection to the database before each query
	private void openConnection() throws SQLException {
    try {
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(url, user, password);
    } catch (ClassNotFoundException e) {
        throw new SQLException("JDBC Driver not found", e);
      }
	}

	// Always called even if there is an error
	private void closeConnection() {
	    try {
	        if (conn != null && !conn.isClosed()) {
	            conn.close();
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	private Film getNextFilm(ResultSet rs) throws SQLException {
		return new Film(
			rs.getInt("id"),
			rs.getString("title"),
			rs.getInt("year"),
			rs.getString("director"),
			rs.getString("stars"),
			rs.getString("review")
		);
	}

	public ArrayList<Film> getAllFilms() {
		
		ArrayList<Film> allFilms = new ArrayList<>();
		try {
			openConnection();

			String sql = "SELECT * FROM films";

			try (PreparedStatement pstmt = conn.prepareStatement(sql); 
				ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Film oneFilm = getNextFilm(rs);
						allFilms.add(oneFilm);
					}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}

		return allFilms;
	}

	// Get a set number of films to limit usage - useful because database holds 1300+ films
	public ArrayList<Film> getFilmsPaginated(int offset, int limit) {

		ArrayList<Film> films = new ArrayList<>();

		try {
			openConnection();

			String sql = "SELECT * FROM films LIMIT ? OFFSET ?";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, limit);
				pstmt.setInt(2, offset);

				try(ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Film oneFilm = getNextFilm(rs);
						films.add(oneFilm);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		return films;
	}

   public Film getFilmByID(int id) {

		Film oneFilm = null;

			try {
				openConnection();

				String sql = "SELECT * FROM films WHERE id = ?";

				try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

					pstmt.setInt(1, id);

					try (ResultSet rs = pstmt.executeQuery()) {

						if (rs.next()) {
							oneFilm = getNextFilm(rs);
						}
					}
				}
			} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					closeConnection();
				}
				return oneFilm;
	}
   
   public void insertFilm(Film film) {
    try {
        openConnection();

        String sql = "INSERT INTO films (title, year, director, stars, review) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, film.getTitle());
            pstmt.setInt(2, film.getYear());
            pstmt.setString(3, film.getDirector());
            pstmt.setString(4, film.getStars());
            pstmt.setString(5, film.getReview());
            pstmt.executeUpdate();
        }

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
   
   public void updateFilm(Film film) {
    try {
        openConnection();

        String sql = "UPDATE films SET title = ?, year = ?, director = ?, stars = ?, review = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, film.getTitle());
            pstmt.setInt(2, film.getYear());
            pstmt.setString(3, film.getDirector());
            pstmt.setString(4, film.getStars());
            pstmt.setString(5, film.getReview());
            pstmt.setInt(6, film.getId());
            pstmt.executeUpdate();
        }

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
   
   public void deleteFilm(int id) {

		try {
			openConnection();

			String sql = "DELETE FROM films WHERE id = ?";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setInt(1, id);

				pstmt.executeUpdate();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
   
   public ArrayList<Film> searchFilms(String searchStr) {

	    ArrayList<Film> results = new ArrayList<>();
	    
		try {
			openConnection();

			// I use LIKE and LOWER to improve the broadness of the search
			String sql = "SELECT * FROM films WHERE LOWER(title) LIKE ? OR CAST(year AS CHAR) LIKE ? OR LOWER(director) LIKE ? OR LOWER(stars) LIKE ? OR LOWER(review) LIKE ?";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

				String search = "%" + searchStr.toLowerCase() + "%";

				pstmt.setString(1, search);
				pstmt.setString(2, search);
				pstmt.setString(3, search);
				pstmt.setString(4, search);
				pstmt.setString(5, search);

				ResultSet rs = pstmt.executeQuery();

				while (rs.next()) {
					results.add(getNextFilm(rs));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}

	    return results;
	}
   
   // Helper method for controllers
    public boolean filmExists(int id) {

	    try {
			openConnection();

			String sql = "SELECT 1 FROM films WHERE id = ?";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setInt(1, id);

				ResultSet rs = pstmt.executeQuery();
				return rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}

	    return false;
	}
   
}
