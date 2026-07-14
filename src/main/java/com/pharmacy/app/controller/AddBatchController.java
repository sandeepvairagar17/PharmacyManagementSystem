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

    // Simple static hand-off from InventoryController - set before switching to this screen
    private static Medicine selectedMedicine;

    public static void setSelectedMedicine(Medicine medicine) {
        selectedMedicine = medicine;
    }

    @FXML private Label titleLabel;
    @FXML private TextField batchNoField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextField quantityField;
    @FXML private TextField costPriceField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        if (selectedMedicine != null) {
            titleLabel.setText("Add Stock Batch - " + selectedMedicine.getName());
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

        int quantity;
        double costPrice;
        try {
            quantity = Integer.parseInt(qtyText);
            costPrice = costPriceField.getText().trim().isEmpty() ? 0 : Double.parseDouble(costPriceField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity and Cost Price must be valid numbers.");
            return;
        }

        if (expiry.isBefore(LocalDate.now())) {
            errorLabel.setText("Expiry date cannot be in the past.");
            return;
        }

        try {
            MedicineDAO.addBatch(selectedMedicine.getId(), batchNo, expiry.toString(), quantity, costPrice);
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