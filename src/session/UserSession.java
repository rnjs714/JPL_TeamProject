package session;

import domain.User;

public class UserSession {
    private User currentUser;

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void login(User user) {
        // TODO: 로그인 성공 응답에서 받은 User 객체를 저장한다.
        this.currentUser = user;
    }

    public void logout() {
        // TODO: 로그아웃 버튼 클릭 시 호출하고, GUI를 로그인 화면으로 돌린다.
        this.currentUser = null;
    }
}
