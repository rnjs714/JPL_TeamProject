package gui;

import controller.BookingController;
import controller.NavigationController;
import domain.Reservation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ReservationPanel extends JPanel {
    private JPanel reservationListPanel;

    public ReservationPanel(BookingController bookingController, NavigationController navigationController) {
        setLayout(new BorderLayout());

        reservationListPanel = new JPanel(new GridLayout(0, 1, 8, 8));

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showMovies());

        add(new JLabel("Reservations"), BorderLayout.NORTH);
        add(new JScrollPane(reservationListPanel), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);

        // TODO: 화면 진입 시 refreshReservations(...)를 호출한다.
    }

    public void refreshReservations(BookingController bookingController) {
        // TODO: 로그인 사용자의 예매 내역을 조회해 예약 번호, 영화, 시간, 좌석, 상태를 표시한다.
        reservationListPanel.removeAll();
        List<Reservation> reservations = bookingController.loadReservations();
        for (Reservation reservation : reservations) {
            JButton reservationButton = new JButton(reservation.getId());
            reservationButton.addActionListener(event -> bookingController.cancelReservation(reservation.getId()));
            reservationListPanel.add(reservationButton);
        }
        revalidate();
        repaint();
    }
}
