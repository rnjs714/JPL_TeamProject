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

import domain.GroupReservation;
import domain.Movie;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import domain.User;
import protocol.Request;
import protocol.Response;
import repository.DataRepository;

/**
 * 클라이언트 한 명의 요청을 처리하는 서버 측 핸들러이다.
 *
 * GUI는 문자열 command와 body를 JSON으로 보내고,
 * 이 클래스는 command에 맞는 repository 메서드를 호출한 뒤 Response로 결과를 돌려준다.
 */
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
                // 한 줄 단위 JSON 요청을 Request 객체로 바꾼 뒤 실제 처리 메서드로 넘긴다.
                Request request = objectMapper.readValue(requestJson, Request.class);
                Response response = handle(request);
                writer.println(objectMapper.writeValueAsString(response));
            }
        } catch (IOException e) {
            throw new IllegalStateException("클라이언트 요청 처리 실패", e);
        }
    }

    private Response handle(Request request) {
        // command 이름은 클라이언트와 서버가 약속한 API 이름이다.
        // 새 기능인 좌석 가격 조회와 그룹 예매도 여기에서 각각의 처리 메서드로 연결된다.
        return switch (request.getCommand()) {
            case "REGISTER" -> register(request.getBody());
            case "LOGIN" -> login(request.getBody());
            case "LIST_MOVIES" -> listMovies();
            case "LIST_SHOWTIMES" -> listShowtimes(request.getBody());
            case "GET_THEATER" -> getTheater(request.getBody());
            case "GET_MOVIES" -> getMovie(request.getBody());
            case "GET_SHOWTIME" -> getShowtime(request.getBody());
            case "CALCULATE_PRICE" -> calculatePrice(request.getBody());
            case "GET_SEAT_PRICE" -> getSeatPrice(request.getBody());
            case "CREATE_GROUP_RESERVATION" -> createGroupReservation(request.getBody());
            case "PAY_GROUP_RESERVATION" -> payGroupReservation(request.getBody());
            case "LIST_GROUP_RESERVATIONS" -> listGroupReservations(request.getBody());
            case "RESERVE" -> reserve(request.getBody());
            case "CANCEL_RESERVATION" -> cancelReservation(request.getBody());
            case "LIST_RESERVATIONS" -> listReservations(request.getBody());
            default -> Response.fail("Unknown command: " + request.getCommand());
        };
    }

    private Response register(Map<String, Object> body) {
        User newUser = new User(
                (String) body.get("id"),
                (String) body.get("password")
        );
        boolean success = repository.register(newUser);
        if (success) {
            return Response.ok("Registration successful", null);
        } else {
            return Response.fail("Registration failed: ID already exists");
        }
    }

    private Response login(Map<String, Object> body) {
        try {
            User user = repository.login((String) body.get("id"), (String) body.get("password"));
            if (user != null) {
                return Response.ok("Login successful", user);
            } else {
                return Response.fail("Login failed: Invalid ID or password");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listMovies() {
        try {
            List<Movie> movies = repository.findMovies();
            if(!movies.isEmpty()) {
                return Response.ok("Movies retrieved successfully", movies);
            } else {
                return Response.fail("No movies available");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listShowtimes(Map<String, Object> body) {
        try {
            List<Showtime> showtimes = repository.findShowtimesByMovie((String) body.get("movieId"));
            if(!showtimes.isEmpty()) {
                return Response.ok("Showtimes retrieved successfully", showtimes);
            } else {
                return Response.fail("No showtimes available for the selected movie");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getTheater(Map<String, Object> body) {
        try {
            Theater theater = repository.findTheater((String) body.get("theaterId"));
            if (theater != null) {
                return Response.ok("Theater retrieved successfully", theater);
            } else {
                return Response.fail("Theater not found");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getMovie(Map<String, Object> body) {
        try {
            Movie movie = repository.findMovie((String) body.get("movieId"));
            if (movie != null) {
                return Response.ok("Movie retrieved successfully", movie);
            } else {
                return Response.fail("Movie not found");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getShowtime(Map<String, Object> body) {
        try {
            Showtime showtime = repository.findShowtime((String) body.get("showtimeId"));
            if (showtime != null) {
                return Response.ok("Showtime retrieved successfully", showtime);
            } else {
                return Response.fail("Showtime not found");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response reserve(Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            String showtimeId = (String) body.get("showtimeId");
            List<String> seatCodes = objectMapper.convertValue(body.get("seatCodes"), new TypeReference<List<String>>() {});
            Reservation reservation = repository.reserve(userId, showtimeId, seatCodes);
            return Response.ok("Reservation successful", reservation);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response calculatePrice(Map<String, Object> body) {
        try {
            String showtimeId = (String) body.get("showtimeId");
            List<String> seatCodes = objectMapper.convertValue(body.get("seatCodes"), new TypeReference<List<String>>() {});
            // 선택 좌석 전체 가격을 서버에서 계산해 클라이언트 화면에 보여준다.
            int totalPrice = repository.calculatePrice(showtimeId, seatCodes);
            return Response.ok("Price calculated successfully", totalPrice);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response getSeatPrice(Map<String, Object> body) {
        try {
            String showtimeId = (String) body.get("showtimeId");
            String seatCode = (String) body.get("seatCode");
            // 좌석 하나의 시야 점수, 가격, 상태를 내려주어 좌석 버튼 UI를 구성하게 한다.
            Map<String, Object> priceInfo = repository.calculateSeatPriceInfo(showtimeId, seatCode);
            return Response.ok("Seat price calculated successfully", priceInfo);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response createGroupReservation(Map<String, Object> body) {
        try {
            String leaderId = (String) body.get("leaderId");
            String showtimeId = (String) body.get("showtimeId");
            List<String> friendIds = objectMapper.convertValue(body.get("friendIds"), new TypeReference<List<String>>() {});
            List<String> seatCodes = objectMapper.convertValue(body.get("seatCodes"), new TypeReference<List<String>>() {});
            // 대표자가 선택한 친구와 좌석을 기준으로 PENDING 그룹 예매를 만들고 좌석을 임시 홀딩한다.
            GroupReservation group = repository.createGroupReservation(leaderId, friendIds, showtimeId, seatCodes);
            return Response.ok("Group reservation is temporarily held. Group members must pay.", group);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response payGroupReservation(Map<String, Object> body) {
        try {
            String groupId = (String) body.get("groupId");
            String userId = (String) body.get("userId");
            // 그룹원 한 명의 결제를 처리한다. 전원 결제가 끝나면 repository에서 자동 확정된다.
            GroupReservation group = repository.payForGroupReservation(groupId, userId);
            return Response.ok("Group payment processed.", group);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listGroupReservations(Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            // 로그인 사용자가 포함된 그룹 예매만 반환해 각자 결제 상태를 확인할 수 있게 한다.
            List<GroupReservation> groups = repository.findGroupReservationsByUser(userId);
            return Response.ok("Group reservations retrieved successfully", groups);
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response cancelReservation(Map<String, Object> body) {
        try {
            String reservationId = (String) body.get("reservationId");
            String requesterId = (String) body.get("requesterId");
            Reservation reservation = repository.cancelReservation(reservationId, requesterId);
            if (reservation != null) {
                return Response.ok("Reservation canceled successfully", reservation);
            } else {
                return Response.fail("Cancellation failed: Reservation not found or unauthorized");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }

    private Response listReservations(Map<String, Object> body) {
        try {
            List<Reservation> reservations = repository.findReservationsByUser((String) body.get("userId"));
            if(!reservations.isEmpty()) {
                return Response.ok("Reservations retrieved successfully", reservations);
            } else {
                return Response.fail("No reservations found for the user");
            }
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
        }
    }
}
