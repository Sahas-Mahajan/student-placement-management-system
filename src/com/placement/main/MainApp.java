package com.placement.main;

import com.placement.config.DBConnection;
import com.placement.exception.EligibilityException;
import com.placement.model.Application;
import com.placement.model.Application.ApplicationStatus;
import com.placement.model.Company;
import com.placement.model.Student;
import com.placement.service.PlacementService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console-based CLI Application for the Student Placement Management System.
 * Provides a production-grade, menu-driven interface with robust input validation,
 * tabular data rendering, and structured error handling.
 */
public class MainApp {

    private final PlacementService placementService;
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MainApp() {
        this.placementService = new PlacementService();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.start();
    }

    public void start() {
        printBanner();
        checkDatabaseHealth();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Select an option (0-5): ", 0, 5);

            try {
                switch (choice) {
                    case 1 -> handleStudentMenu();
                    case 2 -> handleCompanyMenu();
                    case 3 -> handlePlacementMenu();
                    case 4 -> handleReportsMenu();
                    case 5 -> handleTestConnection();
                    case 0 -> {
                        System.out.println("\nExiting Student Placement Management System. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("\n[ERROR] An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // Menus
    // =========================================================================

    private void printBanner() {
        System.out.println("""
            ================================================================================
             STUDENT PLACEMENT MANAGEMENT SYSTEM (SPMS)
             Production Core Java 17+ | Pure JDBC | MySQL Database Engine
            ================================================================================
            """);
    }

    private void checkDatabaseHealth() {
        System.out.print("[System Check] Testing MySQL Database Connection... ");
        try {
            boolean isConnected = DBConnection.getInstance().testConnection();
            if (isConnected) {
                System.out.println("SUCCESS [Connected to " + DBConnection.getInstance().getUrl() + "]");
            } else {
                System.out.println("WARNING [Cannot verify connection. Please verify MySQL service and credentials in db.properties]");
            }
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
        }
    }

    private void printMainMenu() {
        System.out.println("""

            +-------------------------------------------------------------+
            |                      MAIN MENU                              |
            +-------------------------------------------------------------+
            | 1. Student Management                                       |
            | 2. Company Management                                       |
            | 3. Placement & Application Operations                       |
            | 4. Reports & Placement Analytics (INNER JOIN Reports)       |
            | 5. Verify Database Connection Health                        |
            | 0. Exit System                                              |
            +-------------------------------------------------------------+
            """);
    }

    // =========================================================================
    // 1. Student Management Flow
    // =========================================================================

    private void handleStudentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""

                +-------------------------------------------------------------+
                |                    STUDENT MANAGEMENT                       |
                +-------------------------------------------------------------+
                | 1. Register New Student                                     |
                | 2. View All Registered Students                             |
                | 3. Search Student by Roll Number                            |
                | 4. Search Student by ID                                     |
                | 5. Update Student Record                                    |
                | 6. Delete Student Record                                    |
                | 0. Back to Main Menu                                        |
                +-------------------------------------------------------------+
                """);

            int opt = readInt("Enter choice (0-6): ", 0, 6);
            switch (opt) {
                case 1 -> registerNewStudent();
                case 2 -> viewAllStudents();
                case 3 -> searchStudentByRoll();
                case 4 -> searchStudentById();
                case 5 -> updateStudentRecord();
                case 6 -> deleteStudentRecord();
                case 0 -> back = true;
            }
        }
    }

    private void registerNewStudent() {
        System.out.println("\n--- [Register New Student] ---");
        String roll = readString("Enter Roll Number (e.g. CS2024099): ");
        String fName = readString("Enter First Name: ");
        String lName = readString("Enter Last Name: ");
        String email = readString("Enter Email: ");
        String dept = readString("Enter Department (e.g. Computer Science, IT): ");
        double cgpa = readDouble("Enter CGPA (0.00 - 10.00): ", 0.0, 10.0);
        double ssc = readDouble("Enter SSC Percentage (0.00 - 100.00): ", 0.0, 100.0);
        double hsc = readDouble("Enter HSC Percentage (0.00 - 100.00): ", 0.0, 100.0);
        int backlogs = readInt("Enter Active Backlogs count (0 for none): ", 0, 50);
        String phone = readString("Enter Phone Number: ");

        Student student = new Student(roll, fName, lName, email, dept, cgpa, ssc, hsc, backlogs > 0, backlogs, phone);

        try {
            Student saved = placementService.registerStudent(student);
            System.out.println("\n[SUCCESS] Student registered successfully! Assigned Student ID: " + saved.getStudentId());
        } catch (Exception e) {
            System.err.println("\n[FAILED] Registration failed: " + e.getMessage());
        }
    }

    private void viewAllStudents() {
        List<Student> students = placementService.getAllStudents();
        renderStudentsTable(students);
    }

    private void searchStudentByRoll() {
        String roll = readString("Enter Roll Number to search: ");
        Optional<Student> studentOpt = placementService.getStudentByRollNumber(roll);
        if (studentOpt.isPresent()) {
            renderStudentsTable(List.of(studentOpt.get()));
        } else {
            System.out.println("\n[NOT FOUND] No student found with Roll Number: " + roll);
        }
    }

    private void searchStudentById() {
        int id = readInt("Enter Student ID: ", 1, Integer.MAX_VALUE);
        Optional<Student> studentOpt = placementService.getStudentById(id);
        if (studentOpt.isPresent()) {
            renderStudentsTable(List.of(studentOpt.get()));
        } else {
            System.out.println("\n[NOT FOUND] No student found with ID: " + id);
        }
    }

    private void updateStudentRecord() {
        int id = readInt("Enter Student ID to update: ", 1, Integer.MAX_VALUE);
        Optional<Student> studentOpt = placementService.getStudentById(id);
        if (studentOpt.isEmpty()) {
            System.out.println("\n[NOT FOUND] Student with ID " + id + " does not exist.");
            return;
        }

        Student s = studentOpt.get();
        System.out.println("Updating details for: " + s.getFullName() + " (" + s.getRollNumber() + ")");
        System.out.println("Press ENTER to keep existing values.");

        String roll = readOptionalString("Roll Number [" + s.getRollNumber() + "]: ", s.getRollNumber());
        String fName = readOptionalString("First Name [" + s.getFirstName() + "]: ", s.getFirstName());
        String lName = readOptionalString("Last Name [" + s.getLastName() + "]: ", s.getLastName());
        String email = readOptionalString("Email [" + s.getEmail() + "]: ", s.getEmail());
        String dept = readOptionalString("Department [" + s.getDepartment() + "]: ", s.getDepartment());
        double cgpa = readOptionalDouble("CGPA [" + s.getCgpa() + "]: ", s.getCgpa(), 0.0, 10.0);
        double ssc = readOptionalDouble("SSC % [" + s.getSscPercent() + "]: ", s.getSscPercent(), 0.0, 100.0);
        double hsc = readOptionalDouble("HSC % [" + s.getHscPercent() + "]: ", s.getHscPercent(), 0.0, 100.0);
        int backlogs = readOptionalInt("Active Backlogs [" + s.getActiveBacklogs() + "]: ", s.getActiveBacklogs(), 0, 50);
        String phone = readOptionalString("Phone [" + s.getPhone() + "]: ", s.getPhone());

        s.setRollNumber(roll);
        s.setFirstName(fName);
        s.setLastName(lName);
        s.setEmail(email);
        s.setDepartment(dept);
        s.setCgpa(cgpa);
        s.setSscPercent(ssc);
        s.setHscPercent(hsc);
        s.setActiveBacklogs(backlogs);
        s.setPhone(phone);

        try {
            boolean updated = placementService.updateStudent(s);
            if (updated) {
                System.out.println("\n[SUCCESS] Student ID " + id + " updated successfully!");
            } else {
                System.out.println("\n[FAILED] Update failed.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] Update failed: " + e.getMessage());
        }
    }

    private void deleteStudentRecord() {
        int id = readInt("Enter Student ID to delete: ", 1, Integer.MAX_VALUE);
        boolean confirm = readBoolean("Are you sure you want to delete Student ID " + id + "? (y/n): ");
        if (!confirm) {
            System.out.println("Operation canceled.");
            return;
        }

        try {
            boolean deleted = placementService.deleteStudent(id);
            if (deleted) {
                System.out.println("\n[SUCCESS] Student ID " + id + " deleted successfully.");
            } else {
                System.out.println("\n[NOT FOUND] Student ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] Delete failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. Company Management Flow
    // =========================================================================

    private void handleCompanyMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""

                +-------------------------------------------------------------+
                |                    COMPANY MANAGEMENT                       |
                +-------------------------------------------------------------+
                | 1. Register New Company / Placement Drive                   |
                | 2. View All Registered Companies                            |
                | 3. Search Company by ID                                     |
                | 4. Search Company by Name                                   |
                | 5. Update Company Criteria / Package                        |
                | 6. Delete Company Drive                                     |
                | 0. Back to Main Menu                                        |
                +-------------------------------------------------------------+
                """);

            int opt = readInt("Enter choice (0-6): ", 0, 6);
            switch (opt) {
                case 1 -> registerNewCompany();
                case 2 -> viewAllCompanies();
                case 3 -> searchCompanyById();
                case 4 -> searchCompanyByName();
                case 5 -> updateCompanyRecord();
                case 6 -> deleteCompanyRecord();
                case 0 -> back = true;
            }
        }
    }

    private void registerNewCompany() {
        System.out.println("\n--- [Register New Company / Drive] ---");
        String name = readString("Company Name: ");
        String industry = readString("Industry (e.g. IT, FinTech, Automotive): ");
        String role = readString("Job Role (e.g. Software Development Engineer): ");
        double pkg = readDouble("Package in LPA (e.g. 14.50): ", 0.0, 200.0);
        double minCgpa = readDouble("Minimum CGPA required (0.0 - 10.0): ", 0.0, 10.0);
        double minSsc = readDouble("Minimum SSC % required (0.0 - 100.0): ", 0.0, 100.0);
        double minHsc = readDouble("Minimum HSC % required (0.0 - 100.0): ", 0.0, 100.0);
        boolean allowBacklogs = readBoolean("Are active backlogs permitted? (y/n): ");
        String location = readString("Job Location: ");
        LocalDate deadline = readOptionalDate("Registration Deadline (YYYY-MM-DD or press Enter for open): ");

        Company comp = new Company(name, industry, role, pkg, minCgpa, minSsc, minHsc, allowBacklogs, location, deadline);

        try {
            Company saved = placementService.registerCompany(comp);
            System.out.println("\n[SUCCESS] Company registered successfully! Assigned Company ID: " + saved.getCompanyId());
        } catch (Exception e) {
            System.err.println("\n[FAILED] Registration failed: " + e.getMessage());
        }
    }

    private void viewAllCompanies() {
        List<Company> companies = placementService.getAllCompanies();
        renderCompaniesTable(companies);
    }

    private void searchCompanyById() {
        int id = readInt("Enter Company ID: ", 1, Integer.MAX_VALUE);
        Optional<Company> compOpt = placementService.getCompanyById(id);
        if (compOpt.isPresent()) {
            renderCompaniesTable(List.of(compOpt.get()));
        } else {
            System.out.println("\n[NOT FOUND] No company found with ID: " + id);
        }
    }

    private void searchCompanyByName() {
        String name = readString("Enter Company Name: ");
        Optional<Company> compOpt = placementService.getCompanyByName(name);
        if (compOpt.isPresent()) {
            renderCompaniesTable(List.of(compOpt.get()));
        } else {
            System.out.println("\n[NOT FOUND] No company found with name: " + name);
        }
    }

    private void updateCompanyRecord() {
        int id = readInt("Enter Company ID to update: ", 1, Integer.MAX_VALUE);
        Optional<Company> compOpt = placementService.getCompanyById(id);
        if (compOpt.isEmpty()) {
            System.out.println("\n[NOT FOUND] Company ID " + id + " does not exist.");
            return;
        }

        Company c = compOpt.get();
        System.out.println("Updating Drive details for: " + c.getCompanyName());
        System.out.println("Press ENTER to keep existing values.");

        String name = readOptionalString("Company Name [" + c.getCompanyName() + "]: ", c.getCompanyName());
        String industry = readOptionalString("Industry [" + c.getIndustry() + "]: ", c.getIndustry());
        String role = readOptionalString("Job Role [" + c.getJobRole() + "]: ", c.getJobRole());
        double pkg = readOptionalDouble("Package LPA [" + c.getPackageLpa() + "]: ", c.getPackageLpa(), 0.0, 200.0);
        double minCgpa = readOptionalDouble("Min CGPA [" + c.getMinCgpa() + "]: ", c.getMinCgpa(), 0.0, 10.0);
        double minSsc = readOptionalDouble("Min SSC % [" + c.getMinSscPercent() + "]: ", c.getMinSscPercent(), 0.0, 100.0);
        double minHsc = readOptionalDouble("Min HSC % [" + c.getMinHscPercent() + "]: ", c.getMinHscPercent(), 0.0, 100.0);
        boolean backlogs = readOptionalBoolean("Allow Backlogs [" + (c.isAllowBacklogs() ? "y" : "n") + "] (y/n): ", c.isAllowBacklogs());
        String location = readOptionalString("Location [" + c.getLocation() + "]: ", c.getLocation());

        c.setCompanyName(name);
        c.setIndustry(industry);
        c.setJobRole(role);
        c.setPackageLpa(pkg);
        c.setMinCgpa(minCgpa);
        c.setMinSscPercent(minSsc);
        c.setMinHscPercent(minHsc);
        c.setAllowBacklogs(backlogs);
        c.setLocation(location);

        try {
            boolean updated = placementService.updateCompany(c);
            if (updated) {
                System.out.println("\n[SUCCESS] Company ID " + id + " updated successfully!");
            } else {
                System.out.println("\n[FAILED] Update failed.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] Update failed: " + e.getMessage());
        }
    }

    private void deleteCompanyRecord() {
        int id = readInt("Enter Company ID to delete: ", 1, Integer.MAX_VALUE);
        boolean confirm = readBoolean("Are you sure? This will delete associated applications! (y/n): ");
        if (!confirm) {
            System.out.println("Operation canceled.");
            return;
        }

        try {
            boolean deleted = placementService.deleteCompany(id);
            if (deleted) {
                System.out.println("\n[SUCCESS] Company ID " + id + " deleted successfully.");
            } else {
                System.out.println("\n[NOT FOUND] Company ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] Delete failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. Placement & Application Operations Flow
    // =========================================================================

    private void handlePlacementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""

                +-------------------------------------------------------------+
                |             PLACEMENT & APPLICATION OPERATIONS              |
                +-------------------------------------------------------------+
                | 1. Submit Placement Application (Eligibility Validated)     |
                | 2. Check Student Eligibility For All Companies              |
                | 3. Check Eligible Students For a Specific Company           |
                | 4. Update Application Status (Shortlist/Accept/Reject)      |
                | 5. Withdraw / Delete Application                            |
                | 0. Back to Main Menu                                        |
                +-------------------------------------------------------------+
                """);

            int opt = readInt("Enter choice (0-5): ", 0, 5);
            switch (opt) {
                case 1 -> submitApplication();
                case 2 -> checkStudentEligibilityForAll();
                case 3 -> checkEligibleStudentsForCompany();
                case 4 -> updateApplicationStatus();
                case 5 -> withdrawApplication();
                case 0 -> back = true;
            }
        }
    }

    private void submitApplication() {
        System.out.println("\n--- [Submit Placement Application] ---");
        int studentId = readInt("Enter Student ID: ", 1, Integer.MAX_VALUE);
        int companyId = readInt("Enter Company ID: ", 1, Integer.MAX_VALUE);
        String remarks = readString("Enter Application Remarks / Notes (e.g. Applied via on-campus drive): ");

        try {
            Application app = placementService.applyForCompany(studentId, companyId, remarks);
            System.out.println("\n=============================================================");
            System.out.println(" [SUCCESS] Application Submitted Successfully!");
            System.out.println(" Assigned Application ID : " + app.getApplicationId());
            System.out.println(" Status                  : " + app.getStatus());
            System.out.println(" Timestamp               : " + app.getApplicationDate());
            System.out.println("=============================================================");
        } catch (EligibilityException ee) {
            System.err.println("\n=============================================================");
            System.err.println(" [ELIGIBILITY CRITERIA FAILED]");
            System.err.println(ee.getMessage());
            System.err.println("=============================================================");
        } catch (IllegalStateException ise) {
            System.err.println("\n[DUPLICATE APPLICATION] " + ise.getMessage());
        } catch (Exception e) {
            System.err.println("\n[ERROR] Could not submit application: " + e.getMessage());
        }
    }

    private void checkStudentEligibilityForAll() {
        int studentId = readInt("Enter Student ID: ", 1, Integer.MAX_VALUE);
        Optional<Student> studentOpt = placementService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            System.out.println("\n[NOT FOUND] Student with ID " + studentId + " does not exist.");
            return;
        }

        Student s = studentOpt.get();
        System.out.println("\nEligibility Evaluation for: " + s.getFullName() + " (" + s.getRollNumber() + ")");
        System.out.println("Academics: CGPA=" + s.getCgpa() + " | SSC=" + s.getSscPercent() + "% | HSC=" +
                           s.getHscPercent() + "% | Backlogs=" + s.getActiveBacklogs());

        List<Company> allCompanies = placementService.getAllCompanies();
        if (allCompanies.isEmpty()) {
            System.out.println("No companies currently registered.");
            return;
        }

        System.out.println("\n" + "-".repeat(110));
        System.out.printf("%-5s | %-22s | %-25s | %-10s | %-10s | %s%n",
            "CompID", "Company Name", "Job Role", "Package", "Status", "Details / Unmet Criteria");
        System.out.println("-".repeat(110));

        for (Company c : allCompanies) {
            PlacementService.EligibilityReport report = placementService.checkEligibility(studentId, c.getCompanyId());
            String status = report.isEligible() ? "[ELIGIBLE]" : "[INELIGIBLE]";
            String details = report.isEligible()
                ? "Qualifies for all requirements"
                : String.join("; ", report.unmetReasons());

            System.out.printf("%-6d | %-22s | %-25s | %-7.2f LPA | %-10s | %s%n",
                c.getCompanyId(),
                truncate(c.getCompanyName(), 22),
                truncate(c.getJobRole(), 25),
                c.getPackageLpa(),
                status,
                details
            );
        }
        System.out.println("-".repeat(110));
    }

    private void checkEligibleStudentsForCompany() {
        int companyId = readInt("Enter Company ID: ", 1, Integer.MAX_VALUE);
        Optional<Company> compOpt = placementService.getCompanyById(companyId);
        if (compOpt.isEmpty()) {
            System.out.println("\n[NOT FOUND] Company with ID " + companyId + " does not exist.");
            return;
        }

        Company c = compOpt.get();
        System.out.println("\nEligible Pool for: " + c.getCompanyName() + " (" + c.getJobRole() + ")");
        System.out.printf("Criteria: Min CGPA=%.2f | Min SSC=%.1f%% | Min HSC=%.1f%% | Backlogs Allowed=%s%n",
            c.getMinCgpa(), c.getMinSscPercent(), c.getMinHscPercent(), c.isAllowBacklogs() ? "Yes" : "No");

        List<Student> eligibleStudents = placementService.getEligibleStudentsForCompany(companyId);
        renderStudentsTable(eligibleStudents);
    }

    private void updateApplicationStatus() {
        int appId = readInt("Enter Application ID to update: ", 1, Integer.MAX_VALUE);
        System.out.println("""
            Select New Status:
            1. APPLIED
            2. SHORTLISTED
            3. INTERVIEW_SCHEDULED
            4. ACCEPTED (Hired / Offer Received)
            5. REJECTED
            """);

        int choice = readInt("Status choice (1-5): ", 1, 5);
        ApplicationStatus newStatus = switch (choice) {
            case 1 -> ApplicationStatus.APPLIED;
            case 2 -> ApplicationStatus.SHORTLISTED;
            case 3 -> ApplicationStatus.INTERVIEW_SCHEDULED;
            case 4 -> ApplicationStatus.ACCEPTED;
            case 5 -> ApplicationStatus.REJECTED;
            default -> ApplicationStatus.APPLIED;
        };

        String remarks = readString("Enter updated remarks: ");

        try {
            boolean updated = placementService.updateApplicationStatus(appId, newStatus, remarks);
            if (updated) {
                System.out.println("\n[SUCCESS] Application ID " + appId + " updated to status: " + newStatus);
            } else {
                System.out.println("\n[FAILED] Update failed.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] " + e.getMessage());
        }
    }

    private void withdrawApplication() {
        int appId = readInt("Enter Application ID to withdraw/delete: ", 1, Integer.MAX_VALUE);
        boolean confirm = readBoolean("Are you sure you want to withdraw Application ID " + appId + "? (y/n): ");
        if (!confirm) {
            System.out.println("Operation canceled.");
            return;
        }

        try {
            boolean deleted = placementService.withdrawApplication(appId);
            if (deleted) {
                System.out.println("\n[SUCCESS] Application ID " + appId + " withdrawn successfully.");
            } else {
                System.out.println("\n[NOT FOUND] Application ID " + appId + " does not exist.");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. Reports & Placement Analytics Flow (INNER JOIN Reports)
    // =========================================================================

    private void handleReportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""

                +-------------------------------------------------------------+
                |        REPORTS & PLACEMENT ANALYTICS (INNER JOIN)           |
                +-------------------------------------------------------------+
                | 1. Complete Placement Status Report (All Applications)      |
                | 2. Applications by Specific Company                         |
                | 3. Applications by Specific Student                         |
                | 4. Filter Applications by Status (e.g. ACCEPTED / SHORTLIST)|
                | 5. Executive Placement KPI & Salary Statistics Dashboard    |
                | 0. Back to Main Menu                                        |
                +-------------------------------------------------------------+
                """);

            int opt = readInt("Enter choice (0-5): ", 0, 5);
            switch (opt) {
                case 1 -> {
                    List<Application> list = placementService.getAllApplicationsReport();
                    renderApplicationsTable(list, "COMPLETE PLACEMENT APPLICATIONS REPORT (INNER JOIN)");
                }
                case 2 -> {
                    int cId = readInt("Enter Company ID: ", 1, Integer.MAX_VALUE);
                    List<Application> list = placementService.getApplicationsByCompany(cId);
                    renderApplicationsTable(list, "APPLICATIONS FOR COMPANY ID: " + cId);
                }
                case 3 -> {
                    int sId = readInt("Enter Student ID: ", 1, Integer.MAX_VALUE);
                    List<Application> list = placementService.getApplicationsByStudent(sId);
                    renderApplicationsTable(list, "APPLICATIONS FOR STUDENT ID: " + sId);
                }
                case 4 -> {
                    System.out.println("1. APPLIED  2. SHORTLISTED  3. INTERVIEW_SCHEDULED  4. ACCEPTED  5. REJECTED");
                    int st = readInt("Select status (1-5): ", 1, 5);
                    ApplicationStatus status = switch (st) {
                        case 1 -> ApplicationStatus.APPLIED;
                        case 2 -> ApplicationStatus.SHORTLISTED;
                        case 3 -> ApplicationStatus.INTERVIEW_SCHEDULED;
                        case 4 -> ApplicationStatus.ACCEPTED;
                        case 5 -> ApplicationStatus.REJECTED;
                        default -> ApplicationStatus.APPLIED;
                    };
                    List<Application> list = placementService.getApplicationsByStatus(status);
                    renderApplicationsTable(list, "APPLICATIONS WITH STATUS: " + status);
                }
                case 5 -> renderStatisticsDashboard();
                case 0 -> back = true;
            }
        }
    }

    private void renderStatisticsDashboard() {
        Map<String, Object> stats = placementService.getPlacementStatistics();

        System.out.println("\n" + "=".repeat(75));
        System.out.println("                   EXECUTIVE PLACEMENT KPI DASHBOARD");
        System.out.println("=".repeat(75));
        System.out.printf(" Total Registered Students       : %d%n", stats.getOrDefault("total_students", 0));
        System.out.printf(" Total Registered Companies      : %d%n", stats.getOrDefault("total_companies", 0));
        System.out.printf(" Total Placement Applications    : %d%n", stats.getOrDefault("total_applications", 0));
        System.out.println("-".repeat(75));
        System.out.printf(" Applications ACCEPTED (Offers)  : %d%n", stats.getOrDefault("status_ACCEPTED", 0));
        System.out.printf(" Applications SHORTLISTED        : %d%n", stats.getOrDefault("status_SHORTLISTED", 0));
        System.out.printf(" Interviews Scheduled            : %d%n", stats.getOrDefault("status_INTERVIEW_SCHEDULED", 0));
        System.out.printf(" Applications Pending (APPLIED)  : %d%n", stats.getOrDefault("status_APPLIED", 0));
        System.out.printf(" Applications REJECTED           : %d%n", stats.getOrDefault("status_REJECTED", 0));
        System.out.println("-".repeat(75));
        System.out.printf(" Unique Students Placed          : %d%n", stats.getOrDefault("placed_students", 0));
        System.out.printf(" Placement Success Rate          : %.2f%%%n", stats.getOrDefault("placement_rate_percent", 0.0));
        System.out.printf(" Highest CTC Offered             : %.2f LPA%n", stats.getOrDefault("highest_package", 0.0));
        System.out.printf(" Average CTC Offered             : %.2f LPA%n", stats.getOrDefault("avg_package", 0.0));
        System.out.printf(" Minimum CTC Offered             : %.2f LPA%n", stats.getOrDefault("lowest_package", 0.0));
        System.out.println("=".repeat(75));
    }

    // =========================================================================
    // 5. Connection Test
    // =========================================================================

    private void handleTestConnection() {
        System.out.println("\n--- [Database Connection Diagnostics] ---");
        System.out.println("Database URL : " + DBConnection.getInstance().getUrl());
        System.out.println("User         : " + DBConnection.getInstance().getUser());
        System.out.print("Testing connection... ");
        boolean ok = DBConnection.getInstance().testConnection();
        if (ok) {
            System.out.println("SUCCESS! Database is reachable and operational.");
        } else {
            System.out.println("FAILED. Verify MySQL is active on localhost:3306 and credentials in resources/db.properties are accurate.");
        }
    }

    // =========================================================================
    // Tabular Data Renderers
    // =========================================================================

    private void renderStudentsTable(List<Student> students) {
        if (students == null || students.isEmpty()) {
            System.out.println("\n[No student records found]");
            return;
        }

        System.out.println("\n" + "=".repeat(118));
        System.out.printf("%-4s | %-11s | %-20s | %-16s | %-5s | %-6s | %-6s | %-8s | %s%n",
            "ID", "Roll No", "Student Name", "Department", "CGPA", "SSC %", "HSC %", "Backlogs", "Email");
        System.out.println("=".repeat(118));

        for (Student s : students) {
            System.out.printf("%-4d | %-11s | %-20s | %-16s | %-5.2f | %-5.1f%% | %-5.1f%% | %-8s | %s%n",
                s.getStudentId(),
                s.getRollNumber(),
                truncate(s.getFullName(), 20),
                truncate(s.getDepartment(), 16),
                s.getCgpa(),
                s.getSscPercent(),
                s.getHscPercent(),
                s.getActiveBacklogs() > 0 ? (s.getActiveBacklogs() + " (Active)") : "0 (Clear)",
                s.getEmail()
            );
        }
        System.out.println("=".repeat(118));
        System.out.println("Total Records: " + students.size());
    }

    private void renderCompaniesTable(List<Company> companies) {
        if (companies == null || companies.isEmpty()) {
            System.out.println("\n[No company records found]");
            return;
        }

        System.out.println("\n" + "=".repeat(125));
        System.out.printf("%-4s | %-22s | %-25s | %-8s | %-8s | %-7s | %-7s | %-8s | %s%n",
            "ID", "Company Name", "Job Role", "Package", "Min CGPA", "Min SSC", "Min HSC", "Backlogs", "Deadline");
        System.out.println("=".repeat(125));

        for (Company c : companies) {
            System.out.printf("%-4d | %-22s | %-25s | %-5.2fLPA | %-8.2f | %-6.1f%% | %-6.1f%% | %-8s | %s%n",
                c.getCompanyId(),
                truncate(c.getCompanyName(), 22),
                truncate(c.getJobRole(), 25),
                c.getPackageLpa(),
                c.getMinCgpa(),
                c.getMinSscPercent(),
                c.getMinHscPercent(),
                c.isAllowBacklogs() ? "Allowed" : "No",
                c.getRegistrationDeadline() != null ? c.getRegistrationDeadline().toString() : "Open"
            );
        }
        System.out.println("=".repeat(125));
        System.out.println("Total Registered Drives: " + companies.size());
    }

    private void renderApplicationsTable(List<Application> applications, String title) {
        if (applications == null || applications.isEmpty()) {
            System.out.println("\n[" + title + ": No applications found]");
            return;
        }

        System.out.println("\n" + "=".repeat(132));
        System.out.println(" " + title);
        System.out.println("=".repeat(132));
        System.out.printf("%-5s | %-11s | %-18s | %-18s | %-20s | %-8s | %-19s | %s%n",
            "AppID", "Roll No", "Student Name", "Company", "Job Role", "Package", "Status", "Remarks");
        System.out.println("-".repeat(132));

        for (Application a : applications) {
            System.out.printf("%-5d | %-11s | %-18s | %-18s | %-20s | %-5.2fLPA | %-19s | %s%n",
                a.getApplicationId(),
                a.getStudentRollNumber() != null ? a.getStudentRollNumber() : "ID:" + a.getStudentId(),
                truncate(a.getStudentName() != null ? a.getStudentName() : "N/A", 18),
                truncate(a.getCompanyName() != null ? a.getCompanyName() : "ID:" + a.getCompanyId(), 18),
                truncate(a.getJobRole() != null ? a.getJobRole() : "N/A", 20),
                a.getPackageLpa(),
                a.getStatus(),
                truncate(a.getRemarks() != null ? a.getRemarks() : "-", 26)
            );
        }
        System.out.println("=".repeat(132));
        System.out.println("Total Applications: " + applications.size());
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }

    // =========================================================================
    // Robust Scanner Input Utilities (Crash-proof against non-numeric entries)
    // =========================================================================

    private String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.println("Input cannot be empty. Please enter a valid value.");
        }
    }

    private String readOptionalString(String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return defaultValue;
        }
        return line.trim();
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                int val = Integer.parseInt(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer format. Please try again.");
            }
        }
    }

    private int readOptionalInt(String prompt, int defaultValue, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                int val = Integer.parseInt(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer format. Please try again.");
            }
        }
    }

    private double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a number between %.2f and %.2f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric format. Please enter a valid decimal number.");
            }
        }
    }

    private double readOptionalDouble(String prompt, double defaultValue, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                double val = Double.parseDouble(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a number between %.2f and %.2f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric format. Please enter a valid decimal number.");
            }
        }
    }

    private boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line != null) {
                String clean = line.trim().toLowerCase();
                if (clean.equals("y") || clean.equals("yes") || clean.equals("true") || clean.equals("1")) {
                    return true;
                }
                if (clean.equals("n") || clean.equals("no") || clean.equals("false") || clean.equals("0")) {
                    return false;
                }
            }
            System.out.println("Please enter 'y' for yes or 'n' for no.");
        }
    }

    private boolean readOptionalBoolean(String prompt, boolean defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return defaultValue;
        }
        String clean = line.trim().toLowerCase();
        if (clean.equals("y") || clean.equals("yes") || clean.equals("true")) return true;
        if (clean.equals("n") || clean.equals("no") || clean.equals("false")) return false;
        return defaultValue;
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(line.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD (e.g., 2026-12-31).");
            }
        }
    }
}
