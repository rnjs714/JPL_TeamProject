package repository;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import domain.User;

import java.util.ArrayList;
import java.util.List;

public class MovieBookingData {
    private List<User> users;
    private List<Movie> movies;
    private List<Theater> theaters;
    private List<Showtime> showtimes;
    private List<Reservation> reservations;

    public MovieBookingData() {
        this.users = new ArrayList<>();
        this.movies = new ArrayList<>();
        this.theaters = new ArrayList<>();
        this.showtimes = new ArrayList<>();
        this.reservations = new ArrayList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Theater> getTheaters() {
        return theaters;
    }

    public void setTheaters(List<Theater> theaters) {
        this.theaters = theaters;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
