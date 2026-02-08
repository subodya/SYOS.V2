package com.syos.client.presentation.controllers;

import com.syos.client.concurrency.AsyncTaskExecutor;
import com.syos.client.services.ServerConnection;
import com.syos.common.dto.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class CheckoutController {
    
    @FXML private TextField itemCodeField;
    @FXML private TextField quantityField;
    @FXML private TableView<CartItemView> cartTable;
    @FXML private TableColumn<CartItemView, String> itemCodeColumn;
    @FXML private TableColumn<CartItemView, String> nameColumn;
    @FXML private TableColumn<CartItemView, Integer> quantityColumn;
    @FXML private TableColumn<CartItemView, BigDecimal> priceColumn;
    @FXML private Label totalLabel;
    @FXML private Button addButton;
    @FXML private Button checkoutButton;
    @FXML private ProgressIndicator progressIndicator;

    private ObservableList<CartItemView> cart;
    private ServerConnection serverConnection;
    private AsyncTaskExecutor asyncExecutor;
    private List<ItemDto> availableItems;

    @FXML
    public void initialize() {
        System.out.println("CheckoutController initialized");
        
        cart = FXCollections.observableArrayList();
        serverConnection = new ServerConnection();
        asyncExecutor = new AsyncTaskExecutor();
        availableItems = new ArrayList<>();

        // Setup table columns
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        cartTable.setItems(cart);
        progressIndicator.setVisible(false);
        
        // Load available items on startup
        loadAvailableItems();
    }

    private void loadAvailableItems() {
        System.out.println("Loading available items from server...");
        progressIndicator.setVisible(true);
        
        asyncExecutor.executeAsync(
            () -> {
                System.out.println("Fetching items...");
                return serverConnection.getAllItems();
            },
            items -> {
                System.out.println("Received " + items.size() + " items from server");
                availableItems = items;
                
                // Print items to console for debugging
                for (ItemDto item : items) {
                    System.out.println("  - " + item.getItemCode() + ": " + item.getName() + 
                                     " (Rs." + item.getPrice() + ") Stock: " + item.getCurrentStock());
                }
                
                progressIndicator.setVisible(false);
                showInfo("Items loaded", items.size() + " items available");
            },
            error -> {
                System.err.println("Failed to load items: " + error.getMessage());
                error.printStackTrace();
                progressIndicator.setVisible(false);
                showError("Failed to load items: " + error.getMessage());
            }
        );
    }

    @FXML
    private void handleAddToCart() {
        String itemCode = itemCodeField.getText().trim();
        String quantityText = quantityField.getText().trim();
        
        // Validation
        if (itemCode.isEmpty()) {
            showError("Please enter item code");
            return;
        }
        
        if (quantityText.isEmpty()) {
            showError("Please enter quantity");
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showError("Quantity must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid quantity");
            return;
        }

        System.out.println("Adding to cart: " + itemCode + " x " + quantity);
        
        progressIndicator.setVisible(true);
        addButton.setDisable(true);

        // Find item in available items
        ItemDto item = availableItems.stream()
            .filter(i -> i.getItemCode().equalsIgnoreCase(itemCode))
            .findFirst()
            .orElse(null);

        if (item != null) {
            // Item found in cache
            System.out.println("Found item in cache: " + item.getName());
            addItemToCart(item, quantity);
        } else {
            // Item not in cache, fetch from server
            System.out.println("Item not in cache, fetching from server...");
            asyncExecutor.executeAsync(
                () -> serverConnection.getAllItems(),
                items -> {
                    availableItems = items;
                    ItemDto fetchedItem = items.stream()
                        .filter(i -> i.getItemCode().equalsIgnoreCase(itemCode))
                        .findFirst()
                        .orElse(null);
                    
                    if (fetchedItem != null) {
                        addItemToCart(fetchedItem, quantity);
                    } else {
                        showError("Item not found: " + itemCode);
                        progressIndicator.setVisible(false);
                        addButton.setDisable(false);
                    }
                },
                error -> {
                    System.err.println("Error fetching items: " + error.getMessage());
                    error.printStackTrace();
                    showError("Failed to fetch item: " + error.getMessage());
                    progressIndicator.setVisible(false);
                    addButton.setDisable(false);
                }
            );
        }
    }

    private void addItemToCart(ItemDto item, int quantity) {
        System.out.println("Adding to cart: " + item.getName() + " x " + quantity);
        
        // Check stock
        if (item.getCurrentStock() < quantity) {
            showError("Insufficient stock. Available: " + item.getCurrentStock());
            progressIndicator.setVisible(false);
            addButton.setDisable(false);
            return;
        }
        
        // Check if item already in cart
        CartItemView existingItem = cart.stream()
            .filter(cartItem -> cartItem.getItemCode().equals(item.getItemCode()))
            .findFirst()
            .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            cart.remove(existingItem);
            cart.add(new CartItemView(
                item.getItemCode(),
                item.getName(),
                existingItem.getQuantity() + quantity,
                item.getPrice()
            ));
        } else {
            // Add new item
            cart.add(new CartItemView(
                item.getItemCode(),
                item.getName(),
                quantity,
                item.getPrice()
            ));
        }
        
        itemCodeField.clear();
        quantityField.clear();
        updateTotal();
        
        progressIndicator.setVisible(false);
        addButton.setDisable(false);
        
        System.out.println("Item added to cart. Cart size: " + cart.size());
    }

    @FXML
    private void handleCheckout() {
        if (cart.isEmpty()) {
            showError("Cart is empty");
            return;
        }

        System.out.println("Processing checkout with " + cart.size() + " items");

        List<BillItemDto> items = new ArrayList<>();
        for (CartItemView item : cart) {
            items.add(new BillItemDto(
                item.getItemCode(),
                item.getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            ));
        }

        CheckoutRequest request = new CheckoutRequest(
            "CUST-001", // Default customer
            items,
            "CASH",
            "CASHIER-001"
        );

        progressIndicator.setVisible(true);
        checkoutButton.setDisable(true);

        // ASYNC checkout - doesn't block UI
        asyncExecutor.executeAsync(
            () -> {
                System.out.println("Sending checkout request to server...");
                return serverConnection.processCheckout(request);
            },
            bill -> {
                System.out.println("Checkout successful! Bill ID: " + bill.getBillId());
                showSuccess("Checkout completed!\n\n" +
                           "Bill ID: " + bill.getBillId() + "\n" +
                           "Total: Rs." + bill.getTotal());
                cart.clear();
                updateTotal();
                progressIndicator.setVisible(false);
                checkoutButton.setDisable(false);
                
                // Reload items to update stock
                loadAvailableItems();
            },
            error -> {
                System.err.println("Checkout failed: " + error.getMessage());
                error.printStackTrace();
                showError("Checkout failed:\n" + error.getMessage());
                progressIndicator.setVisible(false);
                checkoutButton.setDisable(false);
            }
        );
    }

    private void updateTotal() {
        BigDecimal total = cart.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalLabel.setText("Total: Rs." + String.format("%.2f", total));
    }

    private void showError(String message) {
        System.err.println("ERROR: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        System.out.println("SUCCESS: " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        System.out.println("INFO: " + title + " - " + message);
    }

    // Inner class for TableView
    public static class CartItemView {
        private final String itemCode;
        private final String name;
        private final int quantity;
        private final BigDecimal price;

        public CartItemView(String itemCode, String name, int quantity, BigDecimal price) {
            this.itemCode = itemCode;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getItemCode() { return itemCode; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }
}