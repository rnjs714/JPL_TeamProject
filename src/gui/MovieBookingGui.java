package gui;

public class MovieBookingGui {
    public static void main(String[] args) {
        MainFrame frame = new MainFrame("localhost", 5555);
        frame.setVisible(true);
        // SwingUtilities.invokeLater(() -> {
        //     // TODO: 서버가 켜져 있지 않을 때 사용자에게 연결 실패 메시지를 보여준다.
            
        // });
    }
}
