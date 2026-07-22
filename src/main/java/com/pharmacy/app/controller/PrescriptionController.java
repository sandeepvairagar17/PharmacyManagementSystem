package com.pharmacy.app.controller;

import com.pharmacy.app.dao.PrescriptionDAO;
import com.pharmacy.app.model.Prescription;
import com.pharmacy.app.model.PrescriptionItem;
import com.pharmacy.app.util.DateUtil;
import com.pharmacy.app.util.PrintUtil;
import com.pharmacy.app.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;

public class PrescriptionController {

    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> idCol;
    @FXML private TableColumn<Prescription, String> patientCol;
    @FXML private TableColumn<Prescription, String> doctorCol;
    @FXML private TableColumn<Prescription, String> dateCol;
    @FXML private TableColumn<Prescription, Void> actionsCol;

    private final ObservableList<Prescription> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.toDisplay(data.getValue().getPrescriptionDate())));

        addActionButtons();

        prescriptionTable.setItems(list);
        list.setAll(PrescriptionDAO.getAllPrescriptions());
    }

    private void addActionButtons() {
        Callback<TableColumn<Prescription, Void>, TableCell<Prescription, Void>> cellFactory = col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button printBtn = new Button("Print");
            private final HBox box = new HBox(5, viewBtn, printBtn);
            {
                viewBtn.setOnAction(e -> {
                    Prescription pr = getTableView().getItems().get(getIndex());
                    showItemsDialog(pr);
                });
                printBtn.setOnAction(e -> {
                    Prescription pr = getTableView().getItems().get(getIndex());
                    printPrescription(pr);
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

    private String buildPrescriptionText(Prescription pr) {
        List<PrescriptionItem> items = PrescriptionDAO.getItemsForPrescription(pr.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("Prescription #").append(pr.getId()).append("\n");
        sb.append("Patient: ").append(pr.getPatientName()).append("\n");
        sb.append("Doctor: ").append(pr.getDoctorName() != null ? pr.getDoctorName() : "-").append("\n");
        sb.append("Date: ").append(DateUtil.toDisplay(pr.getPrescriptionDate())).append("\n\n");
        sb.append("Medicines:\n");
        for (PrescriptionItem item : items) {
            sb.append("  - ").append(item.getMedicineName())
                    .append("   Qty: ").append(item.getQuantity())
                    .append(item.getDosage() != null && !item.getDosage().isBlank() ? "   Dosage: " + item.getDosage() : "")
                    .append("\n");
        }
        if (items.isEmpty()) sb.append("  (no items)\n");
        return sb.toString();
    }

    private void showItemsDialog(Prescription pr) {
        String content = buildPrescriptionText(pr);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setPrefSize(450, 300);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Prescription #" + pr.getId());
        dialog.getDialogPane().setContent(textArea);
        ButtonType printType = new ButtonType("Print", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(printType, ButtonType.CLOSE);

        dialog.setResultConverter(bt -> {
            if (bt == printType) {
                PrintUtil.printText("Prescription #" + pr.getId(), content, dialog.getOwner());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void printPrescription(Prescription pr) {
        String content = buildPrescriptionText(pr);
        PrintUtil.printText("Prescription #" + pr.getId(), content, prescriptionTable.getScene().getWindow());
    }

    @FXML
    private void handleNewPrescription() {
        SceneManager.switchTo("/com/pharmacy/app/view/create_prescription.fxml", "New Prescription");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}