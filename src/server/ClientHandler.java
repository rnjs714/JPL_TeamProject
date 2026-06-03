package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

// 클라이언트 요청 처리
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataRepository repository;
    private final ObjectMapper objectMapper;

    // 클라이언트 연결 초기화
    public ClientHandler(Socket socket, DataRepository repository) {
        this.socket = socket;
        this.repository = repository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    // 요청 수신 루프
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String requestJson;
            while ((requestJson = reader.readLine()) != null) {
                Request request = objectMapper.readValue(requestJson, Request.class); // 클라이언트로부터 요청 수신
                Response response = handle(request); // 요청 처리
                writer.println(objectMapper.writeValueAsString(response)); // 처리 결과를 JSON으로 응답
            }
        } catch (IOException e) { // 입출력 오류 처리
            throw new IllegalStateException("Failed to handle client request.", e);
        }
    }

    // command 분기
    private Response handle(Request request) {
        try {
            if (request == null || request.getCommand() == null || request.getCommand().isBlank()) {
                throw new IllegalArgumentException("Missing required field: command");
            }
            switch (request.getCommand()) { // 요청 명령어에 따라 처리 메서드 호출
                case "REGISTER":
                    return register(request.getBody()); // 회원가입 처리
                case "LOGIN":
                    return login(request.getBody()); // 로그인 처리
                case "GET_USER":
                    return getUser(request.getBody()); // 사용자 단건 조회 처리
                case "LIST_MOVIES":
                    return listMovies(); // 영화 목록 처리
                case "LIST_SHOWTIMES":
                    return listShowtimes(request.getBody()); // 상영 일정 목록 처리
                case "GET_THEATER":
                    return getTheater(request.getBody()); // 상영관 조회 처리
                case "GET_MOVIES":
                    return getMovie(request.getBody()); // 영화 조회 처리
                case "GET_SHOWTIME":
                    return getShowtime(request.getBody()); // 상영 일정 조회 처리
                case "GET_SEAT_INFO_LIST":
                    return getSeatInfoList(request.getBody()); // 좌석별 가격 정보 처리
                case "RESERVE":
                    return reserve(request.getBody()); // 예매 생성 처리
                case "CANCEL_RESERVATION":
                    return cancelReservation(request.getBody()); // 예매 취소 처리
                case "LIST_RESERVATIONS":
                    return listReservations(request.getBody()); // 예매 내역 처리
                default:
                    return Response.fail("Unknown command: " + request.getCommand()); // 알 수 없는 명령어 처리
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    // 회원가입 처리
    private Response register(Map<String, Object> body) {
        String id = readString(body, "id");
        String password = readString(body, "password");
        boolean success = repository.register(id, password); // 회원가입 시도 후 성공 여부 반환
        if (success) {
            return Response.ok(null);
        } else {
            return Response.fail("ID already exists");
        }
    }

    // 로그인 처리
    private Response login(Map<String, Object> body) {
        String id = readString(body, "id");
        String password = readString(body, "password");
        User user = repository.login(id, password); // 로그인 시도 후 사용자 객체 반환
        if (user != null) {
            return Response.ok(user);
        } else {
            return Response.fail("Invalid ID or password");
        }
    }

    // 사용자 단건 조회 처리
    private Response getUser(Map<String, Object> body) {
        User user = repository.findUser(readString(body, "id"));
        if (user != null) {
            return Response.ok(user);
        } else {
            return Response.fail("User not found");
        }
    }

    // 영화 목록 처리
    private Response listMovies() {
        List<Movie> movies = repository.findMovies(); // 영화 목록 조회
        if(!movies.isEmpty()) {
            return Response.ok(movies);
        } else {
            return Response.fail("No movies available");
        }
    }

    // 상영 일정 목록 처리
    private Response listShowtimes(Map<String, Object> body) {
        List<Showtime> showtimes = repository.findShowtimesByMovie(readString(body, "movieId")); // 특정 영화의 상영 일정 조회
        if(!showtimes.isEmpty()) {
            return Response.ok(showtimes);
        } else {
            return Response.fail("No showtimes available for the selected movie");
        }
    }

    // 상영관 조회 처리
    private Response getTheater(Map<String, Object> body) {
        Theater theater = repository.findTheater(readString(body, "theaterId")); // 특정 상영관 조회
        if (theater != null) {
            return Response.ok(theater);
        } else {
            return Response.fail("Theater not found");
        }
    }

    // 영화 조회 처리
    private Response getMovie(Map<String, Object> body) {
        Movie movie = repository.findMovie(readString(body, "movieId")); // 특정 영화 조회
        if (movie != null) {
            return Response.ok(movie);
        } else {
            return Response.fail("Movie not found");
        }
    }

    // 상영 일정 조회 처리
    private Response getShowtime(Map<String, Object> body) {
        Showtime showtime = repository.findShowtime(readString(body, "showtimeId")); // 특정 상영 일정 조회
        if (showtime != null) {
            return Response.ok(showtime);
        } else {
            return Response.fail("Showtime not found");
        }
    }

    // 예매 생성 처리
    private Response reserve(Map<String, Object> body) {
        String userId = readString(body, "userId");
        String showtimeId = readString(body, "showtimeId");
        List<String> seatCodes = readStringList(body, "seatCodes");
        Reservation reservation = repository.reserve(userId, showtimeId, seatCodes); // 예매 시도 후 생성된 예매 객체 반환
        return Response.ok(reservation);
    }

    // 예매 취소 처리
    private Response cancelReservation(Map<String, Object> body) {
        String reservationId = readString(body, "reservationId");
        String requesterId = readString(body, "requesterId");
        Reservation reservation = repository.cancelReservation(reservationId, requesterId); // 예매 취소 시도 후 취소된 예매 객체 반환
        if (reservation != null) {
            return Response.ok(reservation);
        } else {
            return Response.fail("Reservation not found or unauthorized");
        }
    }

    // 예매 내역 처리
    private Response listReservations(Map<String, Object> body) {
        List<Reservation> reservations = repository.findReservationsByUser(readString(body, "userId")); // 특정 사용자의 예매 내역 조회
        if(!reservations.isEmpty()) {
            return Response.ok(reservations);
        } else {
            return Response.fail("No reservations found for the user");
        }
    }

    // ===== 가격 추가 기능 =====

    // 좌석별 가격/상태 처리
    private Response getSeatInfoList(Map<String, Object> body) {
        String showtimeId = readString(body, "showtimeId");
        return Response.ok(repository.calculateSeatInfo(showtimeId));
    }

    // 문자열 파라미터 읽기
    private String readString(Map<String, Object> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        Object value = body.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid field type: " + key);
        }
        String result = (String) value;
        if (result.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return result;
    }

    // 문자열 리스트 파라미터 읽기
    private List<String> readStringList(Map<String, Object> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        Object value = body.get(key);
        if (!(value instanceof List<?>)) {
            throw new IllegalArgumentException("Invalid field type: " + key);
        }

        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (!(item instanceof String)) {
                throw new IllegalArgumentException("Invalid list item type: " + key);
            }
            String itemText = (String) item;
            if (itemText.isBlank()) {
                throw new IllegalArgumentException("Invalid list item value: " + key);
            }
            result.add(itemText);
        }
        return result;
    }
}
