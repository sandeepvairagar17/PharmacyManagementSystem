package com.pharmacy.app.controller;

import com.pharmacy.app.dao.DoctorDAO;
import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.dao.PatientDAO;
import com.pharmacy.app.dao.PrescriptionDAO;
import com.pharmacy.app.model.Doctor;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.model.Patient;
import com.pharmacy.app.model.PrescriptionItem;
import com.pharmacy.app.model.User;
import com.pharmacy.app.util.SceneManager;
import com.pharmacy.app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreatePrescriptionController {

    @FXML private ComboBox<Patient> patientBox;
    @FXML private ComboBox<Doctor> doctorBox;
    @FXML private TextField medicineSearchField;
    @FXML private TextField dosageField;
    @FXML private TextField quantityField;
    @FXML private ListView<Medicine> suggestionsList;
    @FXML private ListView<String> itemsListView;
    @FXML private Label errorLabel;

    private final ObservableList<String> displayItems = FXCollections.observableArrayList();
    private final List<PrescriptionItem> items = new ArrayList<>();
    private List<Medicine> currentSuggestions = List.of();

    @FXML
    public void initialize() {
        patientBox.setItems(FXCollections.observableArrayList(PatientDAO.getAllPatients()));
        refreshDoctors();
        itemsListView.setItems(displayItems);
        setupAutocomplete();
    }

    private void refreshDoctors() {
        doctorBox.setItems(FXCollections.observableArrayList(DoctorDAO.getAllDoctors()));
    }

    @FXML
    private void handleAddDoctor() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Doctor");
        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Sara Khan");
        TextField specField = new TextField();
        specField.setPromptText("e.g. Orthopedic");
        grid.add(new Label("Doctor Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Specialization:"), 0, 1);
        grid.add(specField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == addType) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                DoctorDAO.addDoctor(name, specField.getText().trim());
                refreshDoctors();
            }
        }
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
                currentSuggestions = List.of();
                hideSuggestions();
                return;
            }
            currentSuggestions = MedicineDAO.search(keyword);
            if (currentSuggestions.isEmpty()) {
                hideSuggestions();
            } else {
                suggestionsList.setItems(FXCollections.observableArrayList(currentSuggestions));
                showSuggestions();
            }
        });

        suggestionsList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Medicine selected = suggestionsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    medicineSearchField.setText(selected.getName());
                    hideSuggestions();
                    dosageField.requestFocus();
                }
            }
        });

        // Enter key selects the top suggestion, same as clicking it - matches Billing behavior
        medicineSearchField.setOnAction(event -> {
            if (!currentSuggestions.isEmpty()) {
                Medicine top = currentSuggestions.get(0);
                medicineSearchField.setText(top.getName());
                hideSuggestions();
                dosageField.requestFocus();
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
        medicineSearchField.requestFocus();
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

        Doctor doctor = doctorBox.getValue();
        String doctorLabel = doctor != null ? doctor.toString() : "";

        User currentUser = SessionManager.getCurrentUser();

        try {
            PrescriptionDAO.createPrescription(patient.getId(), doctorLabel, currentUser.getId(), items);
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