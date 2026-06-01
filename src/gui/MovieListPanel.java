package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controller.BookingController;
import controller.NavigationController;
import domain.Movie;

public class MovieListPanel extends BasePanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 50);

    private final BookingController bookingController;
    private final JPanel movieListPanel;

    public MovieListPanel(BookingController bookingController, NavigationController navigationController) {
        super("Movies");
        this.bookingController = bookingController;
        this.movieListPanel = new JPanel();
        movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.Y_AXIS));

        contentsPanel.add(new JScrollPane(movieListPanel), BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showHome());
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    public final void refresh() {
        // TODO: 서버에서 영화 목록을 받아 영화 제목/상영시간이 보이는 버튼 또는 카드로 표시한다.
        movieListPanel.removeAll();

        bookingController.resetSelectedMovie();

        List<Movie> movies = bookingController.loadMovies();
        for (Movie movie : movies) {
            JButton movieButton = new JButton(movie.getTitle());
            movieButton.setPreferredSize(ITEM_SIZE);
            movieButton.setMaximumSize(ITEM_SIZE);
            movieButton.setMinimumSize(ITEM_SIZE);
            movieButton.setAlignmentX(CENTER_ALIGNMENT);
            movieButton.addActionListener(event -> bookingController.selectMovie(movie));
            movieListPanel.add(movieButton);
            movieListPanel.add(Box.createVerticalStrut(8));
        }
        revalidate();
        repaint();
    }
}
