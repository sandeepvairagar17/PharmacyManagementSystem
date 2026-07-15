package com.pharmacy.app.model;

public class Medicine {

    private int id;
    private String name;
    private String genericName;
    private String category;
    private String manufacturer;
    private String unit; // pack label, e.g. "strip", "bottle"
    private double unitPrice; // price per SMALLEST sellable unit (e.g. per tablet)
    private int packSize; // how many smallest units make up one pack (e.g. 10 tablets per strip)
    private double taxPct;
    private boolean controlled;
    private String barcode;
    private int lowStockThreshold;

    private int totalStock; // total in smallest units (e.g. total tablets), calculated from batches

    public Medicine() {
        this.packSize = 1;
    }

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

    /** Price per smallest sellable unit (e.g. price of ONE tablet, not the whole strip). */
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    /** How many smallest units are in one pack (e.g. 10 tablets in a strip). 1 = not splittable. */
    public int getPackSize() { return packSize; }
    public void setPackSize(int packSize) { this.packSize = Math.max(1, packSize); }

    /** Convenience: price of a full pack (e.g. full strip), derived from per-unit price. */
    public double getPackPrice() { return unitPrice * packSize; }

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

    /** True if this medicine can be split and sold loose (not just as a whole pack). */
    public boolean isSplittable() { return packSize > 1; }

    /** Human-friendly stock display, e.g. "100 tablets (10 strips)" or just "40" if not splittable. */
    public String getStockDisplay() {
        if (isSplittable()) {
            int fullPacks = totalStock / packSize;
            int loose = totalStock % packSize;
            return totalStock + " total  (" + fullPacks + " " + (unit != null ? unit : "pack") +
                    (loose > 0 ? " + " + loose + " loose" : "") + ")";
        }
        return String.valueOf(totalStock);
    }

    @Override
    public String toString() {
        return name;
    }
}