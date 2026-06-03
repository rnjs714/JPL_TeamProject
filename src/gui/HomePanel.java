package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import controller.AuthController;
import controller.NavigationController;

// 홈 화면
public class HomePanel extends BasePanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final Dimension BANNER_SIZE = new Dimension(800,200);
    private static final Dimension ITEM_SIZE = new Dimension(800, 50);

    private final transient AuthController authController;
    private final JLabel welcomeLabel;

    // 홈 메뉴 구성
    @SuppressWarnings("this-escape")
    public HomePanel (AuthController authController, NavigationController navigationController) {
        super("Home");
        this.authController = authController;
        this.welcomeLabel = new JLabel("Welcome", SwingConstants.CENTER); // 환영 문구 초기화
        welcomeLabel.setPreferredSize(BANNER_SIZE);
        welcomeLabel.setMaximumSize(BANNER_SIZE);
        welcomeLabel.setMinimumSize(BANNER_SIZE);
        welcomeLabel.setAlignmentX(CENTER_ALIGNMENT);
        welcomeLabel.setFont(this.getFont().deriveFont(24.0f));

        JButton movieListButton = new JButton("Book Now"); // 영화 목록 버튼
        movieListButton.setPreferredSize(ITEM_SIZE);
        movieListButton.setMaximumSize(ITEM_SIZE);
        movieListButton.setMinimumSize(ITEM_SIZE);
        movieListButton.setAlignmentX(CENTER_ALIGNMENT);
        movieListButton.addActionListener(event -> navigationController.showMovies());
        JButton reservationButton = new JButton("My Reservations"); // 예매 내역 버튼
        reservationButton.setPreferredSize(ITEM_SIZE);
        reservationButton.setMaximumSize(ITEM_SIZE);
        reservationButton.setMinimumSize(ITEM_SIZE);
        reservationButton.setAlignmentX(CENTER_ALIGNMENT);
        reservationButton.addActionListener(event -> navigationController.showReservations());

        contentsPanel.add(welcomeLabel);
        contentsPanel.add(movieListButton);
        contentsPanel.add(reservationButton);
        JButton logoutButton = new JButton("Logout"); // 로그아웃 버튼
        logoutButton.addActionListener(event -> authController.logout());
        add(logoutButton, BorderLayout.SOUTH);
    }
    
    @Override
    // 환영 문구 갱신
    public void refresh() {
        String userId = authController.getCurrentUser().getId();
        if(userId != null) {
            welcomeLabel.setText("Welcome, " + userId + ".");   // 로그인한 사용자 ID를 포함한 환영 문구로 업데이트
        } else {
            welcomeLabel.setText("Welcome.");
        }
    }
}
