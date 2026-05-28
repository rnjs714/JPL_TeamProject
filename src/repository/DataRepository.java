package repository;

import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;
import domain.TheaterType;
import domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataRepository {
    private File file;
    private ObjectMapper objectMapper;

    public DataRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        initializeIfNeeded();
    }

    public synchronized boolean register(User user) {
        // TODO: JSON 전체를 read()로 읽고, 중복 ID가 없으면 users에 추가한 뒤 write()로 저장한다.
        return false;
    }

    public synchronized User login(String id, String password) {
        // TODO: users에서 id/password가 일치하는 사용자를 찾아 반환한다.
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
    	
    	List<Showtime> targetList = new ArrayList<Showtime>();
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
        // TODO: 변경된 MovieBookingData를 write()로 JSON 파일에 저장하고 생성된 예약을 반환한다.
        return null;
    }

    public synchronized Reservation cancelReservation(String reservationId, String requesterId) {
        // TODO: 예약 ID로 Reservation을 찾고, requesterId가 예약자와 일치하면 상태를 CANCELED로 변경한다.
        // TODO: 필요하면 Showtime의 reservedSeats에서 해당 좌석을 제거한 뒤 JSON 파일에 저장한다.
        return null;
    }

    public synchronized List<Reservation> findReservationsByUser(String userId) {
        // TODO: reservations에서 userId가 일치하는 예약 목록을 반환한다.
        return null;
    }

    private MovieBookingData read() {
        // TODO: objectMapper.readValue(file, MovieBookingData.class)로 JSON 파일 전체를 Java 객체로 변환한다.
    	try {
            return objectMapper.readValue(file, MovieBookingData.class);
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
    }

    private MovieBookingData seedData() {
        // TODO: 초기 영화, 상영관, 상영 일정, 빈 사용자 목록, 빈 예약 목록을 가진 MovieBookingData를 생성한다.
        return null;
    }

    private void validateReservationInput(MovieBookingData data, String userId, String showtimeId, List<String> seatCodes) {
        // TODO: 사용자 존재 여부, 상영 일정 존재 여부, 좌석 형식, 좌석 범위, 이미 예약된 좌석 여부를 검사한다.
    }

    private void validateSeat(Theater theater, String seatCode) {
        // TODO: Theater.isValidSeat()를 이용해 좌석이 상영관 범위 안에 있는지 검사한다.
    }

    private List<String> normalizeSeats(List<String> seatCodes) {
        return seatCodes.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(seatCode -> !seatCode.isBlank())
                .toList();
    }
}
