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

// 좌석 선택 화면
public class SeatSelectionPanel extends BasePanel implements Refreshable {
    private static final Dimension SCREEN_LABEL_SIZE = new Dimension(800, 50);
    private final BookingController bookingController;
    private final JPanel seatGridPanel; // 좌석 버튼을 배치할 패널
    private final JLabel theaterNameLabel; // 선택된 영화관 이름을 표시하는 라벨

    // 좌석 화면 구성
    public SeatSelectionPanel(BookingController bookingController, NavigationController navigationController) {
        super("Seats");
        this.bookingController = bookingController;
        this.seatGridPanel = new JPanel();
        this.theaterNameLabel = new JLabel("", SwingConstants.CENTER);
        theaterNameLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        theaterNameLabel.setOpaque(true);
        theaterNameLabel.setBackground(new Color(200, 200, 200));

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

        JButton reserveButton = new JButton("Reserve"); // 예약 버튼
        reserveButton.addActionListener(event -> bookingController.reserveSelectedSeats());
        JButton backButton = new JButton("Back"); // 뒤로가기 버튼
        backButton.addActionListener(event -> navigationController.showShowtimes());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(reserveButton);
        add(buttonPanel, BorderLayout.SOUTH);

    }

    @Override
    // 좌석 배치 갱신
    public final void refresh() {
        theaterNameLabel.setText(bookingController.getSelectedTheater().getName()); // 선택된 영화관 이름 표시
        
        seatGridPanel.removeAll(); // 기존 좌석 버튼 제거

        bookingController.resetSelectedSeats(); // 선택된 좌석 초기화

        int max_rows = bookingController.getSelectedTheater().getRows();
        int max_cols = bookingController.getSelectedTheater().getColumns();

        seatGridPanel.setLayout(new GridLayout(max_rows, max_cols, 6, 6));

        for(int row=0 ; row<max_rows ; row++) { // 좌석 버튼 생성
            for(int col=1 ; col<=max_cols ; col++) {
                String seatId = "" + (char)(row + 'A') + col;
                
                JToggleButton seatButton = new JToggleButton(seatId);
                seatButton.addActionListener(event -> bookingController.toggleSeat(seatId)); // 좌석 버튼 클릭 시 해당 좌석 선택/해제

                Set<String> reservedSeats = bookingController.getReservedSeats();
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
