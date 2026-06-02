package controller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import client.ApiClient;
import domain.GroupPayment;
import domain.GroupReservation;
import domain.Movie;
import domain.PaymentStatus;
import domain.Reservation;
import domain.Showtime;
import domain.Theater;
import protocol.Response;
import session.BookingSession;
import session.UserSession;

/**
 * GUI 화면과 서버 API 사이를 연결하는 컨트롤러이다.
 *
 * 화면 패널은 이 클래스의 메서드를 호출하고,
 * 이 클래스는 현재 로그인 사용자와 선택된 영화/상영 시간/좌석 정보를 서버 요청에 담아 보낸다.
 */
public class BookingController {
    private final ApiClient apiClient;
    private final UserSession userSession;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;

    public BookingController(ApiClient apiClient, UserSession userSession, BookingSession bookingSession,
            NavigationController navigationController) {
        this.apiClient = apiClient;
        this.userSession = userSession;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
    }

    public Showtime getShowtime(String showtimeId) {
        // 예매 내역 화면에서 showtimeId만 가지고 상영 시간 상세 정보를 다시 가져올 때 사용한다.
        return apiClient.getDataFromServer("GET_SHOWTIME", Map.of("showtimeId", showtimeId), new TypeReference<Showtime>() {});
    }

    public Movie getMovie(String movieId) {
        // 예매 내역에는 movieId만 저장되어 있으므로 화면 표시용 영화 제목을 서버에서 조회한다.
        return apiClient.getDataFromServer("GET_MOVIES", Map.of("movieId", movieId), new TypeReference<Movie>() {});
    }

    public Theater getTheater(String theaterId) {
        // 예매 내역과 좌석 화면에서 상영관 이름, 행/열 정보를 표시하기 위해 조회한다.
        return apiClient.getDataFromServer("GET_THEATER", Map.of("theaterId", theaterId), new TypeReference<Theater>() {});
    }

    public List<Movie> loadMovies() {
        try {
            return apiClient.getDataFromServer("LIST_MOVIES", Map.of(), new TypeReference<List<Movie>>() {});
        } catch (ClassCastException e) {
            throw new IllegalStateException("영화 목록 로드 실패: 응답 데이터 형식이 올바르지 않습니다.", e);
        }
    }

    public void selectMovie(Movie movie) {
        // 선택한 영화는 다음 화면인 상영 시간 목록에서 사용되므로 세션에 보관한다.
        bookingSession.setSelectedMovie(movie);
        navigationController.showShowtimes();
    }

    public List<Showtime> loadShowtimes() {
        try {
            return apiClient.getDataFromServer("LIST_SHOWTIMES",
                Map.of("movieId", bookingSession.getSelectedMovie().getId()), new TypeReference<List<Showtime>>() {});
        } catch (ClassCastException e) {
            throw new IllegalStateException("상영 시간 로드 실패: 응답 데이터 형식이 올바르지 않습니다.", e);
        }
    }

    public void selectShowtime(Showtime showtime) {
        // 좌석 화면은 상영 시간과 상영관 정보가 모두 필요하므로 여기에서 둘 다 세션에 저장한다.
        bookingSession.setSelectedShowtime(showtime);
        bookingSession.setSelectedTheater(apiClient.getDataFromServer("GET_THEATER", Map.of(
            "theaterId", showtime.getTheaterId()), new TypeReference<Theater>() {}));
        navigationController.showSeats();
    }

    public void reserveSelectedSeats() {
        try {
            if(bookingSession.getSelectedSeats().isEmpty()) {
                throw new IllegalStateException("예약할 좌석을 선택해주세요.");
            }

            // 개인 예매는 선택한 좌석이 바로 확정 예약으로 저장된다.
            Response response = apiClient.send("RESERVE", Map.of(
                "userId", userSession.getCurrentUser().getId(),
                "showtimeId", bookingSession.getSelectedShowtime().getId(),
                "seatCodes", bookingSession.getSelectedSeats()));
            if(response.isSuccess()) {
                navigationController.showMessage("Reservation successful!");
                bookingSession.clear();
                navigationController.showReservations();
            } else {
                navigationController.showMessage(response.getMessage());
            }
        } catch (IllegalStateException e) {
            navigationController.showMessage("Reservation failed: " + e.getMessage());
        } catch (Exception e) {
            navigationController.showMessage("An error occurred: " + e.getMessage());
        }
    }

    public int calculateSelectedSeatPrice() {
        if(bookingSession.getSelectedSeats().isEmpty()) {
            return 0;
        }
        // 좌석 가격은 위조를 막기 위해 클라이언트에서 직접 계산하지 않고 서버 계산 결과를 사용한다.
        return apiClient.getDataFromServer("CALCULATE_PRICE", Map.of(
                "showtimeId", bookingSession.getSelectedShowtime().getId(),
                "seatCodes", bookingSession.getSelectedSeats()), new TypeReference<Integer>() {});
    }

