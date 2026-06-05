package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// 공통 패널 레이아웃
public abstract class BasePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    protected final JPanel titlePanel;
    protected final JPanel contentsPanel;

    // 타이틀/콘텐츠 영역 구성
    protected BasePanel(String title) {
        setLayout(new BorderLayout());

        titlePanel = new JPanel(new BorderLayout()); // 타이틀 영역
        titlePanel.setBackground(new Color(50, 130, 50));
        titlePanel.add(new JLabel(title, SwingConstants.CENTER));

        contentsPanel = new JPanel(new BorderLayout()); // 콘텐츠 영역
        contentsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
        contentsPanel.setAlignmentX(CENTER_ALIGNMENT);

        add(titlePanel, BorderLayout.NORTH);
        add(contentsPanel, BorderLayout.CENTER);
    }
}
