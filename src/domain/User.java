package domain;

// 사용자 계정
public class User {
    private String id;
    private String password;

    // JSON 역직렬화
    public User() {
    }

    // 전체 값 생성
    public User(String id, String password) {
        this.id = id;
        this.password = password;
    }

    // 필드 접근자
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
