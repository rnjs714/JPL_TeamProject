package repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.DynamicPriceCalculator;
import domain.GroupPayment;
import domain.GroupReservation;
import domain.GroupReservationStatus;
import domain.Movie;
import domain.PaymentStatus;
import domain.Reservation;
import domain.ReservationStatus;
import domain.SeatStatus;
import domain.Showtime;
import domain.Theater;
import domain.User;

/**
 * JSON 파일에 저장된 영화 예매 데이터를 읽고 수정하는 저장소 클래스이다.
 * 개인 예매는 바로 확정하고, 그룹 예매는 TEMP_HOLD 상태를 거친 뒤 전원 결제 시 확정한다.
 */
public class DataRepository {
    // 그룹 예매 좌석을 임시로 잡아 두는 시간이다. 이 시간이 지나면 PENDING 그룹은 CANCELLED로 바뀐다.
    private static final int GROUP_HOLD_MINUTES = 15;

    private final File file;
    private final ObjectMapper objectMapper;
    private final DynamicPriceCalculator priceCalculator;
    private MovieBookingData data;

    public DataRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.priceCalculator = new DynamicPriceCalculator();
        initializeIfNeeded();
    }

    public synchronized boolean register(User user) {
        List<User> existingUsers = read().getUsers();
        for (User existingUser : existingUsers) {
            if (existingUser.getId().equals(user.getId())) {
                return false;
            }
        }
        existingUsers.add(user);
        write(this.data);
        return true;
    }

    public synchronized User login(String id, String password) {
        List<User> users = read().getUsers();
        for (User user : users) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public synchronized User findUser(String id) {
        List<User> users = read().getUsers();
        for(User user : users) {
            if(id.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    public synchronized List<Movie> findMovies() {
        return read().getMovies();
    }

    public synchronized Movie findMovie(String movieId) {
        List<Movie> movies = findMovies();
        for(Movie movie : movies) {
            if(movieId.equals(movie.getId())) {
                return movie;
            }
        }
        return null;
    }

    public synchronized List<Showtime> findShowtimesByMovie(String movieId) {
        List<Showtime> targetList = new ArrayList<>();
        List<Showtime> showtimes = read().getShowtimes();
        for(Showtime showtime : showtimes) {
            if(movieId.equals(showtime.getMovieId())) {
                targetList.add(showtime);
            }
        }
        return targetList;
    }

    public synchronized Showtime findShowtime(String showtimeId) {
        List<Showtime> showtimes = read().getShowtimes();
        for(Showtime showtime : showtimes) {
            if(showtimeId.equals(showtime.getId())) {
                return showtime;
            }
        }
        return null;
    }

    public synchronized Theater findTheater(String theaterId) {
        List<Theater> theaters = read().getTheaters();
        for(Theater theater : theaters) {
            if(theaterId.equals(theater.getId())) {
                return theater;
            }
        }
        return null;
    }

    /**
     * 개인 예매는 결제까지 완료된 상황으로 보고 바로 RESERVED 처리한다.
     */
    public synchronized Reservation reserve(String userId, String showtimeId, List<String> seatCodes) {
        User user = findUser(userId);
        if(user == null) {
            throw new IllegalArgumentException("Wrong User ID.");
        }
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        cleanupExpiredGroupReservations();
        seatCodes = normalizeSeatCodes(seatCodes);
        validateReservationInput(showtimeId, seatCodes);

        // 가격은 클라이언트가 보내지 않고 서버에서 다시 계산한다.
        Theater theater = findTheater(showtime.getTheaterId());
        int totalPrice = priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);

        showtime.getReservedSeats().addAll(seatCodes);
        Reservation reservation = new Reservation(
                "R" + System.currentTimeMillis(),
                user.getId(),
                showtime.getId(),
                seatCodes,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now(),
                totalPrice
        );

        read().getReservations().add(reservation);
        write(this.data);
        return reservation;
    }

    public synchronized int calculatePrice(String showtimeId, List<String> seatCodes) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        // 만료된 그룹 예매를 먼저 정리해야 이미 풀린 좌석까지 막히는 일이 없다.
        cleanupExpiredGroupReservations();
        seatCodes = normalizeSeatCodes(seatCodes);
        validateReservationInput(showtimeId, seatCodes);
        Theater theater = findTheater(showtime.getTheaterId());
        return priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);
    }

    /**
     * 좌석 하나의 시야 점수, 가격, 현재 상태를 화면에 보여주기 위해 묶어서 반환한다.
     * 좌석 버튼에는 이 정보가 표시되고, TEMP_HOLD/RESERVED 좌석은 선택하지 못하게 된다.
     */
    public synchronized Map<String, Object> calculateSeatPriceInfo(String showtimeId, String seatCode) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }

        cleanupExpiredGroupReservations();
        Theater theater = findTheater(showtime.getTheaterId());
        String normalizedSeatCode = normalizeSeatCode(seatCode);
        validateSeat(theater, normalizedSeatCode);

        int viewScore = priceCalculator.calculateViewScore(theater, normalizedSeatCode);
        int price = priceCalculator.calculateSeatPrice(theater, normalizedSeatCode, showtime.getReservedSeats().size());

        // Map으로 반환하는 이유는 가격, 시야 점수, 좌석 상태를 한 번의 서버 요청으로 전달하기 위해서이다.
        Map<String, Object> priceInfo = new LinkedHashMap<>();
        priceInfo.put("seatCode", normalizedSeatCode);
        priceInfo.put("viewScore", viewScore);
        priceInfo.put("price", price);
        priceInfo.put("seatStatus", getSeatStatus(showtimeId, normalizedSeatCode).name());
        return priceInfo;
    }

    /**
     * 대표자와 친구 목록으로 그룹을 만든다.
     * memberIds에는 대표자도 포함한다.
     */
    public synchronized GroupReservation createGroup(String leaderId, List<String> friendIds) {
        if(findUser(leaderId) == null) {
            throw new IllegalArgumentException("Wrong leader ID.");
        }
        if(friendIds == null || friendIds.isEmpty()) {
            throw new IllegalArgumentException("친구를 한 명 이상 선택해야 합니다.");
        }

        List<String> memberIds = new ArrayList<>();
        memberIds.add(leaderId);
        for(String friendId : friendIds) {
            String normalizedId = friendId.trim();
            if(normalizedId.isEmpty()) {
                continue;
            }
            // 대표자가 자기 자신을 친구 목록에 다시 넣으면 인원 수와 결제 목록이 꼬일 수 있다.
            if(normalizedId.equals(leaderId)) {
                throw new IllegalArgumentException("대표자는 친구 목록에 다시 넣을 수 없습니다.");
            }
            if(memberIds.contains(normalizedId)) {
                throw new IllegalArgumentException("중복된 그룹원 ID: " + normalizedId);
            }
            if(findUser(normalizedId) == null) {
                throw new IllegalArgumentException("존재하지 않는 사용자 ID: " + normalizedId);
            }
            memberIds.add(normalizedId);
        }
        if(memberIds.size() <= 1) {
            throw new IllegalArgumentException("친구를 한 명 이상 선택해야 합니다.");
        }

        GroupReservation group = new GroupReservation("G" + System.currentTimeMillis(), leaderId, memberIds);
        read().getGroupReservations().add(group);
        write(this.data);
        return group;
    }

    /**
     * 대표자가 직접 고른 좌석을 그룹 예매 좌석으로 임시 홀딩한다.
     */
    public synchronized GroupReservation selectSeatsForGroup(String groupId, String leaderId, String showtimeId, List<String> seatCodes) {
        cleanupExpiredGroupReservations();

        GroupReservation group = findGroupReservation(groupId);
        if(group == null) {
            throw new IllegalArgumentException("Wrong group ID.");
        }
        if(!group.getLeaderId().equals(leaderId)) {
            throw new IllegalArgumentException("대표자만 그룹 좌석을 선택할 수 있습니다.");
        }
        if(group.getReservationStatus() != GroupReservationStatus.PENDING) {
            throw new IllegalArgumentException("이미 수정할 수 없는 그룹 예매입니다.");
        }
        if(group.getSeatCodes() != null && !group.getSeatCodes().isEmpty()) {
            throw new IllegalArgumentException("이미 좌석이 선택된 그룹 예매입니다.");
        }

        Showtime showtime = findShowtime(showtimeId);
        if(showtime == null) {
            throw new IllegalArgumentException("Wrong Showtime ID.");
        }
        seatCodes = normalizeSeatCodes(seatCodes);
        if(seatCodes.size() != group.getMemberIds().size()) {
            throw new IllegalArgumentException("그룹 인원 수와 선택 좌석 수가 같아야 합니다.");
        }
        validateGroupSeatInput(showtimeId, seatCodes, groupId);

        Theater theater = findTheater(showtime.getTheaterId());
        int totalPrice = priceCalculator.calculateTotalPrice(theater, showtime, seatCodes);

        // 이 시점부터 좌석은 다른 사용자가 선택하지 못하도록 TEMP_HOLD로 취급된다.
        // 실제 reservedSeats에는 전원 결제 후 confirmGroupReservation에서만 넣는다.
        group.setShowtimeId(showtimeId);
        group.setSeatCodes(seatCodes);
        group.setTotalPrice(totalPrice);
        group.setHoldExpiresAt(LocalDateTime.now().plusMinutes(GROUP_HOLD_MINUTES));

        sendPaymentMessageToGroup(groupId);
        write(this.data);
        return group;
    }

    public synchronized GroupReservation createGroupReservation(String leaderId, List<String> friendIds,
            String showtimeId, List<String> seatCodes) {
        GroupReservation group = createGroup(leaderId, friendIds);
        try {
            return selectSeatsForGroup(group.getGroupId(), leaderId, showtimeId, seatCodes);
        } catch (IllegalArgumentException e) {
            // 좌석 선택에 실패한 그룹을 그대로 남기면 빈 그룹 예매가 목록에 보이므로 즉시 되돌린다.
            read().getGroupReservations().remove(group);
            write(this.data);
            throw e;
        }
    }

    /**
     * 실제 알림 시스템은 없지만, 그룹원에게 결제 요청을 보냈다는 흐름을 표현하는 메서드이다.
     * 과제 범위에서는 문자열 응답으로 결제 요청 단계를 대신한다.
     */
    public synchronized String sendPaymentMessageToGroup(String groupId) {
        GroupReservation group = findGroupReservation(groupId);
        if(group == null) {
            throw new IllegalArgumentException("Wrong group ID.");
        }
        return "Payment request sent to group members: " + group.getMemberIds();
    }

    /**
     * 그룹원이 결제하면 개인 결제 상태를 PAID로 바꾼다.
     * 전원이 결제했을 때만 최종 RESERVED 상태가 된다.
     */
    public synchronized GroupReservation payForGroupReservation(String groupId, String userId) {
        cleanupExpiredGroupReservations();

        GroupReservation group = findGroupReservation(groupId);
        if(group == null) {
            throw new IllegalArgumentException("Wrong group ID.");
        }
        if(group.getReservationStatus() != GroupReservationStatus.PENDING) {
            throw new IllegalArgumentException("이미 확정 또는 취소된 그룹 예매입니다.");
        }
        if(isGroupHoldExpired(group)) {
            cancelGroupReservation(groupId);
            throw new IllegalArgumentException("결제 제한 시간이 초과되었습니다.");
        }
        if(!group.isMember(userId)) {
            throw new IllegalArgumentException("그룹원이 아닌 사용자는 결제할 수 없습니다.");
        }

        GroupPayment payment = group.findPayment(userId);
        if(payment == null) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }
        if(payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("이미 결제한 사용자입니다.");
        }

        payment.pay();
        // 그룹 예매의 핵심 조건이다. 한 명이라도 미결제면 PENDING을 유지하고, 전원 결제 시 확정한다.
        if(isAllMembersPaid(groupId)) {
            confirmGroupReservation(groupId);
        } else {
            write(this.data);
        }
        return group;
    }

    /**
     * 그룹원별 결제 상태를 모두 확인한다.
     * 이 값이 true일 때만 TEMP_HOLD 좌석을 최종 RESERVED 좌석으로 바꿀 수 있다.
     */
    public synchronized boolean isAllMembersPaid(String groupId) {
        GroupReservation group = findGroupReservation(groupId);
        if(group == null) {
            return false;
        }
        for(GroupPayment payment : group.getPaymentList()) {
            if(payment.getPaymentStatus() != PaymentStatus.PAID) {
                return false;
            }
        }
        return true;
    }

    public synchronized GroupReservation confirmGroupReservation(String groupId) {
        GroupReservation group = findGroupReservation(groupId);
        if(group == null) {
            throw new IllegalArgumentException("Wrong group ID.");
        }
        if(group.getReservationStatus() != GroupReservationStatus.PENDING) {
            throw new IllegalArgumentException("이미 확정 또는 취소된 그룹 예매입니다.");
        }
        if(!isAllMembersPaid(groupId)) {
            throw new IllegalArgumentException("아직 결제하지 않은 그룹원이 있습니다.");
        }

        Showtime showtime = findShowtime(group.getShowtimeId());
        validateGroupSeatInput(group.getShowtimeId(), group.getSeatCodes(), groupId);
        // 전원 결제가 끝난 뒤에야 상영 시간의 reservedSeats에 좌석을 넣어 최종 예약 처리한다.
        showtime.getReservedSeats().addAll(group.getSeatCodes());
        group.setReservationStatus(GroupReservationStatus.CONFIRMED);

        // 최종 예매 내역은 대표자 기준으로 저장한다.
        Reservation reservation = new Reservation(
                "R" + System.currentTimeMillis(),
                group.getLeaderId(),
                group.getShowtimeId(),
                group.getSeatCodes(),
                ReservationStatus.CONFIRMED,
                LocalDateTime.now(),
                group.getTotalPrice()
        );
        read().getReservations().add(reservation);
        write(this.data);
        return group;
    }

    public synchronized GroupReservation cancelGroupReservation(String groupId) {
        GroupReservation group = findGroupReservation(groupId);
        if(group == null) {
            throw new IllegalArgumentException("Wrong group ID.");
        }
        if(group.getReservationStatus() == GroupReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("이미 확정된 그룹 예매는 여기서 취소할 수 없습니다.");
        }
        // 그룹 예매는 reservedSeats에 아직 들어가지 않았기 때문에 CANCELLED로 바꾸면 좌석 홀딩이 풀린다.
        group.setReservationStatus(GroupReservationStatus.CANCELLED);
        write(this.data);
        return group;
    }

    public synchronized List<GroupReservation> findGroupReservationsByUser(String userId) {
        cleanupExpiredGroupReservations();

        List<GroupReservation> result = new ArrayList<>();
        for(GroupReservation group : read().getGroupReservations()) {
            if(group.isMember(userId)) {
                result.add(group);
            }
        }
        return result;
    }

    public synchronized Reservation cancelReservation(String reservationId, String requesterId) {
        List<Reservation> reservations = read().getReservations();
        for (Reservation reservation : reservations) {
            if(reservationId.equals(reservation.getId()) ) {
                if(!reservation.getUserId().equals(requesterId)) {
                    throw new IllegalArgumentException("Requester ID does not match.");
                }
                Showtime showtime = findShowtime(reservation.getShowtimeId());
                for(String seatCode : reservation.getSeatCodes()) {
                    showtime.getReservedSeats().remove(seatCode);
                }
                reservation.cancel();
                write(this.data);
                return reservation;
            }
        }
        return null;
    }

    public synchronized List<Reservation> findReservationsByUser(String userId) {
        List<Reservation> targetList = new ArrayList<>();
        List<Reservation> reservations = read().getReservations();

        for(Reservation reservation : reservations) {
            if(userId.equals(reservation.getUserId())) {
                targetList.add(reservation);
            }
        }
        return targetList;
    }

    private MovieBookingData read() {
        try {
            if(data != null) return data;
            data = objectMapper.readValue(file, MovieBookingData.class);
            // 오래된 JSON 파일을 읽을 때 groupReservations가 없으면 getter에서 빈 리스트를 만들어 준다.
            data.getGroupReservations();
            return data;
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 읽기 실패: " + file.getPath(), e);
        }
    }

    private void write(MovieBookingData data) {
        try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 저장 실패: " + file.getPath(), e);
        }
    }

    private void initializeIfNeeded() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                write(seedData());
            } catch (IOException e) {
                throw new RuntimeException("JSON 파일 초기화 실패: " + file.getPath(), e);
            }
        } else if (file.length() == 0) {
            write(seedData());
        }
    }

    private MovieBookingData seedData() {
        return new MovieBookingData();
    }

    private void validateReservationInput(String showtimeId, List<String> seatCodes) {
        Set<String> selectedSeats = new LinkedHashSet<>();
        Showtime showtime = findShowtime(showtimeId);
        Theater theater = findTheater(showtime.getTheaterId());

        for(String seat : seatCodes) {
            // 같은 요청 안에서 같은 좌석이 두 번 들어오면 실제 인원 수와 좌석 수가 맞지 않게 된다.
            if(!selectedSeats.add(seat)) {
                throw new IllegalArgumentException("Duplicated seat: " + seat);
            }
            validateSeat(theater, seat);
            if(showtime.getReservedSeats().contains(seat)) {
                throw new IllegalArgumentException("이미 예약된 좌석: " + seat);
            }
            // 그룹 예매가 잡아 둔 좌석도 개인 예매가 가져갈 수 없도록 막는다.
            if(isTempHeldSeat(showtimeId, seat, null)) {
                throw new IllegalArgumentException("이미 임시 홀딩된 좌석: " + seat);
            }
        }
    }

    private void validateGroupSeatInput(String showtimeId, List<String> seatCodes, String currentGroupId) {
        Set<String> selectedSeats = new LinkedHashSet<>();
        Showtime showtime = findShowtime(showtimeId);
        Theater theater = findTheater(showtime.getTheaterId());

        for(String seat : seatCodes) {
            // 그룹 예매에서도 좌석 중복을 막아야 한 사람이 두 좌석을 점유하는 오류를 피할 수 있다.
            if(!selectedSeats.add(seat)) {
                throw new IllegalArgumentException("Duplicated seat: " + seat);
            }
            validateSeat(theater, seat);
            if(showtime.getReservedSeats().contains(seat)) {
                throw new IllegalArgumentException("이미 예약된 좌석: " + seat);
            }
            // 현재 그룹이 이미 잡아 둔 좌석은 허용하고, 다른 그룹의 TEMP_HOLD 좌석만 막는다.
            if(isTempHeldSeat(showtimeId, seat, currentGroupId)) {
                throw new IllegalArgumentException("이미 임시 홀딩된 좌석: " + seat);
            }
        }
    }

    private void validateSeat(Theater theater, String seatCode) {
        if(theater == null || !theater.isValidSeat(seatCode)) {
            throw new IllegalArgumentException("유효하지 않은 좌석 코드: " + seatCode);
        }
    }

    private List<String> normalizeSeatCodes(List<String> seatCodes) {
        if(seatCodes == null || seatCodes.isEmpty()) {
            throw new IllegalArgumentException("seatCodes must not be empty.");
        }

        List<String> normalizedSeatCodes = new ArrayList<>();
        for(String seatCode : seatCodes) {
            // 좌석 코드는 A1처럼 대문자 형식으로 통일해서 비교 오류를 줄인다.
            normalizedSeatCodes.add(normalizeSeatCode(seatCode));
        }
        return normalizedSeatCodes;
    }

    private String normalizeSeatCode(String seatCode) {
        if(seatCode == null || seatCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid seat code: " + seatCode);
        }
        return seatCode.trim().toUpperCase(Locale.ROOT);
    }

    private GroupReservation findGroupReservation(String groupId) {
        for(GroupReservation group : read().getGroupReservations()) {
            if(groupId.equals(group.getGroupId())) {
                return group;
            }
        }
        return null;
    }

    private SeatStatus getSeatStatus(String showtimeId, String seatCode) {
        Showtime showtime = findShowtime(showtimeId);
        if(showtime.getReservedSeats().contains(seatCode)) {
            return SeatStatus.RESERVED;
        }
        if(isTempHeldSeat(showtimeId, seatCode, null)) {
            return SeatStatus.TEMP_HOLD;
        }
        return SeatStatus.AVAILABLE;
    }

    private boolean isTempHeldSeat(String showtimeId, String seatCode, String currentGroupId) {
        for(GroupReservation group : read().getGroupReservations()) {
            // PENDING이면서 만료되지 않은 그룹만 실제 임시 홀딩 좌석으로 본다.
            if(group.getReservationStatus() == GroupReservationStatus.PENDING
                    && showtimeId.equals(group.getShowtimeId())
                    && !isGroupHoldExpired(group)) {
                if(currentGroupId == null || !currentGroupId.equals(group.getGroupId())) {
                    if(group.getSeatCodes().contains(seatCode)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isGroupHoldExpired(GroupReservation group) {
        return group.getHoldExpiresAt() != null && LocalDateTime.now().isAfter(group.getHoldExpiresAt());
    }

    private void cleanupExpiredGroupReservations() {
        int changed = 0;
        for(GroupReservation group : read().getGroupReservations()) {
            if(group.getReservationStatus() == GroupReservationStatus.PENDING && isGroupHoldExpired(group)) {
                // 만료된 그룹은 CANCELLED로 표시한다. 그러면 isTempHeldSeat에서 더 이상 좌석을 막지 않는다.
                group.setReservationStatus(GroupReservationStatus.CANCELLED);
                changed = 1;
            }
        }
        if(changed == 1) {
            write(this.data);
        }
    }
}
