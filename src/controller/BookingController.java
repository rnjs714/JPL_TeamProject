package controller;

import client.ApiClient;
import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import protocol.Response;
import session.BookingSession;
import session.UserSession;

import java.util.List;
import java.util.Map;

public class BookingController {
    private ApiClient apiClient;
    private UserSession userSession;
    private BookingSession bookingSession;
    private NavigationController navigationController;

    public BookingController(ApiClient apiClient, UserSession userSession, BookingSession bookingSession,
            NavigationController navigationController) {
        this.apiClient = apiClient;
        this.userSession = userSession;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
    }

    public List<Movie> loadMovies() {
        // TODO: LIST_MOVIES 요청을 보내고 response.data를 List<Movie>로 변환해서 반환한다.
        Response response = apiClient.send("LIST_MOVIES", Map.of());
        return List.of();
    }

    public void selectMovie(Movie movie) {
        bookingSession.setSelectedMovie(movie);
        navigationController.showShowtimes();
    }

    public List<Showtime> loadShowtimes() {
        // TODO: 선택된 영화 id로 LIST_SHOWTIMES 요청을 보내고 List<Showtime>으로 변환해서 반환한다.
        Response response = apiClient.send("LIST_SHOWTIMES",
                Map.of("movieId", bookingSession.getSelectedMovie().getId()));
        return List.of();
    }

    public void selectShowtime(Showtime showtime) {
        // TODO: showtime.theaterId로 상영관 정보를 조회해서 bookingSession.setSelectedTheater(...)를 호출한다.
        bookingSession.setSelectedShowtime(showtime);
        navigationController.showSeats();
    }

    public Reservation reserveSelectedSeats() {
        // TODO: 로그인 사용자 id, 선택된 showtime id, 선택 좌석 목록으로 RESERVE 요청을 보낸다.
        // TODO: 성공하면 BookingSession을 clear하고 예매 내역 화면으로 이동한다.
        Response response = apiClient.send("RESERVE", Map.of(
                "userId", userSession.getCurrentUser().getId(),
                "showtimeId", bookingSession.getSelectedShowtime().getId(),
                "seatCodes", bookingSession.getSelectedSeats()));
        return null;
    }

    public List<Reservation> loadReservations() {
        // TODO: LIST_RESERVATIONS 요청을 보내고 response.data를 List<Reservation>으로 변환해서 반환한다.
        Response response = apiClient.send("LIST_RESERVATIONS",
                Map.of("userId", userSession.getCurrentUser().getId()));
        return List.of();
    }

    public void cancelReservation(String reservationId) {
        // TODO: CANCEL_RESERVATION 요청을 보내고, 성공하면 예매 내역 화면을 새로고침한다.
        apiClient.send("CANCEL_RESERVATION", Map.of(
                "reservationId", reservationId,
                "userId", userSession.getCurrentUser().getId()));
    }
}
