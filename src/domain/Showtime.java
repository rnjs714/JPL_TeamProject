package domain;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// 상영 일정
public class Showtime {
    private String id;
    private String movieId;
    private String theaterId;
    private LocalDateTime startsAt;
    private Set<String> reservedSeats;

    // JSON 역직렬화
    public Showtime() {
        this.reservedSeats = new LinkedHashSet<>();
    }

    // 전체 값 생성
    public Showtime(String id, String movieId, String theaterId, LocalDateTime startsAt) {
        this.id = id;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.startsAt = startsAt;
        this.reservedSeats = new LinkedHashSet<>();
    }

    // 좌석 예약 여부
    public boolean isReserved(String seatCode) {
        return reservedSeats.contains(seatCode.toUpperCase());
    }

    // 복수 좌석 예약 처리
    public void reserveSeats(List<String> seatCodes) {
        for (String seatCode : seatCodes) {
            reservedSeats.add(seatCode.toUpperCase());
        }
    }

    // 복수 좌석 예약 해제 처리
    public void releaseSeats(List<String> seatCodes) {
        for (String seatCode : seatCodes) {
            reservedSeats.remove(seatCode.toUpperCase());
        }
    }

    // 필드 접근자
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
