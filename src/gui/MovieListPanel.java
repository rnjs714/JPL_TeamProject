package gui;

import controller.AuthController;
import controller.BookingController;
import domain.Movie;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MovieListPanel extends JPanel {
    private JPanel movieListPanel;

    public MovieListPanel(BookingController bookingController, AuthController authController) {
        setLayout(new BorderLayout());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(event -> authController.logout());

        movieListPanel = new JPanel(new GridLayout(0, 1, 8, 8));

        add(new JLabel("Movies"), BorderLayout.NORTH);
        add(new JScrollPane(movieListPanel), BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);

        // TODO: 화면이 표시될 때 refreshMovies(...)가 호출되도록 MainFrame 또는 NavigationController와 연결한다.
        refreshMovies(bookingController);
    }

    public void refreshMovies(BookingController bookingController) {
        // TODO: 서버에서 영화 목록을 받아 영화 제목/상영시간이 보이는 버튼 또는 카드로 표시한다.
        movieListPanel.removeAll();
        List<Movie> movies = bookingController.loadMovies();
        for (Movie movie : movies) {
            JButton movieButton = new JButton(movie.getTitle());
            movieButton.addActionListener(event -> bookingController.selectMovie(movie));
            movieListPanel.add(movieButton);
        }
        revalidate();
        repaint();
    }
}
