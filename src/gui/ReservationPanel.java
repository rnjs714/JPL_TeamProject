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
import domain.Movie;
import domain.Reservation;
import domain.ReservationStatus;
import domain.Showtime;
import domain.Theater;

public class ReservationPanel extends BasePanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 150);
    private final BookingController bookingController;
    private final JPanel reservationListPanel;
    

    public ReservationPanel(BookingController bookingController, NavigationController navigationController) {
        super("Reservations");
        this.bookingController = bookingController;
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

        List<Reservation> reservations;
        try {
            reservations = bookingController.loadReservations();
        } catch (IllegalStateException e) {
            reservationListPanel.add(new JLabel("No Reservations found."));
            revalidate();
            repaint();
            return;
        }

        if (reservations.isEmpty()) {
            reservationListPanel.add(new JLabel("No reservations found."));
            revalidate();
            repaint();
            return;
        }

        for (Reservation reservation : reservations) {
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
            // 좌석 수를 이용해 개인 예매인지 그룹 예매인지 쉽게 확인할 수 있다.
            JLabel peopleLabel = new JLabel("People: " + reservation.getPeopleCount());
            JLabel statusLabel = new JLabel("Status: " + reservation.getStatus());
            JLabel priceLabel = new JLabel("Price: " + formatPrice(reservation.getTotalPrice()));
            JButton cancelButton = new JButton("Cancel");
            if(reservation.getStatus() == ReservationStatus.CANCELED) {
                cancelButton.setEnabled(false);
            } else {
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
        revalidate();
        repaint();
    }

    private String formatPrice(int totalPrice) {
        if (totalPrice <= 0) {
            return "-";
        }
        return String.format("%,d KRW", totalPrice);
    }
}
