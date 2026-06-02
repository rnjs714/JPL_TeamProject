package controller;

import java.util.List;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import service.ApiException;
import service.ApiService;
import session.UserSession;

// 예매 내역 흐름 제어
public class ReservationController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final NavigationController navigationController;

    // 의존 객체 연결
    public ReservationController(ApiService apiService, UserSession userSession,
            NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.navigationController = navigationController;
    }

    // 예매 내역 로드
    public List<Reservation> loadReservations() {
        try {
            List<Reservation> reservations = apiService.getReservationList(userSession.getCurrentUser().getId());
            if (reservations.isEmpty()) { // 예매 내역이 없는 경우 처리
                throw new IllegalStateException("No reservations found.");
            }
            return reservations;
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            throw new IllegalStateException(e.getMessage());
        }
        
    }

    // 상영 일정 조회
    public Showtime getShowtime(String showtimeId) {
        return apiService.getShowtime(showtimeId);
    }

    // 영화 조회
    public Movie getMovie(String movieId) {
        return apiService.getMovie(movieId);
    }

    // 상영관 조회
    public Theater getTheater(String theaterId) {
        return apiService.getTheater(theaterId);
    }

    // 예매 취소 처리
    public void cancelReservation(String reservationId) {
        if (navigationController.showConfirmation("Do you want to cancel this reservation?")) { // 사용자 확인
            try {
                Reservation reservation = apiService.requestCancelReservation(reservationId, userSession.getCurrentUser().getId());
                navigationController.showMessage("Reservation cancelled successfully.\nReservation ID: " + reservation.getId());
                navigationController.showReservations();
            } catch (ApiException e) { // API 호출 실패 시 예외 처리
                navigationController.showMessage(e.getMessage());
            }
        }
    }
}
