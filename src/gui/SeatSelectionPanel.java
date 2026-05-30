package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

public class SeatSelectionPanel extends BasePanel implements Refreshable {
    private static final Dimension SCREEN_LABEL_SIZE = new Dimension(800, 44);
    private final BookingSession bookingSession;
    private final JPanel seatGridPanel;
    private final JLabel theaterNameLabel;

    public SeatSelectionPanel(BookingController bookingController, 
                                BookingSession bookingSession, 
                                NavigationController navigationController) {
        super("Seats");
        this.bookingSession = bookingSession;
        this.seatGridPanel = new JPanel();
        this.theaterNameLabel = new JLabel("", SwingConstants.CENTER);
        theaterNameLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        theaterNameLabel.setOpaque(true);
        theaterNameLabel.setBackground(new Color(200, 200, 200));

        JLabel theaterScreenLabel = new JLabel("Screen", SwingConstants.CENTER);
        theaterScreenLabel.setPreferredSize(SCREEN_LABEL_SIZE);
        theaterScreenLabel.setMinimumSize(SCREEN_LABEL_SIZE);
        theaterScreenLabel.setMaximumSize(SCREEN_LABEL_SIZE);
        theaterScreenLabel.setAlignmentX(CENTER_ALIGNMENT);
        theaterScreenLabel.setOpaque(true);
        theaterScreenLabel.setBackground(new Color(150, 150, 150));
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(SCREEN_LABEL_SIZE);
        emptyPanel.setMinimumSize(SCREEN_LABEL_SIZE);
        emptyPanel.setMaximumSize(SCREEN_LABEL_SIZE);

        titlePanel.add(theaterNameLabel, BorderLayout.SOUTH);
        contentsPanel.add(theaterScreenLabel, BorderLayout.NORTH);
        contentsPanel.add(emptyPanel, BorderLayout.NORTH);
        contentsPanel.add(seatGridPanel, BorderLayout.CENTER);

        JButton reserveButton = new JButton("Reserve");
        reserveButton.addActionListener(event -> bookingController.reserveSelectedSeats());
        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(reserveButton);
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
