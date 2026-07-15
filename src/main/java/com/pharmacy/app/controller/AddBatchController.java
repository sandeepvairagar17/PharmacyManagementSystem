package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class AddBatchController {

    private static Medicine selectedMedicine;

    public static void setSelectedMedicine(Medicine medicine) {
        selectedMedicine = medicine;
    }

    @FXML private Label titleLabel;
    @FXML private Label quantityLabel;
    @FXML private Label conversionLabel;
    @FXML private TextField batchNoField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextField quantityField;
    @FXML private TextField costPriceField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        if (selectedMedicine != null) {
            titleLabel.setText("Add Stock Batch - " + selectedMedicine.getName());

            if (selectedMedicine.isSplittable()) {
                quantityLabel.setText("Quantity Received (in " + selectedMedicine.getUnit() + "s)*");
                conversionLabel.setText("Each " + selectedMedicine.getUnit() + " = " + selectedMedicine.getPackSize() + " units. Enter how many " + selectedMedicine.getUnit() + "s you received - total units are calculated automatically.");
            } else {
                quantityLabel.setText("Quantity Received*");
                conversionLabel.setText("");
            }

            // Live-update the conversion preview as the pharmacist types
            quantityField.textProperty().addListener((obs, oldVal, newVal) -> updateConversionPreview());
        }
    }

    private void updateConversionPreview() {
        if (selectedMedicine == null || !selectedMedicine.isSplittable()) return;
        try {
            int packs = Integer.parseInt(quantityField.getText().trim());
            int totalUnits = packs * selectedMedicine.getPackSize();
            conversionLabel.setText(packs + " " + selectedMedicine.getUnit() + "(s) x " + selectedMedicine.getPackSize() +
                    " = " + totalUnits + " total units will be added to stock.");
        } catch (NumberFormatException e) {
            conversionLabel.setText("Each " + selectedMedicine.getUnit() + " = " + selectedMedicine.getPackSize() + " units.");
        }
    }

    @FXML
    private void handleSave() {
        if (selectedMedicine == null) {
            errorLabel.setText("No medicine selected. Go back and select one from the inventory table.");
            return;
        }

        String batchNo = batchNoField.getText().trim();
        LocalDate expiry = expiryDatePicker.getValue();
        String qtyText = quantityField.getText().trim();

        if (batchNo.isEmpty() || expiry == null || qtyText.isEmpty()) {
            errorLabel.setText("Batch Number, Expiry Date, and Quantity are required.");
            return;
        }

        int enteredQty;
        double costPricePerPack;
        try {
            enteredQty = Integer.parseInt(qtyText);
            costPricePerPack = costPriceField.getText().trim().isEmpty() ? 0 : Double.parseDouble(costPriceField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity and Cost Price must be valid numbers.");
            return;
        }

        if (expiry.isBefore(LocalDate.now())) {
            errorLabel.setText("Expiry date cannot be in the past.");
            return;
        }

        // Convert entered packs to total smallest units for storage
        int totalUnits = enteredQty * selectedMedicine.getPackSize();
        // Store cost price PER SMALLEST UNIT for consistency with how unit_price works
        double costPricePerUnit = costPricePerPack / selectedMedicine.getPackSize();

        try {
            MedicineDAO.addBatch(selectedMedicine.getId(), batchNo, expiry.toString(), totalUnits, costPricePerUnit);
        } catch (RuntimeException e) {
            errorLabel.setText("Failed to save batch: " + e.getMessage());
            return;
        }

        selectedMedicine = null;
        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }

    @FXML
    private void handleCancel() {
        selectedMedicine = null;
        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }
}