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

// 예매 흐름 제어
public class BookingController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;

    // 의존 객체 연결
    public BookingController(ApiService apiService, UserSession userSession, BookingSession bookingSession,
            NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
    }

    // 상영관 조회
    public Theater getTheater(String theaterId) {
        return apiService.getTheater(theaterId);
    }

    // 선택 영화 조회
    public Movie getSelectedMovie() {
        return bookingSession.getSelectedMovie();
    }

    // 선택 상영관 조회
    public Theater getSelectedTheater() {
        return bookingSession.getSelectedTheater();
    }

    // 선택 상영 일정 조회
    public Showtime getSelectedShowtime() {
        return bookingSession.getSelectedShowtime();
    }

    // 선택 좌석 조회
    public List<String> getSelectedSeats() {
        return bookingSession.getSelectedSeats();
    }

    // 예약 좌석 조회
    public Set<String> getReservedSeats() {
        return bookingSession.getSelectedShowtime().getReservedSeats();
    }

    // 영화 선택 초기화
    public void resetSelectedMovie() {
        bookingSession.setSelectedMovie(null);
        bookingSession.setSelectedShowtime(null);
        bookingSession.setSelectedTheater(null);
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    // 상영 일정 선택 초기화
    public void resetSelectedShowtime() {
        bookingSession.setSelectedShowtime(null);
        bookingSession.setSelectedTheater(null);
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    // 좌석 선택 초기화
    public void resetSelectedSeats() {
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    // 좌석 선택 토글
    public void toggleSeat(String seatCode) {
        List<String> selectedSeats = bookingSession.getSelectedSeats();
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
        } else {
            selectedSeats.add(seatCode);
        }
    }

    // 영화 목록 로드
    public List<Movie> loadMovies() {
        try {
            List<Movie> movies = apiService.getMovieList();
            if(movies.isEmpty()) { // 영화가 없는 경우 처리
                throw new IllegalStateException("No movies found.");
            }
            return movies;
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            throw new IllegalStateException(e.getMessage());
        }
    }

    // 영화 선택 처리
    public void selectMovie(Movie movie) {
        bookingSession.setSelectedMovie(movie);
        navigationController.showShowtimes();
    }

    // 상영 일정 목록 로드
    public List<Showtime> loadShowtimes() {
        try {
            List<Showtime> showtimes = apiService.getShowtimeList(bookingSession.getSelectedMovie().getId());
            if(showtimes.isEmpty()) { // 상영 일정이 없는 경우 처리
                throw new IllegalStateException("No showtimes found for the selected movie.");
            }
            return showtimes;
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            throw new IllegalStateException(e.getMessage());
        } 
    }

    // 상영 일정 선택 처리
    public void selectShowtime(Showtime showtime) {
        bookingSession.setSelectedShowtime(showtime);
        bookingSession.setSelectedTheater(apiService.getTheater(showtime.getTheaterId()));
        navigationController.showSeats();
    }

    // 선택 좌석 예매 요청
    public void reserveSelectedSeats() {
        try {
            List<String> selectedSeats = bookingSession.getSelectedSeats();
            if(selectedSeats == null || selectedSeats.isEmpty()) { // 좌석이 선택되지 않은 경우 처리
                navigationController.showMessage("Please select seats to reserve.");
                return;
            }
            Reservation reservation = apiService.requestReservation(userSession.getCurrentUser().getId(), 
                                            bookingSession.getSelectedShowtime().getId(), 
                                            bookingSession.getSelectedSeats());
            navigationController.showMessage("Reservation successful!\nReservation ID: " + reservation.getId());
            navigationController.showReservations();
            resetSelectedMovie();
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            navigationController.showMessage(e.getMessage());
        }
    }
}
