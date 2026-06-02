package domain;

/**
 * 그룹 예매 전체의 진행 상태를 나타낸다.
 * 그룹 예매는 좌석 선택, 각자 결제, 최종 확정 과정을 거치므로 상태 구분이 필요하다.
 */
public enum GroupReservationStatus {
    // 좌석은 임시 홀딩되었지만 아직 모든 그룹원이 결제하지 않은 상태이다.
    PENDING,
    // 그룹원 전원이 결제해서 좌석이 최종 예약된 상태이다.
    CONFIRMED,
    // 결제 시간이 지났거나 예외가 발생해 그룹 예매가 취소된 상태이다.
    CANCELLED
}
