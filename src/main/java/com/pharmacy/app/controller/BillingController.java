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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
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
        cartNameCol.setCellValueFactory(new PropertyValueFactory<>("name")); // uses getName() -> displayName
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

        discountField.textProperty().addListener((obs, oldVal, newVal) -> recalcTotals());

        setupAutocomplete();
        setupBarcodeScan();
        recalcTotals();
    }

    /**
     * A barcode scanner behaves like a keyboard: it types the code into whatever
     * field is focused, then sends Enter. This handles that Enter keystroke -
     * if the scanned code exactly matches a medicine's barcode, add it straight
     * to the cart (skip needing to click a suggestion, since speed matters at checkout).
     */
    private void setupBarcodeScan() {
        searchField.setOnAction(event -> {
            String code = searchField.getText().trim();
            if (code.isEmpty()) return;

            Medicine exactMatch = MedicineDAO.findByBarcode(code);
            if (exactMatch != null) {
                hideSuggestions();
                searchField.clear();
                promptSaleDetailsAndAdd(exactMatch);
            } else {
                errorLabel.setText("No medicine found with barcode \"" + code + "\". Check it was scanned/entered correctly, or add it via Inventory.");
            }
        });
    }

    private void setupAutocomplete() {
        suggestionsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medicine m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) {
                    setText(null);
                } else {
                    setText(m.getName() + "  (Stock: " + m.getStockDisplay() + ", " + m.getUnit() + " price: " + String.format("%.2f", m.getPackPrice()) + ")");
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
                    promptSaleDetailsAndAdd(selected);
                }
            }
        });

        // Real barcode scanners "type" the code then send Enter automatically -
        // this makes scan-and-go actually work, not just manual clicking.
        searchField.setOnAction(event -> handleEnterKey());
    }

    private void handleEnterKey() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        List<Medicine> matches = MedicineDAO.search(keyword);

        // Prefer an EXACT barcode match first (this is what a real scan produces)
        Medicine exactBarcodeMatch = matches.stream()
                .filter(m -> keyword.equals(m.getBarcode()))
                .findFirst()
                .orElse(null);

        if (exactBarcodeMatch != null) {
            hideSuggestions();
            searchField.clear();
            promptSaleDetailsAndAdd(exactBarcodeMatch);
            return;
        }

        // No exact barcode match - if there's exactly one name match, use it;
        // otherwise leave the suggestion list open for the user to pick manually.
        if (matches.size() == 1) {
            hideSuggestions();
            searchField.clear();
            promptSaleDetailsAndAdd(matches.get(0));
        } else if (matches.isEmpty()) {
            errorLabel.setText("No medicine found matching \"" + keyword + "\". If this was a barcode scan, check the barcode is saved on a medicine in Inventory.");
        }
        // if multiple matches, do nothing extra - suggestion list is already showing them
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

    /**
     * Shows a dialog asking HOW to sell this medicine: as whole packs (e.g. strips)
     * or as loose individual units (e.g. single tablets) - only offered if the
     * medicine's pack size supports splitting. Converts to base units before adding to cart.
     */
    private void promptSaleDetailsAndAdd(Medicine medicine) {
        errorLabel.setText("");

        if (medicine.getTotalStock() <= 0) {
            errorLabel.setText(medicine.getName() + " is out of stock.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText(medicine.getName() + " - Available: " + medicine.getStockDisplay());

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        ToggleGroup saleTypeGroup = new ToggleGroup();
        RadioButton packOption = new RadioButton("Whole " + medicine.getUnit() + "(s)  -  " + String.format("%.2f", medicine.getPackPrice()) + " each");
        RadioButton looseOption = new RadioButton("Loose / individual units  -  " + String.format("%.2f", medicine.getUnitPrice()) + " each");
        packOption.setToggleGroup(saleTypeGroup);
        looseOption.setToggleGroup(saleTypeGroup);
        packOption.setSelected(true);

        TextField qtyField = new TextField("1");

        if (medicine.isSplittable()) {
            grid.add(new Label("Sell as:"), 0, 0);
            grid.add(packOption, 1, 0);
            grid.add(looseOption, 1, 1);
            grid.add(new Label("Quantity:"), 0, 2);
            grid.add(qtyField, 1, 2);
        } else {
            // Not splittable (e.g. a bottle of syrup) - just ask quantity, always "packs" of 1 unit each
            grid.add(new Label("Quantity:"), 0, 0);
            grid.add(qtyField, 1, 0);
        }

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != addButtonType) return;

        int enteredQty;
        try {
            enteredQty = Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity must be a number.");
            return;
        }
        if (enteredQty <= 0) {
            errorLabel.setText("Quantity must be greater than zero.");
            return;
        }

        boolean sellingByPack = !medicine.isSplittable() || packOption.isSelected();
        int baseUnitsToAdd = sellingByPack ? enteredQty * medicine.getPackSize() : enteredQty;
        String saleDescription = sellingByPack
                ? enteredQty + " " + medicine.getUnit() + (enteredQty > 1 ? "s" : "")
                : enteredQty + " loose unit" + (enteredQty > 1 ? "s" : "");

        if (baseUnitsToAdd > medicine.getTotalStock()) {
            errorLabel.setText("Only " + medicine.getStockDisplay() + " available for " + medicine.getName() + ".");
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

        // If this exact medicine is already in the cart, merge quantities
        for (CartItem existing : cart) {
            if (existing.getMedicineId() == medicine.getId()) {
                int newQty = existing.getQuantity() + baseUnitsToAdd;
                if (newQty > medicine.getTotalStock()) {
                    errorLabel.setText("Cannot add - exceeds available stock for " + medicine.getName() + ".");
                    return;
                }
                existing.setQuantity(newQty);
                existing.setDisplayName(medicine.getName() + " (" + newQty + " total units)");
                cartTable.refresh();
                recalcTotals();
                return;
            }
        }

        CartItem item = new CartItem(medicine.getId(), medicine.getName(), medicine.getUnitPrice(),
                medicine.getTaxPct(), medicine.getTotalStock(), medicine.isControlled());
        item.setQuantity(baseUnitsToAdd);
        item.setDisplayName(medicine.getName() + " (" + saleDescription + ")");
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
            return Math.max(0, Math.min(pct, 100));
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