
package com.salonbook;

import com.salonbook.dao.DatabaseConnection;
import com.salonbook.dao.UserDao;
import com.salonbook.ui.Menu;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("       WELCOME TO SALONBOOK");
        System.out.println("====================================");

        // CHECK DATABASE CONNECTION
        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("Database connected successfully!");

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return;
        }

        // CREATE USER TABLE
        UserDao userDao = new UserDao();
        userDao.createTable();

        // START APPLICATION
        Scanner scanner = new Scanner(System.in);

        Menu menu = new Menu(userDao, scanner);
        menu.showMainMenu();

        scanner.close();

        System.out.println("\nSalonBook application closed.");
    }
}
