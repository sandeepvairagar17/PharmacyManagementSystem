package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.dao.ReportDAO;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.util.List;

public class ReportsController {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextArea reportArea;

    @FXML
    public void initialize() {
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
    }

    @FXML
    private void handleSalesReport() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from == null || to == null) {
            reportArea.setText("Please select both From and To dates.");
            return;
        }
        if (from.isAfter(to)) {
            reportArea.setText("'From' date must be before 'To' date.");
            return;
        }

        List<String> lines = ReportDAO.salesReport(from, to);
        reportArea.setText("SALES REPORT: " + from + " to " + to + "\n\n" + String.join("\n", lines));
    }

    @FXML
    private void handleLowStockReport() {
        List<Medicine> lowStock = MedicineDAO.getLowStockMedicines();
        StringBuilder sb = new StringBuilder("LOW STOCK REPORT\n\n");
        sb.append(String.format("%-30s %-12s %-12s\n", "Medicine", "Stock", "Threshold"));
        sb.append("-".repeat(56)).append("\n");

        if (lowStock.isEmpty()) {
            sb.append("No medicines are currently low on stock.");
        } else {
            for (Medicine m : lowStock) {
                sb.append(String.format("%-30s %-12d %-12d\n", m.getName(), m.getTotalStock(), m.getLowStockThreshold()));
            }
        }
        reportArea.setText(sb.toString());
    }

    @FXML
    private void handleExpiryReport() {
        List<String> lines = ReportDAO.expiringBatchesReport(60);
        reportArea.setText("EXPIRY REPORT (next 60 days)\n\n" + String.join("\n", lines));
    }

    @FXML
    private void handleValuationReport() {
        List<String> lines = ReportDAO.inventoryValuationReport();
        reportArea.setText("INVENTORY VALUATION REPORT\n\n" + String.join("\n", lines));
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}