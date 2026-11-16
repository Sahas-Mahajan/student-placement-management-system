# Student Placement Management System (SPMS)

A robust, enterprise-structured console application built with **Core Java (Java 17+)**, **pure JDBC**, and **MySQL**. Designed strictly following software engineering best practices without heavy frameworks like Spring Boot, showcasing raw architectural mastery of **Object-Oriented Design**, the **DAO (Data Access Object) Pattern**, **Thread-Safe Singleton Pattern**, and **leak-free JDBC resource management**.

---

## Key Technical Highlights (Resume-Ready)

- **Pure Core Java + JDBC Architecture**: Zero framework dependencies; demonstrates foundational fluency in Java SE, standard collection frameworks, and JDBC 4.2+ APIs.
- **SQL Injection Prevention**: 100% of SQL interactions utilize `PreparedStatement` with parameterized placeholders (`?`).
- **Zero Resource Leaks**: Strict implementation of Java 7+ `try-with-resources` on every `Connection`, `PreparedStatement`, and `ResultSet` ensuring prompt closure of unmanaged database cursors and connections.
- **DAO Pattern & Clean Separation of Concerns**: Strict boundary separation between Presentation (`MainApp`), Business Logic / Rules Engine (`PlacementService`), Data Persistence (`*DAO`), and Configuration (`DBConnection`).
- **Multi-Table INNER JOIN Reports**: Complex SQL joins aggregating data across `students`, `companies`, and `applications` to output tabular recruitment rosters and salary metrics.
- **Business Rule Validation Engine**: Centralized validation in `PlacementService` enforcing company-specific minimum CGPA, SSC %, HSC %, zero-backlog compliance, and drive deadlines with custom `EligibilityException`.

---

## Entity-Relationship (ER) Diagram (ASCII)

```
 +-----------------------------------------------------------------------+
 |                              STUDENTS                                 |
 +-----------------------------------------------------------------------+
 | PK  student_id       INT AUTO_INCREMENT                               |
 | UQ  roll_number      VARCHAR(30)                                      |
 |     first_name       VARCHAR(50)                                      |
 |     last_name        VARCHAR(50)                                      |
 | UQ  email            VARCHAR(100)                                     |
 |     department       VARCHAR(50)                                      |
 |     cgpa             DECIMAL(4,2)  [0.00 - 10.00]                     |
 |     ssc_percent      DECIMAL(5,2)  [0.00 - 100.00]                    |
 |     hsc_percent      DECIMAL(5,2)  [0.00 - 100.00]                    |
 |     has_backlog      BOOLEAN                                          |
 |     active_backlogs  INT           [>= 0]                             |
 |     phone            VARCHAR(15)                                      |
 |     created_at       TIMESTAMP                                        |
 +-----------------------------------------------------------------------+
        | 1
        |
        | submits
        |
        | N
 +-----------------------------------------------------------------------+
 |                            APPLICATIONS                               |
 +-----------------------------------------------------------------------+
 | PK  application_id   INT AUTO_INCREMENT                               |
 | FK  student_id       INT (REFERENCES students.student_id ON DELETE)   |
 | FK  company_id       INT (REFERENCES companies.company_id ON DELETE)  |
 |     status           ENUM('APPLIED','SHORTLISTED',                    |
 |                           'INTERVIEW_SCHEDULED','ACCEPTED','REJECTED')|
 |     application_date TIMESTAMP                                        |
 |     remarks          VARCHAR(255)                                     |
 |     updated_at       TIMESTAMP                                        |
 | UQ  (student_id, company_id)  [One application per student per drive] |
 +-----------------------------------------------------------------------+
        | N
        |
        | receives
        |
        | 1
 +-----------------------------------------------------------------------+
 |                             COMPANIES                                 |
 +-----------------------------------------------------------------------+
 | PK  company_id            INT AUTO_INCREMENT                          |
 | UQ  company_name          VARCHAR(100)                                |
 |     industry              VARCHAR(50)                                 |
 |     job_role              VARCHAR(100)                                |
 |     package_lpa           DECIMAL(5,2)                                |
 |     min_cgpa              DECIMAL(4,2)                                |
 |     min_ssc_percent       DECIMAL(5,2)                                |
 |     min_hsc_percent       DECIMAL(5,2)                                |
 |     allow_backlogs        BOOLEAN                                     |
 |     location              VARCHAR(100)                                |
 |     registration_deadline DATE                                        |
 |     created_at            TIMESTAMP                                   |
 +-----------------------------------------------------------------------+
```

