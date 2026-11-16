package com.placement.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing an Application submitted by a student for a recruiting company.
 * Also holds joined presentation fields populated by multi-table INNER JOIN reports.
 */
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ApplicationStatus {
        APPLIED,
        SHORTLISTED,
        INTERVIEW_SCHEDULED,
        ACCEPTED,
        REJECTED;

        public static ApplicationStatus fromString(String statusStr) {
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return APPLIED;
            }
            try {
                return ApplicationStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return APPLIED;
            }
        }
    }

    private int applicationId;
    private int studentId;
    private int companyId;
    private ApplicationStatus status;
    private LocalDateTime applicationDate;
    private String remarks;
    private LocalDateTime updatedAt;

    // Joined report fields (populated during INNER JOIN queries)
    private String studentRollNumber;
    private String studentName;
    private String studentDepartment;
    private double studentCgpa;
    private String companyName;
    private String jobRole;
    private double packageLpa;

    public Application() {
        this.status = ApplicationStatus.APPLIED;
        this.applicationDate = LocalDateTime.now();
    }

    /**
     * Constructor for creating a new application.
     */
    public Application(int studentId, int companyId, ApplicationStatus status, String remarks) {
        this.studentId = studentId;
        this.companyId = companyId;
        this.status = status != null ? status : ApplicationStatus.APPLIED;
        this.remarks = remarks;
        this.applicationDate = LocalDateTime.now();
    }

    /**
     * Full constructor for application entity.
     */
    public Application(int applicationId, int studentId, int companyId, ApplicationStatus status,
                       LocalDateTime applicationDate, String remarks, LocalDateTime updatedAt) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.companyId = companyId;
        this.status = status;
        this.applicationDate = applicationDate;
        this.remarks = remarks;
        this.updatedAt = updatedAt;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Joined report getters & setters
    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public double getStudentCgpa() {
        return studentCgpa;
    }

    public void setStudentCgpa(double studentCgpa) {
        this.studentCgpa = studentCgpa;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(double packageLpa) {
        this.packageLpa = packageLpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return applicationId == that.applicationId || (studentId == that.studentId && companyId == that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, studentId, companyId);
    }

    @Override
    public String toString() {
        return String.format(
            "Application[ID=%d, StudentId=%d (%s), CompanyId=%d (%s), Status=%s, Date=%s]",
            applicationId, studentId, studentRollNumber != null ? studentRollNumber : "N/A",
            companyId, companyName != null ? companyName : "N/A", status, applicationDate
        );
    }
}
