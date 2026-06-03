package domain;

// 좌석별 가격/시야/예약 상태 정보
public class SeatInfo {
    private String seatCode;
    private int viewScore;
    private int price;
    private boolean reserved;

    // JSON 역직렬화
    public SeatInfo() {
    }

    // 전체 값 생성
    public SeatInfo(String seatCode, int viewScore, int price, boolean reserved) {
        this.seatCode = seatCode;
        this.viewScore = viewScore;
        this.price = price;
        this.reserved = reserved;
    }

    // 필드 접근자
    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public int getViewScore() {
        return viewScore;
    }

    public void setViewScore(int viewScore) {
        this.viewScore = viewScore;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}
