package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import domain.User;
import protocol.Request;
import protocol.Response;
import repository.DataRepository;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataRepository repository;
    private final ObjectMapper objectMapper;

    public ClientHandler(Socket socket, DataRepository repository) {
        this.socket = socket;
        this.repository = repository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String requestJson;
            while ((requestJson = reader.readLine()) != null) {
                Request request = objectMapper.readValue(requestJson, Request.class);
                Response response = handle(request);
                writer.println(objectMapper.writeValueAsString(response));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to handle client request.", e);
        }
    }

    private Response handle(Request request) {
        // TODO: command 값에 따라 register, login, reserve 등으로 분기한다.
        return switch (request.getCommand()) {
            case "REGISTER" -> register(request.getBody());
            case "LOGIN" -> login(request.getBody());
            case "LIST_MOVIES" -> listMovies();
            case "LIST_SHOWTIMES" -> listShowtimes(request.getBody());
            case "GET_THEATER" -> getTheater(request.getBody());
            case "GET_MOVIES" -> getMovie(request.getBody());
            case "GET_SHOWTIME" -> getShowtime(request.getBody());
            case "RESERVE" -> reserve(request.getBody());
            case "CANCEL_RESERVATION" -> cancelReservation(request.getBody());
            case "LIST_RESERVATIONS" -> listReservations(request.getBody());
            default -> Response.fail("Unknown command: " + request.getCommand());
        };
    }

    private Response register(Map<String, Object> body) {
        // TODO: 회원가입 요청을 처리한다.
        User newUser = new User(
                (String) body.get("id"),
                (String) body.get("password")
        );
        boolean success = repository.register(newUser);
        if (success) {
            return Response.ok(null);
        } else {
            return Response.fail("ID already exists");
        }
    }

    private Response login(Map<String, Object> body) {
        // TODO: 로그인 요청을 처리한다.
        try {
            User user = repository.login((String) body.get("id"), (String) body.get("password"));
            if (user != null) {
                return Response.ok(user);
            } else {
                return Response.fail("Invalid ID or password");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listMovies() {
        // TODO: 영화 목록 요청을 처리한다.
        try {
            List<Movie> movies = repository.findMovies();
            if(!movies.isEmpty()) {
                return Response.ok(movies);
            } else {
                return Response.fail("No movies available");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listShowtimes(Map<String, Object> body) {
        // TODO: 영화 ID로 상영 일정 목록 요청을 처리한다.
        try {
            List<Showtime> showtimes = repository.findShowtimesByMovie((String) body.get("movieId"));
            if(!showtimes.isEmpty()) {
                return Response.ok(showtimes);
            } else {
                return Response.fail("No showtimes available for the selected movie");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getTheater(Map<String, Object> body) {
        // TODO: 상영관 정보 요청을 처리한다.
        try {
            Theater theater = repository.findTheater((String) body.get("theaterId"));
            if (theater != null) {
                return Response.ok(theater);
            } else {
                return Response.fail("Theater not found");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getMovie(Map<String, Object> body) {
        // TODO: 영화 정보 요청을 처리한다.
        try {
            Movie movie = repository.findMovie((String) body.get("movieId"));
            if (movie != null) {
                return Response.ok(movie);
            } else {
                return Response.fail("Movie not found");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getShowtime(Map<String, Object> body) {
        // TODO: 상영 시간 정보 요청을 처리한다.
        try {
            Showtime showtime = repository.findShowtime((String) body.get("showtimeId"));
            if (showtime != null) {
                return Response.ok(showtime);
            } else {
                return Response.fail("Showtime not found");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response reserve(Map<String, Object> body) {
        // TODO: 좌석 예매 요청을 처리한다.
        try {
            String userId = (String) body.get("userId");
            String showtimeId = (String) body.get("showtimeId");
            List<String> seatCodes = objectMapper.convertValue(body.get("seatCodes"), new TypeReference<List<String>>() {});
            Reservation reservation = repository.reserve(userId, showtimeId, seatCodes);
            return Response.ok(reservation);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
        
    }

    private Response cancelReservation(Map<String, Object> body) {
        // TODO: 예약 취소 요청을 처리한다.
        try {
            String reservationId = (String) body.get("reservationId");
            String requesterId = (String) body.get("requesterId");
            Reservation reservation = repository.cancelReservation(reservationId, requesterId);
            if (reservation != null) {
                return Response.ok(reservation);
            } else {
                return Response.fail("Reservation not found or unauthorized");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listReservations(Map<String, Object> body) {
        // TODO: 사용자 ID로 예매 내역 요청을 처리한다.
        try {
            List<Reservation> reservations = repository.findReservationsByUser((String) body.get("userId"));
            if(!reservations.isEmpty()) {
                return Response.ok(reservations);
            } else {
                return Response.fail("No reservations found for the user");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }
}
