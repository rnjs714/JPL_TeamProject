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

public class DataRepository {
    private final File file;
    private final ObjectMapper objectMapper;
    private MovieBookingData data;

    public DataRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        initialize();
    }

    private MovieBookingData seedData() {
        // TODO: 초기 영화, 상영관, 상영 일정, 빈 사용자 목록, 빈 예약 목록을 가진 MovieBookingData를 생성한다.
        return new MovieBookingData();
    }

    private void initialize() {
        // TODO: data 폴더와 JSON 파일이 없으면 생성하고, 비어 있으면 seedData()를 write()로 저장한다.
        if (!file.exists() || file.length() == 0) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                this.data = seedData();
                write();
                return;
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize JSON file: " + file.getPath(), e);
            }
        } 
        try {
            this.data = objectMapper.readValue(file, MovieBookingData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + file.getPath(), e);
        }
    }

    private void write() {
        // TODO: objectMapper.writeValue(file, data)로 MovieBookingData 전체를 JSON 파일에 저장한다.
    	try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON file: " + file.getPath(), e);
        }
    }

    public synchronized boolean register(User user) {
        List<User> existingUsers = data.getUsers();
        for (User existingUser : existingUsers) {
            if (existingUser.getId().equals(user.getId())) {
                return false;
            }
        }
        existingUsers.add(user);
        write();
        return true;
    }

    public synchronized User login(String id, String password) {
        List<User> users = data.getUsers();
        for (User user : users) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public synchronized User findUser(String id) {
        List<User> users = data.getUsers();
        for (User user : users) {
            if (id.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    public synchronized List<Movie> findMovies() {
        return data.getMovies();
    }

    public synchronized Movie findMovie(String movieId) {
        List<Movie> movies = findMovies();
        for (Movie movie : movies) {
            if (movieId.equals(movie.getId())) {
                return movie;
            }
        }
        return null;
    }

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

    public synchronized Showtime findShowtime(String showtimeId) {
        List<Showtime> showtimes = data.getShowtimes();
        for (Showtime showtime : showtimes) {
            if (showtimeId.equals(showtime.getId())) {
                return showtime;
            }
        }
        return null;
    }

    public synchronized Theater findTheater(String theaterId) {
        List<Theater> theaters = data.getTheaters();
        for (Theater theater : theaters) {
            if (theaterId.equals(theater.getId())) {
                return theater;
            }
        }
        return null;
    }

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

    public synchronized Reservation reserve(String userId, String showtimeId, List<String> seatCodes) {
        User user = findUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("Wrong User ID.");
        }
        Showtime showtime = findShowtime(showtimeId);
        if (showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }
        validateReservationInput(showtimeId, seatCodes);
        showtime.getReservedSeats().addAll(seatCodes);
        Reservation reservation = new Reservation(
                "R" + System.currentTimeMillis(),
                user.getId(),
                showtime.getId(),
                seatCodes,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );
        List<Reservation> reservations = data.getReservations();
        reservations.add(reservation);
        write();
        return reservation;
    }

    public synchronized Reservation cancelReservation(String reservationId, String requesterId) {
        List<Reservation> reservations = data.getReservations();
        for (Reservation reservation : reservations) {
            if (reservationId.equals(reservation.getId())) {
                if (!reservation.getUserId().equals(requesterId)) {
                    throw new IllegalArgumentException("Requester ID does not match.");
                }
                Showtime showtime = findShowtime(reservation.getShowtimeId());
                for (String seatCode : reservation.getSeatCodes()) {
                    showtime.getReservedSeats().remove(seatCode);
                }
                reservation.cancel();
                write();
                return reservation;
            }
        }
        return null;
    }

    private void validateReservationInput(String showtimeId, List<String> seatCodes) {
        for (String seat : seatCodes) {
            Showtime showtime = findShowtime(showtimeId);   
            validateSeat(findTheater(showtime.getTheaterId()), seat);
            if (showtime.getReservedSeats().contains(seat)) {
                throw new IllegalArgumentException("Seat already reserved: " + seat);
            }
        }
    }

    private void validateSeat(Theater theater, String seatCode) {
        if (!theater.isValidSeat(seatCode)) {
            throw new IllegalArgumentException("Invalid seat code: " + seatCode);
        }
    }

}
