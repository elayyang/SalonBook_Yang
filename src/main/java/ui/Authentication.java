
package com.salonbook.ui;

import com.salonbook.dao.UserDao;
import com.salonbook.dao.ActivityLogDao;
import com.salonbook.model.User;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.activityLogDao = new ActivityLogDao();
    }

    // MAIN AUTHENTICATION MENU
    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("              SALONBOOK");
            System.out.println("     MULTI-SERVICE BOOKING SYSTEM");
            System.out.println("========================================");
            System.out.println("[1] Register Account");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");
            System.out.println("----------------------------------------");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    registerAccount();
                    break;

                case "2":
                    loginAccount();
                    break;

                case "3":
                    running = false;
                    System.out.println("Thank you for using SalonBook!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // REGISTER ACCOUNT
    private void registerAccount() {

        System.out.println("\n========== ACCOUNT REGISTRATION ==========");

        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.println("\nSelect Account Role:");
        System.out.println("[1] Administrator");
        System.out.println("[2] Receptionist");
        System.out.println("[3] Salon Staff");
        System.out.print("Enter role: ");

        String roleChoice = scanner.nextLine().trim();

        String role;

        switch (roleChoice) {

            case "1":
                role = "ADMINISTRATOR";
                break;

            case "2":
                role = "RECEPTIONIST";
                break;

            case "3":
                role = "SALON STAFF";
                break;

            default:
                System.out.println("Invalid role selection.");
                return;
        }

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        try {

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
                System.out.println("\nRegistration successful!");
                System.out.println("Selected Role: " + role);
                System.out.println("Account Status: PENDING");
                System.out.println("Please wait for administrator approval.");
            } else {
                System.out.println("Registration failed.");
            }

        } catch (RuntimeException e) {
            System.out.println("Registration error: " + e.getMessage());
        }
    }

    // LOGIN ACCOUNT
    private void loginAccount() {

        System.out.println("\n========== SALONBOOK LOGIN ==========");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {

            User user = userDao.login(username, password);

            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }

            if (user.isPending()) {
                System.out.println("Your account is still awaiting approval.");
                return;
            }

            if (user.isDeactivated()) {
                System.out.println("Your account has been deactivated.");
                return;
            }

            if (user.isApproved()) {

                // RECORD SUCCESSFUL LOGIN
                activityLogDao.logAction(
                        user.userId(),
                        "USER_LOGIN",
                        "User " + user.username()
                                + " (ID: " + user.userId()
                                + ") logged in."
                );

                System.out.println("\nLogin successful!");
                System.out.println("Welcome, " + user.fullName() + "!");

                routeUser(user);
            }

        } catch (RuntimeException e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    // ROLE-BASED MENU ROUTING
    private void routeUser(User user) {

        if (user.isAdministrator()) {

            AdminMenu adminMenu = new AdminMenu(userDao, scanner);
            adminMenu.start(user);

            recordLogout(user);

        } else if (user.isReceptionist()) {

            receptionistMenu(user);

        } else if (user.isSalonStaff()) {

            salonStaffMenu(user);

        } else {

            System.out.println("Unrecognized account role.");
        }
    }

    // RECORD LOGOUT
    private void recordLogout(User user) {

        activityLogDao.logAction(
                user.userId(),
                "USER_LOGOUT",
                "User " + user.username()
                        + " (ID: " + user.userId()
                        + ") logged out."
        );

        System.out.println("Logout recorded successfully.");
    }

    // RECEPTIONIST MENU
    private void receptionistMenu(User user) {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("          RECEPTIONIST MENU");
            System.out.println("========================================");
            System.out.println("Welcome, " + user.fullName());
            System.out.println("[1] View Account Information");
            System.out.println("[2] Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    displayAccountInformation(user);
                    break;

                case "2":
                    running = false;
                    recordLogout(user);
                    System.out.println("Receptionist logged out.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // SALON STAFF MENU
    private void salonStaffMenu(User user) {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("           SALON STAFF MENU");
            System.out.println("========================================");
            System.out.println("Welcome, " + user.fullName());
            System.out.println("[1] View Account Information");
            System.out.println("[2] Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    displayAccountInformation(user);
                    break;

                case "2":
                    running = false;
                    recordLogout(user);
                    System.out.println("Salon Staff logged out.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // DISPLAY ACCOUNT INFORMATION
    private void displayAccountInformation(User user) {

        System.out.println("\n========== ACCOUNT INFORMATION ==========");
        System.out.println("User ID: " + user.userId());
        System.out.println("Full Name: " + user.fullName());
        System.out.println("Username: " + user.username());
        System.out.println("Role: " + user.role());
        System.out.println("Status: " + user.status());
    }
}
