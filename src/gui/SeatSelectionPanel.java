package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.BookingController;
import controller.NavigationController;
import domain.SeatInfo;

// Seat selection screen
public class SeatSelectionPanel extends BasePanel implements Refreshable {
    private static final Dimension SCREEN_LABEL_SIZE = new Dimension(800, 50);
    private final transient BookingController bookingController;
    private final transient NavigationController navigationController;
    private final JPanel seatGridPanel;
    private final JLabel theaterNameLabel;
    private final JLabel priceLabel;
    private final JTextField peopleCountField;
    private final Map<String, JToggleButton> seatButtonMap;

    public SeatSelectionPanel(BookingController bookingController, NavigationController navigationController) {
        super("Seats");
        this.bookingController = bookingController;
        this.navigationController = navigationController;
        this.seatGridPanel = new JPanel();
        this.theaterNameLabel = new JLabel("", SwingConstants.CENTER);
        this.priceLabel = new JLabel("Selected Price: -", SwingConstants.CENTER);
        this.peopleCountField = new JTextField(10);
        this.seatButtonMap = new HashMap<>();

        theaterNameLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        theaterNameLabel.setOpaque(true);
        theaterNameLabel.setBackground(new Color(200, 200, 200));
        peopleCountField.setHorizontalAlignment(JTextField.RIGHT);

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

        JButton autoSelectButton = new JButton("Auto Select");
        autoSelectButton.addActionListener(event -> selectBestSeats());

        JPanel autoSelectPanel = new JPanel();
        autoSelectPanel.add(new JLabel("People:"));
        autoSelectPanel.add(peopleCountField);
        autoSelectPanel.add(autoSelectButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(priceLabel);
        buttonPanel.add(reserveButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(autoSelectPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    @Override
    public final void refresh() {
        theaterNameLabel.setText(bookingController.getSelectedTheater().getName());

        seatGridPanel.removeAll();
        seatButtonMap.clear();
        peopleCountField.setText("");
        bookingController.resetSelectedSeats();
        updateSelectedPrice();

        int maxRows = bookingController.getSelectedTheater().getRows();
        int maxColumns = bookingController.getSelectedTheater().getColumns();
        seatGridPanel.setLayout(new GridLayout(maxRows, maxColumns, 6, 6));

        makeButtons();

        revalidate();
        repaint();
    }

    private void makeButtons() {
        try {
            List<SeatInfo> seatInfoList = bookingController.loadSeatInfoList();

            for (SeatInfo seatInfo : seatInfoList) {
                String seatCode = seatInfo.getSeatCode();
                int viewScore = seatInfo.getViewScore();
                int price = seatInfo.getPrice();
                boolean reserved = seatInfo.isReserved();

                JToggleButton seatButton = new JToggleButton(seatCode);
                seatButtonMap.put(seatCode, seatButton);

                if (reserved) {
                    seatButton.setEnabled(false);
                    seatButton.setText("<html><center>" + seatCode + "<br/>Reserved</center></html>");
                } else {
                    seatButton.setText("<html><center>" + seatCode
                            + "<br/>View " + viewScore
                            + "<br/>" + formatPrice(price)
                            + "</center></html>");
                    seatButton.setToolTipText("View score " + viewScore + ", price " + formatPrice(price));
                    seatButton.addActionListener(event -> {
                        bookingController.toggleSeat(seatCode);
                        updateSelectedPrice();
                    });
                }

                seatGridPanel.add(seatButton);
            }
        } catch (IllegalStateException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    private void selectBestSeats() {
        try {
            int peopleCount = Integer.parseInt(peopleCountField.getText().trim());
            bookingController.selectBestSeats(peopleCount);
            updateSeatButtons();
            updateSelectedPrice();
        } catch (NumberFormatException e) {
            navigationController.showMessage("People count must be a number.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    private void updateSeatButtons() {
        List<String> selectedSeats = bookingController.getSelectedSeats();
        for (Map.Entry<String, JToggleButton> entry : seatButtonMap.entrySet()) {
            entry.getValue().setSelected(selectedSeats.contains(entry.getKey()));
        }
    }

    private void updateSelectedPrice() {
        try {
            int totalPrice = bookingController.calculateSelectedSeatPrice();
            priceLabel.setText("Selected Price: " + formatPrice(totalPrice));
        } catch (IllegalStateException e) {
            priceLabel.setText("Selected Price: -");
        }
    }

    private String formatPrice(int totalPrice) {
        if (totalPrice <= 0) {
            return "-";
        }
        return String.format("%,d KRW", totalPrice);
    }
}
