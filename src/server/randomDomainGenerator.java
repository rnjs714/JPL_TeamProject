package server;

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

import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;
import domain.TheaterType;
import domain.User;
import repository.MovieBookingData;

public class randomDomainGenerator {
    private static final String OUTPUT_PATH = "data/movie-booking.json";
    private static final Random RANDOM = new Random();

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

    private static List<User> createUsers() {
        return List.of(
                new User("1234", "1234"),
                new User("skku", "skku"),
                new User("test", "1234"),
                new User("rnjs1085", "1234"),
                new User("rnjs714", "1234"),
                new User("rnjs", "1234"),
                new User("alice", "1234"),
                new User("bob", "1234"),
                new User("charlie", "1234"),
                new User("diana", "1234")
        );
    }

    private static List<Movie> createMovies() {
        return List.of(
                new Movie("M1", "Dune: Part Two", 166),
                new Movie("M2", "Inside Out 2", 96),
                new Movie("M3", "Oppenheimer", 180),
                new Movie("M4", "The Dark Knight", 152),
                new Movie("M5", "Interstellar", 169),
                new Movie("M6", "La La Land", 128),
                new Movie("M7", "Parasite", 132),
                new Movie("M8", "Spider-Man: Across the Spider-Verse", 140),
                new Movie("M9", "Whiplash", 107),
                new Movie("M10", "The Matrix", 136)
        );
    }

    private static List<Theater> createTheaters() {
        return List.of(
                new Theater("T1", "Standard Hall 1", 6, 8, TheaterType.STANDARD),
                new Theater("T2", "IMAX Hall 1", 7, 10, TheaterType.IMAX),
                new Theater("T3", "4DX Hall 1", 5, 7, TheaterType.FOUR_DX),
                new Theater("T4", "Standard Hall 2", 6, 9, TheaterType.STANDARD),
                new Theater("T5", "Premium Standard Hall", 8, 9, TheaterType.STANDARD),
                new Theater("T6", "IMAX Hall 2", 8, 12, TheaterType.IMAX),
                new Theater("T7", "4DX Hall 2", 6, 8, TheaterType.FOUR_DX),
                new Theater("T8", "Standard Hall 3", 5, 10, TheaterType.STANDARD)
        );
    }

    private static List<Showtime> createShowtimes(List<Movie> movies, List<Theater> theaters) {
        List<Showtime> showtimes = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);

        for (int i = 0; i < 34; i++) {
            Movie movie = movies.get(i % movies.size());
            Theater theater = theaters.get((i * 3) % theaters.size());
            LocalDateTime startsAt = start.plusDays(i / 5).plusHours((i % 5) * 3L);
            showtimes.add(new Showtime("S" + (i + 1), movie.getId(), theater.getId(), startsAt));
        }
        return showtimes;
    }

    private static List<Reservation> createReservations(List<User> users, List<Showtime> showtimes,
            List<Theater> theaters) {
        List<Reservation> reservations = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            User user = users.get(i % users.size());
            Showtime showtime = showtimes.get(i % showtimes.size());
            Theater theater = findTheater(theaters, showtime.getTheaterId());
            List<String> seatCodes = pickAvailableSeats(theater, showtime.getReservedSeats(), 1 + RANDOM.nextInt(3));
            showtime.reserveSeats(seatCodes);

            reservations.add(new Reservation(
                    "R" + (1780300000000L + i + 1),
                    user.getId(),
                    showtime.getId(),
                    seatCodes,
                    ReservationStatus.CONFIRMED,
                    LocalDateTime.of(2026, 5, 31, 9, 0).plusMinutes(i * 5L)
            ));
        }
        return reservations;
    }

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

    private static String randomSeat(Theater theater) {
        char row = (char) ('A' + RANDOM.nextInt(theater.getRows()));
        int column = 1 + RANDOM.nextInt(theater.getColumns());
        return row + String.valueOf(column);
    }

    private static Theater findTheater(List<Theater> theaters, String theaterId) {
        for (Theater theater : theaters) {
            if (theaterId.equals(theater.getId())) {
                return theater;
            }
        }
        throw new IllegalArgumentException("Unknown theater ID: " + theaterId);
    }

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
