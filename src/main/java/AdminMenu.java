
package com.salonbook.ui;

import com.salonbook.dao.UserDao;
import com.salonbook.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
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
            System.out.println("[5] Logout");
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
                    approveUserAccount();
                    break;

                case 4:
                    updateUserAccount(admin);
                    break;

                case 5:
                    running = false;
                    System.out.println("Administrator logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Please select 1-5.");
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
    private void approveUserAccount() {

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
            System.out.println("User ID not found in pending registrations.");
            return;
        }

        boolean approved = userDao.approveUser(userId);

        if (approved) {
            System.out.println("\nSUCCESS! User account has been approved.");
            System.out.println("Approved User ID: " + userId);
        } else {
            System.out.println("Unable to approve account. Please try again.");
        }
    }

    // UPDATE USER ROLE AND STATUS
    private void updateUserAccount(User currentAdmin) {

        System.out.println("\n========== UPDATE USER ROLE AND STATUS ==========");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No user records found.");
            return;
        }

        // Display all users
        displayUserTable(users);

        System.out.println("\nEnter 0 at any step to cancel.");

        // STEP 1: SELECT USER ID
        int targetUserId = readIntInput("Enter Target User ID: ");

        if (targetUserId == 0) {
            System.out.println("User update cancelled.");
            return;
        }

        // STEP 2: VERIFY USER EXISTS
        User selectedUser = users.stream()
                .filter(user -> user.userId() == targetUserId)
                .findFirst()
                .orElse(null);

        if (selectedUser == null) {
            System.out.println("ERROR: User ID not found.");
            return;
        }

        // STEP 3: SELF-EDIT SECURITY RESTRICTION
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

        // STEP 4: SELECT NEW ROLE
        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("User update cancelled.");
            return;
        }

        // STEP 5: SELECT NEW STATUS
        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("User update cancelled.");
            return;
        }

        // STEP 6: CONFIRM UPDATE
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
            System.out.println("Invalid confirmation. Update cancelled.");
            return;
        }

        // STEP 7: UPDATE DATABASE
        boolean updated = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus
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
            System.out.println("[1] ADMIN");
            System.out.println("[2] RECEPTIONIST");
            System.out.println("[3] SALON STAFF");
            System.out.println("[0] Cancel");

            System.out.print("Enter New Role: ");

            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return null;
            }

            if (role.equals("ADMIN")
                    || role.equals("RECEPTIONIST")
                    || role.equals("SALON STAFF")) {

                return role;
            }

            System.out.println(
                    "INVALID ROLE! Please enter ADMIN, RECEPTIONIST, or SALON STAFF."
            );
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

            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return null;
            }

            if (status.equals("APPROVED")
                    || status.equals("PENDING")) {

                return status;
            }

            System.out.println(
                    "INVALID STATUS! Please enter APPROVED or PENDING."
            );
        }
    }

    // FORMATTED USER TABLE
    private void displayUserTable(List<User> users) {

        if (users.isEmpty()) {
            System.out.println("No user records found.");
            return;
        }

        String border = "+-------+-----------------+--------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-15s | %-12s | %-10s |%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );

        System.out.println(border);

        for (User user : users) {

            System.out.printf(
                    "| %-5d | %-15s | %-12s | %-10s |%n",
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
