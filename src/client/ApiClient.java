package client;

import protocol.Request;
import protocol.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

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

    public Response send(String command, Map<String, Object> body) {
        try {
            Request request = new Request(command, body);
            writer.println(objectMapper.writeValueAsString(request));
            String responseJson = reader.readLine();
            return objectMapper.readValue(responseJson, Response.class);
        } catch (IOException e) {
            throw new IllegalStateException("서버 통신 실패", e);
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
