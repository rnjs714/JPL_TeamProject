package controller;

import client.ApiClient;
import domain.User;
import protocol.Response;
import session.UserSession;

import java.util.Map;

public class AuthController {
    private ApiClient apiClient;
    private UserSession userSession;
    private NavigationController navigationController;

    public AuthController(ApiClient apiClient, UserSession userSession, NavigationController navigationController) {
        this.apiClient = apiClient;
        this.userSession = userSession;
        this.navigationController = navigationController;
    }

    public void login(String id, String password) {
        // TODO: LOGIN 요청을 보내고, 성공하면 응답 data를 User로 변환해서 userSession.login(user)를 호출한다.
        // TODO: 로그인 성공 후 영화 목록 화면으로 이동하고, 실패하면 LoginPanel에 오류 메시지를 보여준다.
        Response response = apiClient.send("LOGIN", Map.of("id", id, "password", password));
        if (response.isSuccess()) {
            userSession.login(null);
            navigationController.showMovies();
        }
    }

    public void register(String id, String password, String name) {
        // TODO: REGISTER 요청을 보내고, 성공/실패 메시지를 LoginPanel에 표시한다.
        apiClient.send("REGISTER", Map.of("id", id, "password", password, "name", name));
    }

    public void logout() {
        userSession.logout();
        navigationController.showLogin();
    }
}
