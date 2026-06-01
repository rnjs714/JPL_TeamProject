package repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.DynamicPriceCalculator;
import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;
import domain.User;

/**
 * JSON 파일에 저장된 영화 예매 데이터를 읽고 수정하는 저장소 클래스이다.
 *
 * 예매와 가격 계산은 화면이 아니라 서버 저장소에서 최종 확인한다.
 * 이렇게 하면 이미 예약된 좌석이 중복 저장되거나, 클라이언트가 가격을 임의로 바꾸는 상황을 줄일 수 있다.
 */
public class DataRepository {
    private final File file;
    private final ObjectMapper objectMapper;
    private final DynamicPriceCalculator priceCalculator;
    private MovieBookingData data;

    public DataRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.priceCalculator = new DynamicPriceCalculator();
        initializeIfNeeded();
    }

    public synchronized boolean register(User user) {
        List<User> existingUsers = read().getUsers();
        for (User existingUser : existingUsers) {
            if (existingUser.getId().equals(user.getId())) {
                return false;
            }
        }
        existingUsers.add(user);
        write(this.data);
        return true;
    }

    public synchronized User login(String id, String password) {
        List<User> users = read().getUsers();
        for (User user : users) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public synchronized User findUser(String id) {
        List<User> users = read().getUsers();
        for(User user : users) {
            if(id.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    public synchronized List<Movie> findMovies() {
        return read().getMovies();
    }

    public synchronized Movie findMovie(String movieId) {
        List<Movie> movies = findMovies();
        for(Movie movie : movies) {
            if(movieId.equals(movie.getId())) {
                return movie;
            }
        }
        return null;
    }

    public synchronized List<Showtime> findShowtimesByMovie(String movieId) {
        List<Showtime> targetList = new ArrayList<>();
        List<Showtime> showtimes = read().getShowtimes();
        for(Showtime showtime : showtimes) {
            if(movieId.equals(showtime.getMovieId())) {
                targetList.add(showtime);
            }
        }
        return targetList;
    }

    public synchronized Showtime findShowtime(String showtimeId) {
        List<Showtime> showtimes = read().getShowtimes();
        for(Showtime showtime : showtimes) {
            if(showtimeId.equals(showtime.getId())) {
                return showtime;
            }
        }
        return null;
    }

    public synchronized Theater findTheater(String theaterId) {
        List<Theater> theaters = read().getTheaters();
        for(Theater theater : theaters) {
            if(theaterId.equals(theater.getId())) {
                return theater;
            }
        }
        return null;
    }

    /**
     * 사용자가 선택한 좌석을 실제 예약으로 저장한다.
     *
     * 개인 예매와 그룹 예매는 둘 다 seatCodes 리스트를 사용한다.
     * 좌석이 1개이면 개인 예매처럼 동작하고, 여러 개이면 그룹 예매처럼 처리된다.
     */
    public synchronized Reservation reserve(String userId, String showtimeId, List<String> seatCodes) {
        try {
            User user = findUser(userId);
            if(user == null) {
                throw new IllegalArgumentException("Wrong User ID.");
            }
            Showtime showtime = findShowtime(showtimeId);
            if(showtime == null) {
                throw new IllegalArgumentException("Wrong Showtime ID.");
            }

            seatCodes = normalizeSeatCodes(seatCodes);
            validateReservationInput(showtimeId, seatCodes);

            // 가격은 클라이언트가 보내지 않고 서버에서 다시 계산한다.
            Theater theater = findTheater(showtime.getTheaterId());
            int totalPrice = priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);

            // 상영 일정의 예약 좌석과 전체 예약 목록을 같이 변경해야 데이터가 맞는다.
            showtime.getReservedSeats().addAll(seatCodes);
            Reservation reservation = new Reservation(
                    "R" + System.currentTimeMillis(),
                    user.getId(),
                    showtime.getId(),
                    seatCodes,
                    ReservationStatus.CONFIRMED,
                    LocalDateTime.now(),
                    totalPrice
            );

            List<Reservation> reservations = read().getReservations();
            reservations.add(reservation);
            write(this.data);
            return reservation;
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * 현재 선택된 좌석들의 예상 총 가격을 계산한다.
     * 실제 예약 전 미리 보여주는 가격이지만, 중복 좌석 검사는 실제 예약과 똑같이 한다.
     */
    public synchronized int calculatePrice(String showtimeId, List<String> seatCodes) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        seatCodes = normalizeSeatCodes(seatCodes);
        validateReservationInput(showtimeId, seatCodes);
        Theater theater = findTheater(showtime.getTheaterId());
        return priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);
    }

    /**
     * 좌석 버튼 하나에 표시할 시야 점수와 가격 정보를 만든다.
     */
    public synchronized Map<String, Object> calculateSeatPriceInfo(String showtimeId, String seatCode) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        Theater theater = findTheater(showtime.getTheaterId());
        String normalizedSeatCode = normalizeSeatCode(seatCode);
        validateSeat(theater, normalizedSeatCode);

        int viewScore = priceCalculator.calculateViewScore(theater, normalizedSeatCode);
        int price = priceCalculator.calculateSeatPrice(theater, normalizedSeatCode, showtime.getReservedSeats().size());

        Map<String, Object> priceInfo = new LinkedHashMap<>();
        priceInfo.put("seatCode", normalizedSeatCode);
        priceInfo.put("viewScore", viewScore);
        priceInfo.put("price", price);
        return priceInfo;
    }

    /**
     * 그룹 인원 수에 맞는 좌석을 추천한다.
     *
     * 1순위는 같은 줄에 붙어 있는 좌석이다.
     * 연속 좌석이 없으면 떨어져 있더라도 시야 점수가 좋은 좌석을 대안으로 반환한다.
     */
    public synchronized List<String> findRecommendedGroupSeats(String showtimeId, int peopleCount) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }
        if(peopleCount <= 0) {
            throw new IllegalArgumentException("인원 수는 1명 이상이어야 합니다.");
        }

        Theater theater = findTheater(showtime.getTheaterId());
        if(theater == null) {
            throw new IllegalArgumentException("Wrong Theater ID.");
        }
        if(peopleCount > theater.getRows() * theater.getColumns()) {
            throw new IllegalArgumentException("People count is bigger than theater seat count.");
        }

        List<String> continuousSeats = findContinuousSeats(theater, showtime, peopleCount);
        if(!continuousSeats.isEmpty()) {
            return continuousSeats;
        }

        List<String> availableSeats = findAvailableSeats(theater, showtime);
        if(availableSeats.size() >= peopleCount) {
            return findBestAvailableSeats(theater, availableSeats, peopleCount);
        }

        throw new IllegalArgumentException("선택한 인원 수만큼 예약 가능한 좌석이 없습니다.");
    }

    public synchronized Reservation cancelReservation(String reservationId, String requesterId) {
        List<Reservation> reservations = read().getReservations();
        for (Reservation reservation : reservations) {
            if(reservationId.equals(reservation.getId()) ) {
                if(!reservation.getUserId().equals(requesterId)) {
                    throw new IllegalArgumentException("Requester ID does not match.");
                }
                Showtime showtime = findShowtime(reservation.getShowtimeId());
                for(String seatCode : reservation.getSeatCodes()) {
                    showtime.getReservedSeats().remove(seatCode);
                }
                reservation.cancel();
                write(this.data);
                return reservation;
            }
        }
        return null;
    }

    public synchronized List<Reservation> findReservationsByUser(String userId) {
        List<Reservation> targetList = new ArrayList<>();
        List<Reservation> reservations = read().getReservations();

        for(Reservation reservation : reservations) {
            if(userId.equals(reservation.getUserId())) {
                targetList.add(reservation);
            }
        }
        return targetList;
    }

    private MovieBookingData read() {
        try {
            if(data != null) return data;
            return this.data = objectMapper.readValue(file, MovieBookingData.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 읽기 실패: " + file.getPath(), e);
        }
    }

    private void write(MovieBookingData data) {
        try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 저장 실패: " + file.getPath(), e);
        }
    }

    private void initializeIfNeeded() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                write(seedData());
            } catch (IOException e) {
                throw new RuntimeException("JSON 파일 초기화 실패: " + file.getPath(), e);
            }
        } else if (file.length() == 0) {
            write(seedData());
        }
    }

    private MovieBookingData seedData() {
        return new MovieBookingData();
    }

    private void validateReservationInput(String showtimeId, List<String> seatCodes) {
        Set<String> selectedSeats = new LinkedHashSet<>();
        Showtime showtime = findShowtime(showtimeId);
        Theater theater = findTheater(showtime.getTheaterId());

        for(String seat : seatCodes) {
            // 같은 요청 안에서 같은 좌석이 두 번 들어오는 경우를 막는다.
            if(!selectedSeats.add(seat)) {
                throw new IllegalArgumentException("Duplicated seat: " + seat);
            }
            validateSeat(theater, seat);

            // 이미 다른 예약에 들어간 좌석은 다시 예약할 수 없다.
            if(showtime.getReservedSeats().contains(seat)) {
                throw new IllegalArgumentException("이미 예약된 좌석: " + seat);
            }
        }
    }

    private void validateSeat(Theater theater, String seatCode) {
        if(!theater.isValidSeat(seatCode)) {
            throw new IllegalArgumentException("유효하지 않은 좌석 코드: " + seatCode);
        }
    }

    private List<String> normalizeSeatCodes(List<String> seatCodes) {
        if(seatCodes == null || seatCodes.isEmpty()) {
            throw new IllegalArgumentException("seatCodes must not be empty.");
        }

        List<String> normalizedSeatCodes = new ArrayList<>();
        for(String seatCode : seatCodes) {
            normalizedSeatCodes.add(normalizeSeatCode(seatCode));
        }
        return normalizedSeatCodes;
    }

    private String normalizeSeatCode(String seatCode) {
        if(seatCode == null || seatCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid seat code: " + seatCode);
        }
        return seatCode.trim().toUpperCase(Locale.ROOT);
    }

    private List<String> findContinuousSeats(Theater theater, Showtime showtime, int peopleCount) {
        List<String> bestSeats = new ArrayList<>();
        int bestScore = -1;

        // 모든 행을 확인하면서 인원 수만큼 연속된 후보 묶음을 만든다.
        for(int row=0; row<theater.getRows(); row++) {
            for(int startCol=1; startCol<=theater.getColumns() - peopleCount + 1; startCol++) {
                List<String> candidateSeats = new ArrayList<>();
                int candidateScore = 0;
                int checkingError = 0;

                for(int i=0; i<peopleCount; i++) {
                    String seatCode = createSeatCode(row, startCol + i);
                    if(isAvailableSeat(theater, showtime, seatCode)) {
                        candidateSeats.add(seatCode);
                        candidateScore += calculateSeatScore(theater, seatCode);
                    } else {
                        checkingError = 1;
                    }
                }

                // 후보 좌석이 모두 비어 있으면 시야 점수 합으로 가장 좋은 묶음을 갱신한다.
                if(checkingError == 0 && candidateScore > bestScore) {
                    bestScore = candidateScore;
                    bestSeats = candidateSeats;
                }
            }
        }
        return bestSeats;
    }

    private List<String> findAvailableSeats(Theater theater, Showtime showtime) {
        List<String> availableSeats = new ArrayList<>();

        for(int row=0; row<theater.getRows(); row++) {
            for(int col=1; col<=theater.getColumns(); col++) {
                String seatCode = createSeatCode(row, col);
                if(isAvailableSeat(theater, showtime, seatCode)) {
                    availableSeats.add(seatCode);
                }
            }
        }
        return availableSeats;
    }

    private boolean isAvailableSeat(Theater theater, Showtime showtime, String seatCode) {
        return theater.isValidSeat(seatCode) && !showtime.getReservedSeats().contains(seatCode);
    }

    private List<String> findBestAvailableSeats(Theater theater, List<String> availableSeats, int peopleCount) {
        List<String> result = new ArrayList<>();

        while(result.size() < peopleCount) {
            String bestSeat = null;
            int bestScore = -1;

            // 연속 좌석을 못 찾은 경우에는 남은 좌석 중 시야 점수가 높은 좌석부터 고른다.
            for(String seatCode : availableSeats) {
                if(!result.contains(seatCode)) {
                    int score = calculateSeatScore(theater, seatCode);
                    if(score > bestScore) {
                        bestScore = score;
                        bestSeat = seatCode;
                    }
                }
            }

            if(bestSeat == null) {
                break;
            }
            result.add(bestSeat);
        }

        return result;
    }

    private int calculateSeatScore(Theater theater, String seatCode) {
        try {
            return priceCalculator.calculateViewScore(theater, seatCode);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private String createSeatCode(int row, int col) {
        return "" + (char)('A' + row) + col;
    }
}
