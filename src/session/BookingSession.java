package session;

import java.util.ArrayList;
import java.util.List;

import domain.Movie;
import domain.Showtime;
import domain.Theater;

public class BookingSession {
    private Movie selectedMovie;
    private Showtime selectedShowtime;
    private Theater selectedTheater;
    private List<String> selectedSeats;

    public BookingSession() {
        this.selectedSeats = new ArrayList<>();
    }

    public Movie getSelectedMovie() {
        return selectedMovie;
    }

    public void setSelectedMovie(Movie selectedMovie) {
        this.selectedMovie = selectedMovie;
    }

    public Showtime getSelectedShowtime() {
        return selectedShowtime;
    }

    public void setSelectedShowtime(Showtime selectedShowtime) {
        this.selectedShowtime = selectedShowtime;
    }

    public Theater getSelectedTheater() {
        return selectedTheater;
    }

    public void setSelectedTheater(Theater selectedTheater) {
        this.selectedTheater = selectedTheater;
    }

    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<String> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }
}
