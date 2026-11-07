
package com.salonbook;

import com.salonbook.dao.DatabaseInitializer;
import com.salonbook.dao.UserDao;
import com.salonbook.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("              SALONBOOK");
        System.out.println("     MULTI-SERVICE BOOKING SYSTEM");
        System.out.println("========================================");

        // STEP 1: Initialize the database
        try {

            DatabaseInitializer.initializeDatabase();

            System.out.println("Database initialization completed!");

        } catch (RuntimeException e) {

            System.out.println("Unable to start SalonBook.");
            System.out.println("Database Error: " + e.getMessage());

            return;
        }

        // STEP 2: Create DAO
        UserDao userDao = new UserDao();

        // STEP 3: Create one shared Scanner
        Scanner scanner = new Scanner(System.in);

        // STEP 4: Create Authentication
        Authentication authentication =
                new Authentication(userDao, scanner);

        // STEP 5: Start the application
        try {

            authentication.start();

        } finally {

            scanner.close();

            System.out.println("SalonBook application closed.");
        }
    }
}
