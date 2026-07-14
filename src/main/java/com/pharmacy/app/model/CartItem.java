package com.pharmacy.app.model;

/**
 * Represents one line in the billing cart (UI-only, not a database entity).
 * Actual batch selection/deduction happens at checkout time in SaleDAO.
 */
public class CartItem {

    private int medicineId;
    private String name;
    private double unitPrice;
    private double taxPct;
    private int availableStock;
    private boolean controlled;
    private int quantity;

    public CartItem(int medicineId, String name, double unitPrice, double taxPct, int availableStock, boolean controlled) {
        this.medicineId = medicineId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.taxPct = taxPct;
        this.availableStock = availableStock;
        this.controlled = controlled;
        this.quantity = 1;
    }

    public int getMedicineId() { return medicineId; }
    public String getName() { return name; }
    public double getUnitPrice() { return unitPrice; }
    public double getTaxPct() { return taxPct; }
    public int getAvailableStock() { return availableStock; }
    public boolean isControlled() { return controlled; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLineSubtotal() {
        return unitPrice * quantity;
    }

    public double getLineTax() {
        return getLineSubtotal() * (taxPct / 100.0);
    }

    public double getLineTotal() {
        return getLineSubtotal() + getLineTax();
    }
}