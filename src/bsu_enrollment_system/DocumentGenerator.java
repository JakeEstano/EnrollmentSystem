package bsu_enrollment_system;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocumentGenerator {
    public static void generateCOR(int studentId, int semester, String academicYear) {
        try {
            // Get student info
            String studentName = "";
            String studentNumber = "";
            String program = "";
            
            try (Connection conn = DBConnection.getConnection()) {
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
            }
            
            // Get enrolled courses
            // This would be populated from database
            
            // Create PDF document
            Document document = new Document();
            String fileName = "COR_" + studentNumber + "_" + semester + "_" + academicYear + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            
            document.open();
            
            // Add BSU header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLUE);
            Paragraph header = new Paragraph("BATANGAS STATE UNIVERSITY", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph subHeader = new Paragraph("Certificate of Registration", subHeaderFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);
            
            // Add student info
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Student Name: " + studentName));
            document.add(new Paragraph("Student Number: " + studentNumber));
            document.add(new Paragraph("Program: " + program));
            document.add(new Paragraph("Semester: " + semester));
            document.add(new Paragraph("Academic Year: " + academicYear));
            
            // Add enrolled courses table
            // This would be populated with actual courses
            
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            
            document.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void generateGradesReport(int studentId) {
        // Similar implementation for grades report
    }
    
    public static void generateDropForm(int studentId, int enrollmentId, String reason) {
        // Similar implementation for drop form
    }
    
    public static void generateProgramOfStudy(int studentId) {
        // Similar implementation for POS
    }
    
    public static void generateCurriculum(int programId) {
        // Similar implementation for curriculum
    }
}