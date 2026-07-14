package com.pharmacy.app.dao;

import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    /** Inserts a new medicine into the catalog. Returns the generated ID. */
    public static int addMedicine(Medicine m) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO medicines (name, generic_name, category, manufacturer, unit, unit_price, tax_pct, is_controlled, barcode, low_stock_threshold) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getGenericName());
            ps.setString(3, m.getCategory());
            ps.setString(4, m.getManufacturer());
            ps.setString(5, m.getUnit());
            ps.setDouble(6, m.getUnitPrice());
            ps.setDouble(7, m.getTaxPct());
            ps.setInt(8, m.isControlled() ? 1 : 0);
            ps.setString(9, m.getBarcode());
            ps.setInt(10, m.getLowStockThreshold());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add medicine: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Returns all medicines with their total stock quantity
     * (sum of quantity across all non-expired batches).
     */
    public static List<Medicine> getAllMedicines() {
        Connection conn = DatabaseManager.getConnection();
        List<Medicine> list = new ArrayList<>();

        String sql = """
            SELECT m.*, COALESCE(SUM(b.quantity), 0) AS total_stock
            FROM medicines m
            LEFT JOIN batches b ON b.medicine_id = m.id
            GROUP BY m.id
            ORDER BY m.name COLLATE NOCASE
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch medicines: " + e.getMessage(), e);
        }
        return list;
    }

    /** Searches medicines by name or generic name (for search bars / barcode lookup). */
    public static List<Medicine> search(String keyword) {
        Connection conn = DatabaseManager.getConnection();
        List<Medicine> list = new ArrayList<>();

        String sql = """
            SELECT m.*, COALESCE(SUM(b.quantity), 0) AS total_stock
            FROM medicines m
            LEFT JOIN batches b ON b.medicine_id = m.id
            WHERE m.name LIKE ? OR m.generic_name LIKE ? OR m.barcode = ?
            GROUP BY m.id
            ORDER BY m.name COLLATE NOCASE
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
        return list;
    }

    /** Returns medicines whose total stock is at or below their low-stock threshold. */
    public static List<Medicine> getLowStockMedicines() {
        Connection conn = DatabaseManager.getConnection();
        List<Medicine> list = new ArrayList<>();

        String sql = """
            SELECT m.*, COALESCE(SUM(b.quantity), 0) AS total_stock
            FROM medicines m
            LEFT JOIN batches b ON b.medicine_id = m.id
            GROUP BY m.id
            HAVING total_stock <= m.low_stock_threshold
            ORDER BY total_stock ASC
            """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch low stock medicines: " + e.getMessage(), e);
        }
        return list;
    }

    /** Adds a new stock batch for an existing medicine. */
    public static void addBatch(int medicineId, String batchNo, String expiryDate, int quantity, double costPrice) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO batches (medicine_id, batch_no, expiry_date, quantity, cost_price) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setString(2, batchNo);
            ps.setString(3, expiryDate);
            ps.setInt(4, quantity);
            ps.setDouble(5, costPrice);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add batch: " + e.getMessage(), e);
        }
    }

    /** Deletes a medicine (and its batches, via ON DELETE CASCADE). */
    public static void deleteMedicine(int medicineId) {
        Connection conn = DatabaseManager.getConnection();
        String sql = "DELETE FROM medicines WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete medicine: " + e.getMessage(), e);
        }
    }

    private static Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setGenericName(rs.getString("generic_name"));
        m.setCategory(rs.getString("category"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setUnit(rs.getString("unit"));
        m.setUnitPrice(rs.getDouble("unit_price"));
        m.setTaxPct(rs.getDouble("tax_pct"));
        m.setControlled(rs.getInt("is_controlled") == 1);
        m.setBarcode(rs.getString("barcode"));
        m.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        m.setTotalStock(rs.getInt("total_stock"));
        return m;
    }
}