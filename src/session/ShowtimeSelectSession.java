package session;

import domain.Showtime;

import java.util.List;

public class ShowtimeSelectSession implements Session {
    private String movieId;

    public ShowtimeSelectSession(String movieId) {
        this.movieId = movieId;
    }

    @Override
    public void show(SessionManager manager) {
        // TODO: 선택된 영화의 상영 일정을 출력하고, 선택된 showtimeId/theaterId로 SeatSelectSession을 연다.
    }

    private List<Showtime> requestShowtimes(SessionManager manager) {
        // TODO: ApiClient로 LIST_SHOWTIMES 요청을 보내고 응답 data를 List<Showtime>으로 변환한다.
        return null;
    }
}
