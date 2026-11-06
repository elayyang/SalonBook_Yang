
package com.salonbook.ui;

import com.salonbook.dao.UserDao;
import com.salonbook.model.User;

import java.util.List;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    // CONSTRUCTOR
    // Uses shared UserDao and Scanner instances.
    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    // START AUTHENTICATION SYSTEM
    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("             SALONBOOK SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> registerAccount();

                case "2" -> loginAccount();

                case "3" -> {
                    System.out.println("\nThank you for using SalonBook!");
                    running = false;
                }

                default ->
                        System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==========================================
    // REGISTER ACCOUNT
    // ==========================================

    private void registerAccount() {

        System.out.println("\n========================================");
        System.out.println("             REGISTER ACCOUNT");
        System.out.println("========================================");

        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (fullName.isEmpty()
                || username.isEmpty()
                || password.isBlank()) {

            System.out.println("All fields are required.");
            return;
        }

        System.out.println("\n========== SELECT USER ROLE ==========");
        System.out.println("1. ADMINISTRATOR");
        System.out.println("2. RECEPTIONIST");
        System.out.println("3. SALON STAFF");
        System.out.print("Select Role: ");

        String roleChoice = scanner.nextLine();

        String role = switch (roleChoice) {
            case "1" -> "ADMINISTRATOR";
            case "2" -> "RECEPTIONIST";
            case "3" -> "SALON STAFF";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role selection.");
            return;
        }

        User newUser = new User(
                0,
                username,
                password,
                fullName,
                role,
                "PENDING"
        );

        boolean registered = userDao.registerUser(newUser);

        if (registered) {

            System.out.println("\n========================================");
            System.out.println("       REGISTRATION SUCCESSFUL!");
            System.out.println("========================================");
            System.out.println("Full Name: " + fullName);
            System.out.println("Username: " + username);
            System.out.println("Role: " + role);
            System.out.println("Account Status: PENDING");
            System.out.println("----------------------------------------");
            System.out.println("Please wait for Administrator approval.");

        } else {

            System.out.println("\nRegistration failed.");
            System.out.println("Username may already exist or information is invalid.");
        }
    }

    // ==========================================
    // LOGIN ACCOUNT
    // ==========================================

    private void loginAccount() {

        System.out.println("\n========================================");
        System.out.println("                  LOGIN");
        System.out.println("========================================");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {

            System.out.println("\nInvalid username or password.");
            return;
        }

        // CHECK ACCOUNT STATUS

        if (user.isPending()) {

            System.out.println("\nYour account is still PENDING.");
            System.out.println("Please wait for Administrator approval.");
            return;
        }

        if (user.isDeactivated()) {

            System.out.println("\nYour account has been DEACTIVATED.");
            System.out.println("Please contact the Administrator.");
            return;
        }

        if (!user.isApproved()) {

            System.out.println("\nYour account cannot access the system.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("             LOGIN SUCCESSFUL!");
        System.out.println("========================================");
        System.out.println("Welcome, " + user.fullName());
        System.out.println("Role: " + user.role());

        // ROUTE USER BASED ON ROLE

        routeUser(user);
    }

    // ==========================================
    // ROLE-BASED ROUTING
    // ==========================================

    private void routeUser(User user) {

        if (user.isAdministrator()) {

            administratorMenu(user);

        } else if (user.isReceptionist()) {

            receptionistMenu(user);

        } else if (user.isSalonStaff()) {

            salonStaffMenu(user);

        } else {

            System.out.println("Unrecognized user role.");
        }
    }

    // ==========================================
    // ADMINISTRATOR MENU
    // ==========================================

    private void administratorMenu(User admin) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println("\n========================================");
            System.out.println("          ADMINISTRATOR MENU");
            System.out.println("========================================");
            System.out.println("Welcome: " + admin.fullName());
            System.out.println("1. View Pending Accounts");
            System.out.println("2. Approve Account");
            System.out.println("3. View All Accounts");
            System.out.println("4. Logout");
            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> viewPendingAccounts();

                case "2" -> approveAccount();

                case "3" -> viewAllAccounts();

                case "4" -> {
                    System.out.println("\nAdministrator logged out successfully.");
                    loggedIn = false;
                }

                default ->
                        System.out.println("Invalid choice.");
            }
        }
    }

    // ==========================================
    // VIEW PENDING ACCOUNTS
    // ==========================================

    private void viewPendingAccounts() {

        System.out.println("\n========== PENDING ACCOUNTS ==========");

        List<User> users = userDao.getPendingUsers();

        if (users.isEmpty()) {
            System.out.println("No pending accounts found.");
            return;
        }

        for (User user : users) {

            System.out.println("----------------------------------------");
            System.out.println("User ID: " + user.userId());
            System.out.println("Full Name: " + user.fullName());
            System.out.println("Username: " + user.username());
            System.out.println("Role: " + user.role());
            System.out.println("Status: " + user.status());
        }

        System.out.println("----------------------------------------");
    }

    // ==========================================
    // APPROVE ACCOUNT
    // ==========================================

    private void approveAccount() {

        System.out.println("\n========== APPROVE ACCOUNT ==========");

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("There are no pending accounts to approve.");
            return;
        }

        for (User user : pendingUsers) {

            System.out.println("----------------------------------------");
            System.out.println("ID: " + user.userId());
            System.out.println("Name: " + user.fullName());
            System.out.println("Role: " + user.role());
            System.out.println("Status: " + user.status());
        }

        System.out.print("\nEnter User ID to approve: ");

        try {

            int userId = Integer.parseInt(scanner.nextLine());

            boolean approved = userDao.approveUser(userId);

            if (approved) {

                System.out.println("\nAccount approved successfully!");
                System.out.println("The user can now log in.");

            } else {

                System.out.println("\nAccount approval failed.");
                System.out.println("Check the User ID and account status.");
            }

        } catch (NumberFormatException e) {

            System.out.println("Please enter a valid numeric User ID.");
        }
    }

    // ==========================================
    // VIEW ALL ACCOUNTS
    // ==========================================

    private void viewAllAccounts() {

        System.out.println("\n========== ALL REGISTERED ACCOUNTS ==========");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (User user : users) {

            System.out.println("----------------------------------------");
            System.out.println("User ID: " + user.userId());
            System.out.println("Full Name: " + user.fullName());
            System.out.println("Username: " + user.username());
            System.out.println("Role: " + user.role());
            System.out.println("Status: " + user.status());
        }

        System.out.println("----------------------------------------");
    }

    // ==========================================
    // RECEPTIONIST MENU
    // ==========================================

    private void receptionistMenu(User user) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println("\n========================================");
            System.out.println("           RECEPTIONIST MENU");
            System.out.println("========================================");
            System.out.println("Welcome: " + user.fullName());
            System.out.println("1. View Account Information");
            System.out.println("2. Logout");
            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> viewAccountInformation(user);

                case "2" -> {
                    System.out.println("Receptionist logged out successfully.");
                    loggedIn = false;
                }

                default ->
                        System.out.println("Invalid choice.");
            }
        }
    }

    // ==========================================
    // SALON STAFF MENU
    // ==========================================

    private void salonStaffMenu(User user) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println("\n========================================");
            System.out.println("             SALON STAFF MENU");
            System.out.println("========================================");
            System.out.println("Welcome: " + user.fullName());
            System.out.println("1. View Account Information");
            System.out.println("2. Logout");
            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> viewAccountInformation(user);

                case "2" -> {
                    System.out.println("Salon Staff logged out successfully.");
                    loggedIn = false;
                }

                default ->
                        System.out.println("Invalid choice.");
            }
        }
    }

    // ==========================================
    // VIEW ACCOUNT INFORMATION
    // ==========================================

    private void viewAccountInformation(User user) {

        System.out.println("\n========== ACCOUNT INFORMATION ==========");
        System.out.println("User ID: " + user.userId());
        System.out.println("Full Name: " + user.fullName());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.role());
        System.out.println("Status: " + user.status());
    }
}
