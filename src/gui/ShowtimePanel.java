package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.BookingController;
import controller.NavigationController;
import domain.Showtime;
import service.ApiException;

public class ShowtimePanel extends BasePanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 50);
    private final BookingController bookingController;
    private final JPanel showtimeListPanel;
    private final JLabel movieTitleLabel;

    public ShowtimePanel(BookingController bookingController, NavigationController navigationController) {
        super("Showtimes");
        this.bookingController = bookingController;
        this.showtimeListPanel = new JPanel();
        showtimeListPanel.setLayout(new BoxLayout(showtimeListPanel, BoxLayout.Y_AXIS));
        this.movieTitleLabel = new JLabel("", SwingConstants.CENTER);
        movieTitleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        movieTitleLabel.setOpaque(true);
        movieTitleLabel.setBackground(new Color(200, 200, 200));

        titlePanel.add(movieTitleLabel, BorderLayout.SOUTH);
        contentsPanel.add(new JScrollPane(showtimeListPanel), BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showMovies());
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    public final void refresh() {
        // TODO: 선택된 영화의 상영 시간을 조회해 시간/상영관 정보가 보이는 버튼으로 표시한다.
        movieTitleLabel.setText(bookingController.getSelectedMovie().getTitle());

        showtimeListPanel.removeAll();

        bookingController.resetSelectedShowtime();
        
        List<Showtime> showtimes;

        try {
            showtimes = bookingController.loadShowtimes();
        } catch (ApiException e) {
            showtimeListPanel.add(new JLabel("No showtimes found."));
            revalidate();
            repaint();
            return;
        }

        if (showtimes.isEmpty()) {
            showtimeListPanel.add(new JLabel("No showtimes found."));
            revalidate();
            repaint();
            return;
        }

        for (Showtime showtime : showtimes) {
            String theaterName = bookingController.getTheater(showtime.getTheaterId()).getName();
            JButton showtimeButton = new JButton(theaterName + " | " + showtime.getStartsAt().toString());
            showtimeButton.setPreferredSize(ITEM_SIZE);
            showtimeButton.setMinimumSize(ITEM_SIZE);
            showtimeButton.setMaximumSize(ITEM_SIZE);
            showtimeButton.setAlignmentX(CENTER_ALIGNMENT);
            showtimeButton.addActionListener(event -> bookingController.selectShowtime(showtime));
            showtimeListPanel.add(showtimeButton);
            showtimeListPanel.add(Box.createVerticalStrut(8));
        }
        revalidate();
        repaint();
    }
}
