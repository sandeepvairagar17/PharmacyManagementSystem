package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventoryController {

    @FXML private TextField searchField;
    @FXML private TableView<Medicine> medicineTable;

    @FXML private TableColumn<Medicine, String> nameCol;
    @FXML private TableColumn<Medicine, String> genericCol;
    @FXML private TableColumn<Medicine, String> categoryCol;
    @FXML private TableColumn<Medicine, String> manufacturerCol;
    @FXML private TableColumn<Medicine, Double> priceCol;
    @FXML private TableColumn<Medicine, Double> taxCol;
    @FXML private TableColumn<Medicine, Integer> stockCol;
    @FXML private TableColumn<Medicine, String> controlledCol;
    @FXML private TableColumn<Medicine, String> barcodeCol;

    private final ObservableList<Medicine> medicineList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        genericCol.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        taxCol.setCellValueFactory(new PropertyValueFactory<>("taxPct"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("totalStock"));
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        // "Controlled" isn't a simple property - show Yes/No manually
        controlledCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isControlled() ? "Yes" : "No"));

        medicineTable.setItems(medicineList);
        loadMedicines();
    }

    private void loadMedicines() {
        medicineList.setAll(MedicineDAO.getAllMedicines());
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadMedicines();
        } else {
            medicineList.setAll(MedicineDAO.search(keyword));
        }
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        loadMedicines();
    }

    @FXML
    private void handleAddMedicine() {
        SceneManager.switchTo("/com/pharmacy/app/view/add_medicine.fxml", "Add Medicine");
    }

    @FXML
    private void handleAddBatch() {
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Please select a medicine from the table first.");
            alert.showAndWait();
            return;
        }
        AddBatchController.setSelectedMedicine(selected);
        SceneManager.switchTo("/com/pharmacy/app/view/add_batch.fxml", "Add Stock Batch - " + selected.getName());
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}