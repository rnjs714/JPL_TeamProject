package repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
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
import domain.TheaterType;
import domain.User;

// 테스트 도메인 데이터 생성
public class randomDomainGenerator {
    private static final String OUTPUT_PATH = "data/movie-booking.json";
    private static final int USER_COUNT = 10;
    private static final int MOVIE_COUNT = 10;
    private static final int THEATER_COUNT = 8;
    private static final int SHOWTIME_COUNT = 30;
    private static final int RESERVATION_COUNT = 30;
    private static final int MAX_SEATS_PER_RESERVATION = 5;
    private static final Random RANDOM = new Random();
    private static final DynamicPriceCalculator PRICE_CALCULATOR = new DynamicPriceCalculator();

    // 생성기 실행점
    public static void main(String[] args) {
        MovieBookingData data = new MovieBookingData();

        List<User> users = createUsers();
        List<Movie> movies = createMovies();
        List<Theater> theaters = createTheaters();
        List<Showtime> showtimes = createShowtimes(movies, theaters);
        List<Reservation> reservations = createReservations(users, showtimes, theaters);

        data.setUsers(users);
        data.setMovies(movies);
        data.setTheaters(theaters);
        data.setShowtimes(showtimes);
        data.setReservations(reservations);

        writeData(data);
        System.out.println("Generated random domain data at " + OUTPUT_PATH);
    }

    // 사용자 생성
    private static List<User> createUsers() {
        String[] baseUserIds = {
                "1234", "skku", "test", "rnjs1085", "rnjs714",
                "rnjs", "alice", "bob", "charlie", "diana"
        };

        List<User> users = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            String userId = i < baseUserIds.length ? baseUserIds[i] : "user" + (i + 1);
            String password = "skku".equals(userId) ? "skku" : "1234";
            users.add(new User(userId, password));
        }
        return users;
    }

    // 영화 생성
    private static List<Movie> createMovies() {
        String[] titles = {
                "Dune: Part Two",
                "Inside Out 2",
                "Oppenheimer",
                "The Dark Knight",
                "Interstellar",
                "La La Land",
                "Parasite",
                "Spider-Man: Across the Spider-Verse",
                "Whiplash",
                "The Matrix"
        };
        int[] durations = {
                166, 96, 180, 152, 169,
                128, 132, 140, 107, 136
        };

        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < MOVIE_COUNT; i++) {
            String title = i < titles.length ? titles[i] : "Generated Movie " + (i + 1);
            int duration = i < durations.length ? durations[i] : 90 + (i % 8) * 10;
            movies.add(new Movie("M" + (i + 1), title, duration));
        }
        return movies;
    }

    // 상영관 생성
    private static List<Theater> createTheaters() {
        List<Theater> baseTheaters = List.of(
                new Theater("T1", "Standard Hall 1", 6, 8, TheaterType.STANDARD),
                new Theater("T2", "IMAX Hall 1", 7, 10, TheaterType.IMAX),
                new Theater("T3", "4DX Hall 1", 5, 7, TheaterType.FOUR_DX),
                new Theater("T4", "Standard Hall 2", 6, 9, TheaterType.STANDARD),
                new Theater("T5", "Premium Standard Hall", 8, 9, TheaterType.STANDARD),
                new Theater("T6", "IMAX Hall 2", 8, 12, TheaterType.IMAX),
                new Theater("T7", "4DX Hall 2", 6, 8, TheaterType.FOUR_DX),
                new Theater("T8", "Standard Hall 3", 5, 10, TheaterType.STANDARD)
        );

        List<Theater> theaters = new ArrayList<>();
        TheaterType[] theaterTypes = TheaterType.values();
        for (int i = 0; i < THEATER_COUNT; i++) {
            if (i < baseTheaters.size()) {
                theaters.add(baseTheaters.get(i));
                continue;
            }

            TheaterType type = theaterTypes[i % theaterTypes.length];
            theaters.add(new Theater(
                    "T" + (i + 1),
                    type + " Hall " + (i + 1),
                    5 + (i % 4),
                    8 + (i % 5),
                    type
            ));
        }
        return theaters;
    }

    // 상영 일정 생성
    private static List<Showtime> createShowtimes(List<Movie> movies, List<Theater> theaters) {
        List<Showtime> showtimes = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);

        for (int i = 0; i < SHOWTIME_COUNT; i++) {
            Movie movie = movies.get(i % movies.size());
            Theater theater = theaters.get((i * 3) % theaters.size());
            LocalDateTime startsAt = start.plusDays(i / 5).plusHours((i % 5) * 3L);
            showtimes.add(new Showtime("S" + (i + 1), movie.getId(), theater.getId(), startsAt));
        }
        return showtimes;
    }

    // 예매 정보 생성
    private static List<Reservation> createReservations(List<User> users, List<Showtime> showtimes,
            List<Theater> theaters) {
        List<Reservation> reservations = new ArrayList<>();

        for (int i = 0; i < RESERVATION_COUNT; i++) {
            User user = users.get(i % users.size());
            Showtime showtime = showtimes.get(i % showtimes.size());
            Theater theater = findTheater(theaters, showtime.getTheaterId());
            List<String> seatCodes = pickAvailableSeats(
                    theater,
                    showtime.getReservedSeats(),
                    1 + RANDOM.nextInt(MAX_SEATS_PER_RESERVATION)
            );
            showtime.reserveSeats(seatCodes);
            int totalPrice = PRICE_CALCULATOR.calculateTotalPrice(theater, showtime, seatCodes);

            reservations.add(new Reservation(
                    "R" + (1780300000000L + i + 1),
                    user.getId(),
                    showtime.getId(),
                    seatCodes,
                    ReservationStatus.CONFIRMED,
                    LocalDateTime.of(2026, 5, 31, 9, 0).plusMinutes(i * 5L),
                    totalPrice
            ));
        }
        return reservations;
    }

    // 빈 좌석 선택
    private static List<String> pickAvailableSeats(Theater theater, Set<String> reservedSeats, int count) {
        List<String> selectedSeats = new ArrayList<>();
        Set<String> usedSeats = new LinkedHashSet<>(reservedSeats);

        while (selectedSeats.size() < count) {
            String seatCode = randomSeat(theater);
            if (usedSeats.add(seatCode)) {
                selectedSeats.add(seatCode);
            }
        }
        return selectedSeats;
    }

    // 좌석 코드 생성
    private static String randomSeat(Theater theater) {
        char row = (char) ('A' + RANDOM.nextInt(theater.getRows()));
        int column = 1 + RANDOM.nextInt(theater.getColumns());
        return row + String.valueOf(column);
    }

    // 상영관 검색
    private static Theater findTheater(List<Theater> theaters, String theaterId) {
        for (Theater theater : theaters) {
            if (theaterId.equals(theater.getId())) {
                return theater;
            }
        }
        throw new IllegalArgumentException("Unknown theater ID: " + theaterId);
    }

    // 사용자 검색
    private static User findUser(List<User> users, String userId) {
        for (User user : users) {
            if (userId.equals(user.getId())) {
                return user;
            }
        }
        throw new IllegalArgumentException("Unknown user ID: " + userId);
    }

    // JSON 파일 저장
    private static void writeData(MovieBookingData data) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        File file = new File(OUTPUT_PATH);
        file.getParentFile().mkdirs();
        try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write generated data.", e);
        }
    }
}
