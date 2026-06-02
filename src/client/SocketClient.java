package client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import protocol.Request;
import protocol.Response;

// 클라이언트 소켓 통신
public class SocketClient implements Closeable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    // 서버 연결 초기화
    public SocketClient(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to connect to the server."); // 연결 실패 시 예외 발생
        }
    }

    // 요청 전송 및 응답 수신
    public Response send(String command, Map<String, Object> body) {
        try {
            Request request = new Request(command, body);
            writer.println(objectMapper.writeValueAsString(request)); // 요청 전송
            String responseJson = reader.readLine(); // 응답 수신
            Response response = objectMapper.readValue(responseJson, Response.class);
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Server communication failed."); // 통신 실패 시 예외 발생
        }
    }

    // 응답 data 변환
    public <T> T getDataFromServer(String command, Map<String, Object> body, TypeReference<T> dataType) {
        Response response = send(command, body);
        if (!response.isSuccess()) {
            throw new IllegalStateException(response.getMessage()); // 실패 시 예외 발생
        }
        return objectMapper.convertValue(response.getData(), dataType);
    }

    @Override
    // 소켓 종료
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close the client socket."); // 소켓 종료 실패 시 예외 발생
        }
    }
}
