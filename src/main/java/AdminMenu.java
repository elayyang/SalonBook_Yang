
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
            System.out.println("[4] Logout");
            System.out.println("----------------------------------------");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    viewAllUsers();
                    break;

                case "2":
                    viewPendingUsers();
                    break;

                case "3":
                    approveUserAccount();
                    break;

                case "4":
                    running = false;
                    System.out.println("Administrator logged out.");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
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
        System.out.print("Enter Target User ID: ");

        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println("Approval cancelled.");
            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID. Please enter a number.");
            return;
        }

        // Verify that the selected ID is in the pending list
        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.userId() == userId);

        if (!exists) {
            System.out.println("User ID not found in pending registrations.");
            return;
        }

        // Update the database
        boolean approved = userDao.approveUser(userId);

        if (approved) {
            System.out.println("\nSUCCESS! User account has been approved.");
            System.out.println("Approved User ID: " + userId);
        } else {
            System.out.println("Unable to approve account. Please try again.");
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
