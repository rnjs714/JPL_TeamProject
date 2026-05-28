package domain;

public class Movie {
    private String id;
    private String title;
    private int runningMinutes;

    public Movie() {
    }

    public Movie(String id, String title, int runningMinutes) {
        this.id = id;
        this.title = title;
        this.runningMinutes = runningMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRunningMinutes() {
        return runningMinutes;
    }

    public void setRunningMinutes(int runningMinutes) {
        this.runningMinutes = runningMinutes;
    }
}
