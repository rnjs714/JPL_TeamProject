package gui;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.ApiClient;
import controller.AuthController;
import controller.BookingController;
import controller.NavigationController;
import session.BookingSession;
import session.UserSession;

public class MainFrame extends JFrame {
    public static final String LOGIN_SCREEN = "login";
    public static final String HOME_SCREEN = "home";
    public static final String MOVIE_SCREEN = "movie";
    public static final String SHOWTIME_SCREEN = "showtime";
    public static final String SEAT_SCREEN = "seat";
    public static final String RESERVATION_SCREEN = "reservation";

    private final JPanel rootPanel;
    private final CardLayout cardLayout;

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
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        NavigationController navigationController = new NavigationController(this);
        AuthController authController = new AuthController(apiClient, userSession, navigationController);
        BookingController bookingController = new BookingController(apiClient, userSession, bookingSession,
                navigationController);

        rootPanel.add(new LoginPanel(authController), LOGIN_SCREEN);
        rootPanel.add(new HomePanel(authController, navigationController), HOME_SCREEN);
        rootPanel.add(new MovieListPanel(bookingController, navigationController), MOVIE_SCREEN);
        rootPanel.add(new ShowtimePanel(bookingController, navigationController), SHOWTIME_SCREEN);
        rootPanel.add(new SeatSelectionPanel(bookingController, navigationController), SEAT_SCREEN);
        rootPanel.add(new ReservationPanel(bookingController, navigationController), RESERVATION_SCREEN);

        setContentPane(rootPanel);
        showScreen(LOGIN_SCREEN);
    }

    public final void showScreen(String screenName) {
        JPanel targetPanel = switch (screenName) {
            case HOME_SCREEN -> (JPanel) rootPanel.getComponent(1);
            case MOVIE_SCREEN -> (JPanel) rootPanel.getComponent(2);
            case SHOWTIME_SCREEN -> (JPanel) rootPanel.getComponent(3);
            case SEAT_SCREEN -> (JPanel) rootPanel.getComponent(4);
            case RESERVATION_SCREEN -> (JPanel) rootPanel.getComponent(5);
            default -> null;
        };
        if (targetPanel instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
        cardLayout.show(rootPanel, screenName);
    }
}
