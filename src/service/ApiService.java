package service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import client.SocketClient;
import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import domain.User;
import protocol.Response;

// 서버 API 호출 모음
public class ApiService {
    private final SocketClient socketClient;

    // API 클라이언트 연결
    public ApiService(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    // 로그인 요청
    public User login(String id, String password) {
        return requestData("LOGIN", Map.of("id", id, "password", password), new TypeReference<User>() {},
                "[Login failed]");
    }

    // 회원가입 요청
    public void register(String id, String password) {
        sendRequest("REGISTER", Map.of("id", id, "password", password), "[Registration failed]");
    }

    // 영화 단건 조회
    public Movie getMovie(String movieId) {
        return requestData("GET_MOVIES", Map.of("movieId", movieId), new TypeReference<Movie>() {},
                "[Failed to load movie information]");
    }

    // 상영관 단건 조회
    public Theater getTheater(String theaterId) {
        return requestData("GET_THEATER", Map.of("theaterId", theaterId), new TypeReference<Theater>() {},
                "[Failed to load theater information]");
    }

    // 상영 일정 단건 조회
    public Showtime getShowtime(String showtimeId) {
        return requestData("GET_SHOWTIME", Map.of("showtimeId", showtimeId), new TypeReference<Showtime>() {},
                "[Failed to load showtime information]");
    }

    // 영화 목록 조회
    public List<Movie> getMovieList() {
        return requestData("LIST_MOVIES", Map.of(), new TypeReference<List<Movie>>() {},
                "[Failed to load movie list]");
    }

    // 영화별 상영 일정 조회
    public List<Showtime> getShowtimeList(String movieId) {
        return requestData("LIST_SHOWTIMES", Map.of("movieId", movieId), new TypeReference<List<Showtime>>() {},
                "[Failed to load showtime list]");
    }

    // 사용자별 예매 내역 조회
    public List<Reservation> getReservationList(String userId) {
        return requestData("LIST_RESERVATIONS", Map.of("userId", userId),
                new TypeReference<List<Reservation>>() {}, "[Failed to load reservation list]");
    }

    // 예매 생성 요청
    public Reservation requestReservation(String userId, String showtimeId, List<String> selectedSeats) {
        return requestData("RESERVE", Map.of(
                "userId", userId,
                "showtimeId", showtimeId,
                "seatCodes", selectedSeats), new TypeReference<Reservation>() {}, "[Reservation request failed]");
    }

    // 예매 취소 요청
    public Reservation requestCancelReservation(String reservationId, String userId) {
        return requestData("CANCEL_RESERVATION", Map.of(
                "reservationId", reservationId,
                "requesterId", userId), new TypeReference<Reservation>() {}, "[Reservation cancellation request failed]");
    }

    // 데이터 응답 요청
    private <T> T requestData(String command, Map<String, Object> body, TypeReference<T> dataType,
            String failureMessage) {
        try {
            return socketClient.getDataFromServer(command, body, dataType);
        } catch (ClassCastException e) { // 응답 데이터 형식이 예상과 다를 때 예외 처리
            throw new ApiException(failureMessage + " Invalid response data format.");
        } catch (IllegalStateException e) { // API 호출 실패 시 예외 처리
            throw new ApiException(failureMessage + " " + e.getMessage());
        }
    }

    // 성공 여부 요청
    private void sendRequest(String command, Map<String, Object> body, String failureMessage) {
        try {
            Response response = socketClient.send(command, body);
            if(!response.isSuccess()) { // API 호출 실패 시 예외 처리
                throw new ApiException(failureMessage + " " + response.getMessage());
            }
        } catch (IllegalStateException e) { // API 호출 실패 시 예외 처리
            throw new ApiException(failureMessage + " " + e.getMessage());
        } 
    }
}
