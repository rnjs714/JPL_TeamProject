package session;

import domain.User;

// 로그인 사용자 상태
public class UserSession {
    private User currentUser;

    // 현재 사용자 조회
    public User getCurrentUser() {
        return currentUser;
    }

    // 현재 사용자 저장
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