    public Map<String, Object> calculateSeatPriceInfo(String seatCode) {
        // 좌석 버튼에 표시할 시야 점수, 좌석별 가격, 좌석 상태를 한 번에 받아온다.
        return apiClient.getDataFromServer("GET_SEAT_PRICE", Map.of(
                "showtimeId", bookingSession.getSelectedShowtime().getId(),
                "seatCode", seatCode), new TypeReference<Map<String, Object>>() {});
    }

    public GroupReservation createGroupReservation(List<String> friendIds) {
        if(friendIds == null || friendIds.isEmpty()) {
            throw new IllegalStateException("친구를 한 명 이상 입력해주세요.");
        }
        if(bookingSession.getSelectedSeats().isEmpty()) {
            throw new IllegalStateException("그룹 예매로 잡아둘 좌석을 직접 선택해주세요.");
        }

        // 그룹 예매는 대표자가 직접 선택한 좌석을 서버에 보내 TEMP_HOLD 상태로 만든다.
        GroupReservation group = apiClient.getDataFromServer("CREATE_GROUP_RESERVATION", Map.of(
                "leaderId", userSession.getCurrentUser().getId(),
                "showtimeId", bookingSession.getSelectedShowtime().getId(),
                "seatCodes", bookingSession.getSelectedSeats(),
                "friendIds", friendIds), new TypeReference<GroupReservation>() {});
        bookingSession.getSelectedSeats().clear();
        return group;
    }

    public GroupReservation payGroupReservation(String groupId) {
        if(groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalStateException("그룹 ID를 입력해주세요.");
        }

        // 서버는 현재 로그인 사용자의 결제 상태만 PAID로 바꾸고, 전원 결제 여부를 다시 확인한다.
        return apiClient.getDataFromServer("PAY_GROUP_RESERVATION", Map.of(
                "groupId", groupId.trim(),
                "userId", userSession.getCurrentUser().getId()), new TypeReference<GroupReservation>() {});
    }

    /**
     * 현재 로그인 사용자가 해당 그룹 예매에서 이미 결제했는지 확인한다.
     *
     * 서버에서도 중복 결제를 막고 있지만, 화면에서 Pay 버튼을 미리 비활성화하면
     * 사용자가 불필요하게 버튼을 눌러 오류 메시지를 보는 일을 줄일 수 있다.
     */
    public boolean isCurrentUserPaidInGroup(GroupReservation group) {
        if(group == null || userSession.getCurrentUser() == null) {
            return false;
        }

        String currentUserId = userSession.getCurrentUser().getId();
        for(GroupPayment payment : group.getPaymentList()) {
            if(currentUserId.equals(payment.getUserId())) {
                return payment.getPaymentStatus() == PaymentStatus.PAID;
            }
        }
        return false;
    }

    public List<GroupReservation> loadGroupReservations() {
        // 사용자가 속한 그룹 예매를 가져와 결제 대기/확정/취소 상태를 한 화면에 보여준다.
        return apiClient.getDataFromServer("LIST_GROUP_RESERVATIONS",
                Map.of("userId", userSession.getCurrentUser().getId()), new TypeReference<List<GroupReservation>>() {});
    }

    public List<Reservation> loadReservations() {
        try {
            return apiClient.getDataFromServer("LIST_RESERVATIONS",
                    Map.of("userId", userSession.getCurrentUser().getId()), new TypeReference<List<Reservation>>() {});
        } catch (ClassCastException e) {
            throw new IllegalStateException("예매 내역 로드 실패: 응답 데이터 형식이 올바르지 않습니다.", e);
        }
    }

    public void cancelReservation(String reservationId) {
        if(navigationController.showConfirmation("Do you want to cancel this reservation?")) {
            try {
                // 취소 요청에는 현재 로그인 사용자를 함께 보내서 남의 예매를 취소하지 못하게 한다.
                Response response = apiClient.send("CANCEL_RESERVATION", Map.of(
                    "reservationId", reservationId,
                    "requesterId", userSession.getCurrentUser().getId()));
                if(response.isSuccess()) {
                    navigationController.showMessage("Reservation cancelled successfully.");
                    navigationController.showReservations();
                } else {
                    navigationController.showMessage(response.getMessage());
                }
            } catch (IllegalStateException e) {
                navigationController.showMessage("Failed to cancel reservation: " + e.getMessage());
            } catch (Exception e) {
                navigationController.showMessage("An error occurred: " + e.getMessage());
            }
        }
    }
}
