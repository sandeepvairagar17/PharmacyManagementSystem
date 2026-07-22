package com.pharmacy.app.dao;

import com.pharmacy.app.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import com.pharmacy.app.util.DateUtil;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    /** Returns a formatted sales report (as lines of text) between two dates, inclusive. */
    public static List<String> salesReport(LocalDate from, LocalDate to) {
        Connection conn = DatabaseManager.getConnection();
        List<String> lines = new ArrayList<>();

        String sql = """
            SELECT id, created_at, subtotal, discount, tax_amount, total, payment_mode
            FROM sales
            WHERE date(created_at) BETWEEN ? AND ?
            ORDER BY created_at ASC
            """;

        double grandTotal = 0;
        int count = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());

            try (ResultSet rs = ps.executeQuery()) {
                lines.add(String.format("%-6s %-20s %-10s %-10s %-10s %-10s %-8s",
                        "ID", "Date/Time", "Subtotal", "Discount", "Tax", "Total", "Payment"));
                lines.add("-".repeat(85));

                while (rs.next()) {
                    count++;
                    double total = rs.getDouble("total");
                    grandTotal += total;
                    lines.add(String.format("%-6d %-20s %-10.2f %-10.2f %-10.2f %-10.2f %-8s",
                            rs.getInt("id"),
                            DateUtil.toDisplay(rs.getString("created_at")),
                            rs.getDouble("subtotal"),
                            rs.getDouble("discount"),
                            rs.getDouble("tax_amount"),
                            total,
                            rs.getString("payment_mode")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate sales report: " + e.getMessage(), e);
        }

        lines.add("-".repeat(85));
        lines.add(String.format("Total Transactions: %d      Total Revenue: %.2f", count, grandTotal));
        return lines;
    }

    /** Returns batches expiring within the given number of days (including already expired). */
    public static List<String> expiringBatchesReport(int daysAhead) {
        Connection conn = DatabaseManager.getConnection();
        List<String> lines = new ArrayList<>();

        String sql = """
            SELECT m.name, b.batch_no, b.expiry_date, b.quantity
            FROM batches b
            JOIN medicines m ON m.id = b.medicine_id
            WHERE b.quantity > 0 AND date(b.expiry_date) <= date(?, ?)
            ORDER BY b.expiry_date ASC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            ps.setString(2, "+" + daysAhead + " days");

            try (ResultSet rs = ps.executeQuery()) {
                lines.add(String.format("%-30s %-15s %-12s %-8s", "Medicine", "Batch No.", "Expiry Date", "Qty"));
                lines.add("-".repeat(70));

                boolean any = false;
                while (rs.next()) {
                    any = true;
                    String expiry = rs.getString("expiry_date");
                    String flag = expiry.compareTo(LocalDate.now().toString()) < 0 ? "  [EXPIRED]" : "";
                    lines.add(String.format("%-30s %-15s %-12s %-8d%s",
                            rs.getString("name"),
                            rs.getString("batch_no"),
                            DateUtil.toDisplay(expiry),
                            rs.getInt("quantity"),
                            flag));
                }
                if (!any) lines.add("No medicines expiring within " + daysAhead + " days.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate expiry report: " + e.getMessage(), e);
        }

        return lines;
    }

    /** Returns current inventory valuation (total value of stock on hand, based on unit price). */
    public static List<String> inventoryValuationReport() {
        Connection conn = DatabaseManager.getConnection();
        List<String> lines = new ArrayList<>();

        String sql = """
            SELECT m.name, COALESCE(SUM(b.quantity), 0) AS total_qty, m.unit_price,
                   COALESCE(SUM(b.quantity), 0) * m.unit_price AS value
            FROM medicines m
            LEFT JOIN batches b ON b.medicine_id = m.id
            GROUP BY m.id
            HAVING total_qty > 0
            ORDER BY value DESC
            """;

        double grandTotal = 0;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            lines.add(String.format("%-30s %-10s %-12s %-12s", "Medicine", "Qty", "Unit Price", "Value"));
            lines.add("-".repeat(66));

            while (rs.next()) {
                double value = rs.getDouble("value");
                grandTotal += value;
                lines.add(String.format("%-30s %-10d %-12.2f %-12.2f",
                        rs.getString("name"),
                        rs.getInt("total_qty"),
                        rs.getDouble("unit_price"),
                        value));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate inventory valuation report: " + e.getMessage(), e);
        }

        lines.add("-".repeat(66));
        lines.add(String.format("Total Inventory Value: %.2f", grandTotal));
        return lines;
    }
}