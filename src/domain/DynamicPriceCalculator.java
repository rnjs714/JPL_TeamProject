package domain;

import java.util.List;

/**
 * Calculates ticket prices from theater type, seat view score, and current demand.
 *
 * Price formula:
 * base price by theater type + view score premium + demand surcharge
 */
public class DynamicPriceCalculator {
    private static final int STANDARD_BASE_PRICE = 12000;
    private static final int IMAX_BASE_PRICE = 18000;
    private static final int FOUR_DX_BASE_PRICE = 20000;
    private static final int VIEW_SCORE_UNIT_PRICE = 500;
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
            // In group booking, each selected seat is priced and added to one total price.
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
        // Minimum score does not add extra money. Higher scores add 500 KRW per point.
        int viewPremium = (viewScore - ViewScoreCalculator.MIN_SCORE) * VIEW_SCORE_UNIT_PRICE;
        return basePrice + viewPremium + getDemandSurcharge(theater, reservedSeatCount);
    }

    public int calculateViewScore(Theater theater, String seatCode) {
        try {
            return viewScoreCalculator.calculateScore(theater, seatCode);
        } catch (IllegalArgumentException e) {
            // 잘못된 좌석값이 들어오면 가격 계산이 멈추도록 명확한 오류를 던진다.
            // Invalid seat data should stop price calculation clearly.
            throw new IllegalArgumentException("Invalid view score seat: " + seatCode);
        }
    }

    private int getBasePrice(TheaterType theaterType) {
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
        // More reserved seats means higher demand, so a small surcharge is added.
        if (occupancyRate >= HIGH_DEMAND_THRESHOLD) {
            return HIGH_DEMAND_SURCHARGE;
        }
        if (occupancyRate >= MEDIUM_DEMAND_THRESHOLD) {
            return MEDIUM_DEMAND_SURCHARGE;
        }
        return 0;
    }
}
