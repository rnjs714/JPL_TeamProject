package client;

import domain.User;

import java.util.Scanner;

public class ClientContext {
    private ApiClient apiClient;
    private Scanner scanner;
    private User currentUser;

    public ClientContext(ApiClient apiClient, Scanner scanner) {
        this.apiClient = apiClient;
        this.scanner = scanner;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}
