package com.pharmacy.app.model;

public class PrescriptionItem {
    private int medicineId;
    private String medicineName; // for display in the UI cart
    private String dosage;
    private int quantity;

    public PrescriptionItem(int medicineId, String medicineName, String dosage, int quantity) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.quantity = quantity;
    }

    public int getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public int getQuantity() { return quantity; }
}