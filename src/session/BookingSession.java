package session;

import java.util.ArrayList;
import java.util.List;

import domain.Movie;
import domain.Showtime;
import domain.Theater;

public class BookingSession {
    private Movie selectedMovie;
    private Showtime selectedShowtime;
    private Theater selectedTheater;
    private final List<String> selectedSeats;

    public BookingSession() {
        this.selectedSeats = new ArrayList<>();
    }

    public void clear() {
        // TODO: 예매 완료, 취소, 로그아웃 시 선택 상태를 초기화한다.
        selectedMovie = null;
        selectedShowtime = null;
        selectedTheater = null;
        selectedSeats.clear();
    }

    public Movie getSelectedMovie() {
        return selectedMovie;
    }

    public void setSelectedMovie(Movie selectedMovie) {
        // TODO: 영화 카드/목록 클릭 시 선택된 영화를 저장한다.
        this.selectedMovie = selectedMovie;
    }

    public Showtime getSelectedShowtime() {
        return selectedShowtime;
    }

    public void setSelectedShowtime(Showtime selectedShowtime) {
        // TODO: 상영 시간 버튼 클릭 시 선택된 상영 일정을 저장한다.
        this.selectedShowtime = selectedShowtime;
    }

    public Theater getSelectedTheater() {
        return selectedTheater;
    }

    public void setSelectedTheater(Theater selectedTheater) {
        // TODO: 선택된 상영 일정의 theaterId로 상영관 정보를 조회한 뒤 저장한다.
        this.selectedTheater = selectedTheater;
    }

    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void toggleSeat(String seatCode) {
        // TODO: 좌석 버튼 클릭 시 선택/선택해제를 처리한다.
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
        } else {
            selectedSeats.add(seatCode);
        }
    }
}