---

## Project Directory Structure

```
Student Placement Management System/
│
├── src/
│   └── com/
│       └── placement/
│           ├── config/
│           │   └── DBConnection.java          # Thread-safe Singleton JDBC Connection Factory
│           ├── model/
│           │   ├── Student.java               # Student Domain Model
│           │   ├── Company.java               # Company Domain Model
│           │   └── Application.java           # Application Model + Joined Report DTO
│           ├── dao/
│           │   ├── StudentDAO.java            # PreparedStatements CRUD & queries for students
│           │   ├── CompanyDAO.java            # PreparedStatements CRUD & queries for companies
│           │   └── ApplicationDAO.java        # Multi-table INNER JOINs & status updates
│           ├── exception/
│           │   ├── EligibilityException.java  # Custom checked exception with criteria breakdown
│           │   └── DatabaseOperationException.java # Custom runtime exception wrapping SQLExceptions
│           ├── service/
│           │   └── PlacementService.java      # Eligibility engine & business workflows
│           └── main/
│               └── MainApp.java               # Menu-driven, crash-resilient console CLI
│
├── sql/
│   └── schema.sql                             # DDL, indexes, constraints & realistic seed data
│
├── resources/
│   └── db.properties                          # Database connection properties
│
├── lib/                                       # Directory for mysql-connector-j-*.jar
│
├── compile.bat                                # Windows build script
├── run.bat                                    # Windows launcher script
├── compile.sh                                 # Linux/macOS build script
├── run.sh                                     # Linux/macOS launcher script
├── setup_db.bat                               # Automated MySQL schema importer
├── download_connector.bat                     # Automatic driver download utility
└── README.md                                  # Complete system documentation
```

---

## Eligibility Validation Engine

Before any student can apply for a company, `PlacementService.applyForCompany()` executes the following strict verification pipeline:

1. **Existence Verification**: Validates that both the student ID and company ID exist in the database.
2. **Duplicate Application Prevention**: Asserts that the student has not already applied to this company drive.
3. **Academic Threshold Evaluation**:
   - `student.getSscPercent() >= company.getMinSscPercent()`
   - `student.getHscPercent() >= company.getMinHscPercent()`
   - `student.getCgpa() >= company.getMinCgpa()`
4. **Active Backlog Policy**:
   - If `student.isHasBacklog() == true` AND `company.isAllowBacklogs() == false`, application is rejected.
5. **Drive Deadline**:
   - Validates that current date does not exceed `company.getRegistrationDeadline()`.
6. **Detailed Failure Diagnostics**:
   - If any criteria fail, an `EligibilityException` is thrown carrying **all** failing criteria in a formatted bulleted list, providing immediate transparent feedback to students and recruiters.

---

## Setup & Execution Guide

### 1. Prerequisites
- **Java Development Kit (JDK 17 or higher)**
- **MySQL Server (8.0 or higher)**
- **MySQL Connector/J JAR** (`mysql-connector-j-8.3.0.jar` or similar)

### 2. Configure Database & Seed Data

1. Open your terminal or MySQL command line and run the schema script:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
   *(Or on Windows, simply double-click `setup_db.bat`)*

