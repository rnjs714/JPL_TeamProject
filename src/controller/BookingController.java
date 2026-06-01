package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import service.ApiException;
import service.ApiService;
import session.BookingSession;
import session.UserSession;

public class BookingController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;

    public BookingController(ApiService apiService, UserSession userSession, BookingSession bookingSession,
            NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
    }

    public Theater getTheater(String theaterId) {
        return apiService.getTheater(theaterId);
    }

    public Movie getSelectedMovie() {
        return bookingSession.getSelectedMovie();
    }

    public Theater getSelectedTheater() {
        return bookingSession.getSelectedTheater();
    }

    public Showtime getSelectedShowtime() {
        return bookingSession.getSelectedShowtime();
    }

    public List<String> getSelectedSeats() {
        return bookingSession.getSelectedSeats();
    }

    public Set<String> getReservedSeats() {
        return bookingSession.getSelectedShowtime().getReservedSeats();
    }

    public void resetSelectedMovie() {
        bookingSession.setSelectedMovie(null);
        bookingSession.setSelectedShowtime(null);
        bookingSession.setSelectedTheater(null);
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    public void resetSelectedShowtime() {
        bookingSession.setSelectedShowtime(null);
        bookingSession.setSelectedTheater(null);
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    public void resetSelectedSeats() {
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    public void toggleSeat(String seatCode) {
        List<String> selectedSeats = bookingSession.getSelectedSeats();
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
        } else {
            selectedSeats.add(seatCode);
        }
    }

    public List<Movie> loadMovies() {
        try {
            List<Movie> movies = apiService.getMovieList();
            if(movies.isEmpty()) {
                throw new IllegalStateException("No movies found.");
            }
            return movies;
        } catch (ApiException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void selectMovie(Movie movie) {
        bookingSession.setSelectedMovie(movie);
        navigationController.showShowtimes();
    }

    public List<Showtime> loadShowtimes() {
        // TODO: 선택된 영화 id로 LIST_SHOWTIMES 요청을 보내고 List<Showtime>으로 변환해서 반환한다.
        try {
            List<Showtime> showtimes = apiService.getShowtimeList(bookingSession.getSelectedMovie().getId());
            if(showtimes.isEmpty()) {
                throw new IllegalStateException("No showtimes found for the selected movie.");
            }
            return showtimes;
        } catch (ApiException e) {
            throw new IllegalStateException(e.getMessage());
        } 
    }

    public void selectShowtime(Showtime showtime) {
        // TODO: showtime.theaterId로 상영관 정보를 조회해서 bookingSession.setSelectedTheater(...)를 호출한다.
        bookingSession.setSelectedShowtime(showtime);
        bookingSession.setSelectedTheater(apiService.getTheater(showtime.getTheaterId()));
        navigationController.showSeats();
    }

    public void reserveSelectedSeats() {
        // TODO: 로그인 사용자 id, 선택된 showtime id, 선택 좌석 목록으로 RESERVE 요청을 보낸다.
        // TODO: 성공하면 BookingSession 선택 값을 초기화하고 예매 내역 화면으로 이동한다.
        try {
            if(bookingSession.getSelectedSeats().isEmpty()) {
                navigationController.showMessage("Please select seats to reserve.");
                return;
            }
            Reservation reservation = apiService.requestReservation(userSession.getCurrentUser().getId(), 
                                            bookingSession.getSelectedShowtime().getId(), 
                                            bookingSession.getSelectedSeats());
            navigationController.showMessage("Reservation successful!\nReservation ID: " + reservation.getId());
            navigationController.showReservations();
            resetSelectedMovie();
        } catch (ApiException e) {
            navigationController.showMessage(e.getMessage());
        }
    }
}
