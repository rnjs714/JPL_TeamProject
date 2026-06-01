package controller;

import javax.swing.JOptionPane;

import gui.MainFrame;

public class NavigationController {
    private final MainFrame mainFrame;

    public NavigationController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void showLogin() {
        mainFrame.showScreen(MainFrame.LOGIN_SCREEN);
    }

    public void showHome() {
        mainFrame.showScreen(MainFrame.HOME_SCREEN);
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

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(mainFrame, message);
    }

    public boolean showConfirmation(String message) {
        int result = JOptionPane.showConfirmDialog(mainFrame, message, "Confirmation", JOptionPane.YES_NO_OPTION);
        return (result == JOptionPane.YES_OPTION);
    }
}
