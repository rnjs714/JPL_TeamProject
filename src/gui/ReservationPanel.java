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

import controller.BookingController;
import controller.NavigationController;
import domain.GroupPayment;
import domain.GroupReservation;
import domain.GroupReservationStatus;
import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;

/**
 * 사용자의 예매 내역을 보여주는 화면이다.
 *
 * 일반 예매는 바로 확정된 좌석 목록을 보여주고,
 * 그룹 예매는 결제 대기/확정/취소 상태와 그룹원별 결제 여부를 함께 보여준다.
 */
public class ReservationPanel extends BasePanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 150);
    private final BookingController bookingController;
    private final NavigationController navigationController;
    private final JPanel reservationListPanel;

    public ReservationPanel(BookingController bookingController, NavigationController navigationController) {
        super("Reservations");
        this.bookingController = bookingController;
        this.navigationController = navigationController;
        this.reservationListPanel = new JPanel();
        reservationListPanel.setLayout(new BoxLayout(reservationListPanel, BoxLayout.Y_AXIS));

        contentsPanel.add(new JScrollPane(reservationListPanel), BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showHome());
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    public final void refresh() {
        reservationListPanel.removeAll();

        // 개인 예매와 그룹 예매는 저장 구조가 달라서 각각 따로 불러와 같은 화면에 이어서 표시한다.
        addNormalReservations();
        addGroupReservations();

        revalidate();
        repaint();
    }

    private void addNormalReservations() {
        List<Reservation> reservations;
        try {
            // 현재 로그인 사용자의 확정 개인 예매 목록을 서버에서 가져온다.
            reservations = bookingController.loadReservations();
        } catch (IllegalStateException e) {
            reservationListPanel.add(new JLabel("No normal reservations found."));
            return;
        }

        if (reservations.isEmpty()) {
            reservationListPanel.add(new JLabel("No normal reservations found."));
            return;
        }

        reservationListPanel.add(new JLabel("Normal Reservations"));
        for (Reservation reservation : reservations) {
            // Reservation에는 ID 중심 정보만 있으므로 화면 표시용 제목/상영관 정보는 다시 조회한다.
            Showtime showtime = bookingController.getShowtime(reservation.getShowtimeId());
            Movie movie = bookingController.getMovie(showtime.getMovieId());
            Theater theater = bookingController.getTheater(showtime.getTheaterId());

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
            JLabel peopleLabel = new JLabel("People: " + reservation.getPeopleCount());
            JLabel statusLabel = new JLabel("Status: " + reservation.getStatus());
            JLabel priceLabel = new JLabel("Price: " + formatPrice(reservation.getTotalPrice()));
            JButton cancelButton = new JButton("Cancel");
            if(reservation.getStatus() == ReservationStatus.CANCELED) {
                cancelButton.setEnabled(false);
            } else {
                // 취소 후에는 서버 데이터가 바뀌므로 화면을 다시 새로고침한다.
                cancelButton.addActionListener(event -> {
                    bookingController.cancelReservation(reservation.getId());
                    refresh();
                });
            }
            infoPanel.add(idLabel);
            infoPanel.add(titleLabel);
            infoPanel.add(theaterLabel);
            infoPanel.add(showtimeLabel);
            infoPanel.add(seatLabel);
            infoPanel.add(peopleLabel);
            infoPanel.add(statusLabel);
            infoPanel.add(priceLabel);
            reservationPanel.add(infoPanel, BorderLayout.CENTER);
            reservationPanel.add(cancelButton, BorderLayout.EAST);

            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            rowPanel.add(reservationPanel);
            reservationListPanel.add(rowPanel);
        }
    }

    private void addGroupReservations() {
        List<GroupReservation> groups;
        try {
            // 사용자가 대표자이거나 그룹원으로 포함된 그룹 예매를 모두 가져온다.
            groups = bookingController.loadGroupReservations();
        } catch (IllegalStateException e) {
            reservationListPanel.add(new JLabel("No group reservations found."));
            return;
        }

        reservationListPanel.add(new JLabel("Group Reservations"));
        if(groups.isEmpty()) {
            reservationListPanel.add(new JLabel("No group reservations found."));
            return;
        }

        for(GroupReservation group : groups) {
            // 그룹 예매는 아직 개인 Reservation으로 확정되지 않았을 수 있어 별도 카드로 보여준다.
            JPanel groupPanel = new JPanel(new BorderLayout());
            groupPanel.setPreferredSize(ITEM_SIZE);
            groupPanel.setMaximumSize(ITEM_SIZE);
            groupPanel.setMinimumSize(ITEM_SIZE);
            groupPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
            groupPanel.setBackground(new Color(235, 235, 235));

            JPanel infoPanel = new JPanel(new GridLayout(4, 2, 0, 0));
            infoPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            infoPanel.setBackground(new Color(235, 235, 235));

            JLabel idLabel = new JLabel("Group ID: " + group.getGroupId());
            JLabel leaderLabel = new JLabel("Leader: " + group.getLeaderId());
            JLabel memberLabel = new JLabel("Members: " + group.getMemberIds());
            JLabel seatLabel = new JLabel("Seats: " + group.getSeatCodes());
            JLabel statusLabel = new JLabel("Status: " + group.getReservationStatus());
            JLabel payLabel = new JLabel("Payments: " + makePaymentText(group));
            JLabel priceLabel = new JLabel("Price: " + formatPrice(group.getTotalPrice()));
            JLabel expireLabel = new JLabel("Hold Until: " + group.getHoldExpiresAt());

            JButton payButton = new JButton("Pay");
            boolean currentUserPaid = bookingController.isCurrentUserPaidInGroup(group);
            if(group.getReservationStatus() != GroupReservationStatus.PENDING) {
                // 이미 확정 또는 취소된 그룹은 더 이상 결제 버튼을 누를 필요가 없다.
                payButton.setEnabled(false);
            } else if(currentUserPaid) {
                // 그룹 예매는 각자 한 번씩만 결제하면 되므로, 이미 결제한 사용자의 버튼은 막아 둔다.
                payButton.setText("Paid");
                payButton.setEnabled(false);
            } else {
                payButton.addActionListener(event -> {
                    try {
                        // 현재 로그인 사용자의 결제를 서버에 기록한다. 전원 결제 시 서버에서 자동 확정된다.
                        bookingController.payGroupReservation(group.getGroupId());
                        navigationController.showMessage("Group payment completed.");
                        refresh();
                    } catch (IllegalStateException e) {
                        navigationController.showMessage(e.getMessage());
                    }
                });
            }

            infoPanel.add(idLabel);
            infoPanel.add(leaderLabel);
            infoPanel.add(memberLabel);
            infoPanel.add(seatLabel);
            infoPanel.add(statusLabel);
            infoPanel.add(payLabel);
            infoPanel.add(priceLabel);
            infoPanel.add(expireLabel);

            groupPanel.add(infoPanel, BorderLayout.CENTER);
            groupPanel.add(payButton, BorderLayout.EAST);

            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            rowPanel.add(groupPanel);
            reservationListPanel.add(rowPanel);
        }
    }

    private String makePaymentText(GroupReservation group) {
        String result = "";
        for(GroupPayment payment : group.getPaymentList()) {
            if(!result.isEmpty()) {
                result += ", ";
            }
            // 발표나 테스트 때 각 그룹원이 결제했는지 한눈에 볼 수 있도록 userId:상태 형식으로 만든다.
            result += payment.getUserId() + ":" + payment.getPaymentStatus();
        }
        return result;
    }

    private String formatPrice(int totalPrice) {
        if (totalPrice <= 0) {
            return "-";
        }
        return String.format("%,d KRW", totalPrice);
    }
}
