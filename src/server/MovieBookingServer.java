package server;

import repository.DataRepository;

public class MovieBookingServer {
    private static final int PORT = 5555;
    private DataRepository repository;

    public static void main(String[] args) {
        // TODO: 서버 객체를 만들고 start()를 호출한다.
    	DataRepository repository = new DataRepository("data/movie-booking.json");
        System.out.println(repository.findMovie("M1").getTitle());
    }

    private void start() {
        // TODO: ServerSocket을 열고 클라이언트 접속마다 ClientHandler Thread를 생성한다.
    }
}
