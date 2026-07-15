package com.pharmacy.app.model;

public class CartItem {

    private int medicineId;
    private String name;         // plain medicine name, used for lookups
    private String displayName;  // what shows in the cart table, e.g. "Crocin (2 strips)" or "Crocin (3 loose)"
    private double unitPrice;    // price per SMALLEST unit
    private double taxPct;
    private int availableStock;  // in smallest units
    private boolean controlled;
    private int quantity;        // in SMALLEST units - this is what actually gets deducted from stock

    public CartItem(int medicineId, String name, double unitPrice, double taxPct, int availableStock, boolean controlled) {
        this.medicineId = medicineId;
        this.name = name;
        this.displayName = name;
        this.unitPrice = unitPrice;
        this.taxPct = taxPct;
        this.availableStock = availableStock;
        this.controlled = controlled;
        this.quantity = 1;
    }

    public int getMedicineId() { return medicineId; }
    public String getName() { return displayName != null ? displayName : name; }
    public String getPlainName() { return name; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
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