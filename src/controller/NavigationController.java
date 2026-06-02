package controller;

import javax.swing.JOptionPane;

import gui.MainFrame;

// 화면 이동 및 메시지 제어
public class NavigationController {
    private final MainFrame mainFrame;

    // 메인 프레임 연결
    public NavigationController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    // 로그인 화면 이동
    public void showLogin() {
        mainFrame.showScreen(MainFrame.LOGIN_SCREEN);
    }

    // 홈 화면 이동
    public void showHome() {
        mainFrame.showScreen(MainFrame.HOME_SCREEN);
    }

    // 영화 목록 화면 이동
    public void showMovies() {
        mainFrame.showScreen(MainFrame.MOVIE_SCREEN);
    }

    // 상영 일정 화면 이동
    public void showShowtimes() {
        mainFrame.showScreen(MainFrame.SHOWTIME_SCREEN);
    }

    // 좌석 선택 화면 이동
    public void showSeats() {
        mainFrame.showScreen(MainFrame.SEAT_SCREEN);
    }

    // 예매 내역 화면 이동
    public void showReservations() {
        mainFrame.showScreen(MainFrame.RESERVATION_SCREEN);
    }

    // 알림 메시지 표시
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(mainFrame, message);
    }

    // 확인 메시지 표시
    public boolean showConfirmation(String message) {
        int result = JOptionPane.showConfirmDialog(mainFrame, message, "Confirmation", JOptionPane.YES_NO_OPTION);
        return (result == JOptionPane.YES_OPTION);
    }
}
