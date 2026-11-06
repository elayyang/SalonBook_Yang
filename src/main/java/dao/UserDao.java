
package com.salonbook.dao;

import com.salonbook.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // REGISTER NEW ACCOUNT
    // All newly registered accounts automatically receive PENDING status.

    public boolean registerUser(User user) {

        String sql = """
                INSERT INTO users
                (username, password, full_name, role, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

        String role = user.role();

        if (!role.equals("ADMINISTRATOR")
                && !role.equals("RECEPTIONIST")
                && !role.equals("SALON STAFF")) {

            System.out.println("Invalid user role.");
            return false;
        }

        if (user.username().isBlank()
                || user.password().isBlank()
                || user.fullName().isBlank()) {

            System.out.println("All fields are required.");
            return false;
        }

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.fullName());
            statement.setString(4, role);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }


    // LOGIN USER
    public User login(String username, String password) {

        String sql = """
                SELECT user_id, username, password,
                       full_name, role, status
                FROM users
                WHERE username = ?
                AND password = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }


    // FIND USER BY USERNAME
    public User findByUsername(String username) {

        String sql = """
                SELECT user_id, username, password,
                       full_name, role, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

            System.out.println("Error finding user: " + e.getMessage());
        }

        return null;
    }


    // VIEW ALL REGISTERED USERS
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, full_name, role, status
                FROM users
                ORDER BY user_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                User user = new User(
                        result.getInt("user_id"),
                        result.getString("username"),
                        "",
                        result.getString("full_name"),
                        result.getString("role"),
                        result.getString("status")
                );

                users.add(user);
            }

        } catch (SQLException e) {

            System.out.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }


    // VIEW PENDING ACCOUNTS
    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, full_name, role, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY user_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

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

            System.out.println("Error retrieving pending users: "
                    + e.getMessage());
        }

        return users;
    }


    // APPROVE USER ACCOUNT
    public boolean approveUser(int userId) {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE user_id = ?
                AND status = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Account approval error: "
                    + e.getMessage());

            return false;
        }
    }


    // DEACTIVATE USER ACCOUNT
    public boolean deactivateUser(int userId) {

        String sql = """
                UPDATE users
                SET status = 'DEACTIVATED'
                WHERE user_id = ?
                AND status = 'APPROVED'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Account deactivation error: "
                    + e.getMessage());

            return false;
        }
    }

}
