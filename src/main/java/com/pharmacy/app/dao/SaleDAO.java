package com.pharmacy.app.dao;

import com.pharmacy.app.model.CartItem;
import com.pharmacy.app.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class SaleDAO {

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) { super(message); }
    }

    /**
     * Completes a sale: deducts stock (oldest-expiry-first, skipping expired batches),
     * records the sale and its line items, all in a single transaction.
     * Returns the generated sale ID.
     */
    public static int checkout(Integer patientId, Integer prescriptionId, List<CartItem> cart,
                               double discount, String paymentMode, int createdByUserId) {

        Connection conn = DatabaseManager.getConnection(); // shared connection - do NOT close

        double subtotal = cart.stream().mapToDouble(CartItem::getLineSubtotal).sum();
        double taxAmount = cart.stream().mapToDouble(CartItem::getLineTax).sum();
        double total = subtotal + taxAmount - discount;

        String insertSale = "INSERT INTO sales (patient_id, prescription_id, subtotal, discount, tax_amount, total, payment_mode, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertSaleItem = "INSERT INTO sale_items (sale_id, medicine_id, batch_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?, ?)";
        String findBatches = "SELECT id, quantity FROM batches WHERE medicine_id = ? AND quantity > 0 AND expiry_date >= ? ORDER BY expiry_date ASC";
        String updateBatchQty = "UPDATE batches SET quantity = quantity - ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            // 1. Insert the sale header
            int saleId;
            try (PreparedStatement ps = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
                if (patientId != null) ps.setInt(1, patientId); else ps.setNull(1, Types.INTEGER);
                if (prescriptionId != null) ps.setInt(2, prescriptionId); else ps.setNull(2, Types.INTEGER);
                ps.setDouble(3, subtotal);
                ps.setDouble(4, discount);
                ps.setDouble(5, taxAmount);
                ps.setDouble(6, total);
                ps.setString(7, paymentMode);
                ps.setInt(8, createdByUserId);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    saleId = keys.getInt(1);
                }
            }

            // 2. For each cart item, deduct from batches (oldest expiry first) and record sale_items
            String today = LocalDate.now().toString();

            for (CartItem item : cart) {
                int remainingToDeduct = item.getQuantity();

                try (PreparedStatement findPs = conn.prepareStatement(findBatches)) {
                    findPs.setInt(1, item.getMedicineId());
                    findPs.setString(2, today);

                    try (ResultSet rs = findPs.executeQuery()) {
                        while (remainingToDeduct > 0 && rs.next()) {
                            int batchId = rs.getInt("id");
                            int batchQty = rs.getInt("quantity");
                            int deductFromThisBatch = Math.min(batchQty, remainingToDeduct);

                            try (PreparedStatement updatePs = conn.prepareStatement(updateBatchQty)) {
                                updatePs.setInt(1, deductFromThisBatch);
                                updatePs.setInt(2, batchId);
                                updatePs.executeUpdate();
                            }

                            try (PreparedStatement itemPs = conn.prepareStatement(insertSaleItem)) {
                                itemPs.setInt(1, saleId);
                                itemPs.setInt(2, item.getMedicineId());
                                itemPs.setInt(3, batchId);
                                itemPs.setInt(4, deductFromThisBatch);
                                itemPs.setDouble(5, item.getUnitPrice());
                                itemPs.setDouble(6, deductFromThisBatch * item.getUnitPrice() * (1 + item.getTaxPct() / 100.0));
                                itemPs.executeUpdate();
                            }

                            remainingToDeduct -= deductFromThisBatch;
                        }
                    }
                }

                if (remainingToDeduct > 0) {
                    // Not enough valid (non-expired) stock available - abort the whole sale
                    conn.rollback();
                    throw new InsufficientStockException(
                            "Not enough valid stock for \"" + item.getName() + "\". Short by " + remainingToDeduct + " unit(s)."
                    );
                }
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Checkout failed: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
}