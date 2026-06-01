package controller;

import java.util.Map;

import client.ApiClient;
import domain.User;
import protocol.Response;
import session.UserSession;

public class AuthController {
    private final ApiClient apiClient;
    private final UserSession userSession;
    private final NavigationController navigationController;

    public AuthController(ApiClient apiClient, UserSession userSession, NavigationController navigationController) {
        this.apiClient = apiClient;
        this.userSession = userSession;
        this.navigationController = navigationController;
    }

    public User getCurrentUser() {
        return userSession.getCurrentUser();
    }

    public void login(String id, String password) {
        // TODO: 로그인 성공 후 영화 목록 화면으로 이동하고, 실패하면 LoginPanel에 오류 메시지를 보여준다.
        Response response = apiClient.send("LOGIN", Map.of("id", id, "password", password));
        if (response.isSuccess()) {
            userSession.setCurrentUser(new User(id, password));
            navigationController.showHome();
        } else {
            navigationController.showMessage(response.getMessage());
        }
    }

    public void register(String id, String password) {
        // TODO: REGISTER 요청을 보내고, 성공/실패 메시지를 LoginPanel에 표시한다.
        Response response = apiClient.send("REGISTER", Map.of("id", id, "password", password));
        if (response.isSuccess()) {
            navigationController.showMessage(response.getMessage());
        } else {
            navigationController.showMessage(response.getMessage());
        }
    }

    public void logout() {
        userSession.setCurrentUser(null);
        navigationController.showLogin();
    }
}
