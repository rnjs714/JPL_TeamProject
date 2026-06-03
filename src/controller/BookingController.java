package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import domain.Movie;
import domain.Reservation;
import domain.SeatInfo;
import domain.Showtime;
import domain.Theater;
import service.ApiException;
import service.ApiService;
import session.BookingSession;
import session.UserSession;

// 예매 흐름 제어
public class BookingController {
    private final ApiService apiService;
    private final UserSession userSession;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;

    // 의존 객체 연결
    public BookingController(ApiService apiService, UserSession userSession, BookingSession bookingSession,
            NavigationController navigationController) {
        this.apiService = apiService;
        this.userSession = userSession;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
    }

    // 상영관 조회
    public Theater getTheater(String theaterId) {
        return apiService.getTheater(theaterId);
    }

    // 선택 영화 조회
    public Movie getSelectedMovie() {
        return bookingSession.getSelectedMovie();
    }

    // 선택 상영관 조회
    public Theater getSelectedTheater() {
        return bookingSession.getSelectedTheater();
    }

    // 선택 상영 일정 조회
    public Showtime getSelectedShowtime() {
        return bookingSession.getSelectedShowtime();
    }

    // 선택 좌석 조회
    public List<String> getSelectedSeats() {
        return bookingSession.getSelectedSeats();
    }

    // 예약 좌석 조회
    public Set<String> getReservedSeats() {
        return bookingSession.getSelectedShowtime().getReservedSeats();
    }

