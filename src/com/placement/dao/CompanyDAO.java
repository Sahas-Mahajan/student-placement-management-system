package com.placement.dao;

import com.placement.config.DBConnection;
import com.placement.exception.DatabaseOperationException;
import com.placement.model.Company;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Company entity.
 * Handles company registrations, updates, criteria evaluations, and lookups.
 */
public class CompanyDAO {

    private final DBConnection dbConnection;

    public CompanyDAO() {
        this.dbConnection = DBConnection.getInstance();
    }

    /**
     * Persists a new Company record and retrieves the auto-generated company_id.
     */
    public Company save(Company company) {
        String sql = "INSERT INTO companies (company_name, industry, job_role, package_lpa, min_cgpa, " +
                     "min_ssc_percent, min_hsc_percent, allow_backlogs, location, registration_deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, company.getCompanyName());
            ps.setString(2, company.getIndustry());
            ps.setString(3, company.getJobRole());
            ps.setDouble(4, company.getPackageLpa());
            ps.setDouble(5, company.getMinCgpa());
            ps.setDouble(6, company.getMinSscPercent());
            ps.setDouble(7, company.getMinHscPercent());
            ps.setBoolean(8, company.isAllowBacklogs());
            ps.setString(9, company.getLocation());
            if (company.getRegistrationDeadline() != null) {
                ps.setDate(10, Date.valueOf(company.getRegistrationDeadline()));
            } else {
                ps.setNull(10, java.sql.Types.DATE);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating company failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    company.setCompanyId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseOperationException("Creating company failed, no generated ID returned.");
                }
            }

            return company;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while saving company: " + company.getCompanyName(), e);
        }
    }

    /**
     * Updates an existing company's details and criteria.
     */
    public boolean update(Company company) {
        String sql = "UPDATE companies SET company_name = ?, industry = ?, job_role = ?, package_lpa = ?, " +
                     "min_cgpa = ?, min_ssc_percent = ?, min_hsc_percent = ?, allow_backlogs = ?, " +
                     "location = ?, registration_deadline = ? WHERE company_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, company.getCompanyName());
            ps.setString(2, company.getIndustry());
            ps.setString(3, company.getJobRole());
            ps.setDouble(4, company.getPackageLpa());
            ps.setDouble(5, company.getMinCgpa());
            ps.setDouble(6, company.getMinSscPercent());
            ps.setDouble(7, company.getMinHscPercent());
            ps.setBoolean(8, company.isAllowBacklogs());
            ps.setString(9, company.getLocation());
            if (company.getRegistrationDeadline() != null) {
                ps.setDate(10, Date.valueOf(company.getRegistrationDeadline()));
            } else {
                ps.setNull(10, java.sql.Types.DATE);
            }
            ps.setInt(11, company.getCompanyId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while updating company ID: " + company.getCompanyId(), e);
        }
    }

    /**
     * Deletes a company by ID.
     */
    public boolean delete(int companyId) {
        String sql = "DELETE FROM companies WHERE company_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, companyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while deleting company ID: " + companyId, e);
        }
    }

    /**
     * Finds a company by primary key company_id.
     */
    public Optional<Company> findById(int companyId) {
        String sql = "SELECT company_id, company_name, industry, job_role, package_lpa, min_cgpa, " +
                     "min_ssc_percent, min_hsc_percent, allow_backlogs, location, registration_deadline " +
                     "FROM companies WHERE company_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCompany(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while fetching company ID: " + companyId, e);
        }
    }

    /**
     * Finds a company by unique company name.
     */
    public Optional<Company> findByName(String companyName) {
        String sql = "SELECT company_id, company_name, industry, job_role, package_lpa, min_cgpa, " +
                     "min_ssc_percent, min_hsc_percent, allow_backlogs, location, registration_deadline " +
                     "FROM companies WHERE company_name = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, companyName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCompany(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while fetching company name: " + companyName, e);
        }
    }

    /**
     * Retrieves all companies ordered by package (highest first).
     */
    public List<Company> findAll() {
        String sql = "SELECT company_id, company_name, industry, job_role, package_lpa, min_cgpa, " +
                     "min_ssc_percent, min_hsc_percent, allow_backlogs, location, registration_deadline " +
                     "FROM companies ORDER BY package_lpa DESC, company_name ASC";

        List<Company> companies = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                companies.add(mapResultSetToCompany(rs));
            }
            return companies;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while retrieving all companies", e);
        }
    }

    /**
     * Queries all companies for which a student with given credentials is fundamentally eligible.
     */
    public List<Company> findEligibleCompanies(double sscPercent, double hscPercent, double cgpa, boolean hasBacklog) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT company_id, company_name, industry, job_role, package_lpa, min_cgpa, ")
           .append("min_ssc_percent, min_hsc_percent, allow_backlogs, location, registration_deadline ")
           .append("FROM companies ")
           .append("WHERE min_ssc_percent <= ? AND min_hsc_percent <= ? AND min_cgpa <= ? ");

        if (hasBacklog) {
            // Student has active backlog, so company MUST allow backlogs
            sql.append("AND allow_backlogs = TRUE ");
        }
        sql.append("ORDER BY package_lpa DESC");

        List<Company> companies = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setDouble(1, sscPercent);
            ps.setDouble(2, hscPercent);
            ps.setDouble(3, cgpa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    companies.add(mapResultSetToCompany(rs));
                }
            }
            return companies;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while finding eligible companies", e);
        }
    }

    /**
     * Checks if a company name already exists in the system.
     */
    public boolean existsByName(String companyName) {
        String sql = "SELECT 1 FROM companies WHERE company_name = ? LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, companyName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error occurred while checking company existence: " + companyName, e);
        }
    }

    /**
     * Helper mapper converting ResultSet row into Company model.
     */
    private Company mapResultSetToCompany(ResultSet rs) throws SQLException {
        Date deadlineDate = rs.getDate("registration_deadline");
        LocalDate deadline = deadlineDate != null ? deadlineDate.toLocalDate() : null;

        return new Company(
            rs.getInt("company_id"),
            rs.getString("company_name"),
            rs.getString("industry"),
            rs.getString("job_role"),
            rs.getDouble("package_lpa"),
            rs.getDouble("min_cgpa"),
            rs.getDouble("min_ssc_percent"),
            rs.getDouble("min_hsc_percent"),
            rs.getBoolean("allow_backlogs"),
            rs.getString("location"),
            deadline
        );
    }
}
