package domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 그룹 예매 한 건을 저장하는 도메인 클래스이다.
 *
 * 개인 예매와 다르게 그룹 예매는 바로 RESERVED가 되지 않는다.
 * 대표자가 좌석을 고르면 먼저 TEMP_HOLD처럼 임시로 잡아 두고,
 * 그룹원 전원이 결제했을 때만 최종 CONFIRMED 상태로 바뀐다.
 */
public class GroupReservation {
    // groupId는 서버에서 만든 그룹 예매 고유 번호이다. 결제할 때 이 값으로 그룹을 찾는다.
    private String groupId;
    // leaderId는 그룹 예매를 만든 대표자 ID이다. 좌석 선택 권한은 대표자에게만 있다.
    private String leaderId;
    // memberIds에는 대표자와 친구 ID가 모두 들어간다.
    private List<String> memberIds;
    private String showtimeId;
    // seatCodes는 대표자가 직접 선택한 좌석 목록이다. 그룹 인원 수와 같아야 한다.
    private List<String> seatCodes;
    // 그룹 전체 예매 상태이다. PENDING이면 결제 대기, CONFIRMED이면 확정, CANCELLED이면 취소이다.
    private GroupReservationStatus reservationStatus;
    // 그룹원별 결제 여부를 따로 저장해서 전원 결제 여부를 확인한다.
    private List<GroupPayment> paymentList;
    private LocalDateTime createdAt;
    // 임시 홀딩 만료 시간이다. 이 시간이 지나면 좌석은 다시 선택 가능한 상태로 풀린다.
    private LocalDateTime holdExpiresAt;
    // 선택한 모든 좌석의 총 가격이다. 시야 점수 기반 좌석 가격 계산 결과가 반영된다.
    private int totalPrice;

    /**
     * Jackson이 JSON 데이터를 객체로 바꿀 때 사용하는 기본 생성자이다.
     * 리스트 값이 null이 되지 않도록 여기에서 기본값을 만들어 둔다.
     */
    public GroupReservation() {
        this.memberIds = new ArrayList<>();
        this.seatCodes = new ArrayList<>();
        this.paymentList = new ArrayList<>();
        this.reservationStatus = GroupReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 새 그룹 예매를 만들 때 사용하는 생성자이다.
     * 그룹원 목록을 기준으로 결제 정보도 함께 만들어야 나중에 전원 결제 여부를 확인할 수 있다.
     */
    public GroupReservation(String groupId, String leaderId, List<String> memberIds) {
        this();
        this.groupId = groupId;
        this.leaderId = leaderId;
        this.memberIds = memberIds;
        for(String memberId : memberIds) {
            this.paymentList.add(new GroupPayment(memberId));
        }
    }

    /**
     * 현재 사용자가 이 그룹 예매에 포함된 사람인지 확인한다.
     * 그룹원이 아닌 사람이 결제하거나 그룹 예매 내역을 조회하는 것을 막기 위해 사용한다.
     */
    public boolean isMember(String userId) {
        for(String memberId : memberIds) {
            if(memberId.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 특정 그룹원의 결제 정보를 찾는다.
     * 결제 버튼을 눌렀을 때 해당 사용자의 상태만 PAID로 바꾸기 위해 필요하다.
     */
    public GroupPayment findPayment(String userId) {
        for(GroupPayment payment : paymentList) {
            if(payment.getUserId().equals(userId)) {
                return payment;
            }
        }
        return null;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
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

    public GroupReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(GroupReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public List<GroupPayment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<GroupPayment> paymentList) {
        this.paymentList = paymentList;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
