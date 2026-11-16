package com.placement.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model class representing a Student in the placement ecosystem.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private int studentId;
    private String rollNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private double cgpa;
    private double sscPercent;
    private double hscPercent;
    private boolean hasBacklog;
    private int activeBacklogs;
    private String phone;

    public Student() {
    }

    /**
     * Constructor for creating a new Student (before database persistence / auto-generated ID).
     */
    public Student(String rollNumber, String firstName, String lastName, String email,
                   String department, double cgpa, double sscPercent, double hscPercent,
                   boolean hasBacklog, int activeBacklogs, String phone) {
        this.rollNumber = rollNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.cgpa = cgpa;
        this.sscPercent = sscPercent;
        this.hscPercent = hscPercent;
        this.hasBacklog = hasBacklog;
        this.activeBacklogs = activeBacklogs;
        this.phone = phone;
    }

    /**
     * Full constructor including studentId for mapped database records.
     */
    public Student(int studentId, String rollNumber, String firstName, String lastName,
                   String email, String department, double cgpa, double sscPercent,
                   double hscPercent, boolean hasBacklog, int activeBacklogs, String phone) {
        this(rollNumber, firstName, lastName, email, department, cgpa, sscPercent, hscPercent, hasBacklog, activeBacklogs, phone);
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public double getSscPercent() {
        return sscPercent;
    }

    public void setSscPercent(double sscPercent) {
        this.sscPercent = sscPercent;
    }

    public double getHscPercent() {
        return hscPercent;
    }

    public void setHscPercent(double hscPercent) {
        this.hscPercent = hscPercent;
    }

    public boolean isHasBacklog() {
        return hasBacklog;
    }

    public void setHasBacklog(boolean hasBacklog) {
        this.hasBacklog = hasBacklog;
    }

    public int getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(int activeBacklogs) {
        this.activeBacklogs = activeBacklogs;
        this.hasBacklog = (activeBacklogs > 0);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId == student.studentId || Objects.equals(rollNumber, student.rollNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, rollNumber);
    }

    @Override
    public String toString() {
        return String.format(
            "Student[ID=%d, Roll='%s', Name='%s', Dept='%s', CGPA=%.2f, SSC=%.1f%%, HSC=%.1f%%, Backlogs=%d (%s)]",
            studentId, rollNumber, getFullName(), department, cgpa, sscPercent, hscPercent,
            activeBacklogs, hasBacklog ? "Has Backlog" : "Clear"
        );
    }
}
