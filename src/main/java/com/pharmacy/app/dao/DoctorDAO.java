package com.pharmacy.app.dao;

import com.pharmacy.app.model.Doctor;
import com.pharmacy.app.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public static int addDoctor(String name, String specialization) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO doctors (name, specialization) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, specialization);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add doctor: " + e.getMessage(), e);
        }
        return -1;
    }

    public static List<Doctor> getAllDoctors() {
        Connection conn = DatabaseManager.getConnection();
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name COLLATE NOCASE";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("specialization")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch doctors: " + e.getMessage(), e);
        }
        return list;
    }
}