
package com.salonbook.ui;

import com.salonbook.dao.UserDao;
import com.salonbook.dao.ActivityLogDao;
import com.salonbook.model.User;
import com.salonbook.model.ActivityLog;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
        this.scanner = scanner;
    }

    // ADMIN MENU
    public void start(User admin) {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("          SALONBOOK ADMIN MENU");
            System.out.println("========================================");
            System.out.println("Administrator: " + admin.fullName());
            System.out.println("----------------------------------------");
            System.out.println("[1] View All System Users");
            System.out.println("[2] View Pending Registrations");
            System.out.println("[3] Approve User Account");
            System.out.println("[4] Update User Role and Status");
            System.out.println("[5] View System Audit Logs");
            System.out.println("[6] Logout");
            System.out.println("----------------------------------------");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {

                case 1:
                    viewAllUsers();
                    break;

                case 2:
                    viewPendingUsers();
                    break;

                case 3:
                    approveUserAccount(admin);
                    break;

                case 4:
                    updateUserAccount(admin);
                    break;

                case 5:
                    viewSystemAuditLogs(admin);
                    break;

                case 6:
                    running = false;
                    System.out.println("Administrator logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Please select 1-6.");
            }
        }
    }

    // VIEW ALL SYSTEM USERS
    private void viewAllUsers() {

        System.out.println("\n========== ALL SYSTEM USERS ==========");

        List<User> users = userDao.getAllUsers();

        displayUserTable(users);
    }

    // VIEW PENDING REGISTRATIONS
    private void viewPendingUsers() {

        System.out.println("\n========== PENDING REGISTRATIONS ==========");

        List<User> users = userDao.getPendingUsers();

        displayUserTable(users);
    }

    // APPROVE USER ACCOUNT
    private void approveUserAccount(User admin) {

        System.out.println("\n========== APPROVE USER ACCOUNT ==========");

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("No pending registrations found.");
            return;
        }

        displayUserTable(pendingUsers);

        System.out.println("\nEnter 0 to cancel approval.");

        int userId = readIntInput("Enter Target User ID: ");

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.userId() == userId);

        if (!exists) {
            System.out.println(
                    "User ID not found in pending registrations."
            );
            return;
        }

        boolean approved = userDao.approveUser(
                userId,
                admin.userId()
        );

        if (approved) {
            System.out.println(
                    "\nSUCCESS! User account has been approved."
            );
            System.out.println("Approved User ID: " + userId);
        } else {
            System.out.println(
                    "Unable to approve account. Please try again."
            );
        }
    }

    // UPDATE USER ROLE AND STATUS
    private void updateUserAccount(User currentAdmin) {

        System.out.println(
                "\n========== UPDATE USER ROLE AND STATUS =========="
        );

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No user records found.");
            return;
        }

        displayUserTable(users);

        System.out.println("\nEnter 0 at any step to cancel.");

        int targetUserId = readIntInput("Enter Target User ID: ");

        if (targetUserId == 0) {
            System.out.println("User update cancelled.");
            return;
        }

        User selectedUser = users.stream()
                .filter(user -> user.userId() == targetUserId)
                .findFirst()
                .orElse(null);

        if (selectedUser == null) {
            System.out.println("ERROR: User ID not found.");
            return;
        }

        // SELF-EDIT SECURITY RESTRICTION
        if (targetUserId == currentAdmin.userId()) {

            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted."
            );

            return;
        }

        System.out.println("\nSelected User:");
        System.out.println("User ID : " + selectedUser.userId());
        System.out.println("Username: " + selectedUser.username());
        System.out.println("Current Role: " + selectedUser.role());
        System.out.println("Current Status: " + selectedUser.status());

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("User update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("User update cancelled.");
            return;
        }

        System.out.println("\n========== UPDATE SUMMARY ==========");
        System.out.println("Target User ID : " + targetUserId);
        System.out.println("New Role       : " + newRole);
        System.out.println("New Status     : " + newStatus);
        System.out.println("------------------------------------");
        System.out.println("Enter 1 to confirm or 0 to cancel.");

        int confirmation = readIntInput("Your choice: ");

        if (confirmation == 0) {
            System.out.println("User update cancelled.");
            return;
        }

        if (confirmation != 1) {
            System.out.println(
                    "Invalid confirmation. Update cancelled."
            );
            return;
        }

        boolean updated = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus,
                currentAdmin.userId()
        );

        if (updated) {

            System.out.println("\n========================================");
            System.out.println("SUCCESS! USER ACCOUNT UPDATED.");
            System.out.println("========================================");
            System.out.println("User ID: " + targetUserId);
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);

        } else {

            System.out.println("Unable to update user account.");
        }
    }

    // SYSTEM AUDIT LOG MENU
    private void viewSystemAuditLogs(User admin) {

        boolean viewing = true;

        while (viewing) {

            System.out.println("\n========== SYSTEM AUDIT LOGS ==========");
            System.out.println("[1] View All System Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[3] Clear Historical Logs");
            System.out.println("[0] Back to Admin Menu");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {

                case 1:

                    System.out.println(
                            "\n========== ALL SYSTEM LOGS =========="
                    );

                    displayActivityLogs(
                            activityLogDao.getAllLogs()
                    );

                    break;

                case 2:

                    int userId = readIntInput(
                            "Enter User ID to filter: "
                    );

                    if (userId <= 0) {
                        System.out.println("Invalid User ID.");
                        break;
                    }

                    System.out.println(
                            "\n========== FILTERED SYSTEM LOGS =========="
                    );

                    displayActivityLogs(
                            activityLogDao.getLogsByUserId(userId)
                    );

                    break;

                case 3:

                    clearHistoricalLogs(admin);
                    break;

                case 0:

                    viewing = false;
                    break;

                default:

                    System.out.println(
                            "Invalid choice. Please select 0-3."
                    );
            }
        }
    }

    // CLEAR HISTORICAL LOGS WITH CONFIRMATION
    private void clearHistoricalLogs(User admin) {

        System.out.println(
                "\n========== CLEAR HISTORICAL LOGS =========="
        );

        System.out.println(
                "WARNING: This will delete historical activity logs."
        );

        System.out.println(
                "A record of this clearing action will be retained."
        );

        System.out.println("[1] Confirm");
        System.out.println("[0] Cancel");

        int confirmation = readIntInput("Enter choice: ");

        if (confirmation == 1) {

            try {

                activityLogDao.clearAllLogs(admin.userId());

            } catch (RuntimeException e) {

                System.out.println(
                        "Unable to clear historical logs: "
                                + e.getMessage()
                );
            }

        } else if (confirmation == 0) {

            System.out.println("Log clearing cancelled.");

        } else {

            System.out.println(
                    "Invalid choice. No logs were deleted."
            );
        }
    }

    // DISPLAY ACTIVITY LOG TABLE
    private void displayActivityLogs(List<ActivityLog> logs) {

        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        String border =
                "+-------+----------+--------------------+-------------------------------------+----------------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-8s | %-18s | %-35s | %-20s |%n",
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(border);

        for (ActivityLog log : logs) {

            String userId = log.userId() == null
                    ? "N/A"
                    : String.valueOf(log.userId());

            String description = log.description() == null
                    ? ""
                    : log.description();

            String actionType = log.actionType() == null
                    ? ""
                    : log.actionType();

            String timestamp = log.timestamp() == null
                    ? ""
                    : log.timestamp();

            if (description.length() > 35) {
                description = description.substring(0, 32) + "...";
            }

            if (actionType.length() > 18) {
                actionType = actionType.substring(0, 15) + "...";
            }

            if (timestamp.length() > 20) {
                timestamp = timestamp.substring(0, 20);
            }

            System.out.printf(
                    "| %-5d | %-8s | %-18s | %-35s | %-20s |%n",
                    log.id(),
                    userId,
                    actionType,
                    description,
                    timestamp
            );
        }

        System.out.println(border);

        System.out.println("Total logs: " + logs.size());
    }

    // INPUT VALIDATION: INTEGER
    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "INVALID INPUT! Please enter a valid number."
                );
            }
        }
    }

    // INPUT VALIDATION: ROLE
    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable Roles:");
            System.out.println("[1] ADMINISTRATOR");
            System.out.println("[2] RECEPTIONIST");
            System.out.println("[3] SALON STAFF");
            System.out.println("[0] Cancel");

            System.out.print("Enter New Role: ");

            String role = scanner.nextLine()
                    .trim()
                    .toUpperCase();

            switch (role) {

                case "1":
                case "ADMINISTRATOR":
                    return "ADMINISTRATOR";

                case "2":
                case "RECEPTIONIST":
                    return "RECEPTIONIST";

                case "3":
                case "SALON STAFF":
                    return "SALON STAFF";

                case "0":
                    return null;

                default:
                    System.out.println(
                            "INVALID ROLE! Please select a valid role."
                    );
            }
        }
    }

    // INPUT VALIDATION: STATUS
    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable Statuses:");
            System.out.println("[1] APPROVED");
            System.out.println("[2] PENDING");
            System.out.println("[0] Cancel");

            System.out.print("Enter New Status: ");

            String status = scanner.nextLine()
                    .trim()
                    .toUpperCase();

            switch (status) {

                case "1":
                case "APPROVED":
                    return "APPROVED";

                case "2":
                case "PENDING":
                    return "PENDING";

                case "0":
                    return null;

                default:
                    System.out.println(
                            "INVALID STATUS! Please select a valid status."
                    );
            }
        }
    }

    // FORMATTED USER TABLE
    private void displayUserTable(List<User> users) {

        if (users.isEmpty()) {
            System.out.println("No user records found.");
            return;
        }

        String border =
                "+-------+-----------------+----------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-15s | %-14s | %-10s |%n",
                "ID",
                "USERNAME",
                "ROLE",
                "STATUS"
        );

        System.out.println(border);

        for (User user : users) {

            System.out.printf(
                    "| %-5d | %-15s | %-14s | %-10s |%n",
                    user.userId(),
                    user.username(),
                    user.role(),
                    user.status()
            );
        }

        System.out.println(border);
        System.out.println("Total users: " + users.size());
    }
}
