package controller;

import domain.User;
import service.ApiException;
import service.ApiService;
import session.UserSession;

// 인증 흐름 제어
public class AuthController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final NavigationController navigationController;

    // 의존 객체 연결
    public AuthController(ApiService apiService, UserSession userSession, NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.navigationController = navigationController;
    }

    // 로그인 사용자 조회
    public User getCurrentUser() {
        return userSession.getCurrentUser();
    }

    // 로그인 처리
    public void login(String id, String password) {
        try {
            if(id.isBlank() || password.isBlank()) { // 입력 검증
                navigationController.showMessage("ID and password cannot be empty.");
                return;
            }
            User user = apiService.login(id, password);
            userSession.setCurrentUser(user);
            navigationController.showHome(); // 로그인 성공 후 홈 화면으로 이동
        } catch (ApiException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    // 회원가입 처리
    public void register(String id, String password) {
        try {
            if(id.isBlank() || password.isBlank()) { // 입력 검증
                navigationController.showMessage("ID and password cannot be empty.");
                return;
            }
            apiService.register(id, password);
            navigationController.showMessage("Registration successful! Please log in.");
        } catch (ApiException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    // 로그아웃 처리
    public void logout() {
        userSession.setCurrentUser(null);
        navigationController.showLogin();
    }
}
