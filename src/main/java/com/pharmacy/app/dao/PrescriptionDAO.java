package com.pharmacy.app.dao;

import com.pharmacy.app.model.Prescription;
import com.pharmacy.app.model.PrescriptionItem;
import com.pharmacy.app.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    /** Creates a prescription with its medicine items in a single transaction. Returns the new prescription ID. */
    public static int createPrescription(int patientId, String doctorName, int createdByUserId, List<PrescriptionItem> items) {
        Connection conn = DatabaseManager.getConnection();
        String insertPrescription = "INSERT INTO prescriptions (patient_id, doctor_name, created_by) VALUES (?, ?, ?)";
        String insertItem = "INSERT INTO prescription_items (prescription_id, medicine_id, dosage, quantity) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);
            int prescriptionId;

            try (PreparedStatement ps = conn.prepareStatement(insertPrescription, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, patientId);
                ps.setString(2, doctorName);
                ps.setInt(3, createdByUserId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    prescriptionId = keys.getInt(1);
                }
            }

            for (PrescriptionItem item : items) {
                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    ps.setInt(1, prescriptionId);
                    ps.setInt(2, item.getMedicineId());
                    ps.setString(3, item.getDosage());
                    ps.setInt(4, item.getQuantity());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return prescriptionId;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to create prescription: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /** Returns all prescriptions with patient name joined in, most recent first. */
    public static List<Prescription> getAllPrescriptions() {
        Connection conn = DatabaseManager.getConnection();
        List<Prescription> list = new ArrayList<>();

        String sql = """
            SELECT pr.id, pr.patient_id, p.name AS patient_name, pr.doctor_name, pr.prescription_date
            FROM prescriptions pr
            JOIN patients p ON p.id = pr.patient_id
            ORDER BY pr.prescription_date DESC
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Prescription pr = new Prescription();
                pr.setId(rs.getInt("id"));
                pr.setPatientId(rs.getInt("patient_id"));
                pr.setPatientName(rs.getString("patient_name"));
                pr.setDoctorName(rs.getString("doctor_name"));
                pr.setPrescriptionDate(rs.getString("prescription_date"));
                list.add(pr);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch prescriptions: " + e.getMessage(), e);
        }
        return list;
    }

    /** Returns the medicine items belonging to a specific prescription. */
    public static List<PrescriptionItem> getItemsForPrescription(int prescriptionId) {
        Connection conn = DatabaseManager.getConnection();
        List<PrescriptionItem> list = new ArrayList<>();

        String sql = """
            SELECT pi.medicine_id, m.name AS medicine_name, pi.dosage, pi.quantity
            FROM prescription_items pi
            JOIN medicines m ON m.id = pi.medicine_id
            WHERE pi.prescription_id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PrescriptionItem(
                            rs.getInt("medicine_id"),
                            rs.getString("medicine_name"),
                            rs.getString("dosage"),
                            rs.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch prescription items: " + e.getMessage(), e);
        }
        return list;
    }
}