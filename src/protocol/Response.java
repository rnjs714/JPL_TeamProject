package protocol;

// 서버 응답 메시지
public class Response {
    private boolean success;
    private String message;
    private Object data;

    // JSON 역직렬화
    public Response() {
    }

    // 전체 값 생성
    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 성공 응답 생성
    public static Response ok(Object data) {
        return new Response(true, null, data);
    }

    // 실패 응답 생성
    public static Response fail(String message) {
        return new Response(false, message, null);
    }

    // 필드 접근자
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
