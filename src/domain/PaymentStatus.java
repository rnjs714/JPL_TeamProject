package domain;

/**
 * 그룹 예매에서 각 사용자별 결제 여부를 나타낸다.
 */
public enum PaymentStatus {
    // 아직 결제하지 않은 상태이다.
    NOT_PAID,
    // 결제를 완료한 상태이다.
    PAID
}
