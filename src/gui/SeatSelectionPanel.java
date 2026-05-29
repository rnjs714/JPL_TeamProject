package gui;

import controller.BookingController;
import controller.NavigationController;
import session.BookingSession;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SeatSelectionPanel extends JPanel {
    private JPanel seatGridPanel;

    public SeatSelectionPanel(BookingController bookingController, BookingSession bookingSession,
            NavigationController navigationController) {
        setLayout(new BorderLayout());

        seatGridPanel = new JPanel(new GridLayout(0, 8, 6, 6));

        JButton reserveButton = new JButton("Reserve");
        reserveButton.addActionListener(event -> bookingController.reserveSelectedSeats());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(reserveButton);

        add(new JLabel("Seats"), BorderLayout.NORTH);
        add(seatGridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // TODO: 상영 시간 선택 후 화면 진입 시 refreshSeats(...)를 호출한다.
    }

    public void refreshSeats(BookingSession bookingSession) {
        // TODO: Theater rows/columns 기준으로 좌석 버튼을 생성한다.
        // TODO: 이미 예약된 좌석은 비활성화하고, 클릭한 좌석은 bookingSession.toggleSeat(...)로 선택 상태를 바꾼다.
        seatGridPanel.removeAll();
        revalidate();
        repaint();
    }
}
