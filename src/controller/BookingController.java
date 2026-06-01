package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import client.ApiClient;
import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import protocol.Response;
import session.BookingSession;
import session.UserSession;

public class BookingController {
    private final ApiClient apiClient;
    private final UserSession userSession;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;

    public BookingController(ApiClient apiClient, UserSession userSession, BookingSession bookingSession,
            NavigationController navigationController) {
        this.apiClient = apiClient;
        this.userSession = userSession;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
    }

    public Showtime getShowtime(String showtimeId) {
        return apiClient.getDataFromServer("GET_SHOWTIME", Map.of("showtimeId", showtimeId), new TypeReference<Showtime>() {});
    }

    public Movie getMovie(String movieId) {
        return apiClient.getDataFromServer("GET_MOVIES", Map.of("movieId", movieId), new TypeReference<Movie>() {});
    }

    public Theater getTheater(String theaterId) {
        return apiClient.getDataFromServer("GET_THEATER", Map.of("theaterId", theaterId), new TypeReference<Theater>() {});
    }

    public Movie getSelectedMovie() {
        return bookingSession.getSelectedMovie();
    }

    public Theater getSelectedTheater() {
        return bookingSession.getSelectedTheater();
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
        // TODO: LIST_MOVIES 요청을 보내고 response.data를 List<Movie>로 변환해서 반환한다.
        try {
            List<Movie> movies = apiClient.getDataFromServer("LIST_MOVIES", Map.of(), new TypeReference<List<Movie>>() {});
            return movies;
        } catch (ClassCastException e) {
            throw new IllegalStateException("영화 목록 로드 실패: 응답 데이터 형식이 올바르지 않습니다.", e);
        } catch (IllegalStateException e) {
            throw e;
        }
        
    }

    public void selectMovie(Movie movie) {
        bookingSession.setSelectedMovie(movie);
        navigationController.showShowtimes();
    }

    public List<Showtime> loadShowtimes() {
        // TODO: 선택된 영화 id로 LIST_SHOWTIMES 요청을 보내고 List<Showtime>으로 변환해서 반환한다.
        try {
            List<Showtime> showtimes = apiClient.getDataFromServer("LIST_SHOWTIMES",
                Map.of("movieId", bookingSession.getSelectedMovie().getId()), new TypeReference<List<Showtime>>() {});
            return showtimes;
        } catch (ClassCastException e) {
            throw new IllegalStateException("상영 시간 로드 실패: 응답 데이터 형식이 올바르지 않습니다.", e);
        } catch (IllegalStateException e) {
            throw e;
        }
        
    }

    public void selectShowtime(Showtime showtime) {
        // TODO: showtime.theaterId로 상영관 정보를 조회해서 bookingSession.setSelectedTheater(...)를 호출한다.
        bookingSession.setSelectedShowtime(showtime);
        bookingSession.setSelectedTheater(apiClient.getDataFromServer("GET_THEATER", Map.of(
            "theaterId", showtime.getTheaterId()), new TypeReference<Theater>() {}));
        navigationController.showSeats();
    }

    public void reserveSelectedSeats() {
        // TODO: 로그인 사용자 id, 선택된 showtime id, 선택 좌석 목록으로 RESERVE 요청을 보낸다.
        // TODO: 성공하면 BookingSession 선택 값을 초기화하고 예매 내역 화면으로 이동한다.
        try {
            if(bookingSession.getSelectedSeats().isEmpty()) {
                throw new IllegalStateException("예약할 좌석을 선택해주세요.");
            }
            Response response = apiClient.send("RESERVE", Map.of(
                "userId", userSession.getCurrentUser().getId(),
                "showtimeId", bookingSession.getSelectedShowtime().getId(),
                "seatCodes", bookingSession.getSelectedSeats()));
            if(response.isSuccess()) {
                navigationController.showMessage("Reservation successful!");
                resetSelectedMovie();
                navigationController.showReservations();
            } else {
                navigationController.showMessage(response.getMessage());
            }
        } catch (IllegalStateException e) {
            navigationController.showMessage("Reservation failed: " + e.getMessage());
        } catch (Exception e) {
            navigationController.showMessage("An error occurred: " + e.getMessage());
        }
    }

    public List<Reservation> loadReservations() {
        // TODO: LIST_RESERVATIONS 요청을 보내고 response.data를 List<Reservation>으로 변환해서 반환한다.
        try {
            List<Reservation> reservations = apiClient.getDataFromServer("LIST_RESERVATIONS",
                    Map.of("userId", userSession.getCurrentUser().getId()), new TypeReference<List<Reservation>>() {});
            return reservations;
        } catch (ClassCastException e) {
            throw new IllegalStateException("예매 내역 로드 실패: 응답 데이터 형식이 올바르지 않습니다.", e);
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    public void cancelReservation(String reservationId) {
        // TODO: CANCEL_RESERVATION 요청을 보내고, 성공하면 예매 내역 화면을 새로고침한다.
        if(navigationController.showConfirmation("Do you want to cancel this reservation?")) {
            try {
                Response response = apiClient.send("CANCEL_RESERVATION", Map.of(
                    "reservationId", reservationId,
                    "requesterId", userSession.getCurrentUser().getId()));
                if(response.isSuccess()) {
                    navigationController.showMessage("Reservation cancelled successfully.");
                    navigationController.showReservations();
                } else {
                    navigationController.showMessage(response.getMessage());
                }
            } catch (IllegalStateException e) {
                navigationController.showMessage("Failed to cancel reservation: " + e.getMessage());
            } catch (Exception e) {
                navigationController.showMessage("An error occurred: " + e.getMessage());
            }
        }
    }
}
