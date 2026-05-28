package domain;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Showtime {
    private String id;
    private String movieId;
    private String theaterId;
    private LocalDateTime startsAt;
    private Set<String> reservedSeats;

    public Showtime() {
        this.reservedSeats = new LinkedHashSet<>();
    }

    public Showtime(String id, String movieId, String theaterId, LocalDateTime startsAt) {
        this.id = id;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.startsAt = startsAt;
        this.reservedSeats = new LinkedHashSet<>();
    }

    public boolean isReserved(String seatCode) {
        return reservedSeats.contains(seatCode.toUpperCase());
    }

    public void reserveSeat(String seatCode) {
        reservedSeats.add(seatCode.toUpperCase());
    }

    public void reserveSeats(Collection<String> seatCodes) {
        for (String seatCode : seatCodes) {
            reserveSeat(seatCode);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(String theaterId) {
        this.theaterId = theaterId;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public Set<String> getReservedSeats() {
        return reservedSeats;
    }

    public void setReservedSeats(Set<String> reservedSeats) {
        this.reservedSeats = reservedSeats;
    }
}
