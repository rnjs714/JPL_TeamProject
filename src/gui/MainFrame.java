package gui;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.SocketClient;
import controller.AuthController;
import controller.BookingController;
import controller.NavigationController;
import controller.ReservationController;
import service.ApiService;
import session.BookingSession;
import session.UserSession;

// 전체 화면 프레임
public class MainFrame extends JFrame {
    public static final String LOGIN_SCREEN = "login";
    public static final String HOME_SCREEN = "home";
    public static final String MOVIE_SCREEN = "movie";
    public static final String SHOWTIME_SCREEN = "showtime";
    public static final String SEAT_SCREEN = "seat";
    public static final String RESERVATION_SCREEN = "reservation";

    private final Map<String, JPanel> screenMap = new HashMap<>();
    private final JPanel rootPanel;
    private final CardLayout cardLayout;

    // 프레임/컨트롤러/화면 초기화
    public MainFrame(String host, int port) {
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 프레임 기본 설정
        setTitle("Movie Booking");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // API 클라이언트/서비스, 세션, 컨트롤러 초기화
        ApiService apiService = new ApiService(new SocketClient(host, port));
        UserSession userSession = new UserSession();
        BookingSession bookingSession = new BookingSession();

        NavigationController navigationController = new NavigationController(this);
        AuthController authController = new AuthController(apiService, userSession, navigationController);
        BookingController bookingController = new BookingController(apiService, userSession, bookingSession, navigationController);
        ReservationController reservationController = new ReservationController(apiService, userSession, navigationController);
        
        // 화면 등록
        addScreen(LOGIN_SCREEN, new LoginPanel(authController));
        addScreen(HOME_SCREEN, new HomePanel(authController, navigationController));
        addScreen(MOVIE_SCREEN, new MovieListPanel(bookingController, navigationController));
        addScreen(SHOWTIME_SCREEN, new ShowtimePanel(bookingController, navigationController));
        addScreen(SEAT_SCREEN, new SeatSelectionPanel(bookingController, navigationController));
        addScreen(RESERVATION_SCREEN, new ReservationPanel(reservationController, navigationController));
        
        setContentPane(rootPanel);
        showScreen(LOGIN_SCREEN);
    }

    // 화면 등록
    public final void addScreen(String screenName, JPanel panel) {
        screenMap.put(screenName, panel);
        rootPanel.add(panel, screenName);
    }

    // 화면 표시
    public final void showScreen(String screenName) {
        JPanel targetPanel = screenMap.get(screenName);
        if (targetPanel instanceof Refreshable) { // 화면이 Refreshable 인터페이스를 구현한 경우 새로고침
            Refreshable refreshable = (Refreshable) targetPanel;
            refreshable.refresh();
        }
        cardLayout.show(rootPanel, screenName);
    }
}
