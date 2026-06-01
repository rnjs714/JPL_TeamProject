package service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import client.ApiClient;
import domain.User;
import domain.Movie;
import domain.Theater;
import domain.Showtime;
import domain.Reservation;
import protocol.Response;

public class ApiService {
    private final ApiClient apiClient;

    public ApiService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public User login(String id, String password) {
        return requestData("LOGIN", Map.of("id", id, "password", password), new TypeReference<User>() {},
                "Login failed");
    }

    public void register(String id, String password) {
        sendRequest("REGISTER", Map.of("id", id, "password", password), "Registration failed");
    }

    public Movie getMovie(String movieId) {
        return requestData("GET_MOVIES", Map.of("movieId", movieId), new TypeReference<Movie>() {},
                "Failed to load movie information");
    }

    public Theater getTheater(String theaterId) {
        return requestData("GET_THEATER", Map.of("theaterId", theaterId), new TypeReference<Theater>() {},
                "Failed to load theater information");
    }

    public Showtime getShowtime(String showtimeId) {
        return requestData("GET_SHOWTIME", Map.of("showtimeId", showtimeId), new TypeReference<Showtime>() {},
                "Failed to load showtime information");
    }

    public List<Movie> getMovieList() {
        return requestData("LIST_MOVIES", Map.of(), new TypeReference<List<Movie>>() {},
                "Failed to load movie list");
    }

    public List<Showtime> getShowtimeList(String movieId) {
        return requestData("LIST_SHOWTIMES", Map.of("movieId", movieId), new TypeReference<List<Showtime>>() {},
                "Failed to load showtime list");
    }

    public List<Reservation> getReservationList(String userId) {
        return requestData("LIST_RESERVATIONS", Map.of("userId", userId),
                new TypeReference<List<Reservation>>() {}, "Failed to load reservation list");
    }

    public Reservation requestReservation(String userId, String showtimeId, List<String> selectedSeats) {
        return requestData("RESERVE", Map.of(
                "userId", userId,
                "showtimeId", showtimeId,
                "seatCodes", selectedSeats), new TypeReference<Reservation>() {}, "Reservation request failed");
    }

    public Reservation requestCancelReservation(String reservationId, String userId) {
        return requestData("CANCEL_RESERVATION", Map.of(
                "reservationId", reservationId,
                "requesterId", userId), new TypeReference<Reservation>() {}, "Reservation cancellation request failed");
    }

    private <T> T requestData(String command, Map<String, Object> body, TypeReference<T> dataType,
            String failureMessage) {
        try {
            return apiClient.getDataFromServer(command, body, dataType);
        } catch (ClassCastException e) {
            throw new ApiException(failureMessage + ": Invalid response data format.");
        } catch (IllegalStateException e) {
            throw new ApiException(failureMessage + "\n" + e.getMessage());
        }
    }

    private void sendRequest(String command, Map<String, Object> body, String failureMessage) {
        try {
            Response response = apiClient.send(command, body);
            if(!response.isSuccess()) {
                throw new ApiException(failureMessage + "\n" + response.getMessage());
            }
        } catch (IllegalStateException e) {
            throw new ApiException(failureMessage + "\n" + e.getMessage());
        }
    }
}
