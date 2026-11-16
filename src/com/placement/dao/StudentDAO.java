package com.placement.dao;

import com.placement.config.DBConnection;
import com.placement.exception.DatabaseOperationException;
import com.placement.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Student entity.
 * Handles all CRUD operations using PreparedStatements and strict try-with-resources blocks.
 */
public class StudentDAO {

    private final DBConnection dbConnection;

    public StudentDAO() {
        this.dbConnection = DBConnection.getInstance();
    }

    /**
     * Persists a new Student record and retrieves the auto-generated student_id.
     */
    public Student save(Student student) {
        String sql = "INSERT INTO students (roll_number, first_name, last_name, email, department, " +
                     "cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getDepartment());
            ps.setDouble(6, student.getCgpa());
            ps.setDouble(7, student.getSscPercent());
            ps.setDouble(8, student.getHscPercent());
            ps.setBoolean(9, student.isHasBacklog());
            ps.setInt(10, student.getActiveBacklogs());
            ps.setString(11, student.getPhone());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating student failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setStudentId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseOperationException("Creating student failed, no generated ID returned.");
                }
            }

            return student;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while saving student: " + student.getRollNumber(), e);
        }
    }

    /**
     * Updates an existing student record.
     */
    public boolean update(Student student) {
        String sql = "UPDATE students SET roll_number = ?, first_name = ?, last_name = ?, email = ?, " +
                     "department = ?, cgpa = ?, ssc_percent = ?, hsc_percent = ?, has_backlog = ?, " +
                     "active_backlogs = ?, phone = ? WHERE student_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getDepartment());
            ps.setDouble(6, student.getCgpa());
            ps.setDouble(7, student.getSscPercent());
            ps.setDouble(8, student.getHscPercent());
            ps.setBoolean(9, student.isHasBacklog());
            ps.setInt(10, student.getActiveBacklogs());
            ps.setString(11, student.getPhone());
            ps.setInt(12, student.getStudentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while updating student ID: " + student.getStudentId(), e);
        }
    }

    /**
     * Deletes a student by ID.
     */
    public boolean delete(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while deleting student ID: " + studentId, e);
        }
    }

    /**
     * Finds a student by primary key student_id.
     */
    public Optional<Student> findById(int studentId) {
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, department, " +
                     "cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone " +
                     "FROM students WHERE student_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while fetching student ID: " + studentId, e);
        }
    }

    /**
     * Finds a student by unique roll number.
     */
    public Optional<Student> findByRollNumber(String rollNumber) {
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, department, " +
                     "cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone " +
                     "FROM students WHERE roll_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while fetching roll number: " + rollNumber, e);
        }
    }

    /**
     * Finds a student by email address.
     */
    public Optional<Student> findByEmail(String email) {
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, department, " +
                     "cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone " +
                     "FROM students WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while fetching student email: " + email, e);
        }
    }

    /**
     * Retrieves all student records ordered by student ID.
     */
    public List<Student> findAll() {
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, department, " +
                     "cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone " +
                     "FROM students ORDER BY student_id ASC";

        List<Student> students = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while retrieving all students", e);
        }
    }

    /**
     * Retrieves all students belonging to a specified department.
     */
    public List<Student> findByDepartment(String department) {
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, department, " +
                     "cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone " +
                     "FROM students WHERE department = ? ORDER BY cgpa DESC";

        List<Student> students = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            return students;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while retrieving students by department: " + department, e);
        }
    }

    /**
     * Queries students who fulfill specific criteria directly at the database level.
     */
    public List<Student> findEligibleForCriteria(double minSsc, double minHsc, double minCgpa, boolean allowBacklogs) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT student_id, roll_number, first_name, last_name, email, department, ")
           .append("cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone ")
           .append("FROM students WHERE ssc_percent >= ? AND hsc_percent >= ? AND cgpa >= ? ");

        if (!allowBacklogs) {
            sql.append("AND has_backlog = FALSE ");
        }
        sql.append("ORDER BY cgpa DESC");

        List<Student> students = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setDouble(1, minSsc);
            ps.setDouble(2, minHsc);
            ps.setDouble(3, minCgpa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            return students;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while querying eligible students", e);
        }
    }

    /**
     * Checks if a roll number is already registered in the system.
     */
    public boolean existsByRollNumber(String rollNumber) {
        String sql = "SELECT 1 FROM students WHERE roll_number = ? LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while checking roll number existence: " + rollNumber, e);
        }
    }

    /**
     * Checks if an email is already registered in the system.
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM students WHERE email = ? LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while checking email existence: " + email, e);
        }
    }

    /**
     * Helper mapper converting ResultSet row into Student model.
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("student_id"),
            rs.getString("roll_number"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("department"),
            rs.getDouble("cgpa"),
            rs.getDouble("ssc_percent"),
            rs.getDouble("hsc_percent"),
            rs.getBoolean("has_backlog"),
            rs.getInt("active_backlogs"),
            rs.getString("phone")
        );
    }
}
