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
    @FXML private TextField priceField;
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
            errorLabel.setText("Name and Unit Price are required.");
            return;
        }

        double price;
        double tax;
        int threshold;
        try {
            price = Double.parseDouble(priceText);
            tax = taxField.getText().trim().isEmpty() ? 0 : Double.parseDouble(taxField.getText().trim());
            threshold = thresholdField.getText().trim().isEmpty() ? 10 : Integer.parseInt(thresholdField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Price, Tax, and Threshold must be valid numbers.");
            return;
        }

        Medicine m = new Medicine();
        m.setName(name);
        m.setGenericName(genericField.getText().trim());
        m.setCategory(categoryField.getText().trim());
        m.setManufacturer(manufacturerField.getText().trim());
        m.setUnit(unitField.getText().trim().isEmpty() ? "strip" : unitField.getText().trim());
        m.setUnitPrice(price);
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