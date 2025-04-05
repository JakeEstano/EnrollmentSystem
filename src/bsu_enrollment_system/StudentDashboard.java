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

public class StudentDashboard extends JFrame {
    private final int userId;
    private final int studentId;
    private JTabbedPane tabbedPane;
    private List<Map<String, Object>> availableCourses = new ArrayList<>();
    private List<Map<String, Object>> currentEnrollments = new ArrayList<>();
    private List<Map<String, Object>> gradeRecords = new ArrayList<>();
    
    public StudentDashboard(int userId, int studentId) {
        this.userId = userId;
        this.studentId = studentId;
        loadBackgroundImage();
        loadStudentData();
        initializeUI();
    }
    
    private void loadStudentData() {
        loadAvailableCourses();
        loadCurrentEnrollments();
        loadGradeRecords();
    }
    
    private void loadAvailableCourses() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.course_id, c.course_code, c.course_name, c.units, " +
                        "sec.section_name, sec.schedule, sec.section_id " +
                        "FROM courses c " +
                        "JOIN sections sec ON c.course_id = sec.course_id " +
                        "WHERE sec.semester = 1 AND sec.academic_year = '2023-2024' " +
                        "AND sec.current_students < sec.max_students";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            availableCourses.clear();
            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("course_id", rs.getInt("course_id"));
                course.put("course_code", rs.getString("course_code"));
                course.put("course_name", rs.getString("course_name"));
                course.put("units", rs.getDouble("units"));
                course.put("section_name", rs.getString("section_name"));
                course.put("schedule", rs.getString("schedule"));
                course.put("section_id", rs.getInt("section_id"));
                
                // Check prerequisites
                if (!checkPrerequisites(rs.getInt("course_id"))) {
                    course.put("has_prereq", false);
                } else {
                    course.put("has_prereq", true);
                }
                
