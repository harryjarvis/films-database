package model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "films")
public class Films {

    private List<Film> films;

    // REQUIRED for JAXB
    public Films() {}

    @XmlElement(name = "film")
    public List<Film> getFilms() {
        return films;
    }

    public void setFilms(List<Film> films) {
        this.films = films;
    }
}