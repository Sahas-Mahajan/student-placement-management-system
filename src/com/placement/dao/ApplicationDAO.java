package com.placement.dao;

import com.placement.config.DBConnection;
import com.placement.exception.DatabaseOperationException;
import com.placement.model.Application;
import com.placement.model.Application.ApplicationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Application entity.
 * Implements multi-table INNER JOIN queries combining students, companies, and applications.
 */
public class ApplicationDAO {

    private final DBConnection dbConnection;

    public ApplicationDAO() {
        this.dbConnection = DBConnection.getInstance();
    }

    /**
     * Persists an application and retrieves the generated application_id.
     */
    public Application save(Application application) {
        String sql = "INSERT INTO applications (student_id, company_id, status, remarks) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, application.getStudentId());
            ps.setInt(2, application.getCompanyId());
            ps.setString(3, application.getStatus().name());
            ps.setString(4, application.getRemarks());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating application failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    application.setApplicationId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseOperationException("Creating application failed, no generated ID returned.");
                }
            }

            return application;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while submitting application", e);
        }
    }

    /**
     * Updates status and remarks of an application.
     */
    public boolean updateStatus(int applicationId, ApplicationStatus status, String remarks) {
        String sql = "UPDATE applications SET status = ?, remarks = ? WHERE application_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, remarks);
            ps.setInt(3, applicationId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while updating application ID: " + applicationId, e);
        }
    }

    /**
     * Deletes an application by its ID.
     */
    public boolean delete(int applicationId) {
        String sql = "DELETE FROM applications WHERE application_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while deleting application ID: " + applicationId, e);
        }
    }

    /**
     * Finds application by primary key.
     */
    public Optional<Application> findById(int applicationId) {
        String sql = "SELECT application_id, student_id, company_id, status, application_date, remarks, updated_at " +
                     "FROM applications WHERE application_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToApplication(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while fetching application ID: " + applicationId, e);
        }
    }

    /**
     * Checks if a student has already applied for a specific company.
     */
    public boolean hasStudentApplied(int studentId, int companyId) {
        String sql = "SELECT 1 FROM applications WHERE student_id = ? AND company_id = ? LIMIT 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error checking existing application", e);
        }
    }

    /**
     * Finds specific application by student ID and company ID.
     */
    public Optional<Application> findByStudentAndCompany(int studentId, int companyId) {
        String sql = "SELECT application_id, student_id, company_id, status, application_date, remarks, updated_at " +
                     "FROM applications WHERE student_id = ? AND company_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToApplication(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error fetching application for student and company", e);
        }
    }

    /**
     * Fetches all applications submitted by a specific student.
     */
    public List<Application> findByStudentId(int studentId) {
        String sql = "SELECT a.application_id, a.student_id, a.company_id, a.status, a.application_date, a.remarks, a.updated_at, " +
                     "s.roll_number, CONCAT(s.first_name, ' ', s.last_name) AS student_name, s.department, s.cgpa, " +
                     "c.company_name, c.job_role, c.package_lpa " +
                     "FROM applications a " +
                     "INNER JOIN students s ON a.student_id = s.student_id " +
                     "INNER JOIN companies c ON a.company_id = c.company_id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY a.application_date DESC";

        return executeDetailedQuery(sql, ps -> ps.setInt(1, studentId));
    }

    /**
     * Fetches all applications for a specific company.
     */
    public List<Application> findByCompanyId(int companyId) {
        String sql = "SELECT a.application_id, a.student_id, a.company_id, a.status, a.application_date, a.remarks, a.updated_at, " +
                     "s.roll_number, CONCAT(s.first_name, ' ', s.last_name) AS student_name, s.department, s.cgpa, " +
                     "c.company_name, c.job_role, c.package_lpa " +
                     "FROM applications a " +
                     "INNER JOIN students s ON a.student_id = s.student_id " +
                     "INNER JOIN companies c ON a.company_id = c.company_id " +
                     "WHERE a.company_id = ? " +
                     "ORDER BY s.cgpa DESC";

        return executeDetailedQuery(sql, ps -> ps.setInt(1, companyId));
    }

    /**
     * Multi-Table INNER JOIN Query:
     * Generates a comprehensive placement report joining applications, students, and companies.
     */
    public List<Application> findAllWithDetails() {
        String sql = "SELECT a.application_id, a.student_id, a.company_id, a.status, a.application_date, a.remarks, a.updated_at, " +
                     "s.roll_number, CONCAT(s.first_name, ' ', s.last_name) AS student_name, s.department, s.cgpa, " +
                     "c.company_name, c.job_role, c.package_lpa " +
                     "FROM applications a " +
                     "INNER JOIN students s ON a.student_id = s.student_id " +
                     "INNER JOIN companies c ON a.company_id = c.company_id " +
                     "ORDER BY a.application_id ASC";

        return executeDetailedQuery(sql, null);
    }

    /**
     * Multi-Table INNER JOIN Query filtered by application status.
     */
    public List<Application> findByStatusWithDetails(ApplicationStatus status) {
        String sql = "SELECT a.application_id, a.student_id, a.company_id, a.status, a.application_date, a.remarks, a.updated_at, " +
                     "s.roll_number, CONCAT(s.first_name, ' ', s.last_name) AS student_name, s.department, s.cgpa, " +
                     "c.company_name, c.job_role, c.package_lpa " +
                     "FROM applications a " +
                     "INNER JOIN students s ON a.student_id = s.student_id " +
                     "INNER JOIN companies c ON a.company_id = c.company_id " +
                     "WHERE a.status = ? " +
                     "ORDER BY c.package_lpa DESC, s.cgpa DESC";

        return executeDetailedQuery(sql, ps -> ps.setString(1, status.name()));
    }

    /**
     * Aggregation query for placement metrics and statistics.
     */
    public Map<String, Object> getPlacementStatistics() {
        Map<String, Object> stats = new HashMap<>();

        String sqlCounts = "SELECT status, COUNT(*) AS cnt FROM applications GROUP BY status";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCounts);
             ResultSet rs = ps.executeQuery()) {

            int totalApplications = 0;
            while (rs.next()) {
                String st = rs.getString("status");
                int count = rs.getInt("cnt");
                stats.put("status_" + st, count);
                totalApplications += count;
            }
            stats.put("total_applications", totalApplications);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error calculating application status metrics", e);
        }

        String sqlAggregates = 
            "SELECT " +
            "  COUNT(DISTINCT a.student_id) AS placed_students, " +
            "  COALESCE(AVG(c.package_lpa), 0.0) AS avg_package, " +
            "  COALESCE(MAX(c.package_lpa), 0.0) AS highest_package, " +
            "  COALESCE(MIN(c.package_lpa), 0.0) AS lowest_package " +
            "FROM applications a " +
            "INNER JOIN companies c ON a.company_id = c.company_id " +
            "WHERE a.status = 'ACCEPTED'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAggregates);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.put("placed_students", rs.getInt("placed_students"));
                stats.put("avg_package", rs.getDouble("avg_package"));
                stats.put("highest_package", rs.getDouble("highest_package"));
                stats.put("lowest_package", rs.getDouble("lowest_package"));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error calculating placement salary metrics", e);
        }

        return stats;
    }

    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setParameters(PreparedStatement ps) throws SQLException;
    }

    private List<Application> executeDetailedQuery(String sql, PreparedStatementSetter setter) {
        List<Application> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setter != null) {
                setter.setParameters(ps);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDetailedApplication(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error executing multi-table report query", e);
        }
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Timestamp appDate = rs.getTimestamp("application_date");
        Timestamp upDate = rs.getTimestamp("updated_at");

        Application app = new Application();
        app.setApplicationId(rs.getInt("application_id"));
        app.setStudentId(rs.getInt("student_id"));
        app.setCompanyId(rs.getInt("company_id"));
        app.setStatus(ApplicationStatus.fromString(rs.getString("status")));
        app.setRemarks(rs.getString("remarks"));
        if (appDate != null) {
            app.setApplicationDate(appDate.toLocalDateTime());
        }
        if (upDate != null) {
            app.setUpdatedAt(upDate.toLocalDateTime());
        }
        return app;
    }

    private Application mapResultSetToDetailedApplication(ResultSet rs) throws SQLException {
        Application app = mapResultSetToApplication(rs);
        app.setStudentRollNumber(rs.getString("roll_number"));
        app.setStudentName(rs.getString("student_name"));
        app.setStudentDepartment(rs.getString("department"));
        app.setStudentCgpa(rs.getDouble("cgpa"));
        app.setCompanyName(rs.getString("company_name"));
        app.setJobRole(rs.getString("job_role"));
        app.setPackageLpa(rs.getDouble("package_lpa"));
        return app;
    }
}
