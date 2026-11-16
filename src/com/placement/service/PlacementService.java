package com.placement.service;

import com.placement.dao.ApplicationDAO;
import com.placement.dao.CompanyDAO;
import com.placement.dao.StudentDAO;
import com.placement.exception.EligibilityException;
import com.placement.model.Application;
import com.placement.model.Application.ApplicationStatus;
import com.placement.model.Company;
import com.placement.model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer implementing all placement business logic, validation rules,
 * eligibility criteria evaluation, and orchestration between DAOs.
 */
public class PlacementService {

    private final StudentDAO studentDAO;
    private final CompanyDAO companyDAO;
    private final ApplicationDAO applicationDAO;

    public PlacementService() {
        this.studentDAO = new StudentDAO();
        this.companyDAO = new CompanyDAO();
        this.applicationDAO = new ApplicationDAO();
    }

    public PlacementService(StudentDAO studentDAO, CompanyDAO companyDAO, ApplicationDAO applicationDAO) {
        this.studentDAO = studentDAO;
        this.companyDAO = companyDAO;
        this.applicationDAO = applicationDAO;
    }

    // =========================================================================
    // 1. Placement Application & Eligibility Validation
    // =========================================================================

    /**
     * Structure representing the outcome of an eligibility evaluation.
     */
    public record EligibilityReport(
        boolean isEligible,
        Student student,
        Company company,
        List<String> unmetReasons
    ) {}

    /**
     * Evaluates whether a student satisfies all criteria for a target company
     * without persisting an application record.
     */
    public EligibilityReport checkEligibility(int studentId, int companyId) {
        Student student = studentDAO.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student with ID [" + studentId + "] does not exist."));
        Company company = companyDAO.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company with ID [" + companyId + "] does not exist."));

        List<String> unmetReasons = new ArrayList<>();

        // 1. SSC Percentage validation
        if (student.getSscPercent() < company.getMinSscPercent()) {
            unmetReasons.add(String.format(
                "SSC Percentage: %.2f%% is below required minimum of %.2f%%",
                student.getSscPercent(), company.getMinSscPercent()
            ));
        }

        // 2. HSC Percentage validation
        if (student.getHscPercent() < company.getMinHscPercent()) {
            unmetReasons.add(String.format(
                "HSC Percentage: %.2f%% is below required minimum of %.2f%%",
                student.getHscPercent(), company.getMinHscPercent()
            ));
        }

        // 3. CGPA validation
        if (student.getCgpa() < company.getMinCgpa()) {
            unmetReasons.add(String.format(
                "CGPA: %.2f is below required minimum of %.2f",
                student.getCgpa(), company.getMinCgpa()
            ));
        }

        // 4. Backlog validation
        if (student.isHasBacklog() && !company.isAllowBacklogs()) {
            unmetReasons.add(String.format(
                "Backlogs: Student has %d active backlog(s), but %s has a strict zero-backlog policy",
                student.getActiveBacklogs(), company.getCompanyName()
            ));
        }

        // 5. Deadline check
        if (company.getRegistrationDeadline() != null && company.getRegistrationDeadline().isBefore(LocalDate.now())) {
            unmetReasons.add(String.format(
                "Deadline Expired: Registration closed on %s",
                company.getRegistrationDeadline()
            ));
        }

        boolean eligible = unmetReasons.isEmpty();
        return new EligibilityReport(eligible, student, company, unmetReasons);
    }

    /**
     * Validates eligibility criteria and submits a new application.
     * Throws EligibilityException if criteria are not satisfied.
     */
    public Application applyForCompany(int studentId, int companyId, String remarks) throws EligibilityException {
        // Check for duplicate application
        if (applicationDAO.hasStudentApplied(studentId, companyId)) {
            Student student = studentDAO.findById(studentId).orElse(null);
            Company company = companyDAO.findById(companyId).orElse(null);
            String roll = student != null ? student.getRollNumber() : "ID:" + studentId;
            String compName = company != null ? company.getCompanyName() : "ID:" + companyId;
            throw new IllegalStateException("Student [" + roll + "] has already submitted an application for [" + compName + "].");
        }

        // Strict Eligibility Evaluation
        EligibilityReport report = checkEligibility(studentId, companyId);
        if (!report.isEligible()) {
            throw new EligibilityException(
                report.student().getRollNumber(),
                report.company().getCompanyName(),
                report.unmetReasons()
            );
        }

        Application application = new Application(
            studentId,
            companyId,
            ApplicationStatus.APPLIED,
            remarks != null && !remarks.trim().isEmpty() ? remarks.trim() : "Application submitted via Placement Portal"
        );

        return applicationDAO.save(application);
    }

    /**
     * Updates an application status (e.g., SHORTLISTED, ACCEPTED, REJECTED).
     */
    public boolean updateApplicationStatus(int applicationId, ApplicationStatus newStatus, String remarks) {
        Application existing = applicationDAO.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application ID [" + applicationId + "] does not exist."));

        return applicationDAO.updateStatus(applicationId, newStatus, remarks);
    }

    /**
     * Withdraws / deletes an application.
     */
    public boolean withdrawApplication(int applicationId) {
        return applicationDAO.delete(applicationId);
    }

    // =========================================================================
    // 2. Student Management Operations
    // =========================================================================

    public Student registerStudent(Student student) {
        validateStudentData(student);

        if (studentDAO.existsByRollNumber(student.getRollNumber())) {
            throw new IllegalArgumentException("Roll number [" + student.getRollNumber() + "] is already registered.");
        }
        if (studentDAO.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Email [" + student.getEmail() + "] is already registered.");
        }

        return studentDAO.save(student);
    }

