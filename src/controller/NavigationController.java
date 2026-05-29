package controller;

import gui.MainFrame;

public class NavigationController {
    private MainFrame mainFrame;

    public NavigationController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void showLogin() {
        mainFrame.showScreen(MainFrame.LOGIN_SCREEN);
    }

    public void showMovies() {
        mainFrame.showScreen(MainFrame.MOVIE_SCREEN);
    }

    public void showShowtimes() {
        mainFrame.showScreen(MainFrame.SHOWTIME_SCREEN);
    }

    public void showSeats() {
        mainFrame.showScreen(MainFrame.SEAT_SCREEN);
    }

    public void showReservations() {
        mainFrame.showScreen(MainFrame.RESERVATION_SCREEN);
    }
}
