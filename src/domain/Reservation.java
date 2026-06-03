package domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 예매 정보
public class Reservation {
    private String id;
    private String userId;
    private String showtimeId;
    private List<String> seatCodes;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private int totalPrice;

    // JSON 역직렬화
    public Reservation() {
        this.seatCodes = new ArrayList<>();
        this.status = ReservationStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    // 전체 값 생성
    public Reservation(String id, String userId, String showtimeId, List<String> seatCodes, ReservationStatus status, LocalDateTime createdAt) {
        this(id, userId, showtimeId, seatCodes, status, createdAt, 0);
    }

    // 가격 포함 값 생성
    public Reservation(String id, String userId, String showtimeId, List<String> seatCodes,
            ReservationStatus status, LocalDateTime createdAt, int totalPrice) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatCodes = seatCodes;
        this.status = status;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
    }

    // 예매 확정
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    // 예매 취소
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

    // 필드 접근자
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<String> getSeatCodes() {
        return seatCodes;
    }

    public void setSeatCodes(List<String> seatCodes) {
        this.seatCodes = seatCodes;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 가격 접근자
    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
