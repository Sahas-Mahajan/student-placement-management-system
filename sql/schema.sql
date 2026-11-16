-- ==============================================================================
-- Student Placement Management System - Database Schema & Seed Data
-- Database Engine: MySQL 8.0+
-- ==============================================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS placement_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE placement_db;

-- ------------------------------------------------------------------------------
-- 1. Table: students
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS students;

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    cgpa DECIMAL(4, 2) NOT NULL,
    ssc_percent DECIMAL(5, 2) NOT NULL,
    hsc_percent DECIMAL(5, 2) NOT NULL,
    has_backlog BOOLEAN NOT NULL DEFAULT FALSE,
    active_backlogs INT NOT NULL DEFAULT 0,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cgpa CHECK (cgpa >= 0.00 AND cgpa <= 10.00),
    CONSTRAINT chk_ssc CHECK (ssc_percent >= 0.00 AND ssc_percent <= 100.00),
    CONSTRAINT chk_hsc CHECK (hsc_percent >= 0.00 AND hsc_percent <= 100.00),
    CONSTRAINT chk_backlogs CHECK (active_backlogs >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. Table: companies
-- ------------------------------------------------------------------------------
CREATE TABLE companies (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    industry VARCHAR(50) NOT NULL,
    job_role VARCHAR(100) NOT NULL,
    package_lpa DECIMAL(5, 2) NOT NULL,
    min_cgpa DECIMAL(4, 2) NOT NULL DEFAULT 6.00,
    min_ssc_percent DECIMAL(5, 2) NOT NULL DEFAULT 60.00,
    min_hsc_percent DECIMAL(5, 2) NOT NULL DEFAULT 60.00,
    allow_backlogs BOOLEAN NOT NULL DEFAULT FALSE,
    location VARCHAR(100) NOT NULL,
    registration_deadline DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pkg CHECK (package_lpa >= 0.00),
    CONSTRAINT chk_comp_cgpa CHECK (min_cgpa >= 0.00 AND min_cgpa <= 10.00),
    CONSTRAINT chk_comp_ssc CHECK (min_ssc_percent >= 0.00 AND min_ssc_percent <= 100.00),
    CONSTRAINT chk_comp_hsc CHECK (min_hsc_percent >= 0.00 AND min_hsc_percent <= 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. Table: applications
-- ------------------------------------------------------------------------------
CREATE TABLE applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    company_id INT NOT NULL,
    status ENUM('APPLIED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'APPLIED',
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_company FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_company UNIQUE (student_id, company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for performance optimization on frequent queries
CREATE INDEX idx_student_roll ON students(roll_number);
CREATE INDEX idx_student_dept ON students(department);
CREATE INDEX idx_student_cgpa ON students(cgpa);
CREATE INDEX idx_company_name ON companies(company_name);
CREATE INDEX idx_company_min_cgpa ON companies(min_cgpa);
CREATE INDEX idx_application_status ON applications(status);
CREATE INDEX idx_app_student ON applications(student_id);
CREATE INDEX idx_app_company ON applications(company_id);

-- ------------------------------------------------------------------------------
-- Sample Seed Data
-- ------------------------------------------------------------------------------

-- Seed Students
INSERT INTO students (roll_number, first_name, last_name, email, department, cgpa, ssc_percent, hsc_percent, has_backlog, active_backlogs, phone) VALUES
('CS2024001', 'Aarav', 'Sharma', 'aarav.sharma@example.edu', 'Computer Science', 9.25, 89.40, 88.00, FALSE, 0, '+91-9876543210'),
('CS2024002', 'Diya', 'Patel', 'diya.patel@example.edu', 'Computer Science', 8.80, 85.50, 84.00, FALSE, 0, '+91-9876543211'),
('IT2024003', 'Rohan', 'Verma', 'rohan.verma@example.edu', 'Information Technology', 7.45, 78.00, 75.50, FALSE, 0, '+91-9876543212'),
('IT2024004', 'Ananya', 'Iyer', 'ananya.iyer@example.edu', 'Information Technology', 8.10, 82.00, 80.50, FALSE, 0, '+91-9876543213'),
('EC2024005', 'Vikram', 'Singh', 'vikram.singh@example.edu', 'Electronics', 6.80, 72.00, 68.00, TRUE, 1, '+91-9876543214'),
('EC2024006', 'Sneha', 'Reddy', 'sneha.reddy@example.edu', 'Electronics', 7.90, 80.00, 78.00, FALSE, 0, '+91-9876543215'),
('ME2024007', 'Kunal', 'Joshi', 'kunal.joshi@example.edu', 'Mechanical', 6.20, 65.00, 62.00, FALSE, 0, '+91-9876543216'),
('ME2024008', 'Priya', 'Nair', 'priya.nair@example.edu', 'Mechanical', 5.90, 61.00, 58.50, TRUE, 2, '+91-9876543217');

-- Seed Companies
INSERT INTO companies (company_name, industry, job_role, package_lpa, min_cgpa, min_ssc_percent, min_hsc_percent, allow_backlogs, location, registration_deadline) VALUES
('Google India', 'Product / Tech', 'Software Development Engineer', 28.50, 8.50, 80.00, 80.00, FALSE, 'Bengaluru, Karnataka', '2026-12-31'),
('Microsoft IDC', 'Product / Cloud', 'Software Engineer', 25.00, 8.00, 75.00, 75.00, FALSE, 'Hyderabad, Telangana', '2026-12-15'),
('Amazon Web Services', 'Cloud Computing', 'Cloud Support Associate', 16.00, 7.50, 70.00, 70.00, FALSE, 'Bengaluru, Karnataka', '2026-11-30'),
('TCS Digital', 'IT Consulting', 'Systems Engineer (Digital)', 7.50, 7.00, 65.00, 65.00, FALSE, 'Pune, Maharashtra', '2026-12-20'),
('Infosys Technologies', 'IT Services', 'Specialist Programmer', 6.50, 6.00, 60.00, 60.00, TRUE, 'Mysuru, Karnataka', '2026-12-25'),
('Tata Motors', 'Automotive / Core', 'Graduate Engineer Trainee', 6.00, 6.00, 60.00, 60.00, FALSE, 'Pune, Maharashtra', '2026-11-28');

-- Seed Applications
INSERT INTO applications (student_id, company_id, status, remarks) VALUES
(1, 1, 'ACCEPTED', 'Offer letter accepted (28.50 LPA)'),
(1, 2, 'SHORTLISTED', 'Cleared round 2 technical interview'),
(2, 1, 'SHORTLISTED', 'Scheduled for on-site interviews'),
(2, 2, 'ACCEPTED', 'Offer letter accepted (25.00 LPA)'),
(3, 3, 'APPLIED', 'Application under preliminary screening'),
(3, 4, 'INTERVIEW_SCHEDULED', 'Technical Round 1 scheduled for Dec 05'),
(4, 2, 'APPLIED', 'Resume verified against eligibility'),
(4, 4, 'ACCEPTED', 'Offer letter issued (7.50 LPA)'),
(5, 5, 'APPLIED', 'Candidate with 1 backlog eligible as per Infosys policy'),
(6, 4, 'SHORTLISTED', 'Coding round cleared'),
(7, 6, 'APPLIED', 'Awaiting HR review');

-- ------------------------------------------------------------------------------
-- Sample Inner Join Query: Comprehensive Placement Report
-- ------------------------------------------------------------------------------
SELECT 
    a.application_id,
    s.roll_number,
    CONCAT(s.first_name, ' ', s.last_name) AS student_name,
    s.department,
    s.cgpa,
    c.company_name,
    c.job_role,
    c.package_lpa,
    a.status,
    a.application_date,
    a.remarks
FROM applications a
INNER JOIN students s ON a.student_id = s.student_id
INNER JOIN companies c ON a.company_id = c.company_id
ORDER BY a.application_id ASC;
