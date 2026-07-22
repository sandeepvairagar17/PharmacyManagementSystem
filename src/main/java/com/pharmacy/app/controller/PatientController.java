package com.pharmacy.app.controller;

import com.pharmacy.app.dao.PatientDAO;
import com.pharmacy.app.dao.PrescriptionDAO;
import com.pharmacy.app.dao.SaleDAO;
import com.pharmacy.app.model.Patient;
import com.pharmacy.app.model.Prescription;
import com.pharmacy.app.util.DateUtil;
import com.pharmacy.app.util.PrintUtil;
import com.pharmacy.app.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;

public class PatientController {

    @FXML private TextField searchField;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> nameCol;
    @FXML private TableColumn<Patient, String> phoneCol;
    @FXML private TableColumn<Patient, String> dobCol;
    @FXML private TableColumn<Patient, String> allergiesCol;
    @FXML private TableColumn<Patient, String> notesCol;
    @FXML private TableColumn<Patient, Void> actionsCol;

    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(DateUtil.toDisplay(data.getValue().getDob())));
        allergiesCol.setCellValueFactory(new PropertyValueFactory<>("allergies"));
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        addActionButtons();

        patientTable.setItems(patientList);
        loadPatients();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.trim();
            if (keyword.isEmpty()) loadPatients();
            else patientList.setAll(PatientDAO.search(keyword));
        });
    }

    private void loadPatients() {
        patientList.setAll(PatientDAO.getAllPatients());
    }

    private void addActionButtons() {
        Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>> cellFactory = col -> new TableCell<>() {
            private final Button prescriptionsBtn = new Button("Prescriptions");
            private final Button historyBtn = new Button("Purchases");
            private final HBox box = new HBox(5, prescriptionsBtn, historyBtn);
            {
                prescriptionsBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    showPrescriptionsDialog(p);
                });
                historyBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    showPurchaseHistoryDialog(p);
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

    private void showPrescriptionsDialog(Patient patient) {
        List<Prescription> prescriptions = PrescriptionDAO.getPrescriptionsForPatient(patient.getId());

        StringBuilder sb = new StringBuilder();
        if (prescriptions.isEmpty()) {
            sb.append("No prescriptions on file for ").append(patient.getName()).append(".");
        } else {
            for (Prescription pr : prescriptions) {
                sb.append("Prescription #").append(pr.getId())
                        .append("  |  ").append(DateUtil.toDisplay(pr.getPrescriptionDate()))
                        .append("  |  Doctor: ").append(pr.getDoctorName() != null ? pr.getDoctorName() : "-")
                        .append("\n");
                for (var item : PrescriptionDAO.getItemsForPrescription(pr.getId())) {
                    sb.append("    - ").append(item.getMedicineName())
                            .append("  Qty: ").append(item.getQuantity())
                            .append(item.getDosage() != null && !item.getDosage().isBlank() ? "  Dosage: " + item.getDosage() : "")
                            .append("\n");
                }
                sb.append("\n");
            }
        }

        showPrintableDialog("Prescriptions - " + patient.getName(), sb.toString());
    }

    private void showPurchaseHistoryDialog(Patient patient) {
        List<String> lines = SaleDAO.getPurchaseHistoryForPatient(patient.getId());
        String content = String.join("\n", lines);
        showPrintableDialog("Purchase History - " + patient.getName(), content);
    }

    /** Shows a scrollable, monospaced dialog with a Print button (system print dialog - can save as PDF). */
    private void showPrintableDialog(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setPrefSize(500, 400);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(textArea);
        ButtonType printButtonType = new ButtonType("Print", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(printButtonType, ButtonType.CLOSE);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == printButtonType) {
                PrintUtil.printText(title, content, dialog.getOwner());
            }
            return null;
        });

        dialog.showAndWait();
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