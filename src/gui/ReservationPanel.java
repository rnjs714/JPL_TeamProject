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
import service.ApiException;

public class ReservationPanel extends BasePanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 150);
    private final ReservationController reservationController;
    private final JPanel reservationListPanel;
    

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
    public final void refresh() {
        reservationListPanel.removeAll();

        List<Reservation> reservations;
        try {
            reservations = reservationController.loadReservations();
        } catch (ApiException e) {
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
            Showtime showtime = reservationController.getShowtime(reservation.getShowtimeId());
            Movie movie = reservationController.getMovie(showtime.getMovieId());
            Theater theater = reservationController.getTheater(showtime.getTheaterId());

            JPanel reservationPanel = new JPanel(new BorderLayout());
            reservationPanel.setPreferredSize(ITEM_SIZE);
            reservationPanel.setMaximumSize(ITEM_SIZE);
            reservationPanel.setMinimumSize(ITEM_SIZE);
            reservationPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
            reservationPanel.setBackground(new Color(220, 220, 220));
            JPanel infoPanel = new JPanel(new GridLayout(3, 2, 0, 0));
            infoPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            infoPanel.setBackground(new Color(220, 220, 220));

            JLabel idLabel = new JLabel("ID: " + reservation.getId());
            JLabel titleLabel = new JLabel("Movie: " + movie.getTitle());
            JLabel theaterLabel = new JLabel("Theater: " + theater.getName());
            JLabel showtimeLabel = new JLabel("Showtime: " + showtime.getStartsAt());
            JLabel seatLabel = new JLabel("Seats: " + reservation.getSeatCodes());
            JLabel statusLabel = new JLabel("Status: " + reservation.getStatus());
            JButton cancelButton = new JButton("Cancel");
            if(reservation.getStatus() == ReservationStatus.CANCELED) {
                cancelButton.setEnabled(false);
            } else {
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
            reservationPanel.add(infoPanel, BorderLayout.CENTER);
            reservationPanel.add(cancelButton, BorderLayout.EAST);

            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            rowPanel.add(reservationPanel);
            reservationListPanel.add(rowPanel);
        }
        revalidate();
        repaint();
    }
}
