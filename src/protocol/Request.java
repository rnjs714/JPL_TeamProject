package protocol;

import java.util.Map;

// 클라이언트 요청 메시지
public class Request {
    private String command;
    private Map<String, Object> body;

    // JSON 역직렬화
    public Request() {
    }

    // 전체 값 생성
    public Request(String command, Map<String, Object> body) {
        this.command = command;
        this.body = body;
    }

    // 필드 접근자
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }
}
