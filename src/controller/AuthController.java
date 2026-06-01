package controller;

import domain.User;
import service.ApiException;
import service.ApiService;
import session.UserSession;

public class AuthController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final NavigationController navigationController;

    public AuthController(ApiService apiService, UserSession userSession, NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.navigationController = navigationController;
    }

    public User getCurrentUser() {
        return userSession.getCurrentUser();
    }

    public void login(String id, String password) {
        // TODO: 로그인 성공 후 영화 목록 화면으로 이동하고, 실패하면 LoginPanel에 오류 메시지를 보여준다.
        try {
            if(id.isBlank() || password.isBlank()) {
                navigationController.showMessage("ID and password cannot be empty.");
                return;
            }
            User user = apiService.login(id, password);
            userSession.setCurrentUser(user);
            navigationController.showHome();
        } catch (ApiException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    public void register(String id, String password) {
        // TODO: REGISTER 요청을 보내고, 성공/실패 메시지를 LoginPanel에 표시한다.
        try {
            if(id.isBlank() || password.isBlank()) {
                navigationController.showMessage("ID and password cannot be empty.");
                return;
            }
            apiService.register(id, password);
            navigationController.showMessage("Registration successful! Please log in.");
        } catch (ApiException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    public void logout() {
        userSession.setCurrentUser(null);
        navigationController.showLogin();
    }
}
