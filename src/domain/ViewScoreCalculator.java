package domain;

/**
 * Calculates a seat view score from its position in a theater layout.
 * Seats closer to the center receive a higher score.
 */
public class ViewScoreCalculator {
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 10;

    public int calculateScore(Theater theater, String seatCode) {
        validateSeatPosition(theater, seatCode);

        double maxDistance = calculateMaxDistanceFromCenter(theater);
        if (maxDistance == 0.0) {
            return MAX_SCORE;
        }

        double distance = calculateDistanceFromCenter(theater, seatCode);
        double normalizedDistance = distance / maxDistance;
        int score = (int) Math.round(MAX_SCORE - normalizedDistance * (MAX_SCORE - MIN_SCORE));
        return clamp(score, MIN_SCORE, MAX_SCORE);
    }

    public double calculateDistanceFromCenter(Theater theater, String seatCode) {
        validateSeatPosition(theater, seatCode);

        int row = parseRow(seatCode);
        int column = parseColumn(seatCode);
        double centerRow = (theater.getRows() + 1) / 2.0;
        double centerColumn = (theater.getColumns() + 1) / 2.0;
        double rowDistance = row - centerRow;
        double columnDistance = column - centerColumn;
        return Math.sqrt(rowDistance * rowDistance + columnDistance * columnDistance);
    }

    private double calculateMaxDistanceFromCenter(Theater theater) {
        double centerRow = (theater.getRows() + 1) / 2.0;
        double centerColumn = (theater.getColumns() + 1) / 2.0;
        double rowDistance = Math.max(centerRow - 1, theater.getRows() - centerRow);
        double columnDistance = Math.max(centerColumn - 1, theater.getColumns() - centerColumn);
        return Math.sqrt(rowDistance * rowDistance + columnDistance * columnDistance);
    }

    private void validateSeatPosition(Theater theater, String seatCode) {
        if (theater == null) {
            throw new IllegalArgumentException("theater must not be null.");
        }
        if (!theater.isValidSeat(seatCode)) {
            throw new IllegalArgumentException("seat position is outside of the theater layout.");
        }
    }

    private int parseRow(String seatCode) {
        return Character.toUpperCase(seatCode.charAt(0)) - 'A' + 1;
    }

    private int parseColumn(String seatCode) {
        return Integer.parseInt(seatCode.substring(1));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
