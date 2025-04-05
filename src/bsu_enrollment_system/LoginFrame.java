package bsu_enrollment_system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginFrame() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Batangas State University - Enrollment System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // BSU Color Scheme
        Color bsuBlue = new Color(0, 51, 153);
        Color bsuYellow = new Color(255, 204, 0);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        // Logo (placeholder)
        JLabel logoLabel = new JLabel("BSU LOGO", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoLabel.setForeground(bsuBlue);
        headerPanel.add(logoLabel, BorderLayout.CENTER);
        
        // Title
        JLabel titleLabel = new JLabel("BATANGAS STATE UNIVERSITY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(bsuBlue);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Subtitle
        JLabel subTitleLabel = new JLabel("Enrollment Management System", SwingConstants.CENTER);
        subTitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        headerPanel.add(subTitleLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Login Form Panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));
        loginPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loginPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        loginPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loginPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        loginPanel.add(passwordField, gbc);
        
        // Login Button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(bsuBlue);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.addActionListener(this::performLogin);
        loginPanel.add(loginButton, gbc);
        
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Color.WHITE);
        JLabel footerLabel = new JLabel("© 2023 Batangas State University");
        footerLabel.setForeground(bsuBlue);
        footerPanel.add(footerLabel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void performLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.user_id, u.role, s.student_id FROM users u " +
                       "LEFT JOIN students s ON u.user_id = s.user_id " +
                       "WHERE u.username = ? AND u.password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");
                int studentId = rs.getInt("student_id");
                
                dispose(); // Close login window
                
                switch (role) {
                    case "student":
                        new StudentDashboard(userId, studentId).setVisible(true);
                        break;
                    case "program_chair":
                        new ProgramChairDashboard(userId).setVisible(true);
                        break;
                    case "dean":
                        new DeanDashboard(userId).setVisible(true);
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", 
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), 
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}