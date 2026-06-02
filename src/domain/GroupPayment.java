package domain;

import java.time.LocalDateTime;

/**
 * 그룹 예매에서 한 명의 결제 상태를 저장하는 클래스이다.
 *
 * 그룹 예매는 "전원 결제 시 확정" 방식이므로,
 * 그룹 전체 상태와 별도로 사용자별 결제 상태를 기록해야 한다.
 */
public class GroupPayment {
    // 결제 대상 사용자 ID이다. group.memberIds의 각 사용자와 연결된다.
    private String userId;
    // 아직 결제하지 않았는지, 이미 결제했는지를 구분한다.
    private PaymentStatus paymentStatus;
    // 결제 완료 시간을 저장해서 결제 흐름을 추적할 수 있게 한다.
    private LocalDateTime paidAt;

    /**
     * JSON 역직렬화용 기본 생성자이다.
     * 결제 정보가 새로 만들어지면 기본 상태는 NOT_PAID로 둔다.
     */
    public GroupPayment() {
        this.paymentStatus = PaymentStatus.NOT_PAID;
    }

    public GroupPayment(String userId) {
        this.userId = userId;
        this.paymentStatus = PaymentStatus.NOT_PAID;
    }

    /**
     * 사용자가 결제했을 때 호출한다.
     * 이 메서드는 결제 상태와 결제 시간을 한 번에 바꿔서 데이터가 어긋나지 않게 한다.
     */
    public void pay() {
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
