package src.main.java.WolfBooks.dao;

import src.main.java.WolfBooks.models.EnrollmentModel;
import src.main.java.WolfBooks.models.UserModel;
import src.main.java.WolfBooks.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    // Create a new enrollment
    public boolean createEnrollment(EnrollmentModel enrollment) {
        String sql = "INSERT INTO Enrollments (user_id, course_id, user_status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, enrollment.getUserId());
            pstmt.setString(2, enrollment.getCourseId());
            pstmt.setString(3, enrollment.getUserStatus());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve an enrollment by User ID and Course ID
    public EnrollmentModel getEnrollmentById(String userId, String courseId) {
        String sql = "SELECT * FROM Enrollments WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractEnrollmentFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EnrollmentModel> getStudentEnrollments(UserModel student) {
        final String enrolled = "enrolled";
        String sql = "SELECT * FROM Enrollments WHERE user_id = ? AND user_status = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, student.getUserId());
            pstmt.setString(2, enrolled);
            ResultSet rs = pstmt.executeQuery();
            List<EnrollmentModel> enrollments = new ArrayList<>();
            while (rs.next()) {
                enrollments.add(extractEnrollmentFromResultSet(rs));
            }
            return enrollments;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Get list of enrolled students for a course
    public List<UserModel> getEnrolledStudents(String courseId) {
        List<UserModel> students = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u " +
                    "JOIN Enrollments e ON u.user_id = e.user_id " +
                    "WHERE e.course_id = ? AND e.user_status = 'enrolled'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                students.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // Get worklist (pending enrollments) for a course
    public List<UserModel> getWorklistForCourse(String courseId) {
        List<UserModel> pendingStudents = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u " +
                    "JOIN Enrollments e ON u.user_id = e.user_id " +
                    "WHERE e.course_id = ? AND e.user_status = 'Pending'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                pendingStudents.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingStudents;

    }

    // Retrieve all enrollments for a specific course
    public List<EnrollmentModel> getEnrollmentsByCourse(String courseId) {
        List<EnrollmentModel> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM Enrollments WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                enrollments.add(extractEnrollmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    // Update an existing enrollment's status
    public boolean updateEnrollmentStatus(String userId, String courseId, String newStatus) {
        if (!checkCourseCapacity(courseId) && "Approved".equals(newStatus)) {
            return false; // Cannot approve if course is at capacity
        }

        String sql = "UPDATE Enrollments SET user_status = ? WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, userId);
            pstmt.setString(3, courseId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an existing enrollment
    public boolean deleteEnrollment(String userId, String courseId) {
        String sql = "DELETE FROM Enrollments WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, courseId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check course capacity
    private boolean checkCourseCapacity(String courseId) {
        String sql = "SELECT COUNT(*) as enrolled, c.capacity " +
                    "FROM Enrollments e " +
                    "JOIN Courses c ON e.course_id = c.course_id " +
                    "WHERE e.course_id = ? AND e.user_status = 'Approved' " +
                    "GROUP BY c.capacity";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int enrolled = rs.getInt("enrolled");
                int capacity = rs.getInt("capacity");
                return enrolled < capacity;
            }
            return true; // If no enrollments yet
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Utility method to extract EnrollmentModel from ResultSet
    private EnrollmentModel extractEnrollmentFromResultSet(ResultSet rs) throws SQLException {
        return new EnrollmentModel(
            rs.getString("user_id"),
            rs.getString("course_id"),
            rs.getString("user_status")
        );
    }

    // Utility method to extract UserModel from ResultSet
    private UserModel extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new UserModel(
            rs.getString("user_id"),
            rs.getString("firstname"),
            rs.getString("lastname"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("user_role"),
            rs.getBoolean("first_login")
        );
    }
}
