package domain;

import java.util.ArrayList;
import java.util.List;

// 상영관 정보
public class Theater {
    private String id;
    private String name;
    private int rows;
    private int columns;
    private TheaterType type;

    // JSON 역직렬화
    public Theater() {
    }

    // 전체 값 생성
    public Theater(String id, String name, int rows, int columns, TheaterType type) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.type = type;
    }

    // 좌석 범위 검증
    public boolean isValidSeat(String seatCode) {
        if (seatCode == null) {
            return false;
        }
        int row = Character.toUpperCase(seatCode.charAt(0)) - 'A';
        int column = Integer.parseInt(seatCode.substring(1));
        return 0 <= row && row < rows && 1 <= column && column <= columns;
    }

    public List<String> getAllSeatCodes() {
        List<String> seatCodes = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 1; column <= columns; column++) {
                seatCodes.add("" + (char) ('A' + row) + column);
            }
        }
        return seatCodes;
    }

    // 필드 접근자
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public TheaterType getType() {
        return type;
    }

    public void setType(TheaterType type) {
        this.type = type;
    }
}