    // 영화 선택 초기화
    public void resetSelectedMovie() {
        bookingSession.setSelectedMovie(null);
        bookingSession.setSelectedShowtime(null);
        bookingSession.setSelectedTheater(null);
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    // 상영 일정 선택 초기화
    public void resetSelectedShowtime() {
        bookingSession.setSelectedShowtime(null);
        bookingSession.setSelectedTheater(null);
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    // 좌석 선택 초기화
    public void resetSelectedSeats() {
        bookingSession.setSelectedSeats(new ArrayList<>());
    }

    // 좌석 선택 토글
    public void toggleSeat(String seatCode) {
        List<String> selectedSeats = bookingSession.getSelectedSeats();
        if (selectedSeats.contains(seatCode)) {
            selectedSeats.remove(seatCode);
        } else {
            selectedSeats.add(seatCode);
        }
    }

    // 영화 목록 로드
    public List<Movie> loadMovies() {
        try {
            List<Movie> movies = apiService.getMovieList();
            if(movies.isEmpty()) { // 영화가 없는 경우 처리
                throw new IllegalStateException("No movies found.");
            }
            return movies;
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            throw new IllegalStateException(e.getMessage());
        }
    }

    // 영화 선택 처리
    public void selectMovie(Movie movie) {
        bookingSession.setSelectedMovie(movie);
        navigationController.showShowtimes();
    }

    // 상영 일정 목록 로드
    public List<Showtime> loadShowtimes() {
        try {
            List<Showtime> showtimes = apiService.getShowtimeList(bookingSession.getSelectedMovie().getId());
            if(showtimes.isEmpty()) { // 상영 일정이 없는 경우 처리
                throw new IllegalStateException("No showtimes found for the selected movie.");
            }
            return showtimes;
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            throw new IllegalStateException(e.getMessage());
        } 
    }

    // 상영 일정 선택 처리
    public void selectShowtime(Showtime showtime) {
        bookingSession.setSelectedShowtime(showtime);
        bookingSession.setSelectedTheater(apiService.getTheater(showtime.getTheaterId()));
        navigationController.showSeats();
    }

    public List<SeatInfo> loadSeatInfoList() {
        try {
            List<SeatInfo> seatInfoList = apiService.getSeatInfoList(bookingSession.getSelectedShowtime().getId());
            if(seatInfoList.isEmpty()) {
                throw new IllegalStateException("Failed to load seat information.");
            }
            bookingSession.setSeatInfoList(seatInfoList);
            return seatInfoList;
        } catch (ApiException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    // 선택 좌석 예매 요청
    public void reserveSelectedSeats() {
        try {
            List<String> selectedSeats = bookingSession.getSelectedSeats();
            if(selectedSeats == null || selectedSeats.isEmpty()) { // 좌석이 선택되지 않은 경우 처리
                navigationController.showMessage("Please select seats to reserve.");
                return;
            }
            Reservation reservation = apiService.requestReservation(userSession.getCurrentUser().getId(), 
                                            bookingSession.getSelectedShowtime().getId(), 
                                            bookingSession.getSelectedSeats());
            navigationController.showMessage("Reservation successful!\nReservation ID: " + reservation.getId());
            navigationController.showReservations();
            resetSelectedMovie();
        } catch (ApiException e) { // API 호출 실패 시 예외 처리
            navigationController.showMessage(e.getMessage());
        }
    }

    // ===== 가격 추가 기능 =====

    // 선택 좌석 가격 계산
    public int calculateSelectedSeatPrice() {
        List<String> selectedSeats = bookingSession.getSelectedSeats();

        if (selectedSeats == null || selectedSeats.isEmpty()) {
            return 0;
        }
        int totalPrice = 0;
        for(SeatInfo seatInfo : bookingSession.getSeatInfoList()) {
            if(selectedSeats.contains(seatInfo.getSeatCode())) {
                totalPrice += seatInfo.getPrice();
            }
        }
        return totalPrice;
    }

    // 이용 가능한 좌석 중 시야 점수 합이 가장 높은 가로 연속 좌석 조회
    public void selectBestSeats(int peopleCount) {
        if (peopleCount <= 0) {
            throw new IllegalArgumentException("peopleCount must be greater than 0.");
        }

        resetSelectedSeats(); // 자동 선택은 기존 선택 좌석을 대체한다.

        List<SeatInfo> seatInfoList = bookingSession.getSeatInfoList();
        if(seatInfoList == null || seatInfoList.isEmpty()) {
            seatInfoList = loadSeatInfoList(); // 좌석 화면에서 아직 정보를 불러오지 않은 경우 직접 로드
        }

        Theater theater = bookingSession.getSelectedTheater();
        if(theater == null || peopleCount > theater.getColumns()) {
            throw new IllegalStateException("No consecutive seats match to people count.");
        }
        
        // 연속 좌석 후보를 빠르게 확인하기 위해 좌석 코드를 기준으로 좌석 정보를 저장한다.
        Map<String, SeatInfo> seatInfoMap = new HashMap<>();
        for(SeatInfo seatInfo : seatInfoList) {
            seatInfoMap.put(seatInfo.getSeatCode(), seatInfo);
        }

        List<String> bestSeats = new ArrayList<>();
        int bestViewScore = -1;

        // 모든 좌석을 시작점으로 보고, 같은 행에서 peopleCount만큼 오른쪽으로 이어지는지 확인한다.
        for(String seatCode : theater.getAllSeatCodes()) {
            List<String> currentSeats = new ArrayList<>();
            int currentViewScore = 0;
            boolean available = true;

            char row = seatCode.charAt(0);
            int column = Integer.parseInt(seatCode.substring(1));
            
            if(column > theater.getColumns() - peopleCount + 1) {
                continue; // 현재 열에서 시작하면 필요한 인원수만큼 같은 행에 배치할 수 없다.
            }

            for(int offset = 0; offset < peopleCount; offset++) {
                String targetSeatCode = "" + row + (column + offset); // 시작 좌석부터 오른쪽 좌석을 차례로 확인
                SeatInfo seatInfo = seatInfoMap.get(targetSeatCode);

                if(seatInfo == null || seatInfo.isReserved()) {
                    available = false; // 후보 중 하나라도 없거나 예약되어 있으면 해당 묶음은 제외
                    break;
                }

                currentSeats.add(targetSeatCode);
                currentViewScore += seatInfo.getViewScore();
            }

            if(available && currentViewScore > bestViewScore) {
                bestSeats = currentSeats; // 현재까지 찾은 후보 중 시야 점수 합이 가장 높은 묶음 저장
                bestViewScore = currentViewScore;
            }
        }

        if(bestSeats.isEmpty()) {
            throw new IllegalStateException("No consecutive seats match to people count.");
        }

        for(String seat : bestSeats) {
            bookingSession.getSelectedSeats().add(seat); // 찾은 추천 좌석을 실제 선택 목록에 반영
        }
    }

}
