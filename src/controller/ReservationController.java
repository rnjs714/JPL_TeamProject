package controller;

import java.util.List;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import service.ApiException;
import service.ApiService;
import session.UserSession;

public class ReservationController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final NavigationController navigationController;

    public ReservationController(ApiService apiService, UserSession userSession,
            NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.navigationController = navigationController;
    }

    public List<Reservation> loadReservations() {
        return apiService.getReservationList(userSession.getCurrentUser().getId());
    }

    public Showtime getShowtime(String showtimeId) {
        return apiService.getShowtime(showtimeId);
    }

    public Movie getMovie(String movieId) {
        return apiService.getMovie(movieId);
    }

    public Theater getTheater(String theaterId) {
        return apiService.getTheater(theaterId);
    }

    public void cancelReservation(String reservationId) {
        if (navigationController.showConfirmation("Do you want to cancel this reservation?")) {
            try {
                Reservation reservation = apiService.requestCancelReservation(reservationId, userSession.getCurrentUser().getId());
                navigationController.showMessage("Reservation cancelled successfully.\nReservation ID: " + reservation.getId());
                navigationController.showReservations();
            } catch (ApiException e) {
                navigationController.showMessage(e.getMessage());
            }
        }
    }
}
