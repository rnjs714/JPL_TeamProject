package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
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
import domain.GroupReservation;
import session.BookingSession;

/**
 * 좌석 선택 화면이다.
 *
 * 개인 예매는 좌석 선택 후 바로 Reserve 버튼을 누른다.
 * 그룹 예매는 대표자가 친구 ID를 입력하고 좌석을 직접 선택한 뒤 TEMP_HOLD 상태로 잡아 둔다.
 */
public class SeatSelectionPanel extends BasePanel implements Refreshable {
    private static final Dimension SCREEN_LABEL_SIZE = new Dimension(800, 44);
    private final BookingController bookingController;
    private final BookingSession bookingSession;
    private final NavigationController navigationController;
    private final JPanel seatGridPanel;
    private final JLabel theaterNameLabel;
    private final JLabel priceLabel;
    // 그룹 예매 시 대표자가 함께 예매할 친구 ID를 쉼표로 입력하는 칸이다.
    private final JTextField friendIdsField;
    // 좌석 코드와 버튼을 연결해 두면 선택 상태를 새로고침할 때 쉽게 반영할 수 있다.
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
        this.friendIdsField = new JTextField("", 12);
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
        JButton groupHoldButton = new JButton("Hold Group Seats");
        // 그룹 예매는 바로 확정하지 않고, 선택 좌석을 임시 홀딩한 뒤 그룹원 결제를 기다린다.
        groupHoldButton.addActionListener(event -> holdGroupSeats());
        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(priceLabel);
        buttonPanel.add(new JLabel("Friends"));
        buttonPanel.add(friendIdsField);
        buttonPanel.add(groupHoldButton);
        buttonPanel.add(reserveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public final void refresh() {
        theaterNameLabel.setText(bookingSession.getSelectedTheater().getName());

        // 화면에 들어올 때마다 좌석 버튼을 새로 만든다.
        // 예약/임시 홀딩 상태는 서버 데이터가 바뀔 수 있으므로 매번 다시 확인한다.
        seatGridPanel.removeAll();
        seatButtons.clear();

        bookingSession.getSelectedSeats().clear();
        updateSelectedPrice();

        int max_rows = bookingSession.getSelectedTheater().getRows();
        int max_cols = bookingSession.getSelectedTheater().getColumns();

        seatGridPanel.setLayout(new GridLayout(max_rows, max_cols, 6, 6));

        for(int row=0 ; row<max_rows ; row++) {
            for(int col=1 ; col<=max_cols ; col++) {
                String seatId = "" + (char)(row + 'A') + col;

                JToggleButton seatButton = new JToggleButton(seatId);
                seatButton.addActionListener(event -> {
                    // 좌석을 누르면 세션의 선택 목록을 바꾸고, 선택된 좌석들의 총 가격을 다시 계산한다.
                    bookingSession.toggleSeat(seatId);
                    updateSelectedPrice();
                });

                Set<String> reservedSeats = bookingSession.getSelectedShowtime().getReservedSeats();
                if(reservedSeats.contains(seatId)) {
                    // 이미 확정 예약된 좌석은 어떤 예매 방식에서도 다시 선택할 수 없다.
                    seatButton.setEnabled(false);
                    seatButton.setText("<html><center>" + seatId + "<br/>Reserved</center></html>");
                } else {
                    // 예약되지 않은 좌석은 서버에서 시야 점수와 가격, TEMP_HOLD 여부를 받아 표시한다.
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
            // 선택 좌석이 바뀔 때마다 서버 계산 가격을 받아 화면 하단에 보여준다.
            int totalPrice = bookingController.calculateSelectedSeatPrice();
            priceLabel.setText("Selected Price: " + formatPrice(totalPrice));
        } catch (IllegalStateException e) {
            priceLabel.setText("Selected Price: -");
        }
    }

    private void setSeatPriceText(JToggleButton seatButton, String seatId) {
        try {
            // 좌석별 가격 정보에는 시야 점수, 가격, 좌석 상태가 함께 들어 있다.
            Map<String, Object> priceInfo = bookingController.calculateSeatPriceInfo(seatId);
            int viewScore = readInt(priceInfo.get("viewScore"));
            int price = readInt(priceInfo.get("price"));
            String seatStatus = String.valueOf(priceInfo.get("seatStatus"));

            if("TEMP_HOLD".equals(seatStatus)) {
                // 다른 그룹이 결제 대기 중인 좌석은 아직 확정 예약은 아니지만 중복 선택을 막는다.
                seatButton.setEnabled(false);
                seatButton.setText("<html><center>" + seatId + "<br/>TEMP_HOLD</center></html>");
                return;
            }

            // 사용자가 좌석을 고르기 전에 시야 점수와 예상 가격을 바로 볼 수 있게 버튼에 표시한다.
            seatButton.setText("<html><center>" + seatId
                    + "<br/>View " + viewScore
                    + "<br/>" + formatPrice(price)
                    + "</center></html>");
            seatButton.setToolTipText("View score " + viewScore + ", price " + formatPrice(price));
        } catch (IllegalStateException e) {
            seatButton.setText(seatId);
        }
    }

    private void holdGroupSeats() {
        try {
            List<String> friendIds = readFriendIds();
            // 대표자가 입력한 친구 목록과 현재 선택 좌석을 서버로 보내 그룹 예매를 만든다.
            GroupReservation group = bookingController.createGroupReservation(friendIds);
            updateSeatButtonSelection();
            refresh();
            navigationController.showMessage("Group ID: " + group.getGroupId()
                    + "\nSeats are TEMP_HOLD until all members pay.");
        } catch (IllegalStateException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    private List<String> readFriendIds() {
        List<String> friendIds = new ArrayList<>();
        String text = friendIdsField.getText().trim();
        if(text.isEmpty()) {
            return friendIds;
        }

        // 입력 예시는 user1,user2 형태이다. 쉼표 기준으로 나누고 빈 값은 제외한다.
        String[] ids = text.split(",");
        for(int i=0; i<ids.length; i++) {
            String id = ids[i].trim();
            if(!id.isEmpty()) {
                friendIds.add(id);
            }
        }
        return friendIds;
    }

    private void updateSeatButtonSelection() {
        // 그룹 예매 생성 후 세션의 선택 좌석이 비워졌을 수 있으므로 버튼 선택 상태를 맞춰 준다.
        for(Map.Entry<String, JToggleButton> entry : seatButtons.entrySet()) {
            entry.getValue().setSelected(bookingSession.getSelectedSeats().contains(entry.getKey()));
        }
    }

    private int readInt(Object value) {
        // Jackson 응답 값은 Integer 또는 다른 Number 타입으로 들어올 수 있어 안전하게 숫자로 변환한다.
        if(value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String formatPrice(int totalPrice) {
        if (totalPrice <= 0) {
            return "-";
        }
        return String.format("%,d KRW", totalPrice);
    }
}
