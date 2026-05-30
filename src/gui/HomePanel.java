package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.AuthController;
import controller.NavigationController;

public class HomePanel extends JPanel implements Refreshable {
    private static final Dimension ITEM_SIZE = new Dimension(800, 50);

    private final AuthController authController;
    private final JLabel welcomeLabel;

    public HomePanel (AuthController authController, NavigationController navigationController) {
        this.authController = authController;
        this.welcomeLabel = new JLabel("Welcome", SwingConstants.CENTER);
        welcomeLabel.setPreferredSize(ITEM_SIZE);
        welcomeLabel.setMaximumSize(ITEM_SIZE);
        welcomeLabel.setMinimumSize(ITEM_SIZE);
        welcomeLabel.setAlignmentX(CENTER_ALIGNMENT);
       
       
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(50, 130, 50));
        titlePanel.add(new JLabel("Home", SwingConstants.CENTER), BorderLayout.CENTER);


        JButton movieListButton = new JButton("Book Now");
        movieListButton.setPreferredSize(ITEM_SIZE);
        movieListButton.setMaximumSize(ITEM_SIZE);
        movieListButton.setMinimumSize(ITEM_SIZE);
        movieListButton.setAlignmentX(CENTER_ALIGNMENT);
        movieListButton.addActionListener(event -> navigationController.showMovies());

        JButton reservationButton = new JButton("My Reservations");
        reservationButton.setPreferredSize(ITEM_SIZE);
        reservationButton.setMaximumSize(ITEM_SIZE);
        reservationButton.setMinimumSize(ITEM_SIZE);
        reservationButton.setAlignmentX(CENTER_ALIGNMENT);
        reservationButton.addActionListener(event -> navigationController.showReservations());

        JPanel contentsPanel = new JPanel();
        contentsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
        contentsPanel.setAlignmentX(CENTER_ALIGNMENT);
        contentsPanel.add(welcomeLabel);
        contentsPanel.add(movieListButton);
        contentsPanel.add(reservationButton);


        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(event -> authController.logout());

        
        add(titlePanel, BorderLayout.NORTH);
        add(contentsPanel, BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);
    }
    
    @Override
    public void refresh() {
        String userId = authController.getCurrentUser().getId();
        if(userId != null) {
            welcomeLabel.setText("Welcome, " + userId);
        } else {
            welcomeLabel.setText("Welcome");
        }
    }
}
