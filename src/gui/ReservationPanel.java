package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import controller.NavigationController;
import controller.ReservationController;
import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;

// 예매 내역 화면
public class ReservationPanel extends BasePanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final Dimension ITEM_SIZE = new Dimension(800, 150);
    private final transient ReservationController reservationController;
    private final JPanel reservationListPanel; // 개인 예매 목록을 표시할 패널
    

    // 예매 목록 영역 구성
    @SuppressWarnings("this-escape")
    public ReservationPanel(ReservationController reservationController, NavigationController navigationController) {
        super("Reservations");
        this.reservationController = reservationController;
        this.reservationListPanel = new JPanel();
        reservationListPanel.setLayout(new BoxLayout(reservationListPanel, BoxLayout.Y_AXIS));

        contentsPanel.add(new JScrollPane(reservationListPanel), BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showHome());
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    // 예매 목록 갱신
    public final void refresh() {
        reservationListPanel.removeAll(); // 기존 예매 목록 제거

        addPersonalReservations();

        revalidate();
        repaint();
    }

    // 개인 예매 목록 추가
    private void addPersonalReservations() {
        List<Reservation> reservations;
        try {
            reservations = reservationController.loadReservations(); // 예매 목록 로드
        } catch (IllegalStateException e) { // 예매 목록 로드 실패 시 에러 메시지 표시
            addMessage(reservationListPanel, e.getMessage());
            return;
        }

        for (Reservation reservation : reservations) { // 각 예매마다 정보 패널 생성
            Showtime showtime = reservationController.getShowtime(reservation.getShowtimeId()); // 상영 일정 조회
            Movie movie = reservationController.getMovie(showtime.getMovieId()); // 영화 조회
            Theater theater = reservationController.getTheater(showtime.getTheaterId()); // 상영관 조회

            JPanel reservationPanel = new JPanel(new BorderLayout());
            reservationPanel.setPreferredSize(ITEM_SIZE);
            reservationPanel.setMaximumSize(ITEM_SIZE);
            reservationPanel.setMinimumSize(ITEM_SIZE);
            reservationPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
            reservationPanel.setBackground(new Color(220, 220, 220));
            JPanel infoPanel = new JPanel(new GridLayout(4, 2, 0, 0));
            infoPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            infoPanel.setBackground(new Color(220, 220, 220));

            JLabel idLabel = new JLabel("ID: " + reservation.getId());
            JLabel titleLabel = new JLabel("Movie: " + movie.getTitle());
            JLabel theaterLabel = new JLabel("Theater: " + theater.getName());
            JLabel showtimeLabel = new JLabel("Showtime: " + showtime.getStartsAt());
            JLabel seatLabel = new JLabel("Seats: " + reservation.getSeatCodes());
            JLabel statusLabel = new JLabel("Status: " + reservation.getStatus());
            JLabel priceLabel = new JLabel("Price: " + formatPrice(reservation.getTotalPrice()));
            JButton cancelButton = new JButton("Cancel");

            if(reservation.getStatus() == ReservationStatus.CANCELED) { // 이미 취소된 예매는 취소 버튼 비활성화
                cancelButton.setEnabled(false);
            } else { // 취소 버튼 클릭 시 예매 취소 처리 후 목록 갱신
                cancelButton.addActionListener(event -> {
                reservationController.cancelReservation(reservation.getId());
                refresh();
                });
            }
            infoPanel.add(idLabel);
            infoPanel.add(titleLabel);
            infoPanel.add(theaterLabel);
            infoPanel.add(showtimeLabel);
            infoPanel.add(seatLabel);
            infoPanel.add(statusLabel);
            infoPanel.add(priceLabel);
            reservationPanel.add(infoPanel, BorderLayout.CENTER);
            reservationPanel.add(cancelButton, BorderLayout.EAST);

            addItem(reservationListPanel, reservationPanel);
        }
    }

    // 안내 메시지 추가
    private void addMessage(JPanel listPanel, String message) {
        JLabel messageLabel = new JLabel(message);
        messageLabel.setBorder(new EmptyBorder(12, 12, 12, 12));
        messageLabel.setMaximumSize(messageLabel.getPreferredSize());
        listPanel.add(messageLabel);
    }

    // 목록 아이템 추가
    private void addItem(JPanel listPanel, JPanel itemPanel) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rowPanel.add(itemPanel);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
        listPanel.add(rowPanel);
    }

}
