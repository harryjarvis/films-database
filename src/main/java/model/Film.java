package model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "film")
public class Film {

    private int id;
    private String title;
    private int year;
    private String director;
    private String stars;
    private String review;

    public Film(int id, String title, int year, String director, String stars, String review) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.stars = stars;
        this.review = review;
    }

    public Film() {}
    
    @XmlElement
    public int getId() { return id; }
    
    public void setId(int id) { this.id = id; }
    
    @XmlElement
    public String getTitle() { return title; }
    
    public void setTitle(String title) { this.title = title; }

    @XmlElement
    public int getYear() { return year; }
    
    public void setYear(int year) { this.year = year; }

    @XmlElement
    public String getDirector() { return director; }
    
    public void setDirector(String director) { this.director = director; }

    @XmlElement
    public String getStars() { return stars; }
    
    public void setStars(String stars) { this.stars = stars; }

    @XmlElement
    public String getReview() { return review; }
    
    public void setReview(String review) { this.review = review; }

    @Override
    public String toString() {
        return id + "|" + title + "|" + year + "|" + director + "|" + stars + "|" + review;
    }
}