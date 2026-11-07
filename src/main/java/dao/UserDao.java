
package com.salonbook.dao;

import com.salonbook.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // REGISTER USER
    public boolean registerUser(User user) {

        String sql = """
                INSERT INTO users
                (username, password, full_name, role, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.fullName());
            statement.setString(4, user.role().toUpperCase(Locale.ROOT));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    // LOGIN USER
    public User login(String username, String password) {

        String sql = """
                SELECT user_id, username, password, full_name, role, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getInt("user_id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("full_name"),
                            result.getString("role"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }

        return null;
    }

    // FIND USER BY USERNAME
    public User findByUsername(String username) {

        String sql = """
                SELECT user_id, username, password, full_name, role, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getInt("user_id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("full_name"),
                            result.getString("role"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("User search failed: " + e.getMessage(), e);
        }

        return null;
    }

    // GET ALL USERS
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, full_name, role, status
                FROM users
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                users.add(new User(
                        result.getInt("user_id"),
                        result.getString("username"),
                        "",
                        result.getString("full_name"),
                        result.getString("role"),
                        result.getString("status")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve users: " + e.getMessage(), e);
        }

        return users;
    }

    // GET PENDING USERS
    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, full_name, role, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY user_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                users.add(new User(
                        result.getInt("user_id"),
                        result.getString("username"),
                        "",
                        result.getString("full_name"),
                        result.getString("role"),
                        result.getString("status")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to retrieve pending users: " + e.getMessage(), e
            );
        }

        return users;
    }

    // APPROVE USER
    public boolean approveUser(int userId, int adminId) {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE user_id = ? AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            boolean updated = statement.executeUpdate() > 0;

            if (updated) {
                activityLogDao.logAction(
                        adminId,
                        "USER_APPROVED",
                        "Admin " + adminId + " approved User " + userId
                );
            }

            return updated;

        } catch (SQLException e) {
            throw new RuntimeException("User approval failed: " + e.getMessage(), e);
        }
    }

    // DEACTIVATE USER
    public boolean deactivateUser(int userId, int adminId) {

        String sql = """
                UPDATE users
                SET status = 'DEACTIVATED'
                WHERE user_id = ? AND status = 'APPROVED'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            boolean updated = statement.executeUpdate() > 0;

            if (updated) {
                activityLogDao.logAction(
                        adminId,
                        "USER_UPDATED",
                        "Admin " + adminId +
                                " deactivated User " + userId
                );
            }

            return updated;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "User deactivation failed: " + e.getMessage(), e
            );
        }
    }

    // UPDATE USER ROLE AND STATUS
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus,
            int adminId) {

        String normalizedRole = newRole.trim().toUpperCase(Locale.ROOT);
        String normalizedStatus = newStatus.trim().toUpperCase(Locale.ROOT);

        String sql = """
                UPDATE users
                SET role = ?, status = ?
                WHERE user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedRole);
            statement.setString(2, normalizedStatus);
            statement.setInt(3, userId);

            boolean updated = statement.executeUpdate() > 0;

            if (updated) {
                activityLogDao.logAction(
                        adminId,
                        "USER_UPDATED",
                        "Admin " + adminId +
                                " updated User " + userId +
                                " role to " + normalizedRole +
                                " and status to " + normalizedStatus
                );
            }

            return updated;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "User role and status update failed: " + e.getMessage(), e
            );
        }
    }

    // BACKWARD-COMPATIBLE APPROVAL METHOD
    public boolean approveUser(int userId) {
        return approveUser(userId, getAdminIdPlaceholder());
    }

    // BACKWARD-COMPATIBLE DEACTIVATION METHOD
    public boolean deactivateUser(int userId) {
        return deactivateUser(userId, getAdminIdPlaceholder());
    }

    // BACKWARD-COMPATIBLE UPDATE METHOD
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus) {

        return updateUserRoleAndStatus(
                userId, newRole, newStatus, getAdminIdPlaceholder()
        );
    }

    // TEMPORARY PLACEHOLDER
    private int getAdminIdPlaceholder() {
        return 1;
    }
}
