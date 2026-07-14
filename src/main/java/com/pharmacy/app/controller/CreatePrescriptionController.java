package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.dao.PatientDAO;
import com.pharmacy.app.dao.PrescriptionDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.model.Patient;
import com.pharmacy.app.model.PrescriptionItem;
import com.pharmacy.app.model.User;
import com.pharmacy.app.util.SceneManager;
import com.pharmacy.app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.List;

public class CreatePrescriptionController {

    @FXML private ComboBox<Patient> patientBox;
    @FXML private TextField doctorField;
    @FXML private TextField medicineSearchField;
    @FXML private TextField dosageField;
    @FXML private TextField quantityField;
    @FXML private ListView<Medicine> suggestionsList;
    @FXML private ListView<String> itemsListView;
    @FXML private Label errorLabel;

    private final ObservableList<String> displayItems = FXCollections.observableArrayList();
    private final List<PrescriptionItem> items = new ArrayList<>();

    @FXML
    public void initialize() {
        patientBox.setItems(FXCollections.observableArrayList(PatientDAO.getAllPatients()));
        itemsListView.setItems(displayItems);
        setupAutocomplete();
    }

    private void setupAutocomplete() {
        suggestionsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medicine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getName() + "  (" + m.getCategory() + ")");
            }
        });

        medicineSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.trim();
            if (keyword.isEmpty()) {
                hideSuggestions();
                return;
            }
            List<Medicine> matches = MedicineDAO.search(keyword);
            if (matches.isEmpty()) {
                hideSuggestions();
            } else {
                suggestionsList.setItems(FXCollections.observableArrayList(matches));
                showSuggestions();
            }
        });

        suggestionsList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Medicine selected = suggestionsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    medicineSearchField.setText(selected.getName());
                    hideSuggestions();
                }
            }
        });
    }

    private void showSuggestions() {
        suggestionsList.setVisible(true);
        suggestionsList.setManaged(true);
    }

    private void hideSuggestions() {
        suggestionsList.setVisible(false);
        suggestionsList.setManaged(false);
    }

    @FXML
    private void handleAddItem() {
        errorLabel.setText("");
        String keyword = medicineSearchField.getText().trim();
        String qtyText = quantityField.getText().trim();

        if (keyword.isEmpty() || qtyText.isEmpty()) {
            errorLabel.setText("Enter a medicine name and quantity.");
            return;
        }

        List<Medicine> results = MedicineDAO.search(keyword);
        if (results.isEmpty()) {
            errorLabel.setText("No medicine found matching \"" + keyword + "\".");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity must be a number.");
            return;
        }

        Medicine medicine = results.get(0);
        String dosage = dosageField.getText().trim();

        items.add(new PrescriptionItem(medicine.getId(), medicine.getName(), dosage, quantity));
        displayItems.add(medicine.getName() + " - Qty: " + quantity + (dosage.isEmpty() ? "" : " - " + dosage));

        medicineSearchField.clear();
        dosageField.clear();
        quantityField.clear();
        hideSuggestions();
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        Patient patient = patientBox.getValue();
        if (patient == null) {
            errorLabel.setText("Please select a patient.");
            return;
        }
        if (items.isEmpty()) {
            errorLabel.setText("Add at least one medicine to the prescription.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        String doctorName = doctorField.getText().trim();

        try {
            PrescriptionDAO.createPrescription(patient.getId(), doctorName, currentUser.getId(), items);
        } catch (RuntimeException e) {
            errorLabel.setText("Failed to save prescription: " + e.getMessage());
            return;
        }

        SceneManager.switchTo("/com/pharmacy/app/view/prescriptions.fxml", "Prescriptions");
    }

    @FXML
    private void handleCancel() {
        SceneManager.switchTo("/com/pharmacy/app/view/prescriptions.fxml", "Prescriptions");
    }
}