    public boolean updateStudent(Student student) {
        validateStudentData(student);
        if (student.getStudentId() <= 0) {
            throw new IllegalArgumentException("Valid student ID is required for update.");
        }
        return studentDAO.update(student);
    }

    public boolean deleteStudent(int studentId) {
        return studentDAO.delete(studentId);
    }

    public Optional<Student> getStudentById(int studentId) {
        return studentDAO.findById(studentId);
    }

    public Optional<Student> getStudentByRollNumber(String rollNumber) {
        return studentDAO.findByRollNumber(rollNumber != null ? rollNumber.trim() : "");
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    public List<Student> getStudentsByDepartment(String department) {
        return studentDAO.findByDepartment(department);
    }

    private void validateStudentData(Student s) {
        if (s == null) {
            throw new IllegalArgumentException("Student object cannot be null.");
        }
        if (s.getRollNumber() == null || s.getRollNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Student Roll Number is required.");
        }
        if (s.getFirstName() == null || s.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student First Name is required.");
        }
        if (s.getEmail() == null || !s.getEmail().contains("@")) {
            throw new IllegalArgumentException("A valid Email address is required.");
        }
        if (s.getCgpa() < 0.0 || s.getCgpa() > 10.0) {
            throw new IllegalArgumentException("CGPA must be between 0.00 and 10.00.");
        }
        if (s.getSscPercent() < 0.0 || s.getSscPercent() > 100.0) {
            throw new IllegalArgumentException("SSC percentage must be between 0.00% and 100.00%.");
        }
        if (s.getHscPercent() < 0.0 || s.getHscPercent() > 100.0) {
            throw new IllegalArgumentException("HSC percentage must be between 0.00% and 100.00%.");
        }
        if (s.getActiveBacklogs() < 0) {
            throw new IllegalArgumentException("Active backlogs count cannot be negative.");
        }
        s.setHasBacklog(s.getActiveBacklogs() > 0);
    }

    // =========================================================================
    // 3. Company Management Operations
    // =========================================================================

    public Company registerCompany(Company company) {
        validateCompanyData(company);

        if (companyDAO.existsByName(company.getCompanyName())) {
            throw new IllegalArgumentException("Company [" + company.getCompanyName() + "] is already registered.");
        }

        return companyDAO.save(company);
    }

    public boolean updateCompany(Company company) {
        validateCompanyData(company);
        if (company.getCompanyId() <= 0) {
            throw new IllegalArgumentException("Valid company ID is required for update.");
        }
        return companyDAO.update(company);
    }

    public boolean deleteCompany(int companyId) {
        return companyDAO.delete(companyId);
    }

    public Optional<Company> getCompanyById(int companyId) {
        return companyDAO.findById(companyId);
    }

    public Optional<Company> getCompanyByName(String companyName) {
        return companyDAO.findByName(companyName != null ? companyName.trim() : "");
    }

    public List<Company> getAllCompanies() {
        return companyDAO.findAll();
    }

    private void validateCompanyData(Company c) {
        if (c == null) {
            throw new IllegalArgumentException("Company object cannot be null.");
        }
        if (c.getCompanyName() == null || c.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        if (c.getJobRole() == null || c.getJobRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Job role is required.");
        }
        if (c.getPackageLpa() < 0.0) {
            throw new IllegalArgumentException("Package LPA cannot be negative.");
        }
        if (c.getMinCgpa() < 0.0 || c.getMinCgpa() > 10.0) {
            throw new IllegalArgumentException("Minimum CGPA must be between 0.00 and 10.00.");
        }
        if (c.getMinSscPercent() < 0.0 || c.getMinSscPercent() > 100.0) {
            throw new IllegalArgumentException("Minimum SSC % must be between 0.00% and 100.00%.");
        }
        if (c.getMinHscPercent() < 0.0 || c.getMinHscPercent() > 100.0) {
            throw new IllegalArgumentException("Minimum HSC % must be between 0.00% and 100.00%.");
        }
    }

    // =========================================================================
    // 4. Multi-Table Reports & Analytics
    // =========================================================================

    public List<Company> getEligibleCompaniesForStudent(int studentId) {
        Student student = studentDAO.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student ID [" + studentId + "] not found."));

        return companyDAO.findEligibleCompanies(
            student.getSscPercent(),
            student.getHscPercent(),
            student.getCgpa(),
            student.isHasBacklog()
        );
    }

    public List<Student> getEligibleStudentsForCompany(int companyId) {
        Company company = companyDAO.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company ID [" + companyId + "] not found."));

        return studentDAO.findEligibleForCriteria(
            company.getMinSscPercent(),
            company.getMinHscPercent(),
            company.getMinCgpa(),
            company.isAllowBacklogs()
        );
    }

    public List<Application> getAllApplicationsReport() {
        return applicationDAO.findAllWithDetails();
    }

    public List<Application> getApplicationsByStudent(int studentId) {
        return applicationDAO.findByStudentId(studentId);
    }

    public List<Application> getApplicationsByCompany(int companyId) {
        return applicationDAO.findByCompanyId(companyId);
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationDAO.findByStatusWithDetails(status);
    }

    public Map<String, Object> getPlacementStatistics() {
        Map<String, Object> stats = applicationDAO.getPlacementStatistics();
        int totalStudents = studentDAO.findAll().size();
        int totalCompanies = companyDAO.findAll().size();
        stats.put("total_students", totalStudents);
        stats.put("total_companies", totalCompanies);

        int placed = (int) stats.getOrDefault("placed_students", 0);
        double placementRate = totalStudents > 0 ? ((double) placed / totalStudents) * 100.0 : 0.0;
        stats.put("placement_rate_percent", placementRate);

        return stats;
    }
}
