package com.pharmacy.app.model;

public class Medicine {

    private int id;
    private String name;
    private String genericName;
    private String category;
    private String manufacturer;
    private String unit;
    private double unitPrice;
    private double taxPct;
    private boolean controlled;
    private String barcode;
    private int lowStockThreshold;

    // Not a DB column - calculated by joining with batches (total quantity across all batches)
    private int totalStock;

    public Medicine() {
    }

    public Medicine(int id, String name, String genericName, String category, String manufacturer,
                    String unit, double unitPrice, double taxPct, boolean controlled,
                    String barcode, int lowStockThreshold) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.category = category;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.taxPct = taxPct;
        this.controlled = controlled;
        this.barcode = barcode;
        this.lowStockThreshold = lowStockThreshold;
    }

    // Getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTaxPct() { return taxPct; }
    public void setTaxPct(double taxPct) { this.taxPct = taxPct; }

    public boolean isControlled() { return controlled; }
    public void setControlled(boolean controlled) { this.controlled = controlled; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    @Override
    public String toString() {
        return name;
    }
}