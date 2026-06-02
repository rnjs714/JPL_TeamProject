package gui;

// GUI 실행 시작점
public class MovieBookingGui {
    // 메인 프레임 실행
    public static void main(String[] args) {
        MainFrame frame = new MainFrame("localhost", 5555);
        frame.setVisible(true);
    }
}
