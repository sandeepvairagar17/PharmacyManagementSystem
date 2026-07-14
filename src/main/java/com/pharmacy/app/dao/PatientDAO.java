package com.pharmacy.app.dao;

import com.pharmacy.app.model.Patient;
import com.pharmacy.app.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public static int addPatient(Patient p) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO patients (name, phone, dob, allergies, notes) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getDob());
            ps.setString(4, p.getAllergies());
            ps.setString(5, p.getNotes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add patient: " + e.getMessage(), e);
        }
        return -1;
    }

    public static List<Patient> getAllPatients() {
        Connection conn = DatabaseManager.getConnection();
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name COLLATE NOCASE";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch patients: " + e.getMessage(), e);
        }
        return list;
    }

    public static List<Patient> search(String keyword) {
        Connection conn = DatabaseManager.getConnection();
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE name LIKE ? OR phone LIKE ? ORDER BY name COLLATE NOCASE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Patient search failed: " + e.getMessage(), e);
        }
        return list;
    }

    private static Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("dob"),
                rs.getString("allergies"),
                rs.getString("notes")
        );
    }
}