package domain;

public class Theater {
    private String id;
    private String name;
    private int rows;
    private int columns;
    private TheaterType type;

    public Theater() {
    }

    public Theater(String id, String name, int rows, int columns, TheaterType type) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.type = type;
    }

    public boolean isValidSeat(String seatCode) {
        if (seatCode == null || !seatCode.matches("[A-Za-z][1-9][0-9]*")) {
            return false;
        }
        int row = Character.toUpperCase(seatCode.charAt(0)) - 'A';
        int column = Integer.parseInt(seatCode.substring(1));
        return row >= 0 && row < rows && column >= 1 && column <= columns;
    }

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
