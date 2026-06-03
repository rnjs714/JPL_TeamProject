package domain;

import java.util.List;

/**
 * 상영관 종류, 시야 점수, 현재 예약률을 이용해 좌석 가격을 계산한다.
 *
 * 기본 공식은 다음과 같다.
 * 상영관 기본 가격 + 시야 점수 추가 금액 + 예약률 추가 금액
 */
public class DynamicPriceCalculator {
    // 상영관 종류별 기본 가격이다. 가격 정책을 바꾸고 싶으면 이 상수부터 확인하면 된다.
    private static final int STANDARD_BASE_PRICE = 12000;
    private static final int IMAX_BASE_PRICE = 18000;
    private static final int FOUR_DX_BASE_PRICE = 20000;
    // 시야 점수가 1점 올라갈 때마다 붙는 추가 금액이다.
    private static final int VIEW_SCORE_UNIT_PRICE = 500;
    // 예약률이 높을 때 붙는 수요 기반 추가 금액이다.
    private static final int MEDIUM_DEMAND_SURCHARGE = 1000;
    private static final int HIGH_DEMAND_SURCHARGE = 2000;
    private static final double MEDIUM_DEMAND_THRESHOLD = 0.50;
    private static final double HIGH_DEMAND_THRESHOLD = 0.80;

    private final ViewScoreCalculator viewScoreCalculator;

    public DynamicPriceCalculator() {
        this.viewScoreCalculator = new ViewScoreCalculator();
    }

    public int calculateTotalPrice(Theater theater, Showtime showtime, List<String> seatCodes) {
        if (theater == null) {
            throw new IllegalArgumentException("Theater information is missing.");
        }
        if (showtime == null) {
            throw new IllegalArgumentException("Showtime information is missing.");
        }
        if (seatCodes == null || seatCodes.isEmpty()) {
            throw new IllegalArgumentException("seatCodes must not be empty.");
        }

        int totalPrice = 0;
        int reservedSeatCount = showtime.getReservedSeats().size();
        for (int i = 0; i < seatCodes.size(); i++) {
            // 여러 좌석을 선택한 경우 좌석별 가격을 각각 계산해서 총합을 만든다.
            totalPrice += calculateSeatPrice(theater, seatCodes.get(i), reservedSeatCount + i);
        }
        return totalPrice;
    }

    public int calculateSeatPrice(Theater theater, String seatCode, int reservedSeatCount) {
        if (theater == null) {
            throw new IllegalArgumentException("Theater information is missing.");
        }
        if (reservedSeatCount < 0) {
            reservedSeatCount = 0;
        }

        int basePrice = getBasePrice(theater.getType());
        int viewScore = calculateViewScore(theater, seatCode);

        // 최저 점수인 1점은 추가 요금이 없고, 그보다 좋은 좌석일수록 단계적으로 가격이 올라간다.
        // 시야 점수가 높을수록 관람하기 좋은 좌석으로 보고 추가 금액을 붙인다.
        int viewPremium = (viewScore - ViewScoreCalculator.MIN_SCORE) * VIEW_SCORE_UNIT_PRICE;
        return basePrice + viewPremium + getDemandSurcharge(theater, reservedSeatCount);
    }

    public int calculateViewScore(Theater theater, String seatCode) {
        try {
            return viewScoreCalculator.calculateScore(theater, seatCode);
        } catch (IllegalArgumentException e) {
            // 잘못된 좌석값이 들어오면 가격 계산을 계속하지 않고 명확한 오류를 던진다.
            throw new IllegalArgumentException("Invalid view score seat: " + seatCode);
        }
    }

    private int getBasePrice(TheaterType theaterType) {
        // 상영관 타입별로 출발 가격이 다르다. IMAX/4DX는 기본 관람료가 더 높게 설정되어 있다.
        switch (theaterType) {
            case IMAX:
                return IMAX_BASE_PRICE;
            case FOUR_DX:
                return FOUR_DX_BASE_PRICE;
            case STANDARD:
            default:
                return STANDARD_BASE_PRICE;
        }
    }

    private int getDemandSurcharge(Theater theater, int reservedSeatCount) {
        int seatCount = theater.getRows() * theater.getColumns();
        if (seatCount == 0) {
            return 0;
        }

        double occupancyRate = (double) reservedSeatCount / seatCount;

        // 이미 예약된 좌석이 많을수록 수요가 높다고 보고 추가 요금을 붙인다.
        if (occupancyRate >= HIGH_DEMAND_THRESHOLD) {
            return HIGH_DEMAND_SURCHARGE;
        }
        if (occupancyRate >= MEDIUM_DEMAND_THRESHOLD) {
            return MEDIUM_DEMAND_SURCHARGE;
        }
        return 0;
    }
}
