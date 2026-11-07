
package com.salonbook.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {

        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL CHECK (
                        role IN ('ADMINISTRATOR', 'RECEPTIONIST', 'SALON STAFF')
                    ),
                    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (
                        status IN ('PENDING', 'APPROVED', 'DEACTIVATED')
                    ),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String clientsTable = """
                CREATE TABLE IF NOT EXISTS clients (
                    client_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    contact_number TEXT,
                    email TEXT,
                    address TEXT
                );
                """;

        String servicesTable = """
                CREATE TABLE IF NOT EXISTS services (
                    service_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    service_name TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL,
                    duration_minutes INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'AVAILABLE'
                );
                """;

        String appointmentsTable = """
                CREATE TABLE IF NOT EXISTS appointments (
                    appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    client_id INTEGER NOT NULL,
                    service_id INTEGER NOT NULL,
                    staff_id INTEGER NOT NULL,
                    appointment_date TEXT NOT NULL,
                    appointment_time TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',

                    FOREIGN KEY (client_id)
                        REFERENCES clients(client_id),

                    FOREIGN KEY (service_id)
                        REFERENCES services(service_id),

                    FOREIGN KEY (staff_id)
                        REFERENCES users(user_id)
                );
                """;

        String activityLogsTable = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT NOT NULL,
                    description TEXT,
                    log_date TEXT DEFAULT CURRENT_DATE,
                    log_time TEXT DEFAULT CURRENT_TIME,

                    FOREIGN KEY (user_id)
                        REFERENCES users(user_id)
                        ON DELETE SET NULL
                );
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(clientsTable);
            statement.execute(servicesTable);
            statement.execute(appointmentsTable);
            statement.execute(activityLogsTable);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_appointments_date
                    ON appointments(appointment_date);
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_activity_logs_user
                    ON activity_logs(user_id);
                    """);

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to initialize SalonBook database.", e
            );
        }
    }
}
