package session;

import domain.Reservation;

import java.util.List;

public class ReservationDetailSession implements Session {
    @Override
    public void show(SessionManager manager) {
        // TODO: 로그인한 사용자의 예매 내역을 요청하고 각 예약 정보를 출력한다.
    }

    private List<Reservation> requestReservations(SessionManager manager) {
        // TODO: ApiClient로 LIST_RESERVATIONS 요청을 보내고 응답 data를 List<Reservation>으로 변환한다.
        return null;
    }

    private void printReservation(Reservation reservation) {
        // TODO: 예약 ID, 상영 일정 ID, 좌석, 예약 상태, 예약 일시를 보기 좋게 출력한다.
    }
}
