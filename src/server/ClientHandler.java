package server;

import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.User;
import protocol.Request;
import protocol.Response;
import repository.DataRepository;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataRepository repository;
    private ObjectMapper objectMapper;

    public ClientHandler(Socket socket, DataRepository repository) {
        this.socket = socket;
        this.repository = repository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void run() {
        // TODO: 클라이언트 요청 JSON을 읽고 Request로 변환한 뒤 Response JSON을 반환한다.
        try {
            
        } catch (Exception e) {
        }
    }

    private Response handle(Request request) {
        // TODO: command 값에 따라 register, login, reserve 등으로 분기한다.
        switch (request.getCommand()) {
            case "register":
                return register(request.getBody());
            case "login":
                return login(request.getBody());
            case "list_movies":
                return listMovies();
            case "list_showtimes":
                return listShowtimes(request.getBody());
            case "reserve":
                return reserve(request.getBody());
            case "cancel_reservation":
                return cancelReservation(request.getBody());
            case "list_reservations":
                return listReservations(request.getBody());
            default:
                return Response.fail("Unknown command: " + request.getCommand());
        }
    }

    private Response register(Map<String, Object> body) {
        // TODO: 회원가입 요청을 처리한다.
        User newUser = new User(
                (String) body.get("id"),
                (String) body.get("password"),
                (String) body.get("name")
        );
        boolean success = repository.register(newUser);
        if (success) {
            return Response.ok("Registration successful", null);
        } else {
            return Response.fail("Registration failed: ID already exists");
        }
    }

    private Response login(Map<String, Object> body) {
        // TODO: 로그인 요청을 처리한다.
        try {
            User user = repository.login((String) body.get("id"), (String) body.get("password"));
            return Response.ok("Login successful", user);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listMovies() {
        // TODO: 영화 목록 요청을 처리한다.
        try {
            List<Movie> movies = repository.findMovies();
            return Response.ok("Movies retrieved successfully", movies);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listShowtimes(Map<String, Object> body) {
        // TODO: 영화 ID로 상영 일정 목록 요청을 처리한다.
        try {
            List<Showtime> showtimes = repository.findShowtimesByMovie((String) body.get("movieId"));
            return Response.ok("Showtimes retrieved successfully", showtimes);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response reserve(Map<String, Object> body) {
        // TODO: 좌석 예매 요청을 처리한다.
        try {
            String userId = (String) body.get("userId");
            String showtimeId = (String) body.get("showtimeId");
            List<String> seatCodes = (List<String>) body.get("seatCodes");
            Reservation reservation = repository.reserve(userId, showtimeId, seatCodes);
            return Response.ok("Reservation successful", reservation);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
        
    }

    private Response cancelReservation(Map<String, Object> body) {
        // TODO: 예약 취소 요청을 처리한다.
        return null;
    }

    private Response listReservations(Map<String, Object> body) {
        // TODO: 사용자 ID로 예매 내역 요청을 처리한다.
        try {
            List<Reservation> reservations = repository.findReservationsByUser((String) body.get("userId"));
            return Response.ok("Reservations retrieved successfully", reservations);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }
}
