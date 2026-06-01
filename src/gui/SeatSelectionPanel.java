package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.BookingController;
import controller.NavigationController;
import session.BookingSession; 

public class SeatSelectionPanel extends BasePanel implements Refreshable {
    private static final Dimension SCREEN_LABEL_SIZE = new Dimension(800, 44);
    private final BookingController bookingController;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;
    private final JPanel seatGridPanel;
    private final JLabel theaterNameLabel;
    private final JLabel priceLabel;
    private final JTextField groupCountField;
    private final Map<String, JToggleButton> seatButtons;

    public SeatSelectionPanel(BookingController bookingController, 
                                BookingSession bookingSession, 
                                NavigationController navigationController) {
        super("Seats");
        this.bookingController = bookingController;
        this.bookingSession = bookingSession;
        this.navigationController = navigationController;
        this.seatGridPanel = new JPanel();
        this.theaterNameLabel = new JLabel("", SwingConstants.CENTER);
        this.priceLabel = new JLabel("Selected Price: -", SwingConstants.CENTER);
        this.groupCountField = new JTextField("2", 4);
        this.seatButtons = new HashMap<>();
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
        JButton groupSelectButton = new JButton("Auto Group Seats");
        groupSelectButton.addActionListener(event -> autoSelectGroupSeats());
        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(priceLabel);
        buttonPanel.add(new JLabel("People"));
        buttonPanel.add(groupCountField);
        buttonPanel.add(groupSelectButton);
        buttonPanel.add(reserveButton);
        add(buttonPanel, BorderLayout.SOUTH);

    }

    @Override
    public final void refresh() {
        // TODO: Theater rows/columns 기준으로 좌석 버튼을 생성한다.
        // TODO: 이미 예약된 좌석은 비활성화하고, 클릭한 좌석은 bookingSession.toggleSeat(...)로 선택 상태를 바꾼다.
        theaterNameLabel.setText(bookingSession.getSelectedTheater().getName());
        
        seatGridPanel.removeAll();
        seatButtons.clear();

        bookingSession.getSelectedSeats().clear(); // 좌석 선택 초기화

        updateSelectedPrice();

        int max_rows = bookingSession.getSelectedTheater().getRows();
        int max_cols = bookingSession.getSelectedTheater().getColumns();

        seatGridPanel.setLayout(new GridLayout(max_rows, max_cols, 6, 6));

        for(int row=0 ; row<max_rows ; row++) {
            for(int col=1 ; col<=max_cols ; col++) {
                String seatId = "" + (char)(row + 'A') + col;
                
                JToggleButton seatButton = new JToggleButton(seatId);
                seatButton.addActionListener(event -> {
                    bookingSession.toggleSeat(seatId);
                    updateSelectedPrice();
                });

                Set<String> reservedSeats = bookingSession.getSelectedShowtime().getReservedSeats();
                if(reservedSeats.contains(seatId)) {
                    seatButton.setEnabled(false);
                    seatButton.setText("<html><center>" + seatId + "<br/>Reserved</center></html>");
                } else {
                    setSeatPriceText(seatButton, seatId);
                }
                seatButtons.put(seatId, seatButton);
                seatGridPanel.add(seatButton);
            }
        }

        revalidate();
        repaint();
    }

    private void updateSelectedPrice() {
        try {
            int totalPrice = bookingController.calculateSelectedSeatPrice();
            priceLabel.setText("Selected Price: " + formatPrice(totalPrice));
        } catch (IllegalStateException e) {
            priceLabel.setText("Selected Price: -");
        }
    }

    private void setSeatPriceText(JToggleButton seatButton, String seatId) {
        try {
            Map<String, Object> priceInfo = bookingController.calculateSeatPriceInfo(seatId);
            int viewScore = readInt(priceInfo.get("viewScore"));
            int price = readInt(priceInfo.get("price"));
            seatButton.setText("<html><center>" + seatId
                    + "<br/>View " + viewScore
                    + "<br/>" + formatPrice(price)
                    + "</center></html>");
            seatButton.setToolTipText("View score " + viewScore + ", price " + formatPrice(price));
        } catch (IllegalStateException e) {
            seatButton.setText(seatId);
        }
    }

    private void autoSelectGroupSeats() {
        try {
            int peopleCount = Integer.parseInt(groupCountField.getText().trim());

            // 서버가 이미 예약된 좌석을 제외하고 그룹 인원 수에 맞는 좌석을 추천한다.
            List<String> seatCodes = bookingController.recommendGroupSeats(peopleCount);

            bookingSession.getSelectedSeats().clear();
            bookingSession.getSelectedSeats().addAll(seatCodes);

            updateSeatButtonSelection();
            updateSelectedPrice();
            if(isContinuousSeatList(seatCodes)) {
                navigationController.showMessage("Recommended continuous seats: " + seatCodes);
            } else {
                navigationController.showMessage("Continuous seats are not enough. Alternative seats: " + seatCodes);
            }
        } catch (NumberFormatException e) {
            navigationController.showMessage("Please enter a valid people count.");
        } catch (IllegalStateException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    private void updateSeatButtonSelection() {
        for(Map.Entry<String, JToggleButton> entry : seatButtons.entrySet()) {
            entry.getValue().setSelected(bookingSession.getSelectedSeats().contains(entry.getKey()));
        }
    }

    private int readInt(Object value) {
        if(value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private boolean isContinuousSeatList(List<String> seatCodes) {
        if(seatCodes == null || seatCodes.size() <= 1) {
            return true;
        }

        char row = seatCodes.get(0).charAt(0);
        int beforeColumn = Integer.parseInt(seatCodes.get(0).substring(1));
        for(int i=1; i<seatCodes.size(); i++) {
            String seatCode = seatCodes.get(i);
            int currentColumn = Integer.parseInt(seatCode.substring(1));
            if(seatCode.charAt(0) != row || currentColumn != beforeColumn + 1) {
                return false;
            }
            beforeColumn = currentColumn;
        }
        return true;
    }

    private String formatPrice(int totalPrice) {
        if (totalPrice <= 0) {
            return "-";
        }
        return String.format("%,d KRW", totalPrice);
    }
}
