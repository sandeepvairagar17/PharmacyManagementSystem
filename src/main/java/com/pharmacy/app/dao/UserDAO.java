package com.pharmacy.app.dao;

import com.pharmacy.app.model.User;
import com.pharmacy.app.util.DatabaseManager;
import com.pharmacy.app.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static void seedDefaultAdminIfNeeded() {
        Connection conn = DatabaseManager.getConnection();
        String checkSql = "SELECT COUNT(*) FROM users";
        String insertSql = "INSERT INTO users (username, password_hash, full_name, role, is_active) VALUES (?, ?, ?, ?, 1)";

        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, PasswordUtil.hash("admin123"));
                    ps.setString(3, "Administrator");
                    ps.setString(4, User.Role.ADMIN.name());
                    ps.executeUpdate();
                    System.out.println("Default admin account created - username: admin, password: admin123");
                    System.out.println("IMPORTANT: Change this password after first login.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed default admin: " + e.getMessage(), e);
        }
    }

    public static User authenticate(String username, String plainPassword) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verify(plainPassword, storedHash)) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
        return null;
    }

    public static void createUser(String username, String plainPassword, String fullName, User.Role role) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO users (username, password_hash, full_name, role, is_active) VALUES (?, ?, ?, ?, 1)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(plainPassword));
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /** Returns all users (for the Manage Users admin screen). */
    public static List<User> getAllUsers() {
        Connection conn = DatabaseManager.getConnection();
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name COLLATE NOCASE";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch users: " + e.getMessage(), e);
        }
        return list;
    }

    /** Activates/deactivates a user account (admin can disable a staff login without deleting history). */
    public static void setActive(int userId, boolean active) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user status: " + e.getMessage(), e);
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                User.Role.valueOf(rs.getString("role")),
                rs.getInt("is_active") == 1
        );
    }
}