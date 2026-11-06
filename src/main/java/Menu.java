
package com.salonbook.ui;

import com.salonbook.dao.UserDao;
import com.salonbook.model.User;

import java.util.List;
import java.util.Scanner;

public class Menu {

    private final UserDao userDao;
    private final Scanner scanner;

    public Menu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    // MAIN MENU
    public void showMainMenu() {

        boolean running = true;

        while (running) {

            System.out.println("\n====================================");
            System.out.println("          SALONBOOK SYSTEM");
            System.out.println("====================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("====================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> registerAccount();
                case "2" -> loginAccount();
                case "3" -> {
                    System.out.println("Thank you for using SalonBook!");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // REGISTER
    private void registerAccount() {

        System.out.println("\n========== REGISTER ACCOUNT ==========");

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        System.out.println("\nSELECT ROLE");
        System.out.println("1. ADMINISTRATOR");
        System.out.println("2. RECEPTIONIST");
        System.out.println("3. SALON STAFF");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine();

        String role = switch (choice) {
            case "1" -> "ADMINISTRATOR";
            case "2" -> "RECEPTIONIST";
            case "3" -> "SALON STAFF";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role selection.");
            return;
        }

        User user = new User(
                0,
                username,
                password,
                fullName,
                role,
                "PENDING"
        );

        if (userDao.registerUser(user)) {
            System.out.println("\nAccount registered successfully!");
            System.out.println("Role: " + role);
            System.out.println("Status: PENDING");
            System.out.println("Wait for Administrator approval.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    // LOGIN
    private void loginAccount() {

        System.out.println("\n========== LOGIN ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        if (!user.status().equals("APPROVED")) {
            System.out.println("Your account is not approved.");
            System.out.println("Current Status: " + user.status());
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.fullName());

        showUserMenu(user);
    }

    // ROLE-BASED MENU
    private void showUserMenu(User user) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println("\n====================================");
            System.out.println("          SALONBOOK SYSTEM");
            System.out.println("====================================");
            System.out.println("Welcome: " + user.fullName());
            System.out.println("Role: " + user.role());

            if (user.role().equals("ADMINISTRATOR")) {

                System.out.println("1. View All Accounts");
                System.out.println("2. Approve Accounts");
                System.out.println("3. Logout");

            } else {

                System.out.println("1. View Account Information");
                System.out.println("2. Logout");
            }

            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            if (user.role().equals("ADMINISTRATOR")) {

                switch (choice) {
                    case "1" -> viewAccounts();
                    case "2" -> approveAccounts();
                    case "3" -> loggedIn = false;
                    default -> System.out.println("Invalid choice.");
                }

            } else {

                switch (choice) {
                    case "1" -> viewAccountInformation(user);
                    case "2" -> loggedIn = false;
                    default -> System.out.println("Invalid choice.");
                }
            }
        }

        System.out.println("Successfully logged out!");
    }

    // VIEW ACCOUNTS
    private void viewAccounts() {

        System.out.println("\n========== REGISTERED ACCOUNTS ==========");

        List<User> users = userDao.getAllUsers();

        for (User user : users) {

            System.out.println("--------------------------------");
            System.out.println("ID: " + user.userId());
            System.out.println("Name: " + user.fullName());
            System.out.println("Username: " + user.username());
            System.out.println("Role: " + user.role());
            System.out.println("Status: " + user.status());
        }

        if (users.isEmpty()) {
            System.out.println("No accounts found.");
        }
    }

    // APPROVE ACCOUNTS
    private void approveAccounts() {

        System.out.println("\n========== PENDING ACCOUNTS ==========");

        List<User> users = userDao.getAllUsers();

        boolean found = false;

        for (User user : users) {

            if (user.status().equals("PENDING")) {
                found = true;

                System.out.println("--------------------------------");
                System.out.println("ID: " + user.userId());
                System.out.println("Name: " + user.fullName());
                System.out.println("Username: " + user.username());
                System.out.println("Role: " + user.role());
            }
        }

        if (!found) {
            System.out.println("No pending accounts.");
            return;
        }

        System.out.print("Enter User ID to approve: ");

        try {
            int id = Integer.parseInt(scanner.nextLine());

            if (userDao.approveUser(id)) {
                System.out.println("Account approved successfully!");
            } else {
                System.out.println("Unable to approve account.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    // VIEW OWN ACCOUNT
    private void viewAccountInformation(User user) {

        System.out.println("\n========== ACCOUNT INFORMATION ==========");
        System.out.println("User ID: " + user.userId());
        System.out.println("Full Name: " + user.fullName());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.role());
        System.out.println("Status: " + user.status());
    }
}
