package com.pharmacy.app.controller;

import com.pharmacy.app.dao.PatientDAO;
import com.pharmacy.app.model.Patient;
import com.pharmacy.app.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddPatientController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private TextArea allergiesField;
    @FXML private TextArea notesField;
    @FXML private Label errorLabel;

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Name is required.");
            return;
        }

        Patient p = new Patient();
        p.setName(name);
        p.setPhone(phoneField.getText().trim());
        p.setDob(dobPicker.getValue() != null ? dobPicker.getValue().toString() : null); // stored as ISO yyyy-MM-dd
        p.setAllergies(allergiesField.getText().trim());
        p.setNotes(notesField.getText().trim());

        try {
            PatientDAO.addPatient(p);
        } catch (RuntimeException e) {
            errorLabel.setText("Failed to save: " + e.getMessage());
            return;
        }

        SceneManager.switchTo("/com/pharmacy/app/view/patients.fxml", "Patients");
    }

    @FXML
    private void handleCancel() {
        SceneManager.switchTo("/com/pharmacy/app/view/patients.fxml", "Patients");
    }
}