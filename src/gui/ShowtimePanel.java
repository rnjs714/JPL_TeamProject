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

// 상영 일정 화면
public class ShowtimePanel extends BasePanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final Dimension ITEM_SIZE = new Dimension(800, 50);
    private final transient BookingController bookingController;
    private final JPanel showtimeListPanel; // 상영 일정 목록을 표시할 패널
    private final JLabel movieTitleLabel; // 선택된 영화 제목을 표시할 레이블

    // 상영 일정 목록 영역 구성
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

        JButton backButton = new JButton("Back"); // 뒤로가기 버튼
        backButton.addActionListener(event -> navigationController.showMovies());
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    // 상영 일정 목록 갱신
    public final void refresh() {
        movieTitleLabel.setText(bookingController.getSelectedMovie().getTitle()); // 선택된 영화 제목 표시
        showtimeListPanel.removeAll(); // 기존 상영 일정 목록 제거
        bookingController.resetSelectedShowtime(); // 선택된 상영 일정 초기화
        
        List<Showtime> showtimes;
        try {
            showtimes = bookingController.loadShowtimes(); // 상영 일정 로드
        } catch (IllegalStateException e) { // 상영 일정 로드 실패 시 에러 메시지 표시
            showtimeListPanel.add(new JLabel(e.getMessage()));
            revalidate();
            repaint();
            return;
        }

        for (Showtime showtime : showtimes) { // 각 상영 일정마다 버튼 생성
            String theaterName = bookingController.getTheater(showtime.getTheaterId()).getName(); // 상영관 이름 조회
            JButton showtimeButton = new JButton(theaterName + " | " + showtime.getStartsAt().toString()); // 버튼 텍스트에 상영관 이름과 시작 시간 표시
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
