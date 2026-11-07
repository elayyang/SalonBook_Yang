
package com.salonbook.dao;

import com.salonbook.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // INSERT NEW ACTIVITY LOG
    public void logAction(
            int userId,
            String actionType,
            String description
    ) {

        String sql = """
                INSERT INTO activity_logs
                    (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Failed to record activity log: " + e.getMessage()
            );

            throw new RuntimeException("Activity logging failed.", e);
        }
    }

    // RETRIEVE ALL ACTIVITY LOGS
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                ORDER BY timestamp DESC, id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                logs.add(mapActivityLog(result));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to retrieve activity logs.", e
            );
        }

        return logs;
    }

    // RETRIEVE ACTIVITY LOGS BY USER ID
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    logs.add(mapActivityLog(result));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to retrieve user activity logs.", e
            );
        }

        return logs;
    }

    // CLEAR HISTORICAL LOGS SAFELY
    // Records the clearing action after deleting old logs.
    public void clearAllLogs(int adminId) {

        String deleteSql = "DELETE FROM activity_logs";

        String logSql = """
                INSERT INTO activity_logs
                    (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement deleteStatement =
                            connection.prepareStatement(deleteSql);
                    PreparedStatement logStatement =
                            connection.prepareStatement(logSql)
            ) {

                deleteStatement.executeUpdate();

                logStatement.setInt(1, adminId);
                logStatement.setString(2, "LOGS_CLEARED");
                logStatement.setString(
                        3,
                        "Administrator ID " + adminId
                                + " cleared historical activity logs."
                );

                logStatement.executeUpdate();

                connection.commit();

                System.out.println("Historical logs cleared successfully.");

            } catch (SQLException e) {

                connection.rollback();

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to clear historical activity logs.", e
            );
        }
    }

    // CONVERT DATABASE RECORD INTO ACTIVITYLOG OBJECT
    private ActivityLog mapActivityLog(ResultSet result)
            throws SQLException {

        int userIdValue = result.getInt("user_id");

        Integer userId = result.wasNull()
                ? null
                : userIdValue;

        return new ActivityLog(
                result.getInt("id"),
                userId,
                result.getString("action_type"),
                result.getString("description"),
                result.getString("timestamp")
        );
    }
}
