package session;

import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import protocol.Response;

import java.util.List;

public class SeatSelectSession implements Session {
    private String showtimeId;
    private String theaterId;

    public SeatSelectSession(String showtimeId, String theaterId) {
        this.showtimeId = showtimeId;
        this.theaterId = theaterId;
    }

    @Override
    public void show(SessionManager manager) {
        // TODO: 좌석 배치를 출력하고 좌석 입력을 받은 뒤 reserve()를 호출한다.
    }

    private void printSeatMap(Theater theater, Showtime showtime) {
        // TODO: rows/columns를 기준으로 좌석표를 출력하고 예약된 좌석은 표시한다.
    }

    private String readSeatCode(SessionManager manager) {
        // TODO: 사용자에게 A1, B3 같은 좌석 코드를 입력받아 반환한다.
        return null;
    }

    private void reserve(SessionManager manager, String seatCode) {
        // TODO: RESERVE 요청을 보내 예매를 생성하고 성공/실패 메시지를 출력한다.
    }
}
