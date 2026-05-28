package server;

import protocol.Request;
import protocol.Response;
import repository.DataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataRepository repository;
    private ObjectMapper objectMapper;

    public ClientHandler(Socket socket, DataRepository repository) {
        this.socket = socket;
        this.repository = repository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void run() {
        // TODO: 클라이언트 요청 JSON을 읽고 Request로 변환한 뒤 Response JSON을 반환한다.
    }

    private Response handle(Request request) {
        // TODO: command 값에 따라 register, login, reserve 등으로 분기한다.
        return null;
    }

    private Response register(Map<String, Object> body) {
        // TODO: 회원가입 요청을 처리한다.
        return null;
    }

    private Response login(Map<String, Object> body) {
        // TODO: 로그인 요청을 처리한다.
        return null;
    }

    private Response listMovies() {
        // TODO: 영화 목록 요청을 처리한다.
        return null;
    }

    private Response listShowtimes(Map<String, Object> body) {
        // TODO: 영화 ID로 상영 일정 목록 요청을 처리한다.
        return null;
    }

    private Response reserve(Map<String, Object> body) {
        // TODO: 좌석 예매 요청을 처리한다.
        return null;
    }

    private Response cancelReservation(Map<String, Object> body) {
        // TODO: 예약 취소 요청을 처리한다.
        return null;
    }

    private Response listReservations(Map<String, Object> body) {
        // TODO: 사용자 ID로 예매 내역 요청을 처리한다.
        return null;
    }
}
