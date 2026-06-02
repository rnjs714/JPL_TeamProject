package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controller.BookingController;
import controller.NavigationController;
import domain.Movie;

// 영화 목록 화면
public class MovieListPanel extends BasePanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 50);

    private final BookingController bookingController;
    private final JPanel movieListPanel; // 영화 목록을 표시할 패널

    // 영화 목록 영역 구성
    public MovieListPanel(BookingController bookingController, NavigationController navigationController) {
        super("Movies");
        this.bookingController = bookingController;
        this.movieListPanel = new JPanel();
        movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.Y_AXIS));

        contentsPanel.add(new JScrollPane(movieListPanel), BorderLayout.CENTER);

        JButton backButton = new JButton("Back"); // 뒤로가기 버튼
        backButton.addActionListener(event -> navigationController.showHome());
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    // 영화 목록 갱신
    public final void refresh() {
        movieListPanel.removeAll(); // 기존 영화 목록 제거
        bookingController.resetSelectedMovie(); // 선택된 영화 초기화

        List<Movie> movies;
        try {
            movies = bookingController.loadMovies(); // 영화 목록 로드
        } catch (IllegalStateException e) { // 영화 목록 로드 실패 시 에러 메시지 표시
            movieListPanel.add(new JLabel(e.getMessage()));
            revalidate();
            repaint();
            return;
        }
        
        for (Movie movie : movies) { // 각 영화마다 버튼 생성
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
