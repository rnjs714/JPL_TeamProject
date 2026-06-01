package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import repository.DataRepository;

public class MovieBookingServer {
    private static final int PORT = 5555;
    private final DataRepository repository;

    public MovieBookingServer() {
        this.repository = new DataRepository("data/movie-booking.json");
    }

    public static void main(String[] args) {
    	new MovieBookingServer().start();
    }

    private void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(socket, repository));
                thread.start();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start the server.", e);
        }
    }
}
