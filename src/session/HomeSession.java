package session;

import java.util.Scanner;

public class HomeSession implements Session {
    @Override
    public void show(SessionManager manager) {
        // TODO: 홈 메뉴를 출력하고 영화 선택, 예매 내역, 로그아웃 중 하나를 처리한다.
    }

    private void logout(SessionManager manager) {
        manager.getContext().logout();
        manager.replace(new LoginSession());
    }
}
