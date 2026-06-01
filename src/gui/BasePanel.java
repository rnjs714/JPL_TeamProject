package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public abstract class BasePanel extends JPanel {
    protected final JPanel titlePanel;
    protected final JPanel contentsPanel;

    protected BasePanel(String title) {
        setLayout(new BorderLayout());

        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(50, 130, 50));
        titlePanel.add(new JLabel(title, SwingConstants.CENTER));

        contentsPanel = new JPanel(new BorderLayout());
        contentsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
        contentsPanel.setAlignmentX(CENTER_ALIGNMENT);

        add(titlePanel, BorderLayout.NORTH);
        add(contentsPanel, BorderLayout.CENTER);
    }
}
