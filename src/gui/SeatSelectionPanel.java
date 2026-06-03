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

// 좌석 선택 화면
public class SeatSelectionPanel extends BasePanel implements Refreshable {
    private static final Dimension SCREEN_LABEL_SIZE = new Dimension(800, 50);
    private final transient BookingController bookingController;
    private final transient NavigationController navigationController;
    private final JPanel seatGridPanel; // 좌석 버튼을 배치할 패널
    private final JLabel theaterNameLabel; // 선택된 영화관 이름을 표시하는 라벨
    private final JLabel priceLabel; // 선택된 좌석 총 가격을 표시하는 라벨
    private final JTextField peopleCountField; // 자동 좌석 선택에 사용할 인원수 입력 필드
    private final Map<String, JToggleButton> seatButtonMap; // 좌석 코드로 버튼을 다시 찾기 위한 맵

    // 좌석 화면 구성
    public SeatSelectionPanel(BookingController bookingController, NavigationController navigationController) {
        super("Seats");
        this.bookingController = bookingController;
        this.navigationController = navigationController;
        this.seatGridPanel = new JPanel();
        this.theaterNameLabel = new JLabel("", SwingConstants.CENTER);
        theaterNameLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        theaterNameLabel.setOpaque(true);
        theaterNameLabel.setBackground(new Color(200, 200, 200));
        this.priceLabel = new JLabel("Selected Price: -", SwingConstants.CENTER);
        this.peopleCountField = new JTextField(10);
        peopleCountField.setHorizontalAlignment(JTextField.RIGHT);
        this.seatButtonMap = new HashMap<>();
        

        JLabel theaterScreenLabel = new JLabel("Screen", SwingConstants.CENTER); // 극장 스크린 레이블
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

        JButton reserveButton = new JButton("Reserve"); // 개인 예약 버튼
        reserveButton.addActionListener(event -> bookingController.reserveSelectedSeats());
        JButton backButton = new JButton("Back"); // 뒤로가기 버튼
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JButton autoSelectButton = new JButton("Auto Select");
        autoSelectButton.addActionListener(event -> selectBestSeats());

        // 입력 필드와 실행 버튼을 한 줄에 배치한다.
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
    // 좌석 배치 갱신
    public final void refresh() {
        theaterNameLabel.setText(bookingController.getSelectedTheater().getName()); // 선택된 영화관 이름 표시
        
        seatGridPanel.removeAll(); // 기존 좌석 버튼 제거
        seatButtonMap.clear(); // 새 버튼을 만들기 전에 이전 버튼 참조 제거
        peopleCountField.setText(""); // 화면 재진입 시 이전 인원수 입력값 제거

        bookingController.resetSelectedSeats(); // 선택된 좌석 초기화

        updateSelectedPrice(); // 선택된 좌석 가격 초기화

        int max_rows = bookingController.getSelectedTheater().getRows();
        int max_cols = bookingController.getSelectedTheater().getColumns();

        seatGridPanel.setLayout(new GridLayout(max_rows, max_cols, 6, 6));

        makeButtons();

        revalidate();
        repaint();
    }

    // ===== 가격 추가 기능 =====

    // 좌석 정보에 맞춰 좌석 버튼 생성
    private void makeButtons() {
        try {
            List<SeatInfo> seatInfoList = bookingController.loadSeatInfoList();

            for(SeatInfo seatInfo : seatInfoList) {
                String seatCode = seatInfo.getSeatCode();
                int viewScore = seatInfo.getViewScore();
                int price = seatInfo.getPrice();
                boolean reserved = seatInfo.isReserved();

                JToggleButton seatButton = new JToggleButton(seatCode);
                seatButtonMap.put(seatCode, seatButton); // 자동 선택 후 버튼 상태를 갱신하기 위해 저장

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
                        bookingController.toggleSeat(seatCode); // 버튼 선택 상태와 별도로 세션의 선택 좌석 목록 갱신
                        updateSelectedPrice(); // 선택 좌석 목록 기준으로 총 가격 다시 계산
                    });
                }
                seatGridPanel.add(seatButton);
            }
            
        } catch (IllegalStateException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    // 자동 좌석 선택
    private void selectBestSeats() {
        try {
            int peopleCount = Integer.parseInt(peopleCountField.getText().trim()); // 입력값을 인원수로 변환
            bookingController.selectBestSeats(peopleCount); // 컨트롤러에서 추천 좌석을 선택 목록에 저장
            updateSeatButtons(); // 컨트롤러의 선택 목록을 실제 토글 버튼 선택 상태에 반영
            updateSelectedPrice(); // 자동 선택된 좌석 기준으로 총 가격 갱신
        } catch (NumberFormatException e) {
            navigationController.showMessage("People count must be a number.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            navigationController.showMessage(e.getMessage());
        }
    }

    // 선택 좌석 버튼 상태 갱신
    private void updateSeatButtons() {
        List<String> selectedSeats = bookingController.getSelectedSeats();
        for(Map.Entry<String, JToggleButton> entry : seatButtonMap.entrySet()) {
            entry.getValue().setSelected(selectedSeats.contains(entry.getKey())); // 선택 목록에 포함된 좌석만 토글 선택 처리
        }
    }

    // 선택 좌석 가격 갱신
    private void updateSelectedPrice() {
        try {
            int totalPrice = bookingController.calculateSelectedSeatPrice();
            priceLabel.setText("Selected Price: " + formatPrice(totalPrice));
        } catch (IllegalStateException e) {
            priceLabel.setText("Selected Price: -");
        }
    }

    // 가격 형식
    private String formatPrice(int totalPrice) {
        if (totalPrice <= 0) {
            return "-";
        }
        return String.format("%,d KRW", totalPrice);
    }
}
