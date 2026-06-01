package domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private String id;
    private String userId;
    private String showtimeId;
    private List<String> seatCodes;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    // 예약 확정 시 서버에서 계산한 최종 금액이다.
    private int totalPrice;

    public Reservation() {
        this.seatCodes = new ArrayList<>();
        this.status = ReservationStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    public Reservation(String id, String userId, String showtimeId, List<String> seatCodes, ReservationStatus status, LocalDateTime createdAt) {
        this(id, userId, showtimeId, seatCodes, status, createdAt, 0);
    }

    public Reservation(String id, String userId, String showtimeId, List<String> seatCodes, ReservationStatus status, LocalDateTime createdAt, int totalPrice) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatCodes = seatCodes;
        this.status = status;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

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

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getPeopleCount() {
        // 그룹 예매 인원 수는 예약된 좌석 수와 같다.
        if(seatCodes == null) {
            return 0;
        }
        return seatCodes.size();
    }
}
