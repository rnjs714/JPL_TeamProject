package gui;

import client.ApiClient;
import controller.AuthController;
import controller.BookingController;
import controller.NavigationController;
import session.BookingSession;
import session.UserSession;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    public static final String LOGIN_SCREEN = "login";
    public static final String MOVIE_SCREEN = "movie";
    public static final String SHOWTIME_SCREEN = "showtime";
    public static final String SEAT_SCREEN = "seat";
    public static final String RESERVATION_SCREEN = "reservation";

    private JPanel rootPanel;
    private CardLayout cardLayout;

    public MainFrame(String host, int port) {
        // TODO: 창 크기, 위치, 종료 동작, 공통 폰트/색상 등을 설정한다.
        setTitle("Movie Booking");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ApiClient apiClient = new ApiClient(host, port);
        UserSession userSession = new UserSession();
        BookingSession bookingSession = new BookingSession();

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        NavigationController navigationController = new NavigationController(this);
        AuthController authController = new AuthController(apiClient, userSession, navigationController);
        BookingController bookingController = new BookingController(apiClient, userSession, bookingSession,
                navigationController);

        rootPanel.add(new LoginPanel(authController), LOGIN_SCREEN);
        rootPanel.add(new MovieListPanel(bookingController, authController), MOVIE_SCREEN);
        rootPanel.add(new ShowtimePanel(bookingController, navigationController), SHOWTIME_SCREEN);
        rootPanel.add(new SeatSelectionPanel(bookingController, bookingSession, navigationController), SEAT_SCREEN);
        rootPanel.add(new ReservationPanel(bookingController, navigationController), RESERVATION_SCREEN);

        setContentPane(rootPanel);
        showScreen(LOGIN_SCREEN);
    }

    public void showScreen(String screenName) {
        // TODO: 화면 이동 전에 필요한 데이터 새로고침 hooks를 추가한다.
        cardLayout.show(rootPanel, screenName);
    }
}
