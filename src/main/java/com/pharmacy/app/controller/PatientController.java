package com.pharmacy.app.controller;

import com.pharmacy.app.dao.PatientDAO;
import com.pharmacy.app.model.Patient;
import com.pharmacy.app.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PatientController {

    @FXML private TextField searchField;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> nameCol;
    @FXML private TableColumn<Patient, String> phoneCol;
    @FXML private TableColumn<Patient, String> dobCol;
    @FXML private TableColumn<Patient, String> allergiesCol;
    @FXML private TableColumn<Patient, String> notesCol;

    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        allergiesCol.setCellValueFactory(new PropertyValueFactory<>("allergies"));
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        patientTable.setItems(patientList);
        loadPatients();
    }

    private void loadPatients() {
        patientList.setAll(PatientDAO.getAllPatients());
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) loadPatients();
        else patientList.setAll(PatientDAO.search(keyword));
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        loadPatients();
    }

    @FXML
    private void handleAddPatient() {
        SceneManager.switchTo("/com/pharmacy/app/view/add_patient.fxml", "Add Patient");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}