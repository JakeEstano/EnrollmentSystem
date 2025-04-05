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

public class ProgramChairDashboard extends JFrame {
    private final int userId;
    private List<Map<String, Object>> classSections = new ArrayList<>();
    private List<Map<String, Object>> pendingIrregRequests = new ArrayList<>();
    private List<Map<String, Object>> pendingDropRequests = new ArrayList<>();
    
    public ProgramChairDashboard(int userId) {
        this.userId = userId;
        loadBackgroundImage();
        loadProgramChairData();
        initializeUI();
    }
    
    private void loadProgramChairData() {
        loadClassSections();
        loadPendingIrregularRequests();
        loadPendingDropRequests();
    }
    
    private void loadClassSections() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sec.section_id, c.course_code, c.course_name, " +
                        "sec.section_name, sec.schedule, sec.current_students, " +
                        "CONCAT(u.first_name, ' ', u.last_name) as faculty_name " +
                        "FROM sections sec " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "LEFT JOIN users u ON sec.faculty_id = u.user_id " +
                        "WHERE sec.semester = 1 AND sec.academic_year = '2023-2024' " +
                        "AND c.program_id = (SELECT program_id FROM users WHERE user_id = ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            classSections.clear();
            while (rs.next()) {
                Map<String, Object> section = new HashMap<>();
                section.put("section_id", rs.getInt("section_id"));
                section.put("course_code", rs.getString("course_code"));
                section.put("course_name", rs.getString("course_name"));
                section.put("section_name", rs.getString("section_name"));
                section.put("schedule", rs.getString("schedule"));
                section.put("current_students", rs.getInt("current_students"));
                section.put("faculty_name", rs.getString("faculty_name"));
                classSections.add(section);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadPendingIrregularRequests() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.request_id, s.student_id, s.student_number, " +
                        "CONCAT(u.first_name, ' ', u.last_name) as student_name, " +
                        "c.course_code, c.course_name, sec.section_name " +
                        "FROM irreg_enrollment_requests r " +
                        "JOIN students s ON r.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "JOIN sections sec ON r.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "WHERE r.status = 'pending' " +
                        "AND c.program_id = (SELECT program_id FROM users WHERE user_id = ?)";
            
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
                        "c.course_code, c.course_name, d.reason, e.enrollment_id " +
                        "FROM drop_requests d " +
                        "JOIN enrollments e ON d.enrollment_id = e.enrollment_id " +
                        "JOIN sections sec ON e.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "JOIN students s ON e.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "WHERE d.status = 'pending' " +
                        "AND c.program_id = (SELECT program_id FROM users WHERE user_id = ?)";
            
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
                pendingDropRequests.add(request);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
   private void initializeUI() {
    setTitle("BSU Enrollment System - Program Chair Dashboard");
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
    
    // 1. Class Management Tab
    tabbedPane.addTab("Class Management", createClassManagementPanel());
    
    // 2. Irregular Approvals Tab
    tabbedPane.addTab("Irregular Approvals", createIrregularApprovalsPanel());
    
    // 3. Drop Approvals Tab
    tabbedPane.addTab("Drop Approvals", createDropApprovalsPanel());
    
    // 4. Reports Tab
    tabbedPane.addTab("Reports", createReportsPanel());
    
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    add(mainPanel);
}

private Image backgroundImage;

private void loadBackgroundImage() {
    try {
        // Option 1: Try loading from resources folder
        backgroundImage = new ImageIcon(getClass().getResource("/programchairportal.png")).getImage();
    } catch (Exception e1) {
        try {
            // Option 2: Try loading from specific package in resources
            backgroundImage = new ImageIcon(getClass().getResource("/bsu_enrollment_system/programchairportal.png")).getImage();
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
                                    File.separator + "programchairportal.png").getImage();
                } catch (Exception e4) {
                    System.err.println("Could not load background image. Please check the file path.");
                    e4.printStackTrace();
                }
            }
        }
    }
}
    
    private JPanel createClassManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Class list table
        String[] columns = {"Course Code", "Course Name", "Section", "Schedule", "Students", "Faculty"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        for (Map<String, Object> section : classSections) {
            model.addRow(new Object[]{
                section.get("course_code"),
                section.get("course_name"),
                section.get("section_name"),
                section.get("schedule"),
                section.get("current_students"),
                section.get("faculty_name")
            });
        }
        
        JTable classTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(classTable);
        
        // Generate class list button
        JButton genClassListButton = new JButton("Generate Class List");
        genClassListButton.addActionListener(e -> generateClassList(classTable));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(genClassListButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void generateClassList(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a class to generate list",
                "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int sectionId = (int) classSections.get(selectedRow).get("section_id");
        
        try (Connection conn = DBConnection.getConnection()) {
            // Get class list
            String sql = "SELECT s.student_number, CONCAT(u.first_name, ' ', u.last_name) as student_name " +
                        "FROM enrollments e " +
                        "JOIN students s ON e.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "WHERE e.section_id = ? AND e.status = 'enrolled' " +
                        "ORDER BY u.last_name, u.first_name";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();
            
            // Create class list content
            StringBuilder classListContent = new StringBuilder();
            classListContent.append("BATANGAS STATE UNIVERSITY\n");
            classListContent.append("CLASS LIST\n\n");
            classListContent.append("Course: ").append(classSections.get(selectedRow).get("course_code"))
                          .append(" - ").append(classSections.get(selectedRow).get("course_name")).append("\n");
            classListContent.append("Section: ").append(classSections.get(selectedRow).get("section_name")).append("\n");
            classListContent.append("Schedule: ").append(classSections.get(selectedRow).get("schedule")).append("\n\n");
            classListContent.append("STUDENTS:\n");
            classListContent.append("----------------------------------------------------\n");
            classListContent.append("Student Number\tName\n");
            
            while (rs.next()) {
                classListContent.append(rs.getString("student_number")).append("\t")
                              .append(rs.getString("student_name")).append("\n");
            }
            
            // Save to file
            String fileName = "ClassList_" + classSections.get(selectedRow).get("course_code") + 
                            "_" + classSections.get(selectedRow).get("section_name") + ".txt";
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(classListContent.toString());
            }
            
            JOptionPane.showMessageDialog(this, 
                "Class list generated successfully: " + fileName,
                "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating class list: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createIrregularApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Pending requests table
        String[] columns = {"Select", "Student Number", "Student Name", "Course", "Section"};
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
                request.get("course_code") + " - " + request.get("course_name"),
                request.get("section_name")
            });
        }
        
        JTable requestsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        
        // Approval buttons
        JButton approveButton = new JButton("Approve Selected");
        JButton rejectButton = new JButton("Reject Selected");
        
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
                        "SET approved_by_chair = ?, status = ? " +
                        "WHERE request_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, approve);
            stmt.setString(2, approve ? "pending_dean" : "rejected");
            stmt.setInt(3, requestId);
            stmt.executeUpdate();
            
            // If approved, enroll the student
            if (approve) {
                Map<String, Object> request = pendingIrregRequests.get(selectedRow);
                int studentId = (int) request.get("student_id");
                int sectionId = getSectionIdForRequest(requestId);
                
                // Enroll student
                String enrollSql = "INSERT INTO enrollments (student_id, section_id, status) " +
                                 "VALUES (?, ?, 'enrolled')";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, studentId);
                enrollStmt.setInt(2, sectionId);
                enrollStmt.executeUpdate();
                
                // Update section count
                String updateSql = "UPDATE sections SET current_students = current_students + 1 " +
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
                    request.get("course_code") + " - " + request.get("course_name"),
                    request.get("section_name")
                });
            }
            
            JOptionPane.showMessageDialog(this, 
                "Request has been " + (approve ? "approved and forwarded to the Dean" : "rejected"),
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
        String[] columns = {"Select", "Student Number", "Student Name", "Course", "Reason"};
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
                request.get("course_code") + " - " + request.get("course_name"),
                request.get("reason")
            });
        }
        
        JTable dropRequestsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(dropRequestsTable);
        
        // Action buttons
        JButton approveDropButton = new JButton("Approve Drop");
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
            String status = approve ? "approved_chair" : "rejected";
            
            // Update drop request status
            String sql = "UPDATE drop_requests " +
                        "SET approved_by_chair = ?, status = ? " +
                        "WHERE drop_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, approve);
            stmt.setString(2, status);
            stmt.setInt(3, dropId);
            stmt.executeUpdate();
            
            // If approved, update enrollment status
            if (approve) {
                String enrollSql = "UPDATE enrollments SET status = 'dropped' " +
                                  "WHERE enrollment_id = ?";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, enrollmentId);
                enrollStmt.executeUpdate();
                
                // Update section count
                String sectionSql = "UPDATE sections s " +
                                   "JOIN enrollments e ON s.section_id = e.section_id " +
                                   "SET s.current_students = s.current_students - 1 " +
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
                    request.get("course_code") + " - " + request.get("course_name"),
                    request.get("reason")
                });
            }
            
            JOptionPane.showMessageDialog(this, 
                "Drop request has been " + (approve ? "approved and forwarded to the Dean" : "rejected"),
                "Request Processed", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error processing drop request: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Report options
        String[] reportTypes = {
            "Student Count by Status", 
            "Class Load Report", 
            "Faculty Loading",
            "Irregular Students List"
        };
        JComboBox<String> reportCombo = new JComboBox<>(reportTypes);
        
        JButton generateButton = new JButton("Generate Report");
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        
        generateButton.addActionListener(e -> {
            String selectedReport = (String) reportCombo.getSelectedItem();
            generateReport(selectedReport, reportArea);
        });
        
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Report:"));
        topPanel.add(reportCombo);
        topPanel.add(generateButton);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void generateReport(String reportType, JTextArea reportArea) {
        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder reportContent = new StringBuilder();
            reportContent.append("BATANGAS STATE UNIVERSITY\n");
            reportContent.append(reportType.toUpperCase()).append("\n\n");
            
            if (reportType.equals("Student Count by Status")) {
                // Count regular vs irregular students
                String sql = "SELECT status, COUNT(*) as count FROM students " +
                            "WHERE program_id = (SELECT program_id FROM users WHERE user_id = ?) " +
                            "GROUP BY status";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                reportContent.append("Student Status Count:\n");
                reportContent.append("----------------------\n");
                
                while (rs.next()) {
                    reportContent.append(rs.getString("status").toUpperCase()).append(": ")
                                 .append(rs.getInt("count")).append("\n");
                }
                
            } else if (reportType.equals("Class Load Report")) {
                // Class load information
                String sql = "SELECT c.course_code, c.course_name, sec.section_name, " +
                            "sec.current_students, sec.max_students " +
                            "FROM sections sec " +
                            "JOIN courses c ON sec.course_id = c.course_id " +
                            "WHERE sec.semester = 1 AND sec.academic_year = '2023-2024' " +
                            "AND c.program_id = (SELECT program_id FROM users WHERE user_id = ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                reportContent.append("Class Load Report:\n");
                reportContent.append("----------------------------------------------------\n");
                reportContent.append("Course\t\tSection\tEnrolled/Max\tPercentage\n");
                
                while (rs.next()) {
                    int current = rs.getInt("current_students");
                    int max = rs.getInt("max_students");
                    double percentage = (double) current / max * 100;
                    
                    reportContent.append(rs.getString("course_code")).append(" - ")
                                 .append(rs.getString("course_name")).append("\t")
                                 .append(rs.getString("section_name")).append("\t")
                                 .append(current).append("/").append(max).append("\t")
                                 .append(String.format("%.1f%%", percentage)).append("\n");
                }
                
            } else if (reportType.equals("Faculty Loading")) {
                // Faculty loading
                String sql = "SELECT CONCAT(u.first_name, ' ', u.last_name) as faculty_name, " +
                            "SUM(c.units) as total_units, COUNT(sec.section_id) as num_classes " +
                            "FROM sections sec " +
                            "JOIN courses c ON sec.course_id = c.course_id " +
                            "JOIN users u ON sec.faculty_id = u.user_id " +
                            "WHERE sec.semester = 1 AND sec.academic_year = '2023-2024' " +
                            "AND c.program_id = (SELECT program_id FROM users WHERE user_id = ?) " +
                            "GROUP BY u.user_id";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                reportContent.append("Faculty Loading Report:\n");
                reportContent.append("----------------------------------------------------\n");
                reportContent.append("Faculty Name\t\tClasses\tTotal Units\n");
                
                while (rs.next()) {
                    reportContent.append(rs.getString("faculty_name")).append("\t\t")
                                 .append(rs.getInt("num_classes")).append("\t")
                                 .append(rs.getDouble("total_units")).append("\n");
                }
                
            } else if (reportType.equals("Irregular Students List")) {
                // Irregular students
                String sql = "SELECT s.student_number, CONCAT(u.first_name, ' ', u.last_name) as student_name " +
                            "FROM students s " +
                            "JOIN users u ON s.user_id = u.user_id " +
                            "WHERE s.status = 'irregular' " +
                            "AND s.program_id = (SELECT program_id FROM users WHERE user_id = ?) " +
                            "ORDER BY u.last_name, u.first_name";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                reportContent.append("Irregular Students List:\n");
                reportContent.append("----------------------------------------------------\n");
                reportContent.append("Student Number\tName\n");
                
                while (rs.next()) {
                    reportContent.append(rs.getString("student_number")).append("\t")
                                 .append(rs.getString("student_name")).append("\n");
                }
            }
            
            reportArea.setText(reportContent.toString());
            
        } catch (SQLException ex) {
            reportArea.setText("Error generating report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}