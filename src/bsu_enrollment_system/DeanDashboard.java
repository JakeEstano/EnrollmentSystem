package bsu_enrollment_system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeanDashboard extends JFrame {
    private final int userId;
    private List<Map<String, Object>> pendingIrregRequests = new ArrayList<>();
    private List<Map<String, Object>> pendingDropRequests = new ArrayList<>();
    private String currentCollegeName = "College of Engineering";
    
    public DeanDashboard(int userId) {
        this.userId = userId;
        loadBackgroundImage();
        loadDeanData();
        initializeUI();
    }
    
    private void loadDeanData() {
        loadPendingIrregularRequests();
        loadPendingDropRequests();
        loadCollegeInfo();
    }
    
    private void loadCollegeInfo() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT college_name FROM colleges WHERE dean_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentCollegeName = rs.getString("college_name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadPendingIrregularRequests() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.request_id, s.student_id, s.student_number, " +
                        "CONCAT(u.first_name, ' ', u.last_name) as student_name, " +
                        "c.course_code, c.course_name, sec.section_name, p.program_name " +
                        "FROM irreg_enrollment_requests r " +
                        "JOIN students s ON r.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "JOIN sections sec ON r.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "JOIN programs p ON c.program_id = p.program_id " +
                        "JOIN colleges col ON p.college_id = col.college_id " +
                        "WHERE r.status = 'pending_dean' " +
                        "AND col.dean_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            pendingIrregRequests.clear();
            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("request_id", rs.getInt("request_id"));
                request.put("student_id", rs.getInt("student_id"));
                request.put("student_number", rs.getString("student_number"));
                request.put("student_name", rs.getString("student_name"));
                request.put("course_code", rs.getString("course_code"));
                request.put("course_name", rs.getString("course_name"));
                request.put("section_name", rs.getString("section_name"));
                request.put("program_name", rs.getString("program_name"));
                pendingIrregRequests.add(request);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadPendingDropRequests() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT d.drop_id, s.student_id, s.student_number, " +
                        "CONCAT(u.first_name, ' ', u.last_name) as student_name, " +
                        "c.course_code, c.course_name, d.reason, e.enrollment_id, p.program_name " +
                        "FROM drop_requests d " +
                        "JOIN enrollments e ON d.enrollment_id = e.enrollment_id " +
                        "JOIN sections sec ON e.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "JOIN students s ON e.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "JOIN programs p ON c.program_id = p.program_id " +
                        "JOIN colleges col ON p.college_id = col.college_id " +
                        "WHERE d.status = 'approved_chair' " +
                        "AND col.dean_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            pendingDropRequests.clear();
            while (rs.next()) {
                Map<String, Object> request = new HashMap<>();
                request.put("drop_id", rs.getInt("drop_id"));
                request.put("student_id", rs.getInt("student_id"));
                request.put("student_number", rs.getString("student_number"));
                request.put("student_name", rs.getString("student_name"));
                request.put("course_code", rs.getString("course_code"));
                request.put("course_name", rs.getString("course_name"));
                request.put("reason", rs.getString("reason"));
                request.put("enrollment_id", rs.getInt("enrollment_id"));
                request.put("program_name", rs.getString("program_name"));
                pendingDropRequests.add(request);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void initializeUI() {
    setTitle("BSU Enrollment System - Dean Dashboard");
    setSize(1200, 800);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    
    // Main Panel with BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Header Panel with background image
    JPanel headerPanel = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback to a gradient if image couldn't be loaded
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color bsuBlue = new Color(0, 51, 153);
                GradientPaint gp = new GradientPaint(0, 0, bsuBlue, 0, h, new Color(0, 102, 204));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        }
    };
    headerPanel.setPreferredSize(new Dimension(1200, 150)); // Set your desired header height
    
    
    
    // Add header to the top of the main panel
    mainPanel.add(headerPanel, BorderLayout.NORTH);
    
    // Tabbed Pane
    JTabbedPane tabbedPane = new JTabbedPane();
    
    // 1. College Management Tab
    tabbedPane.addTab("College Management", createCollegeManagementPanel());
    
    // 2. Irregular Approvals Tab
    tabbedPane.addTab("Irregular Approvals", createIrregularApprovalsPanel());
    
    // 3. Drop Approvals Tab
    tabbedPane.addTab("Drop Approvals", createDropApprovalsPanel());
    
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    add(mainPanel);
}
    
    private Image backgroundImage;
    
    private void loadBackgroundImage() {
    try {
        // Option 1: Try loading from resources folder
        backgroundImage = new ImageIcon(getClass().getResource("/deanportal.png")).getImage();
    } catch (Exception e1) {
        try {
            // Option 2: Try loading from specific package in resources
            backgroundImage = new ImageIcon(getClass().getResource("/bsu_enrollment_system/deanportal.png")).getImage();
        } catch (Exception e2) {
            try {
                // Option 3: Try loading from project directory
                backgroundImage = new ImageIcon("bsuheader.jpg").getImage();
            } catch (Exception e3) {
                try {
                    // Option 4: Try loading from absolute path
                    String userDir = System.getProperty("user.dir");
                    backgroundImage = new ImageIcon(userDir + File.separator + "src" + 
                                    File.separator + "bsu_enrollment_system" + 
                                    File.separator + "deanportal.png").getImage();
                } catch (Exception e4) {
                    System.err.println("Could not load background image. Please check the file path.");
                    e4.printStackTrace();
                }
            }
        }
    }
}
    
    private JPanel createCollegeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // College information form
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        JLabel nameLabel = new JLabel("College Name:");
        JTextField nameField = new JTextField(currentCollegeName);
        
        JLabel logoLabel = new JLabel("College Logo:");
        JButton logoButton = new JButton("Upload Logo");
        logoButton.setEnabled(false); // Placeholder for future implementation
        
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(logoLabel);
        formPanel.add(logoButton);
        
        // Save button
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveCollegeChanges(nameField.getText()));
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(saveButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void saveCollegeChanges(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "College name cannot be empty",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            // Update college name
            String sql = "UPDATE colleges SET college_name = ? WHERE dean_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setInt(2, userId);
            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                currentCollegeName = newName;
                JOptionPane.showMessageDialog(this, 
                    "College information updated successfully!\n" +
                    "All documents will now reflect the new college name: " + newName,
                    "Changes Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to update college information",
                    "System Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error saving changes: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createIrregularApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Pending requests table
        String[] columns = {"Select", "Student Number", "Student Name", "Program", "Course", "Section"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        
        for (Map<String, Object> request : pendingIrregRequests) {
            model.addRow(new Object[]{
                false,
                request.get("student_number"),
                request.get("student_name"),
                request.get("program_name"),
                request.get("course_code") + " - " + request.get("course_name"),
                request.get("section_name")
            });
        }
        
        JTable requestsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        
        // Approval buttons
        JButton approveButton = new JButton("Final Approve");
        JButton rejectButton = new JButton("Reject");
        
        approveButton.addActionListener(e -> processIrregularRequest(requestsTable, model, true));
        rejectButton.addActionListener(e -> processIrregularRequest(requestsTable, model, false));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void processIrregularRequest(JTable table, DefaultTableModel model, boolean approve) {
        int selectedRow = -1;
        int requestId = -1;
        
        // Find selected row
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((boolean) model.getValueAt(i, 0)) {
                selectedRow = i;
                requestId = (int) pendingIrregRequests.get(i).get("request_id");
                break;
            }
        }
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a request to process",
                "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String status = approve ? "approved" : "rejected";
            
            // Update request status
            String sql = "UPDATE irreg_enrollment_requests " +
                        "SET approved_by_dean = ?, status = ? " +
                        "WHERE request_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, approve);
            stmt.setString(2, status);
            stmt.setInt(3, requestId);
            stmt.executeUpdate();
            
            // If rejected, remove the enrollment
            if (!approve) {
                int studentId = (int) pendingIrregRequests.get(selectedRow).get("student_id");
                int sectionId = getSectionIdForRequest(requestId);
                
                // Remove enrollment
                String enrollSql = "DELETE FROM enrollments " +
                                 "WHERE student_id = ? AND section_id = ? " +
                                 "AND status = 'enrolled'";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, studentId);
                enrollStmt.setInt(2, sectionId);
                enrollStmt.executeUpdate();
                
                // Update section count
                String updateSql = "UPDATE sections SET current_students = current_students - 1 " +
                                  "WHERE section_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, sectionId);
                updateStmt.executeUpdate();
            }
            
            // Refresh data
            loadPendingIrregularRequests();
            model.setRowCount(0);
            for (Map<String, Object> request : pendingIrregRequests) {
                model.addRow(new Object[]{
                    false,
                    request.get("student_number"),
                    request.get("student_name"),
                    request.get("program_name"),
                    request.get("course_code") + " - " + request.get("course_name"),
                    request.get("section_name")
                });
            }
            
            JOptionPane.showMessageDialog(this, 
                "Request has been " + (approve ? "approved" : "rejected"),
                "Request Processed", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error processing request: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private int getSectionIdForRequest(int requestId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT section_id FROM irreg_enrollment_requests WHERE request_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, requestId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("section_id");
            }
            return -1;
        }
    }
    
    private JPanel createDropApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Pending drop requests table
        String[] columns = {"Select", "Student Number", "Student Name", "Program", "Course", "Reason"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        
        for (Map<String, Object> request : pendingDropRequests) {
            model.addRow(new Object[]{
                false,
                request.get("student_number"),
                request.get("student_name"),
                request.get("program_name"),
                request.get("course_code") + " - " + request.get("course_name"),
                request.get("reason")
            });
        }
        
        JTable dropRequestsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(dropRequestsTable);
        
        // Final action buttons
        JButton approveDropButton = new JButton("Final Approve Drop");
        JButton rejectDropButton = new JButton("Reject Drop");
        
        approveDropButton.addActionListener(e -> processDropRequest(dropRequestsTable, model, true));
        rejectDropButton.addActionListener(e -> processDropRequest(dropRequestsTable, model, false));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(approveDropButton);
        buttonPanel.add(rejectDropButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void processDropRequest(JTable table, DefaultTableModel model, boolean approve) {
        int selectedRow = -1;
        int dropId = -1;
        int enrollmentId = -1;
        
        // Find selected row
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((boolean) model.getValueAt(i, 0)) {
                selectedRow = i;
                dropId = (int) pendingDropRequests.get(i).get("drop_id");
                enrollmentId = (int) pendingDropRequests.get(i).get("enrollment_id");
                break;
            }
        }
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a request to process",
                "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String status = approve ? "approved" : "rejected";
            
            // Update drop request status
            String sql = "UPDATE drop_requests " +
                        "SET approved_by_dean = ?, status = ? " +
                        "WHERE drop_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, approve);
            stmt.setString(2, status);
            stmt.setInt(3, dropId);
            stmt.executeUpdate();
            
            // If rejected, re-enroll the student
            if (!approve) {
                String enrollSql = "UPDATE enrollments SET status = 'enrolled' " +
                                  "WHERE enrollment_id = ?";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, enrollmentId);
                enrollStmt.executeUpdate();
                
                // Update section count
                String sectionSql = "UPDATE sections s " +
                                   "JOIN enrollments e ON s.section_id = e.section_id " +
                                   "SET s.current_students = s.current_students + 1 " +
                                   "WHERE e.enrollment_id = ?";
                PreparedStatement sectionStmt = conn.prepareStatement(sectionSql);
                sectionStmt.setInt(1, enrollmentId);
                sectionStmt.executeUpdate();
            }
            
            // Refresh data
            loadPendingDropRequests();
            model.setRowCount(0);
            for (Map<String, Object> request : pendingDropRequests) {
                model.addRow(new Object[]{
                    false,
                    request.get("student_number"),
                    request.get("student_name"),
                    request.get("program_name"),
                    request.get("course_code") + " - " + request.get("course_name"),
                    request.get("reason")
                });
            }
            
            JOptionPane.showMessageDialog(this, 
                "Drop request has been " + (approve ? "approved" : "rejected"),
                "Request Processed", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error processing drop request: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}