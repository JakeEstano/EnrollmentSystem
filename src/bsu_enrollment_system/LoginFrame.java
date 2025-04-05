package bsu_enrollment_system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton passwordVisibilityToggle;
    private Image backgroundImage;
    private boolean passwordVisible = false;
    
    public LoginFrame() {
        loadBackgroundImage();
        initializeUI();
    }
    
   private void loadBackgroundImage() {
    try {
        backgroundImage = new ImageIcon("C:\\Users\\Irish Kaye\\Documents\\jek enrollment\\EnrollmentSystem\\src\\images\\logo bg.jpg").getImage();
    } catch (Exception e) {
        System.err.println("Could not load background image. Please check the file path.");
        e.printStackTrace();
    }
}

private void initializeUI() {
    setTitle("Batangas State University - Enrollment System");
    setSize(800, 600);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    // Main Panel with background image
    JPanel mainPanel = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 51, 153), 0, getHeight(), new Color(0, 102, 204));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    };
    mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

    // White background panel for login section with red border
    JPanel loginBackgroundPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded edges
            g2d.dispose();
        }
    };
    loginBackgroundPanel.setBackground(Color.WHITE);
    loginBackgroundPanel.setLayout(new BoxLayout(loginBackgroundPanel, BoxLayout.Y_AXIS));
    loginBackgroundPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(204, 0, 0), 3),
            new EmptyBorder(25, 25, 25, 25)));
    loginBackgroundPanel.setMaximumSize(new Dimension(400, 350));

    // Login Form Panel (transparent for styling)
    JPanel loginFormPanel = new JPanel();
    loginFormPanel.setLayout(new BoxLayout(loginFormPanel, BoxLayout.Y_AXIS));
    loginFormPanel.setOpaque(false);

    // Header
    JLabel headerLabel = new JLabel("Enrollment System");
    headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
    headerLabel.setForeground(Color.BLACK);
    headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    loginFormPanel.add(headerLabel);
    loginFormPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Username Field
    usernameField = new JTextField("Username");
    usernameField.setFont(new Font("Arial", Font.PLAIN, 15));
    usernameField.setForeground(Color.GRAY);
    usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    usernameField.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent evt) {
            if (usernameField.getText().equals("Username")) {
                usernameField.setText("");
                usernameField.setForeground(Color.BLACK);
            }
        }
        public void focusLost(FocusEvent evt) {
            if (usernameField.getText().isEmpty()) {
                usernameField.setForeground(Color.GRAY);
                usernameField.setText("Username");
            }
        }
    });
    loginFormPanel.add(usernameField);
    loginFormPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    // Password Field with toggle
    passwordField = new JPasswordField("Password");
    passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
    passwordField.setForeground(Color.GRAY);
    passwordField.setEchoChar((char) 0);
    passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    passwordField.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent evt) {
            if (String.valueOf(passwordField.getPassword()).equals("Password")) {
                passwordField.setText("");
                passwordField.setForeground(Color.BLACK);
                passwordField.setEchoChar('•');
            }
        }
        public void focusLost(FocusEvent evt) {
            if (String.valueOf(passwordField.getPassword()).isEmpty()) {
                passwordField.setForeground(Color.GRAY);
                passwordField.setText("Password");
                passwordField.setEchoChar((char) 0);
            }
        }
    });

    passwordVisibilityToggle = new JButton("👁");
    passwordVisibilityToggle.setFont(new Font("Arial", Font.PLAIN, 16));
    passwordVisibilityToggle.setBorder(BorderFactory.createEmptyBorder());
    passwordVisibilityToggle.setContentAreaFilled(false);
    passwordVisibilityToggle.setFocusPainted(false);
    passwordVisibilityToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
    passwordVisibilityToggle.addActionListener(e -> {
        if (!String.valueOf(passwordField.getPassword()).equals("Password")) {
            if (passwordVisible) {
                passwordField.setEchoChar('•');
            } else {
                passwordField.setEchoChar((char) 0);
            }
            passwordVisible = !passwordVisible;
        }
    });

    JPanel passwordPanel = new JPanel(new BorderLayout());
    passwordPanel.setOpaque(false);
    passwordPanel.add(passwordField, BorderLayout.CENTER);
    passwordPanel.add(passwordVisibilityToggle, BorderLayout.EAST);
    loginFormPanel.add(passwordPanel);

    // Password info
    JLabel passwordInfoLabel = new JLabel("* Password is case sensitive");
    passwordInfoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
    passwordInfoLabel.setForeground(Color.GRAY);
    passwordInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
    loginFormPanel.add(passwordInfoLabel);

    // Login Button
    JButton loginButton = new JButton("Login");
    loginButton.setFont(new Font("Arial", Font.BOLD, 16));
    loginButton.setBackground(new Color(0, 102, 204));
    loginButton.setForeground(Color.WHITE);
    loginButton.setFocusPainted(false);
    loginButton.setBorderPainted(false);
    loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    loginButton.setMaximumSize(new Dimension(350, 45));
    loginButton.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            loginButton.setBackground(new Color(0, 86, 179));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            loginButton.setBackground(new Color(0, 102, 204));
        }
    });
    loginButton.addActionListener(this::performLogin);
    loginFormPanel.add(loginButton);
    loginFormPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    loginBackgroundPanel.add(loginFormPanel);

    // Centering the login panel
    JPanel wrapperPanel = new JPanel(new GridBagLayout());
    wrapperPanel.setOpaque(false);
    wrapperPanel.add(loginBackgroundPanel);
    mainPanel.add(wrapperPanel, BorderLayout.CENTER);

    // Header with semi-transparent background
    JPanel headerPanel = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    };
    headerPanel.setOpaque(false);

    JLabel titleLabel = new JLabel("Batangas State University", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Cambria", Font.BOLD, 36));
    titleLabel.setForeground(Color.BLACK);

    JLabel engineeringLabel = new JLabel("The National Engineering University", SwingConstants.CENTER);
    engineeringLabel.setFont(new Font("Arial", Font.BOLD, 32));
    engineeringLabel.setForeground(new Color(255, 51, 51));

    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setOpaque(false);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    engineeringLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    textPanel.add(titleLabel);
    textPanel.add(engineeringLabel);

    headerPanel.add(textPanel, BorderLayout.CENTER);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

    // Blue panel for "Enrollment System"
    JPanel enrollmentPanel = new JPanel();
    enrollmentPanel.setBackground(new Color(0, 102, 204));
    enrollmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    enrollmentPanel.setLayout(new BoxLayout(enrollmentPanel, BoxLayout.Y_AXIS));

    JLabel enrollmentLabel = new JLabel("Leading Innovations, Transforming Lives, Building the Nation", SwingConstants.CENTER);
    enrollmentLabel.setFont(new Font("Sans-serif", Font.BOLD | Font.ITALIC, 16));
    enrollmentLabel.setForeground(Color.WHITE);
    enrollmentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    enrollmentPanel.add(enrollmentLabel);


    // Add header and enrollment panel together
    Box headerBox = Box.createVerticalBox();
    headerBox.add(headerPanel);
    headerBox.add(enrollmentPanel);

    mainPanel.add(headerBox, BorderLayout.NORTH);

    // Footer
    JPanel footerPanel = new JPanel();
    footerPanel.setOpaque(false);
    JLabel footerLabel = new JLabel("© 2025 Batangas State University");
    footerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    footerLabel.setForeground(Color.WHITE);
    footerPanel.add(footerLabel);
    mainPanel.add(footerPanel, BorderLayout.SOUTH);

    add(mainPanel);
}

    
    private void performLogin(ActionEvent e) {
        // Get username, cleaning up placeholder if necessary
        String username = usernameField.getText();
        if (username.equals("Username")) username = "";
        
        // Get password, cleaning up placeholder if necessary
        String password = new String(passwordField.getPassword());
        if (password.equals("Password")) password = "";
        
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
                         new StudentDashboard(String.valueOf(studentId)).setVisible(true);

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
                // Set system look and feel for better integration
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Customize UI components
                UIManager.put("Button.background", Color.WHITE);
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("OptionPane.background", Color.WHITE);
                UIManager.put("OptionPane.messageBackground", Color.WHITE);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}