                availableCourses.add(course);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private boolean checkPrerequisites(int courseId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            // Check if course has prerequisites
            String prereqSql = "SELECT COUNT(*) as count FROM prerequisites WHERE course_id = ?";
            PreparedStatement prereqStmt = conn.prepareStatement(prereqSql);
            prereqStmt.setInt(1, courseId);
            ResultSet prereqRs = prereqStmt.executeQuery();
            
            if (prereqRs.next() && prereqRs.getInt("count") == 0) {
                return true; // No prerequisites
            }
            
            // Get all prerequisites for the course
            String sql = "SELECT r.course_id, r.course_code, r.course_name " +
                        "FROM prerequisites p " +
                        "JOIN courses r ON p.required_course_id = r.course_id " +
                        "WHERE p.course_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            
            // Check if student has passed all prerequisites
            while (rs.next()) {
                int requiredCourseId = rs.getInt("course_id");
                
                String passedSql = "SELECT COUNT(*) as count FROM enrollments e " +
                                 "JOIN sections s ON e.section_id = s.section_id " +
                                 "WHERE e.student_id = ? AND s.course_id = ? " +
                                 "AND e.status = 'completed' AND e.grade <= 3.0";
                
                PreparedStatement passedStmt = conn.prepareStatement(passedSql);
                passedStmt.setInt(1, studentId);
                passedStmt.setInt(2, requiredCourseId);
                ResultSet passedRs = passedStmt.executeQuery();
                
                if (passedRs.next() && passedRs.getInt("count") == 0) {
                    return false; // Prerequisite not met
                }
            }
            return true;
        }
    }
    
    private void loadCurrentEnrollments() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT e.enrollment_id, c.course_code, c.course_name, " +
                        "sec.section_name, sec.schedule, e.status " +
                        "FROM enrollments e " +
                        "JOIN sections sec ON e.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "WHERE e.student_id = ? AND e.status = 'enrolled'";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            currentEnrollments.clear();
            while (rs.next()) {
                Map<String, Object> enrollment = new HashMap<>();
                enrollment.put("enrollment_id", rs.getInt("enrollment_id"));
                enrollment.put("course_code", rs.getString("course_code"));
                enrollment.put("course_name", rs.getString("course_name"));
                enrollment.put("section_name", rs.getString("section_name"));
                enrollment.put("schedule", rs.getString("schedule"));
                currentEnrollments.add(enrollment);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadGradeRecords() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.course_code, c.course_name, c.units, " +
                        "e.grade, sec.semester, sec.academic_year " +
                        "FROM enrollments e " +
                        "JOIN sections sec ON e.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "WHERE e.student_id = ? AND e.status = 'completed'";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            gradeRecords.clear();
            while (rs.next()) {
                Map<String, Object> grade = new HashMap<>();
                grade.put("course_code", rs.getString("course_code"));
                grade.put("course_name", rs.getString("course_name"));
                grade.put("units", rs.getDouble("units"));
                grade.put("grade", rs.getDouble("grade"));
                grade.put("semester", rs.getInt("semester"));
                grade.put("academic_year", rs.getString("academic_year"));
                gradeRecords.add(grade);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    
    private Image backgroundImage;

    private void loadBackgroundImage() {
    try {
        // Option 1: Try loading from resources folder
        backgroundImage = new ImageIcon(getClass().getResource("/studentdash.png")).getImage();
    } catch (Exception e1) {
        try {
            // Option 2: Try loading from specific package in resources
            backgroundImage = new ImageIcon(getClass().getResource("/bsu_enrollment_system/studentdash.png")).getImage();
        } catch (Exception e2) {
            try {
                // Option 3: Try loading from project directory
                backgroundImage = new ImageIcon("studentdash.png").getImage();
            } catch (Exception e3) {
                try {
                    // Option 4: Try loading from absolute path
                    String userDir = System.getProperty("user.dir");
                    backgroundImage = new ImageIcon(userDir + File.separator + "src" + 
                                     File.separator + "bsu_enrollment_system" + 
                                     File.separator + "studentdash.png").getImage();
                } catch (Exception e4) {
                    System.err.println("Could not load background image. Please check the file path.");
                    e4.printStackTrace();
                }
            }
        }
    }
}
    
    private void initializeUI() {
    setTitle("BSU Enrollment System - Student Dashboard");
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
    tabbedPane = new JTabbedPane();
    
    // 1. Enrollment Tab
    tabbedPane.addTab("Enrollment", createEnrollmentPanel());
    
    // 2. Documents Tab
    tabbedPane.addTab("Documents", createDocumentsPanel());
    
    // 3. Grades Tab
    tabbedPane.addTab("Grades", createGradesPanel());
    
    // 4. Drop Requests Tab
    tabbedPane.addTab("Drop Requests", createDropRequestsPanel());
    
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    add(mainPanel);
}
    
    private JPanel createEnrollmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Check student status
        boolean isRegular = checkStudentStatus();
        
        if (isRegular) {
            // Regular enrollment process
            JPanel regularPanel = new JPanel(new BorderLayout());
            
            // Available courses table
            String[] columns = {"Select", "Course Code", "Course Name", "Units", "Section", "Schedule", "Prerequisites"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : String.class;
                }
            };
            
            for (Map<String, Object> course : availableCourses) {
                String prereqStatus = (boolean) course.get("has_prereq") ? "Met" : "Not Met";
                model.addRow(new Object[]{
                    false,
                    course.get("course_code"),
                    course.get("course_name"),
                    course.get("units"),
                    course.get("section_name"),
                    course.get("schedule"),
                    prereqStatus
                });
            }
            
            JTable coursesTable = new JTable(model);
            coursesTable.setFillsViewportHeight(true);
            JScrollPane scrollPane = new JScrollPane(coursesTable);
            
            // Enrollment button
            JButton enrollButton = new JButton("Enroll Selected Courses");
            enrollButton.addActionListener(e -> enrollCourses(coursesTable, model));
            
            regularPanel.add(scrollPane, BorderLayout.CENTER);
            regularPanel.add(enrollButton, BorderLayout.SOUTH);
            
            panel.add(regularPanel, BorderLayout.CENTER);
        } else {
            // Irregular enrollment process
            JPanel irregPanel = new JPanel(new BorderLayout());
            
            JLabel infoLabel = new JLabel("<html><center>As an irregular student, you need to submit " +
                    "a Program of Study, Curriculum, and Proposal Slip for approval.</center></html>");
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Document upload components
            JButton uploadPosButton = new JButton("Upload Program of Study");
            JButton uploadCurrButton = new JButton("Upload Curriculum");
            JButton uploadPropButton = new JButton("Upload Proposal Slip");
            
            // Course selection table
            String[] irregColumns = {"Select", "Course Code", "Course Name", "Units", "Section", "Schedule", "Prerequisites"};
            DefaultTableModel irregModel = new DefaultTableModel(irregColumns, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : String.class;
                }
            };
            
            for (Map<String, Object> course : availableCourses) {
                String prereqStatus = (boolean) course.get("has_prereq") ? "Met" : "Not Met";
                irregModel.addRow(new Object[]{
                    false,
                    course.get("course_code"),
                    course.get("course_name"),
                    course.get("units"),
                    course.get("section_name"),
                    course.get("schedule"),
                    prereqStatus
                });
            }
            
            JTable irregCoursesTable = new JTable(irregModel);
            irregCoursesTable.setFillsViewportHeight(true);
            JScrollPane irregScrollPane = new JScrollPane(irregCoursesTable);
            
            // Submit request button
            JButton submitRequestButton = new JButton("Submit Enrollment Request");
            submitRequestButton.addActionListener(e -> submitIrregularRequest(irregCoursesTable, irregModel));
            
            // Layout
            JPanel uploadPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            uploadPanel.add(uploadPosButton);
            uploadPanel.add(uploadCurrButton);
            uploadPanel.add(uploadPropButton);
            
            JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
            centerPanel.add(infoLabel, BorderLayout.NORTH);
            centerPanel.add(uploadPanel, BorderLayout.CENTER);
            centerPanel.add(irregScrollPane, BorderLayout.SOUTH);
            
            irregPanel.add(centerPanel, BorderLayout.CENTER);
            irregPanel.add(submitRequestButton, BorderLayout.SOUTH);
            
            panel.add(irregPanel, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    private boolean checkStudentStatus() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT status FROM students WHERE student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status").equals("regular");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    private void enrollCourses(JTable table, DefaultTableModel model) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            List<Integer> selectedSections = new ArrayList<>();
            
            // Check selected courses
            for (int i = 0; i < model.getRowCount(); i++) {
                if ((boolean) model.getValueAt(i, 0)) {
                    int sectionId = (int) availableCourses.get(i).get("section_id");
                    selectedSections.add(sectionId);
                    
                    // Check prerequisites again
                    int courseId = (int) availableCourses.get(i).get("course_id");
                    if (!checkPrerequisites(courseId)) {
                        JOptionPane.showMessageDialog(this, 
                            "You don't meet the prerequisites for " + 
                            availableCourses.get(i).get("course_code") + " - " + 
                            availableCourses.get(i).get("course_name"),
                            "Enrollment Error", JOptionPane.ERROR_MESSAGE);
                        conn.rollback();
                        return;
                    }
                }
            }
            
            if (selectedSections.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please select at least one course to enroll",
                    "Enrollment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Enroll in selected courses
            for (int sectionId : selectedSections) {
                String enrollSql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'enrolled')";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, studentId);
                enrollStmt.setInt(2, sectionId);
                enrollStmt.executeUpdate();
                
                // Update section count
                String updateSql = "UPDATE sections SET current_students = current_students + 1 WHERE section_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, sectionId);
                updateStmt.executeUpdate();
            }
            
            conn.commit();
            
            // Generate COR
            generateCOR();
            
            // Refresh data
            loadStudentData();
            
            JOptionPane.showMessageDialog(this, 
                "Enrollment successful! Your COR has been generated.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error during enrollment: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void submitIrregularRequest(JTable table, DefaultTableModel model) {
        List<Integer> selectedSections = new ArrayList<>();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((boolean) model.getValueAt(i, 0)) {
                int sectionId = (int) availableCourses.get(i).get("section_id");
                selectedSections.add(sectionId);
            }
        }
        
        if (selectedSections.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one course for your request",
                "Request Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            // Create irregular enrollment request
            for (int sectionId : selectedSections) {
                String sql = "INSERT INTO irreg_enrollment_requests " +
                           "(student_id, section_id, status) VALUES (?, ?, 'pending')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                stmt.setInt(2, sectionId);
                stmt.executeUpdate();
            }
            
            JOptionPane.showMessageDialog(this, 
                "Your enrollment request has been submitted for approval.", 
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error submitting request: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void generateCOR() {
        try (Connection conn = DBConnection.getConnection()) {
            // Get student info
            String studentName = "";
            String studentNumber = "";
            String program = "";
            
            String sql = "SELECT s.student_number, u.first_name, u.last_name, p.program_name " +
                       "FROM students s " +
                       "JOIN users u ON s.user_id = u.user_id " +
                       "JOIN programs p ON u.program_id = p.program_id " +
                       "WHERE s.student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                studentName = rs.getString("first_name") + " " + rs.getString("last_name");
                studentNumber = rs.getString("student_number");
                program = rs.getString("program_name");
            }
            
            // Get enrolled courses for this semester
            String coursesSql = "SELECT c.course_code, c.course_name, c.units, sec.section_name, sec.schedule " +
                              "FROM enrollments e " +
                              "JOIN sections sec ON e.section_id = sec.section_id " +
                              "JOIN courses c ON sec.course_id = c.course_id " +
                              "WHERE e.student_id = ? AND e.status = 'enrolled' " +
                              "AND sec.semester = 1 AND sec.academic_year = '2023-2024'";
            
            PreparedStatement coursesStmt = conn.prepareStatement(coursesSql);
            coursesStmt.setInt(1, studentId);
            ResultSet coursesRs = coursesStmt.executeQuery();
            
            // Create COR content
            StringBuilder corContent = new StringBuilder();
            corContent.append("BATANGAS STATE UNIVERSITY\n");
            corContent.append("CERTIFICATE OF REGISTRATION\n\n");
            corContent.append("Student Name: ").append(studentName).append("\n");
            corContent.append("Student Number: ").append(studentNumber).append("\n");
            corContent.append("Program: ").append(program).append("\n");
            corContent.append("Semester: 1\n");
            corContent.append("Academic Year: 2023-2024\n\n");
            corContent.append("ENROLLED COURSES:\n");
            corContent.append("----------------------------------------------------\n");
            corContent.append("Code\tName\t\tUnits\tSection\tSchedule\n");
            
            while (coursesRs.next()) {
                corContent.append(coursesRs.getString("course_code")).append("\t")
                         .append(coursesRs.getString("course_name")).append("\t")
                         .append(coursesRs.getDouble("units")).append("\t")
                         .append(coursesRs.getString("section_name")).append("\t")
                         .append(coursesRs.getString("schedule")).append("\n");
            }
            
            // Save to file
            String fileName = "COR_" + studentNumber + "_1_2023-2024.txt";
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(corContent.toString());
            }
            
            // Save document record
            String docSql = "INSERT INTO documents " +
                          "(document_name, document_type, student_id, semester, academic_year, file_path) " +
                          "VALUES (?, 'cor', ?, 1, '2023-2024', ?)";
            PreparedStatement docStmt = conn.prepareStatement(docSql);
            docStmt.setString(1, "Certificate of Registration");
            docStmt.setInt(2, studentId);
            docStmt.setString(3, fileName);
            docStmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, 
                "COR generated successfully: " + fileName,
                "Document Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating COR: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Document generation buttons
        JButton genCorButton = new JButton("Generate COR");
        JButton genGradesButton = new JButton("Generate Grades Report");
        JButton genPosButton = new JButton("Generate Program of Study");
        JButton genCurrButton = new JButton("Generate Curriculum");
        
        genCorButton.addActionListener(e -> generateCOR());
        genGradesButton.addActionListener(e -> generateGradesReport());
        genPosButton.addActionListener(e -> generateProgramOfStudy());
        genCurrButton.addActionListener(e -> generateCurriculum());
        
        // Layout
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.add(genCorButton);
        buttonPanel.add(genGradesButton);
        buttonPanel.add(genPosButton);
        buttonPanel.add(genCurrButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void generateGradesReport() {
        try (Connection conn = DBConnection.getConnection()) {
            // Get student info
            String studentName = "";
            String studentNumber = "";
            
            String infoSql = "SELECT s.student_number, u.first_name, u.last_name " +
                           "FROM students s JOIN users u ON s.user_id = u.user_id " +
                           "WHERE s.student_id = ?";
            PreparedStatement infoStmt = conn.prepareStatement(infoSql);
            infoStmt.setInt(1, studentId);
            
            ResultSet infoRs = infoStmt.executeQuery();
            if (infoRs.next()) {
                studentName = infoRs.getString("first_name") + " " + infoRs.getString("last_name");
                studentNumber = infoRs.getString("student_number");
            }
            
            // Calculate GWA
            double gwa = calculateGWA();
            
            // Create grades report content
            StringBuilder reportContent = new StringBuilder();
            reportContent.append("BATANGAS STATE UNIVERSITY\n");
            reportContent.append("OFFICIAL GRADE REPORT\n\n");
            reportContent.append("Student Name: ").append(studentName).append("\n");
            reportContent.append("Student Number: ").append(studentNumber).append("\n\n");
            reportContent.append("GRADES:\n");
            reportContent.append("----------------------------------------------------\n");
            reportContent.append("Semester\tCourse\t\tUnits\tGrade\n");
            
            for (Map<String, Object> grade : gradeRecords) {
                reportContent.append(grade.get("semester")).append("\t\t")
                             .append(grade.get("course_code")).append("\t")
                             .append(grade.get("units")).append("\t")
                             .append(grade.get("grade")).append("\n");
            }
            
            reportContent.append("\nGENERAL WEIGHTED AVERAGE: ").append(String.format("%.2f", gwa)).append("\n");
            
            // Save to file
            String fileName = "Grades_" + studentNumber + ".txt";
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(reportContent.toString());
            }
            
            // Save document record
            String docSql = "INSERT INTO documents " +
                          "(document_name, document_type, student_id, file_path) " +
                          "VALUES (?, 'grades', ?, ?)";
            PreparedStatement docStmt = conn.prepareStatement(docSql);
            docStmt.setString(1, "Grade Report");
            docStmt.setInt(2, studentId);
            docStmt.setString(3, fileName);
            docStmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, 
                String.format("Grades report generated! Your GWA: %.2f\nFile: %s", gwa, fileName),
                "Document Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating grades report: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private double calculateGWA() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT SUM(c.units * e.grade) / SUM(c.units) as gwa " +
                        "FROM enrollments e JOIN sections s ON e.section_id = s.section_id " +
                        "JOIN courses c ON s.course_id = c.course_id " +
                        "WHERE e.student_id = ? AND e.status = 'completed'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("gwa");
            }
            return 0.0;
        }
    }
    
    private void generateProgramOfStudy() {
        try (Connection conn = DBConnection.getConnection()) {
            // Get student info
            String studentName = "";
            String studentNumber = "";
            String program = "";
            
            String sql = "SELECT s.student_number, u.first_name, u.last_name, p.program_name " +
                       "FROM students s " +
                       "JOIN users u ON s.user_id = u.user_id " +
                       "JOIN programs p ON u.program_id = p.program_id " +
                       "WHERE s.student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                studentName = rs.getString("first_name") + " " + rs.getString("last_name");
                studentNumber = rs.getString("student_number");
                program = rs.getString("program_name");
            }
            
            // Get curriculum courses
            String coursesSql = "SELECT c.course_code, c.course_name, c.units, c.year_level, c.semester " +
                              "FROM courses c " +
                              "WHERE c.program_id = (SELECT program_id FROM users WHERE user_id = ?) " +
                              "ORDER BY c.year_level, c.semester, c.course_code";
            
            PreparedStatement coursesStmt = conn.prepareStatement(coursesSql);
            coursesStmt.setInt(1, userId);
            ResultSet coursesRs = coursesStmt.executeQuery();
            
            // Create POS content
            StringBuilder posContent = new StringBuilder();
            posContent.append("BATANGAS STATE UNIVERSITY\n");
            posContent.append("PROGRAM OF STUDY\n\n");
            posContent.append("Student Name: ").append(studentName).append("\n");
            posContent.append("Student Number: ").append(studentNumber).append("\n");
            posContent.append("Program: ").append(program).append("\n\n");
            posContent.append("CURRICULUM:\n");
            posContent.append("----------------------------------------------------\n");
            posContent.append("Year\tSem\tCode\tName\t\tUnits\n");
            
            while (coursesRs.next()) {
                posContent.append(coursesRs.getInt("year_level")).append("\t")
                         .append(coursesRs.getInt("semester")).append("\t")
                         .append(coursesRs.getString("course_code")).append("\t")
                         .append(coursesRs.getString("course_name")).append("\t")
                         .append(coursesRs.getDouble("units")).append("\n");
            }
            
            // Save to file
            String fileName = "POS_" + studentNumber + ".txt";
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(posContent.toString());
            }
            
            // Save document record
            String docSql = "INSERT INTO documents " +
                          "(document_name, document_type, student_id, file_path) " +
                          "VALUES (?, 'pos', ?, ?)";
            PreparedStatement docStmt = conn.prepareStatement(docSql);
            docStmt.setString(1, "Program of Study");
            docStmt.setInt(2, studentId);
            docStmt.setString(3, fileName);
            docStmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, 
                "Program of Study generated successfully: " + fileName,
                "Document Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating Program of Study: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void generateCurriculum() {
        try (Connection conn = DBConnection.getConnection()) {
            // Get program info
            String programName = "";
            int programId = 0;
            
            String sql = "SELECT p.program_id, p.program_name " +
                       "FROM programs p " +
                       "JOIN users u ON p.program_id = u.program_id " +
                       "WHERE u.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                programId = rs.getInt("program_id");
                programName = rs.getString("program_name");
            }
            
            // Get curriculum courses
            String coursesSql = "SELECT c.course_code, c.course_name, c.units, c.year_level, c.semester " +
                              "FROM courses c " +
                              "WHERE c.program_id = ? " +
                              "ORDER BY c.year_level, c.semester, c.course_code";
            
            PreparedStatement coursesStmt = conn.prepareStatement(coursesSql);
            coursesStmt.setInt(1, programId);
            ResultSet coursesRs = coursesStmt.executeQuery();
            
            // Create curriculum content
            StringBuilder currContent = new StringBuilder();
            currContent.append("BATANGAS STATE UNIVERSITY\n");
            currContent.append("PROGRAM CURRICULUM\n\n");
            currContent.append("Program: ").append(programName).append("\n\n");
            currContent.append("COURSES:\n");
            currContent.append("----------------------------------------------------\n");
            currContent.append("Year\tSem\tCode\tName\t\tUnits\n");
            
            while (coursesRs.next()) {
                currContent.append(coursesRs.getInt("year_level")).append("\t")
                          .append(coursesRs.getInt("semester")).append("\t")
                          .append(coursesRs.getString("course_code")).append("\t")
                          .append(coursesRs.getString("course_name")).append("\t")
                          .append(coursesRs.getDouble("units")).append("\n");
            }
            
            // Save to file
            String fileName = "Curriculum_" + programName.replace(" ", "_") + ".txt";
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(currContent.toString());
            }
            
            // Save document record
            String docSql = "INSERT INTO documents " +
                          "(document_name, document_type, student_id, file_path) " +
                          "VALUES (?, 'curriculum', ?, ?)";
            PreparedStatement docStmt = conn.prepareStatement(docSql);
            docStmt.setString(1, "Curriculum");
            docStmt.setInt(2, studentId);
            docStmt.setString(3, fileName);
            docStmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, 
                "Curriculum generated successfully: " + fileName,
                "Document Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating Curriculum: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Grades table
        String[] columns = {"Course Code", "Course Name", "Units", "Grade", "Semester", "Academic Year"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        for (Map<String, Object> grade : gradeRecords) {
            model.addRow(new Object[]{
                grade.get("course_code"),
                grade.get("course_name"),
                grade.get("units"),
                grade.get("grade"),
                grade.get("semester"),
                grade.get("academic_year")
            });
        }
        
        JTable gradesTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        
        // GWA display
        try {
            double gwa = calculateGWA();
            JLabel gwaLabel = new JLabel(String.format("General Weighted Average: %.2f", gwa));
            gwaLabel.setFont(new Font("Arial", Font.BOLD, 14));
            gwaLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JPanel gwaPanel = new JPanel();
            gwaPanel.add(gwaLabel);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(gwaPanel, BorderLayout.SOUTH);
        } catch (SQLException ex) {
            panel.add(new JLabel("Error loading grades"), BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    private JPanel createDropRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Current enrollments table
        String[] columns = {"Select", "Course Code", "Course Name", "Section", "Schedule"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        
        for (Map<String, Object> enrollment : currentEnrollments) {
            model.addRow(new Object[]{
                false,
                enrollment.get("course_code"),
                enrollment.get("course_name"),
                enrollment.get("section_name"),
                enrollment.get("schedule")
            });
        }
        
        JTable enrollmentsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        
        // Drop button
        JButton dropButton = new JButton("Request to Drop Selected Course");
        dropButton.addActionListener(e -> requestDrop(enrollmentsTable, model));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(dropButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void requestDrop(JTable table, DefaultTableModel model) {
        int selectedRow = -1;
        int enrollmentId = -1;
        
        // Find selected row
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((boolean) model.getValueAt(i, 0)) {
                selectedRow = i;
                enrollmentId = (int) currentEnrollments.get(i).get("enrollment_id");
                break;
            }
        }
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a course to drop",
                "Drop Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, "Enter reason for dropping:");
        if (reason != null && !reason.isEmpty()) {
            try (Connection conn = DBConnection.getConnection()) {
                // Create drop request
                String sql = "INSERT INTO drop_requests " +
                            "(enrollment_id, reason, status) " +
                            "VALUES (?, ?, 'pending')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, enrollmentId);
                stmt.setString(2, reason);
                stmt.executeUpdate();
                
                // Generate drop form
                generateDropForm(enrollmentId, reason);
                
                // Refresh data
                loadCurrentEnrollments();
                
                JOptionPane.showMessageDialog(this, 
                    "Drop request submitted successfully! Drop form generated.",
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error submitting drop request: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void generateDropForm(int enrollmentId, String reason) {
        try (Connection conn = DBConnection.getConnection()) {
            // Get enrollment info
            String studentName = "";
            String studentNumber = "";
            String courseInfo = "";
            
            String sql = "SELECT s.student_number, u.first_name, u.last_name, " +
                        "c.course_code, c.course_name " +
                        "FROM enrollments e " +
                        "JOIN students s ON e.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "JOIN sections sec ON e.section_id = sec.section_id " +
                        "JOIN courses c ON sec.course_id = c.course_id " +
                        "WHERE e.enrollment_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                studentName = rs.getString("first_name") + " " + rs.getString("last_name");
                studentNumber = rs.getString("student_number");
                courseInfo = rs.getString("course_code") + " - " + rs.getString("course_name");
            }
            
            // Create drop form content
            StringBuilder formContent = new StringBuilder();
            formContent.append("BATANGAS STATE UNIVERSITY\n");
            formContent.append("COURSE DROP REQUEST FORM\n\n");
            formContent.append("Student Name: ").append(studentName).append("\n");
            formContent.append("Student Number: ").append(studentNumber).append("\n");
            formContent.append("Course to Drop: ").append(courseInfo).append("\n");
            formContent.append("Reason for Dropping: ").append(reason).append("\n\n");
            formContent.append("----------------------------------------------------\n");
            formContent.append("FOR OFFICE USE ONLY\n\n");
            formContent.append("Program Chair Approval: __________ Date: _______\n");
            formContent.append("Dean Approval: __________ Date: _______\n");
            
            // Save to file
            String fileName = "DropForm_" + studentNumber + "_" + enrollmentId + ".txt";
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(formContent.toString());
            }
            
            // Save document record
            String docSql = "INSERT INTO documents " +
                          "(document_name, document_type, student_id, file_path) " +
                          "VALUES (?, 'drop_form', ?, ?)";
            PreparedStatement docStmt = conn.prepareStatement(docSql);
            docStmt.setString(1, "Drop Form");
            docStmt.setInt(2, studentId);
            docStmt.setString(3, fileName);
            docStmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, 
                "Drop form generated successfully: " + fileName,
                "Document Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating drop form: " + ex.getMessage(),
                "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}