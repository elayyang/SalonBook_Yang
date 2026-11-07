
package com.salonbook.dao;

import java.sql.Connection;
import java.sql.ResultSet;
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
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action_type TEXT NOT NULL,
                    description TEXT,
                    timestamp TEXT DEFAULT CURRENT_TIMESTAMP,

                    FOREIGN KEY (user_id)
                        REFERENCES users(user_id)
                        ON DELETE CASCADE
                );
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Enable foreign key constraints
            statement.execute("PRAGMA foreign_keys = ON");

            // Create main tables
            statement.execute(usersTable);
            statement.execute(clientsTable);
            statement.execute(servicesTable);
            statement.execute(appointmentsTable);

            // Check and migrate the old activity_logs structure
            if (tableExists(connection, "activity_logs")
                    && !columnExists(connection, "activity_logs", "action_type")) {

                System.out.println("Migrating old activity_logs table...");

                statement.execute("""
                        ALTER TABLE activity_logs
                        RENAME TO activity_logs_legacy;
                        """);

                statement.execute(activityLogsTable);

                // Preserve existing activity log records
                statement.execute("""
                        INSERT INTO activity_logs
                            (id, user_id, action_type, description, timestamp)
                        SELECT
                            log_id,
                            user_id,
                            action,
                            description,
                            COALESCE(log_date, CURRENT_DATE)
                                || ' '
                                || COALESCE(log_time, '00:00:00')
                        FROM activity_logs_legacy;
                        """);

                statement.execute("DROP TABLE activity_logs_legacy");

                System.out.println("Activity logs migration successful!");
            } else {
                // Create the new table if it does not exist
                statement.execute(activityLogsTable);
            }

            // Create indexes
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

    private static boolean tableExists(
            Connection connection, String tableName
    ) throws SQLException {

        try (var statement = connection.prepareStatement("""
                SELECT name
                FROM sqlite_master
                WHERE type = 'table' AND name = ?
                """)) {

            statement.setString(1, tableName);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static boolean columnExists(
            Connection connection,
            String tableName,
            String columnName
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "PRAGMA table_info(" + tableName + ")")) {

            while (result.next()) {

                if (columnName.equalsIgnoreCase(
                        result.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
