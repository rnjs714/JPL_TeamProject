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

import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;
import domain.User;
import domain.DynamicPriceCalculator;

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
        // TODO: JSON 전체를 read()로 읽고, 중복 ID가 없으면 users에 추가한 뒤 write()로 저장한다.
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
        // TODO: users에서 id/password가 일치하는 사용자를 찾아 반환한다.
        List<User> users = read().getUsers();
        for (User user : users) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public synchronized User findUser(String id) {
        // TODO: users에서 id가 일치하는 사용자를 찾아 반환한다.
        List<User> users = read().getUsers();
    	for(User user : users) {
    		if(id.equals(user.getId())) {
    			return user;
    		}
    	}
        return null;
    }

    public synchronized List<Movie> findMovies() {
        // TODO: JSON에서 전체 영화 목록을 읽어 반환한다.
        return read().getMovies();
    }

    public synchronized Movie findMovie(String movieId) {
        // TODO: movies에서 movieId가 일치하는 영화를 찾아 반환한다.
    	List<Movie> movies = findMovies();
    	for(Movie movie : movies) {
    		if(movieId.equals(movie.getId())) {
    			return movie;
    		}
    	}
        return null;
    }

    public synchronized List<Showtime> findShowtimesByMovie(String movieId) {
        // TODO: showtimes에서 movieId가 일치하는 상영 일정 목록을 반환한다.

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
        // TODO: showtimes에서 showtimeId가 일치하는 상영 일정을 찾아 반환한다.
    	List<Showtime> showtimes = read().getShowtimes();
    	for(Showtime showtime : showtimes) {
    		if(showtimeId.equals(showtime.getId())) {
    			return showtime;
    		}
    	}
    	return null;
    }

    public synchronized Theater findTheater(String theaterId) {
        // TODO: theaters에서 theaterId가 일치하는 상영관을 찾아 반환한다.
    	List<Theater> theaters = read().getTheaters();
    	for(Theater theater : theaters) {
    		if(theaterId.equals(theater.getId())) {
    			return theater;
    		}
    	}
        return null;
    }

    public synchronized Reservation reserve(String userId, String showtimeId, List<String> seatCodes) {
        // TODO: JSON 전체를 읽고, 좌석 유효성/중복 예약 여부를 검사한 뒤 Reservation을 생성하고 좌석을 reservedSeats에 추가한다.
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
            // 이렇게 해야 사용자가 임의로 가격을 바꾸는 상황을 막을 수 있다.
            Theater theater = findTheater(showtime.getTheaterId());
            int totalPrice = priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);
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

    public synchronized int calculatePrice(String showtimeId, List<String> seatCodes) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        // 선택된 좌석들이 실제로 예약 가능한지 확인한 뒤 총 가격을 계산한다.
        seatCodes = normalizeSeatCodes(seatCodes);
        validateReservationInput(showtimeId, seatCodes);
        Theater theater = findTheater(showtime.getTheaterId());
        return priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);
    }

    public synchronized Map<String, Object> calculateSeatPriceInfo(String showtimeId, String seatCode) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        Theater theater = findTheater(showtime.getTheaterId());
        String normalizedSeatCode = normalizeSeatCode(seatCode);
        validateSeat(theater, normalizedSeatCode);

        // 좌석 한 칸에 대해 화면에 보여줄 시야 점수와 가격을 같이 만든다.
        int viewScore = priceCalculator.calculateViewScore(theater, normalizedSeatCode);
        int price = priceCalculator.calculateSeatPrice(theater, normalizedSeatCode, showtime.getReservedSeats().size());

        Map<String, Object> priceInfo = new LinkedHashMap<>();
        priceInfo.put("seatCode", normalizedSeatCode);
        priceInfo.put("viewScore", viewScore);
        priceInfo.put("price", price);
        return priceInfo;
    }

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

        // 먼저 같은 줄에서 붙어 있는 좌석을 찾는다. 그룹 예매는 같이 앉는 것이 가장 자연스럽기 때문이다.
        List<String> continuousSeats = findContinuousSeats(theater, showtime, peopleCount);
        if(!continuousSeats.isEmpty()) {
            return continuousSeats;
        }

        // 붙어 있는 좌석이 부족하면 떨어져 있는 좌석이라도 가능한 대안을 찾아서 반환한다.
        List<String> availableSeats = findAvailableSeats(theater, showtime);
        if(availableSeats.size() >= peopleCount) {
            return new ArrayList<>(availableSeats.subList(0, peopleCount));
        }

        throw new IllegalArgumentException("선택한 인원 수만큼 예약 가능한 좌석이 없습니다.");
    }

    public synchronized Reservation cancelReservation(String reservationId, String requesterId) {
        // TODO: 예약 ID로 Reservation을 찾고, requesterId가 예약자와 일치하면 상태를 CANCELED로 변경한다.
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
                // reservations.remove(reservation);
                write(this.data);
                return reservation;
            }
        }
        // TODO: 필요하면 Showtime의 reservedSeats에서 해당 좌석을 제거한 뒤 JSON 파일에 저장한다.
        return null;
    }

    public synchronized List<Reservation> findReservationsByUser(String userId) {
        // TODO: reservations에서 userId가 일치하는 예약 목록을 반환한다.

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
        // TODO: objectMapper.readValue(file, MovieBookingData.class)로 JSON 파일 전체를 Java 객체로 변환한다.
    	try {
            if(data != null) return data;
            return this.data = objectMapper.readValue(file, MovieBookingData.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 읽기 실패: " + file.getPath(), e);
        }
    }

    private void write(MovieBookingData data) {
        // TODO: objectMapper.writeValue(file, data)로 MovieBookingData 전체를 JSON 파일에 저장한다.
    	try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 저장 실패: " + file.getPath(), e);
        }
    }

    private void initializeIfNeeded() {
        // TODO: data 폴더와 JSON 파일이 없으면 생성하고, 비어 있으면 seedData()를 write()로 저장한다.
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
        // TODO: 초기 영화, 상영관, 상영 일정, 빈 사용자 목록, 빈 예약 목록을 가진 MovieBookingData를 생성한다.
        return new MovieBookingData();
    }

    private void validateReservationInput(String showtimeId, List<String> seatCodes) {
        // TODO: 사용자 존재 여부, 상영 일정 존재 여부, 좌석 형식, 좌석 범위, 이미 예약된 좌석 여부를 검사한다.
        Set<String> selectedSeats = new LinkedHashSet<>();
        Showtime showtime = findShowtime(showtimeId);
        Theater theater = findTheater(showtime.getTheaterId());
        for(String seat : seatCodes) {
            if(!selectedSeats.add(seat)) {
                throw new IllegalArgumentException("Duplicated seat: " + seat);
            }
            validateSeat(theater, seat);
            if(showtime.getReservedSeats().contains(seat)) {
            	throw new IllegalArgumentException("이미 예약된 좌석: " + seat);
            }
        }
    }

    private void validateSeat(Theater theater, String seatCode) {
        // TODO: Theater.isValidSeat()를 이용해 좌석이 상영관 범위 안에 있는지 검사한다.
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
        List<String> result = new ArrayList<>();

        // 같은 행에서 왼쪽부터 오른쪽으로 보면서 연속으로 비어 있는 좌석을 찾는다.
        for(int row=0; row<theater.getRows(); row++) {
            result.clear();
            for(int col=1; col<=theater.getColumns(); col++) {
                String seatCode = createSeatCode(row, col);
                if(isAvailableSeat(theater, showtime, seatCode)) {
                    result.add(seatCode);
                    if(result.size() == peopleCount) {
                        return new ArrayList<>(result);
                    }
                } else {
                    result.clear();
                }
            }
        }
        return new ArrayList<>();
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

    private String createSeatCode(int row, int col) {
        return "" + (char)('A' + row) + col;
    }
}
