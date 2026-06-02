package repository;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import domain.User;

import java.util.ArrayList;
import java.util.List;

// JSON 루트 데이터
public class MovieBookingData {
    private List<User> users;
    private List<Movie> movies;
    private List<Theater> theaters;
    private List<Showtime> showtimes;
    private List<Reservation> reservations;

    // 빈 데이터 초기화
    public MovieBookingData() {
        this.users = new ArrayList<>();
        this.movies = new ArrayList<>();
        this.theaters = new ArrayList<>();
        this.showtimes = new ArrayList<>();
        this.reservations = new ArrayList<>();
    }

    // 사용자 목록 접근자
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    // 영화 목록 접근자
    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    // 상영관 목록 접근자
    public List<Theater> getTheaters() {
        return theaters;
    }

    public void setTheaters(List<Theater> theaters) {
        this.theaters = theaters;
    }

    // 상영 일정 목록 접근자
    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    // 예매 목록 접근자
    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
