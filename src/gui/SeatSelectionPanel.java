package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.BookingController;
import controller.NavigationController;
import session.BookingSession; 

public class SeatSelectionPanel extends JPanel implements Refreshable {
    private final BookingSession bookingSession;
    private final JPanel seatGridPanel;
    private final JLabel theaterNameLabel;

    public SeatSelectionPanel(BookingController bookingController, 
                                BookingSession bookingSession, 
                                NavigationController navigationController) {
        this.bookingSession = bookingSession;
        this.seatGridPanel = new JPanel();
        this.theaterNameLabel = new JLabel("", SwingConstants.CENTER);
        theaterNameLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        theaterNameLabel.setOpaque(true);
        theaterNameLabel.setBackground(new Color(200, 200, 200));


        setLayout(new BorderLayout());


        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Seats", SwingConstants.CENTER);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(50, 130, 50));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(theaterNameLabel, BorderLayout.SOUTH);


        JPanel contentsPanel = new JPanel(new BorderLayout());
        contentsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentsPanel.add(seatGridPanel, BorderLayout.CENTER);


        JButton reserveButton = new JButton("Reserve");
        reserveButton.addActionListener(event -> bookingController.reserveSelectedSeats());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(reserveButton);


        add(titlePanel, BorderLayout.NORTH);
        add(contentsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

    }

    @Override
    public final void refresh() {
        // TODO: Theater rows/columns 기준으로 좌석 버튼을 생성한다.
        // TODO: 이미 예약된 좌석은 비활성화하고, 클릭한 좌석은 bookingSession.toggleSeat(...)로 선택 상태를 바꾼다.
        theaterNameLabel.setText(bookingSession.getSelectedTheater().getName());
        
        seatGridPanel.removeAll();

        bookingSession.getSelectedSeats().clear(); // 좌석 선택 초기화

        int max_rows = bookingSession.getSelectedTheater().getRows();
        int max_cols = bookingSession.getSelectedTheater().getColumns();

        seatGridPanel.setLayout(new GridLayout(max_rows, max_cols, 6, 6));

        for(int row=0 ; row<max_rows ; row++) {
            for(int col=1 ; col<=max_cols ; col++) {
                String seatId = "" + (char)(row + 'A') + col;
                
                JToggleButton seatButton = new JToggleButton(seatId);
                seatButton.addActionListener(event -> bookingSession.toggleSeat(seatId));

                Set<String> reservedSeats = bookingSession.getSelectedShowtime().getReservedSeats();
                if(reservedSeats.contains(seatId)) {
                    seatButton.setEnabled(false);
                }
                seatGridPanel.add(seatButton);
            }
        }

        revalidate();
        repaint();
    }
}