2. Open `resources/db.properties` and verify your credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/placement_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   db.user=root
   db.password=your_mysql_password
   db.driver=com.mysql.cj.jdbc.Driver
   ```

### 3. Add MySQL Connector JAR

Place `mysql-connector-j-8.3.0.jar` into the `lib/` directory:
- Run `download_connector.bat` (Windows), OR
- Download manually from [Maven Central - MySQL Connector/J](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar) and save it inside `lib/`.

### 4. Compile the Application

#### Windows (Command Prompt / PowerShell):
```cmd
javac -d bin -sourcepath src src/com/placement/main/MainApp.java
copy /Y resources\db.properties bin\db.properties
```
*(Or run `compile.bat`)*

#### Linux / macOS:
```bash
chmod +x compile.sh run.sh
./compile.sh
```

### 5. Run the Application

#### Windows (Command Prompt / PowerShell):
```cmd
java -cp "bin;resources;lib/*" com.placement.main.MainApp
```
*(Or run `run.bat`)*

#### Linux / macOS:
```bash
java -cp "bin:resources:lib/*" com.placement.main.MainApp
```
*(Or run `./run.sh`)*

---

## Sample Multi-Table INNER JOIN Queries

### 1. Comprehensive Placement Status Report
Joins `applications`, `students`, and `companies` to produce an end-to-end audit report:
```sql
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
```

### 2. Applications Filtered by Specific Recruitment Drive
```sql
SELECT 
    a.application_id,
    s.roll_number,
    CONCAT(s.first_name, ' ', s.last_name) AS student_name,
    s.department,
    s.cgpa,
    s.ssc_percent,
    s.hsc_percent,
    a.status,
    a.remarks
FROM applications a
INNER JOIN students s ON a.student_id = s.student_id
INNER JOIN companies c ON a.company_id = c.company_id
WHERE c.company_id = 1
ORDER BY s.cgpa DESC;
```

### 3. Executive KPI Dashboard Aggregation
```sql
SELECT 
    COUNT(DISTINCT a.student_id) AS total_placed_students,
    ROUND(AVG(c.package_lpa), 2) AS average_ctc_lpa,
    MAX(c.package_lpa)          AS highest_ctc_lpa,
    MIN(c.package_lpa)          AS lowest_ctc_lpa
FROM applications a
INNER JOIN companies c ON a.company_id = c.company_id
WHERE a.status = 'ACCEPTED';
```

---

## Menu Architecture & Features

```
+-------------------------------------------------------------+
|                      MAIN MENU                              |
+-------------------------------------------------------------+
| 1. Student Management                                       |
|    - Register New Student (validates unique roll & email)   |
|    - View All Students (tabular output with formatting)     |
|    - Search by Roll Number or Primary Key                   |
|    - Update Academic Records & Backlog Counts               |
|    - Delete Student Record (cascades to applications)       |
|                                                             |
| 2. Company Management                                       |
|    - Register New Drive (Package LPA, CGPA, Backlog policy) |
|    - View All Drives & Hiring Deadlines                     |
|    - Search Company by ID or Name                           |
|    - Update Hiring Criteria / Offered Package               |
|    - Delete Recruitment Drive                               |
|                                                             |
| 3. Placement & Application Operations                       |
|    - Submit Application (Automated Eligibility Evaluation)  |
|    - Check Student Eligibility Against All Companies        |
|    - View Eligible Candidate Pool for Specific Company      |
|    - Update Application Status (Shortlist/Accept/Reject)    |
|    - Withdraw / Cancel Application                          |
|                                                             |
| 4. Reports & Analytics (INNER JOIN Queries)                 |
|    - Full Placement Roster (Joined Student + Company View)  |
|    - Applications Filtered by Company                       |
|    - Applications Filtered by Student                       |
|    - Filter by Status (APPLIED, ACCEPTED, REJECTED, etc.)   |
|    - Executive Placement KPI & Salary Metrics Dashboard     |
|                                                             |
| 5. Verify Database Connection Health                        |
| 0. Exit System                                              |
+-------------------------------------------------------------+
```

---

## Defensive Programming Standards Applied

1. **Thread-Safe Singleton Connection Manager**:
   - Uses double-checked locking with `volatile` instance variable in `DBConnection`.
   - Dynamic configuration loader supporting both classpath and filesystem fallbacks.
2. **Leak-Free Resource Management**:
   - Every single database call encapsulates `Connection`, `PreparedStatement`, and `ResultSet` within `try-with-resources`.
3. **Crash-Proof Console Input**:
   - Custom scanner reader methods handle non-numeric input gracefully, looping until valid input is received instead of throwing unhandled `InputMismatchException`s.
4. **Data Integrity & Relational Constraints**:
   - Database level `CHECK` constraints on CGPA (0.00-10.00) and percentages (0.00-100.00).
   - `UNIQUE` compound index on `(student_id, company_id)` preventing duplicate applications at the engine level.
   - Foreign key constraints with `ON DELETE CASCADE`.
