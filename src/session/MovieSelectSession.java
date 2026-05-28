package session;

import domain.Movie;

import java.util.List;

public class MovieSelectSession implements Session {
    @Override
    public void show(SessionManager manager) {
        // TODO: 서버에서 영화 목록을 받아 출력하고, 사용자가 고른 영화 ID로 ShowtimeSelectSession을 연다.
    }

    private List<Movie> requestMovies(SessionManager manager) {
        // TODO: ApiClient로 LIST_MOVIES 요청을 보내고 응답 data를 List<Movie>로 변환한다.
        return null;
    }
}
