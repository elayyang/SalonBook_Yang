
package com.salonbook;

import com.salonbook.dao.DatabaseInitializer;
import com.salonbook.dao.UserDao;
import com.salonbook.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("       WELCOME TO SALONBOOK SYSTEM");
        System.out.println("========================================");

        // STEP 1: INITIALIZE DATABASE
        try {

            DatabaseInitializer.initializeDatabase();

            System.out.println("Database initialization completed!");

        } catch (RuntimeException e) {

            System.out.println("Unable to start SalonBook.");
            System.out.println("Database Error: " + e.getMessage());

            return;
        }

        // STEP 2: INSTANTIATE DAO
        UserDao userDao = new UserDao();

        // STEP 3: CREATE ONE SHARED SCANNER
        Scanner scanner = new Scanner(System.in);

        // STEP 4: INSTANTIATE AUTHENTICATION
        Authentication authentication =
                new Authentication(userDao, scanner);

        // STEP 5: DELEGATE EXECUTION TO AUTHENTICATION
        try {

            authentication.start();

        } finally {

            // CLOSE SHARED SCANNER WHEN APPLICATION EXITS
            scanner.close();

            System.out.println("\n========================================");
            System.out.println("     SALONBOOK APPLICATION CLOSED");
            System.out.println("========================================");
        }
    }
}
