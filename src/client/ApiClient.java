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

public class ApiClient implements Closeable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    public ApiClient(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        } catch (IOException e) {
            throw new IllegalStateException("서버에 연결할 수 없습니다.", e);
        }
    }

    public Response send(String command, Map<String, Object> body) throws IllegalStateException {
        try {
            Request request = new Request(command, body);
            writer.println(objectMapper.writeValueAsString(request));
            String responseJson = reader.readLine();
            Response response = objectMapper.readValue(responseJson, Response.class);
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("서버 통신 실패", e);
        }
    }

    public <T> T getDataFromServer(String command, Map<String, Object> body, TypeReference<T> dataType) throws IllegalStateException {
        try {
            Response response = send(command, body);
            if (!response.isSuccess()) {
                throw new IllegalStateException("API 요청 실패: " + response.getMessage());
            }
            return objectMapper.convertValue(response.getData(), dataType);
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("클라이언트 소켓 종료 실패", e);
        }
    }
}
