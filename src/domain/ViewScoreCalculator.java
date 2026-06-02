package domain;

/**
 * 좌석 위치를 기준으로 시야 점수를 계산하는 클래스이다.
 * 중앙에 가까운 좌석일수록 화면을 보기 좋다고 판단해 높은 점수를 준다.
 */
public class ViewScoreCalculator {
    // 시야 점수는 1점부터 10점까지로 제한한다. 화면 중앙에 가까울수록 높은 점수를 받는다.
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 10;

    public int calculateScore(Theater theater, String seatCode) {
        validateSeatPosition(theater, seatCode);

        double maxDistance = calculateMaxDistanceFromCenter(theater);
        if (maxDistance == 0.0) {
            return MAX_SCORE;
        }

        double distance = calculateDistanceFromCenter(theater, seatCode);

        // 중앙과의 거리가 가까우면 normalizedDistance가 작아지고, 그만큼 높은 점수를 받는다.
        double normalizedDistance = distance / maxDistance;
        int score = (int) Math.round(MAX_SCORE - normalizedDistance * (MAX_SCORE - MIN_SCORE));
        return clamp(score, MIN_SCORE, MAX_SCORE);
    }

    public double calculateDistanceFromCenter(Theater theater, String seatCode) {
        validateSeatPosition(theater, seatCode);

        // A1, C5 같은 좌석 코드를 행 번호와 열 번호로 바꿔 중앙 좌표와 비교한다.
        int row = parseRow(seatCode);
        int column = parseColumn(seatCode);
        double centerRow = (theater.getRows() + 1) / 2.0;
        double centerColumn = (theater.getColumns() + 1) / 2.0;
        double rowDistance = row - centerRow;
        double columnDistance = column - centerColumn;
        return Math.sqrt(rowDistance * rowDistance + columnDistance * columnDistance);
    }

    private double calculateMaxDistanceFromCenter(Theater theater) {
        // 상영관에서 중앙으로부터 가장 멀리 떨어질 수 있는 거리를 구해 점수 계산의 기준으로 삼는다.
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
        // 존재하지 않는 좌석은 시야 점수와 가격을 계산할 수 없으므로 즉시 오류로 처리한다.
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
