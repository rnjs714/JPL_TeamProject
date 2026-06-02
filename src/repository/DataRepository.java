package repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;
import domain.User;

// JSON 데이터 저장소
public class DataRepository {
    private final File file;
    private final ObjectMapper objectMapper;
    private MovieBookingData data;

    // 저장소 초기화
    public DataRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        initialize();
    }

    // 기본 데이터 생성
    private MovieBookingData seedData() {
        return new MovieBookingData();
    }

    // JSON 파일 로드
    private void initialize() {
        if (!file.exists() || file.length() == 0) { // 파일이 존재하지 않거나 비어있는 경우 기본 데이터로 초기화
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                this.data = seedData(); // 기본 데이터 생성
                write(); // 기본 데이터를 JSON 파일에 저장
                return;
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize JSON file: " + file.getPath(), e);
            }
        } 
        try { // 기존 파일에서 데이터 로드
            this.data = objectMapper.readValue(file, MovieBookingData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + file.getPath(), e);
        }
    }

    // JSON 파일 저장
    private void write() {
    	try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON file: " + file.getPath(), e);
        }
    }

    // 사용자 등록
    public synchronized boolean register(String id, String password) {
        List<User> existingUsers = data.getUsers();
        for (User existingUser : existingUsers) {
            if (existingUser.getId().equals(id)) {
                return false;
            }
        }
        existingUsers.add(new User(id, password));
        write();
        return true;
    }

    // 사용자 로그인
    public synchronized User login(String id, String password) {
        List<User> users = data.getUsers();
        for (User user : users) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // 사용자 단건 조회
    public synchronized User findUser(String id) {
        List<User> users = data.getUsers();
        for (User user : users) {
            if (id.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    // 영화 목록 조회
    public synchronized List<Movie> findMovies() {
        return data.getMovies();
    }

    // 영화 단건 조회
    public synchronized Movie findMovie(String movieId) {
        List<Movie> movies = findMovies();
        for (Movie movie : movies) {
            if (movieId.equals(movie.getId())) {
                return movie;
            }
        }
        return null;
    }

    // 영화별 상영 일정 조회
    public synchronized List<Showtime> findShowtimesByMovie(String movieId) {
        List<Showtime> targetList = new ArrayList<>();
        List<Showtime> showtimes = data.getShowtimes();
        for (Showtime showtime : showtimes) {
            if (movieId.equals(showtime.getMovieId())) {
                targetList.add(showtime);
            }
        }
        return targetList;
    }

    // 상영 일정 단건 조회
    public synchronized Showtime findShowtime(String showtimeId) {
        List<Showtime> showtimes = data.getShowtimes();
        for (Showtime showtime : showtimes) {
            if (showtimeId.equals(showtime.getId())) {
                return showtime;
            }
        }
        return null;
    }

    // 상영관 단건 조회
    public synchronized Theater findTheater(String theaterId) {
        List<Theater> theaters = data.getTheaters();
        for (Theater theater : theaters) {
            if (theaterId.equals(theater.getId())) {
                return theater;
            }
        }
        return null;
    }

    // 사용자별 예매 조회
    public synchronized List<Reservation> findReservationsByUser(String userId) {
        List<Reservation> targetList = new ArrayList<>();
        List<Reservation> reservations = data.getReservations();

        for (Reservation reservation : reservations) {
            if (userId.equals(reservation.getUserId())) {
                targetList.add(reservation);
            }
        }
        return targetList;
    }

    // 예매 생성
    public synchronized Reservation reserve(String userId, String showtimeId, List<String> seatCodes) {
        User user = findUser(userId);
        if (user == null) { // 사용자 조회 실패 시 예외 발생
            throw new IllegalArgumentException("Wrong User ID.");
        }
        Showtime showtime = findShowtime(showtimeId);
        if (showtime == null) { // 상영 일정 조회 실패 시 예외 발생
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }
        validateReservationInput(showtimeId, seatCodes); // 예매 입력 검증 (좌석 코드 유효성 및 예약 가능 여부)
        showtime.reserveSeats(seatCodes); // 좌석 예약 처리 (예약 불가능한 좌석이 있는 경우 예외 발생)
        Reservation reservation = new Reservation(
                "R" + System.currentTimeMillis(),
                user.getId(),
                showtime.getId(),
                seatCodes,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        ); // 예매 객체 생성
        List<Reservation> reservations = data.getReservations();
        reservations.add(reservation); // 예매 저장
        write(); // 변경된 데이터 JSON 파일에 저장
        return reservation;
    }

    // 예매 취소
    public synchronized Reservation cancelReservation(String reservationId, String requesterId) {
        List<Reservation> reservations = data.getReservations();
        for (Reservation reservation : reservations) {
            if (reservationId.equals(reservation.getId())) {
                if (!reservation.getUserId().equals(requesterId)) { // 요청자 ID와 예매자 ID가 일치하지 않는 경우 예외 발생
                    throw new IllegalArgumentException("Requester ID does not match.");
                }
                Showtime showtime = findShowtime(reservation.getShowtimeId());
                showtime.releaseSeats(reservation.getSeatCodes()); // 좌석 예약 해제
                reservation.cancel(); // 예매 상태 변경
                write(); // 변경된 데이터 JSON 파일에 저장
                return reservation;
            }
        }
        return null;
    }

    // 예매 입력 검증
    private void validateReservationInput(String showtimeId, List<String> seatCodes) {
        for (String seat : seatCodes) {
            Showtime showtime = findShowtime(showtimeId);   
            validateSeat(findTheater(showtime.getTheaterId()), seat);
            if (showtime.isReserved(seat)) { // 좌석이 이미 예약된 경우 예외 발생
                throw new IllegalArgumentException("Seat already reserved: " + seat);
            }
        }
    }

    // 좌석 코드 검증
    private void validateSeat(Theater theater, String seatCode) {
        if (!theater.isValidSeat(seatCode)) { // 좌석 코드가 상영관의 좌석 배치에 존재하지 않는 경우 예외 발생
            throw new IllegalArgumentException("Invalid seat code: " + seatCode);
        }
    }

}
