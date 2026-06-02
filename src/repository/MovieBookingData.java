package repository;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import domain.User;
import domain.GroupReservation;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 파일에 저장되는 전체 데이터 묶음이다.
 *
 * 서버는 이 객체를 파일에서 읽어 온 뒤 회원, 영화, 상영 시간, 예매 정보를 수정하고
 * 다시 JSON 파일로 저장한다. 새로 추가된 그룹 예매 정보도 여기에서 함께 관리한다.
 */
public class MovieBookingData {
    private List<User> users;
    private List<Movie> movies;
    private List<Theater> theaters;
    private List<Showtime> showtimes;
    private List<Reservation> reservations;
    // 그룹 예매는 개인 예매와 다르게 결제 대기 상태가 있으므로 별도 목록으로 저장한다.
    private List<GroupReservation> groupReservations;

    public MovieBookingData() {
        this.users = new ArrayList<>();
        this.movies = new ArrayList<>();
        this.theaters = new ArrayList<>();
        this.showtimes = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.groupReservations = new ArrayList<>();
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

    public List<GroupReservation> getGroupReservations() {
        // 기존 JSON 파일에는 groupReservations 항목이 없을 수 있어 null 방어 처리를 한다.
        if(groupReservations == null) {
            groupReservations = new ArrayList<>();
        }
        return groupReservations;
    }

    public void setGroupReservations(List<GroupReservation> groupReservations) {
        this.groupReservations = groupReservations;
    }
}
