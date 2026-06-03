package session;

import java.util.ArrayList;
import java.util.List;

import domain.Movie;
import domain.SeatInfo;
import domain.Showtime;
import domain.Theater;

// 예매 진행 상태
public class BookingSession {
    private Movie selectedMovie;
    private Showtime selectedShowtime;
    private Theater selectedTheater;
    private List<String> selectedSeats;
    private List<SeatInfo> seatInfoList;

    // 기본 선택값 초기화
    public BookingSession() {
        this.selectedSeats = new ArrayList<>();
        this.seatInfoList = new ArrayList<>();
    }

    // 선택 영화 접근자
    public Movie getSelectedMovie() {
        return selectedMovie;
    }

    public void setSelectedMovie(Movie selectedMovie) {
        this.selectedMovie = selectedMovie;
    }

    // 선택 상영 일정 접근자
    public Showtime getSelectedShowtime() {
        return selectedShowtime;
    }

    public void setSelectedShowtime(Showtime selectedShowtime) {
        this.selectedShowtime = selectedShowtime;
    }

    // 선택 상영관 접근자
    public Theater getSelectedTheater() {
        return selectedTheater;
    }

    public void setSelectedTheater(Theater selectedTheater) {
        this.selectedTheater = selectedTheater;
    }

    // 선택 좌석 접근자
    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<String> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    // 좌석 정보 리스트 접근자
    public List<SeatInfo> getSeatInfoList() {
        return seatInfoList;
    }

    public void setSeatInfoList(List<SeatInfo> seatInfoList) {
        this.seatInfoList = seatInfoList;
    }
}
