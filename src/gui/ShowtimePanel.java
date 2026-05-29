package gui;

import controller.BookingController;
import controller.NavigationController;
import domain.Showtime;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ShowtimePanel extends JPanel {
    private JPanel showtimeListPanel;

    public ShowtimePanel(BookingController bookingController, NavigationController navigationController) {
        setLayout(new BorderLayout());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showMovies());

        showtimeListPanel = new JPanel(new GridLayout(0, 1, 8, 8));

        add(new JLabel("Showtimes"), BorderLayout.NORTH);
        add(new JScrollPane(showtimeListPanel), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);

        // TODO: 영화 선택 후 화면 진입 시 refreshShowtimes(...)를 호출한다.
    }

    public void refreshShowtimes(BookingController bookingController) {
        // TODO: 선택된 영화의 상영 시간을 조회해 시간/상영관 정보가 보이는 버튼으로 표시한다.
        showtimeListPanel.removeAll();
        List<Showtime> showtimes = bookingController.loadShowtimes();
        for (Showtime showtime : showtimes) {
            JButton showtimeButton = new JButton(showtime.getStartsAt().toString());
            showtimeButton.addActionListener(event -> bookingController.selectShowtime(showtime));
            showtimeListPanel.add(showtimeButton);
        }
        revalidate();
        repaint();
    }
}
