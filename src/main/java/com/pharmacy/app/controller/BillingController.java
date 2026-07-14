package com.pharmacy.app.controller;

import com.pharmacy.app.dao.MedicineDAO;
import com.pharmacy.app.dao.SaleDAO;
import com.pharmacy.app.model.CartItem;
import com.pharmacy.app.model.Medicine;
import com.pharmacy.app.model.User;
import com.pharmacy.app.util.SceneManager;
import com.pharmacy.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

public class BillingController {

    @FXML private TextField searchField;
    @FXML private ListView<Medicine> suggestionsList;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartNameCol;
    @FXML private TableColumn<CartItem, Integer> cartQtyCol;
    @FXML private TableColumn<CartItem, Double> cartPriceCol;
    @FXML private TableColumn<CartItem, Double> cartTaxCol;
    @FXML private TableColumn<CartItem, String> cartLineTotalCol;
    @FXML private TableColumn<CartItem, Void> cartRemoveCol;

    @FXML private TextField discountField;
    @FXML private ComboBox<String> paymentModeBox;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private Label errorLabel;

    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cartNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        cartQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        cartTaxCol.setCellValueFactory(new PropertyValueFactory<>("taxPct"));
        cartLineTotalCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getLineTotal())));

        addRemoveButtonToTable();

        cartTable.setItems(cart);
        cart.addListener((javafx.collections.ListChangeListener<CartItem>) c -> recalcTotals());

        paymentModeBox.setItems(FXCollections.observableArrayList("CASH", "CARD", "UPI", "OTHER"));
        paymentModeBox.getSelectionModel().selectFirst();

        // Live discount recalculation as the user types
        discountField.textProperty().addListener((obs, oldVal, newVal) -> recalcTotals());

        setupAutocomplete();
        recalcTotals();
    }

    private void setupAutocomplete() {
        suggestionsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medicine m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) {
                    setText(null);
                } else {
                    setText(m.getName() + "  (Stock: " + m.getTotalStock() + ", Price: " + m.getUnitPrice() + ")");
                }
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
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
                    hideSuggestions();
                    searchField.clear();
                    promptQuantityAndAdd(selected);
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

    private void addRemoveButtonToTable() {
        Callback<TableColumn<CartItem, Void>, TableCell<CartItem, Void>> cellFactory = col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cart.remove(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        };
        cartRemoveCol.setCellFactory(cellFactory);
    }

    private void promptQuantityAndAdd(Medicine medicine) {
        errorLabel.setText("");

        if (medicine.getTotalStock() <= 0) {
            errorLabel.setText(medicine.getName() + " is out of stock.");
            return;
        }

        TextInputDialog qtyDialog = new TextInputDialog("1");
        qtyDialog.setTitle("Quantity");
        qtyDialog.setHeaderText(medicine.getName() + " - Available: " + medicine.getTotalStock());
        qtyDialog.setContentText("Enter quantity:");
        Optional<String> qtyResult = qtyDialog.showAndWait();

        if (qtyResult.isEmpty()) return;

        int quantity;
        try {
            quantity = Integer.parseInt(qtyResult.get().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity must be a number.");
            return;
        }

        if (quantity <= 0) {
            errorLabel.setText("Quantity must be greater than zero.");
            return;
        }
        if (quantity > medicine.getTotalStock()) {
            errorLabel.setText("Only " + medicine.getTotalStock() + " unit(s) available for " + medicine.getName() + ".");
            return;
        }

        if (medicine.isControlled()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Controlled Drug Confirmation");
            confirm.setHeaderText(medicine.getName() + " is a controlled/scheduled drug.");
            confirm.setContentText("Confirm this dispensing is authorized and properly documented?");
            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
                return;
            }
        }

        for (CartItem existing : cart) {
            if (existing.getMedicineId() == medicine.getId()) {
                int newQty = existing.getQuantity() + quantity;
                if (newQty > medicine.getTotalStock()) {
                    errorLabel.setText("Cannot add - exceeds available stock for " + medicine.getName() + ".");
                    return;
                }
                existing.setQuantity(newQty);
                cartTable.refresh();
                recalcTotals();
                return;
            }
        }

        CartItem item = new CartItem(medicine.getId(), medicine.getName(), medicine.getUnitPrice(),
                medicine.getTaxPct(), medicine.getTotalStock(), medicine.isControlled());
        item.setQuantity(quantity);
        cart.add(item);
    }

    private void recalcTotals() {
        double subtotal = cart.stream().mapToDouble(CartItem::getLineSubtotal).sum();
        double tax = cart.stream().mapToDouble(CartItem::getLineTax).sum();
        double discountPct = parseDiscountPct();
        double discountAmount = subtotal * (discountPct / 100.0);
        double total = subtotal + tax - discountAmount;

        subtotalLabel.setText(String.format("Subtotal: %.2f", subtotal));
        taxLabel.setText(String.format("Tax: %.2f", tax));
        discountLabel.setText(String.format("Discount: -%.2f (%.1f%%)", discountAmount, discountPct));
        totalLabel.setText(String.format("Total: %.2f", Math.max(total, 0)));
    }

    private double parseDiscountPct() {
        try {
            double pct = Double.parseDouble(discountField.getText().trim());
            return Math.max(0, Math.min(pct, 100)); // clamp between 0-100%
        } catch (Exception e) {
            return 0;
        }
    }

    @FXML
    private void handleCheckout() {
        errorLabel.setText("");

        if (cart.isEmpty()) {
            errorLabel.setText("Cart is empty. Add at least one item before checkout.");
            return;
        }

        double subtotal = cart.stream().mapToDouble(CartItem::getLineSubtotal).sum();
        double discountAmount = subtotal * (parseDiscountPct() / 100.0);
        String paymentMode = paymentModeBox.getValue();
        User currentUser = SessionManager.getCurrentUser();

        try {
            int saleId = SaleDAO.checkout(null, null, cart, discountAmount, paymentMode, currentUser.getId());

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Sale Complete");
            success.setHeaderText("Sale #" + saleId + " completed successfully.");
            success.setContentText(totalLabel.getText());
            success.showAndWait();

            cart.clear();
            discountField.setText("0");
            recalcTotals();

        } catch (SaleDAO.InsufficientStockException e) {
            errorLabel.setText(e.getMessage());
        } catch (RuntimeException e) {
            errorLabel.setText("Checkout failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/pharmacy/app/view/dashboard.fxml", "Pharmacy Management System - Dashboard");
    }
}