package com.placement.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing a recruiting Company with placement eligibility criteria.
 */
public class Company implements Serializable {
    private static final long serialVersionUID = 1L;

    private int companyId;
    private String companyName;
    private String industry;
    private String jobRole;
    private double packageLpa;
    private double minCgpa;
    private double minSscPercent;
    private double minHscPercent;
    private boolean allowBacklogs;
    private String location;
    private LocalDate registrationDeadline;

    public Company() {
    }

    /**
     * Constructor without companyId for new company registration.
     */
    public Company(String companyName, String industry, String jobRole, double packageLpa,
                   double minCgpa, double minSscPercent, double minHscPercent,
                   boolean allowBacklogs, String location, LocalDate registrationDeadline) {
        this.companyName = companyName;
        this.industry = industry;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.minCgpa = minCgpa;
        this.minSscPercent = minSscPercent;
        this.minHscPercent = minHscPercent;
        this.allowBacklogs = allowBacklogs;
        this.location = location;
        this.registrationDeadline = registrationDeadline;
    }

    /**
     * Full constructor including companyId for database-mapped entities.
     */
    public Company(int companyId, String companyName, String industry, String jobRole,
                   double packageLpa, double minCgpa, double minSscPercent,
                   double minHscPercent, boolean allowBacklogs, String location,
                   LocalDate registrationDeadline) {
        this(companyName, industry, jobRole, packageLpa, minCgpa, minSscPercent, minHscPercent, allowBacklogs, location, registrationDeadline);
        this.companyId = companyId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
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

    public double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public double getMinSscPercent() {
        return minSscPercent;
    }

    public void setMinSscPercent(double minSscPercent) {
        this.minSscPercent = minSscPercent;
    }

    public double getMinHscPercent() {
        return minHscPercent;
    }

    public void setMinHscPercent(double minHscPercent) {
        this.minHscPercent = minHscPercent;
    }

    public boolean isAllowBacklogs() {
        return allowBacklogs;
    }

    public void setAllowBacklogs(boolean allowBacklogs) {
        this.allowBacklogs = allowBacklogs;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return companyId == company.companyId || Objects.equals(companyName, company.companyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, companyName);
    }

    @Override
    public String toString() {
        return String.format(
            "Company[ID=%d, Name='%s', Role='%s', Package=%.2f LPA, MinCGPA=%.2f, BacklogsAllowed=%s, Deadline=%s]",
            companyId, companyName, jobRole, packageLpa, minCgpa,
            allowBacklogs ? "Yes" : "No", registrationDeadline != null ? registrationDeadline.toString() : "Open"
        );
    }
}
