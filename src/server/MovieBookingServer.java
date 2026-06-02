package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import repository.DataRepository;

// 소켓 서버 실행
public class MovieBookingServer {
    private static final int PORT = 5555;
    private final DataRepository repository;

    // 공유 저장소 생성
    public MovieBookingServer() {
        this.repository = new DataRepository("data/movie-booking.json"); // JSON 파일 기반 저장소 초기화
    }

    // 서버 시작점
    public static void main(String[] args) {
    	new MovieBookingServer().start();
    }

    // 클라이언트 연결 수락
    private void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept(); // 클라이언트 연결 수락
                Thread thread = new Thread(new ClientHandler(socket, repository)); // 클라이언트 요청 처리 핸들러를 별도의 스레드로 실행
                thread.start(); // 클라이언트 요청 처리 시작
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start the server.", e);
        }
    }
}
