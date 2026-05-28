package session;

import domain.User;
import protocol.Response;

import java.util.Map;
import java.util.Scanner;

public class LoginSession implements Session {
    @Override
    public void show(SessionManager manager) {
        // TODO: 로그인, 회원가입, 종료 메뉴를 출력하고 사용자 선택에 따라 login(), register(), stop()을 호출한다.
    }

    private void login(SessionManager manager) {
        // TODO: ID와 비밀번호를 입력받아 LOGIN 요청을 보내고, 성공하면 currentUser를 저장한 뒤 HomeSession으로 이동한다.
    }

    private void register(SessionManager manager) {
        // TODO: ID, 비밀번호, 이름을 입력받아 REGISTER 요청을 보내고 결과 메시지를 출력한다.
    }
}
