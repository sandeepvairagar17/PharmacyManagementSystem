package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EditMedicineController {

    private static Medicine medicineToEdit;

    public static void setMedicineToEdit(Medicine medicine) {
        medicineToEdit = medicine;
    }

    @FXML private TextField nameField;
    @FXML private TextField genericField;
    @FXML private TextField categoryField;
    @FXML private TextField manufacturerField;
    @FXML private TextField unitField;
    @FXML private TextField packSizeField;
    @FXML private TextField priceField;
    @FXML private TextField taxField;
    @FXML private TextField barcodeField;
    @FXML private TextField thresholdField;
    @FXML private CheckBox controlledCheckBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        if (medicineToEdit == null) return;

        nameField.setText(medicineToEdit.getName());
        genericField.setText(medicineToEdit.getGenericName());
        categoryField.setText(medicineToEdit.getCategory());
        manufacturerField.setText(medicineToEdit.getManufacturer());
        unitField.setText(medicineToEdit.getUnit());
        packSizeField.setText(String.valueOf(medicineToEdit.getPackSize()));
        priceField.setText(String.format("%.2f", medicineToEdit.getPackPrice())); // show as PACK price for editing
        taxField.setText(String.valueOf(medicineToEdit.getTaxPct()));
        barcodeField.setText(medicineToEdit.getBarcode());
        thresholdField.setText(String.valueOf(medicineToEdit.getLowStockThreshold()));
        controlledCheckBox.setSelected(medicineToEdit.isControlled());
    }

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

        medicineToEdit.setName(name);
        medicineToEdit.setGenericName(genericField.getText().trim());
        medicineToEdit.setCategory(categoryField.getText().trim());
        medicineToEdit.setManufacturer(manufacturerField.getText().trim());
        medicineToEdit.setUnit(unitField.getText().trim().isEmpty() ? "strip" : unitField.getText().trim());
        medicineToEdit.setPackSize(packSize);
        medicineToEdit.setUnitPrice(packPrice / packSize);
        medicineToEdit.setTaxPct(tax);
        medicineToEdit.setControlled(controlledCheckBox.isSelected());
        medicineToEdit.setBarcode(barcodeField.getText().trim().isEmpty() ? null : barcodeField.getText().trim());
        medicineToEdit.setLowStockThreshold(threshold);

        try {
            MedicineDAO.updateMedicine(medicineToEdit);
        } catch (RuntimeException e) {
            errorLabel.setText("Failed to save: " + e.getMessage());
            return;
        }

        medicineToEdit = null;
        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }

    @FXML
    private void handleCancel() {
        medicineToEdit = null;
        SceneManager.switchTo("/com/pharmacy/app/view/inventory.fxml", "Inventory");
    }
}