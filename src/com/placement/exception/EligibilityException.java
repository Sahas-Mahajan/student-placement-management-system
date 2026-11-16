package com.placement.exception;

import java.util.Collections;
import java.util.List;

/**
 * Custom checked exception thrown when a student does not meet the eligibility
 * criteria established by a company (e.g., minimum CGPA, SSC %, HSC %, or backlog policies).
 */
public class EligibilityException extends Exception {

    private final String studentRollNumber;
    private final String companyName;
    private final List<String> unmetCriteria;

    public EligibilityException(String message) {
        super(message);
        this.studentRollNumber = null;
        this.companyName = null;
        this.unmetCriteria = Collections.emptyList();
    }

    public EligibilityException(String message, Throwable cause) {
        super(message, cause);
        this.studentRollNumber = null;
        this.companyName = null;
        this.unmetCriteria = Collections.emptyList();
    }

    public EligibilityException(String studentRollNumber, String companyName, List<String> unmetCriteria) {
        super(buildDetailedMessage(studentRollNumber, companyName, unmetCriteria));
        this.studentRollNumber = studentRollNumber;
        this.companyName = companyName;
        this.unmetCriteria = unmetCriteria != null ? List.copyOf(unmetCriteria) : Collections.emptyList();
    }

    private static String buildDetailedMessage(String rollNumber, String company, List<String> criteria) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student [Roll: ").append(rollNumber != null ? rollNumber : "N/A")
          .append("] is NOT eligible to apply for Company [").append(company != null ? company : "N/A")
          .append("]. Unmet criteria:\n");
        if (criteria != null && !criteria.isEmpty()) {
            for (int i = 0; i < criteria.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(criteria.get(i)).append("\n");
            }
        } else {
            sb.append("  - General eligibility criteria not met.\n");
        }
        return sb.toString().trim();
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public List<String> getUnmetCriteria() {
        return unmetCriteria;
    }
}
