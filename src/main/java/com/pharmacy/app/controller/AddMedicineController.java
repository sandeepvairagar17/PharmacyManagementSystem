package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddMedicineController {

    @FXML private TextField nameField;
    @FXML private TextField genericField;
    @FXML private TextField categoryField;
    @FXML private TextField manufacturerField;
    @FXML private TextField unitField;
    @FXML private TextField packSizeField;
    @FXML private TextField priceField; // this is PACK price (e.g. strip price), not per-unit
    @FXML private TextField taxField;
    @FXML private TextField barcodeField;
    @FXML private TextField thresholdField;
    @FXML private CheckBox controlledCheckBox;
    @FXML private Label errorLabel;

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            errorLabel.setText("Name and Pack Price are required.");
            return;
        }

        double packPrice;
        double tax;
        int threshold;
        int packSize;
        try {
            packPrice = Double.parseDouble(priceText);
            tax = taxField.getText().trim().isEmpty() ? 0 : Double.parseDouble(taxField.getText().trim());
            threshold = thresholdField.getText().trim().isEmpty() ? 10 : Integer.parseInt(thresholdField.getText().trim());
            packSize = packSizeField.getText().trim().isEmpty() ? 1 : Integer.parseInt(packSizeField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Pack Price, Tax, Threshold, and Units per Pack must be valid numbers.");
            return;
        }

        if (packSize <= 0) {
            errorLabel.setText("Units per Pack must be at least 1.");
            return;
        }

        // Store price PER SMALLEST UNIT internally - derived from the pack price the pharmacist entered.
        double perUnitPrice = packPrice / packSize;

        Medicine m = new Medicine();
        m.setName(name);
        m.setGenericName(genericField.getText().trim());
        m.setCategory(categoryField.getText().trim());
        m.setManufacturer(manufacturerField.getText().trim());
        m.setUnit(unitField.getText().trim().isEmpty() ? "strip" : unitField.getText().trim());
        m.setPackSize(packSize);
        m.setUnitPrice(perUnitPrice);
        m.setTaxPct(tax);
        m.setControlled(controlledCheckBox.isSelected());
        m.setBarcode(barcodeField.getText().trim().isEmpty() ? null : barcodeField.getText().trim());
        m.setLowStockThreshold(threshold);

        try {
            MedicineDAO.addMedicine(m);
        } catch (RuntimeException e) {
            errorLabel.setText("Failed to save: " + e.getMessage());
            return;
        }

        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }

    @FXML
    private void handleCancel() {
        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }
}