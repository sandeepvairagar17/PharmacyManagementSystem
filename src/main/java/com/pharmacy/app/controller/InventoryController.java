package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class InventoryController {

    @FXML private TextField searchField;
    @FXML private TableView<Medicine> medicineTable;

    @FXML private TableColumn<Medicine, String> nameCol;
    @FXML private TableColumn<Medicine, String> genericCol;
    @FXML private TableColumn<Medicine, String> categoryCol;
    @FXML private TableColumn<Medicine, String> packCol;
    @FXML private TableColumn<Medicine, Double> unitPriceCol;
    @FXML private TableColumn<Medicine, String> packPriceCol;
    @FXML private TableColumn<Medicine, Double> taxCol;
    @FXML private TableColumn<Medicine, String> stockCol;
    @FXML private TableColumn<Medicine, String> controlledCol;
    @FXML private TableColumn<Medicine, String> barcodeCol;
    @FXML private TableColumn<Medicine, Void> actionsCol;

    private final ObservableList<Medicine> medicineList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        genericCol.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        taxCol.setCellValueFactory(new PropertyValueFactory<>("taxPct"));
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        packCol.setCellValueFactory(data -> {
            Medicine m = data.getValue();
            return new SimpleStringProperty(m.isSplittable() ? m.getPackSize() + " per " + m.getUnit() : m.getUnit());
        });
        packPriceCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getPackPrice())));
        stockCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStockDisplay()));
        controlledCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isControlled() ? "Yes" : "No"));

        addActionButtons();

        medicineTable.setItems(medicineList);
        loadMedicines();

        // Live prefix search - filters as you type, no need to click a Search button
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.trim();
            if (keyword.isEmpty()) {
                loadMedicines();
            } else {
                medicineList.setAll(MedicineDAO.search(keyword));
            }
        });
    }

    private void loadMedicines() {
        medicineList.setAll(MedicineDAO.getAllMedicines());
    }

    private void addActionButtons() {
        Callback<TableColumn<Medicine, Void>, TableCell<Medicine, Void>> cellFactory = col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Medicine m = getTableView().getItems().get(getIndex());
                    EditMedicineController.setMedicineToEdit(m);
                    SceneManager.switchTo("/com/pharmacy/app/view/edit_medicine.fxml", "Edit Medicine - " + m.getName());
                });
                deleteBtn.setOnAction(e -> {
                    Medicine m = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText("Delete \"" + m.getName() + "\"?");
                    confirm.setContentText("This also removes all its stock batches. This cannot be undone.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                MedicineDAO.deleteMedicine(m.getId());
                                loadMedicines();
                            } catch (RuntimeException ex) {
                                Alert error = new Alert(Alert.AlertType.ERROR);
                                error.setHeaderText("Cannot delete this medicine");
                                error.setContentText("It has existing sales history, which must be kept for records. " +
                                        "Consider editing it instead, or setting its stock to 0.");
                                error.showAndWait();
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
        actionsCol.setCellFactory(cellFactory);
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
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