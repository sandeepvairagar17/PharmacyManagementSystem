package com.pharmacy.app.controller;

import com.pharmacy.app.dao.PrescriptionDAO;
import com.pharmacy.app.model.Prescription;
import com.pharmacy.app.model.PrescriptionItem;
import com.pharmacy.app.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class PrescriptionController {

    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> idCol;
    @FXML private TableColumn<Prescription, String> patientCol;
    @FXML private TableColumn<Prescription, String> doctorCol;
    @FXML private TableColumn<Prescription, String> dateCol;

    private final ObservableList<Prescription> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("prescriptionDate"));

        prescriptionTable.setItems(list);
        list.setAll(PrescriptionDAO.getAllPrescriptions());
    }

    @FXML
    private void handleNewPrescription() {
        SceneManager.switchTo("/com/pharmacy/app/view/create_prescription.fxml", "New Prescription");
    }

    @FXML
    private void handleViewItems() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Select a prescription from the table first.");
            alert.showAndWait();
            return;
        }

        List<PrescriptionItem> items = PrescriptionDAO.getItemsForPrescription(selected.getId());
        StringBuilder sb = new StringBuilder();
        for (PrescriptionItem item : items) {
            sb.append(item.getMedicineName())
                    .append(" - Qty: ").append(item.getQuantity())
                    .append(item.getDosage() != null && !item.getDosage().isBlank() ? " - Dosage: " + item.getDosage() : "")
                    .append("\n");
        }
        if (sb.isEmpty()) sb.append("No items found.");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prescription #" + selected.getId());
        alert.setHeaderText("Patient: " + selected.getPatientName() + " | Doctor: " + selected.getDoctorName());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}