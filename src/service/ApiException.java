package service;

// API 계층 예외
public class ApiException extends RuntimeException {
    // 메시지 기반 예외 생성
    public ApiException(String message) {
        super(message);
    }
}
