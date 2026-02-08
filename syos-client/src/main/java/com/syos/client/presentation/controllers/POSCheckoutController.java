package com.syos.client.presentation.controllers;

import com.syos.client.concurrency.AsyncTaskExecutor;
import com.syos.client.services.ServerConnection;
import com.syos.common.dto.ItemDto;
import com.syos.common.dto.BillItemDto;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class POSCheckoutController {

    @FXML private FlowPane productGrid;
    @FXML private TextField searchField;
    
    // Cart Table
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> itemNameCol;
    @FXML private TableColumn<CartItem, Integer> qtyCol;
    @FXML private TableColumn<CartItem, String> priceCol;
    @FXML private TableColumn<CartItem, String> totalCol;
    
    @FXML private Label totalAmountLabel;

    private ServerConnection serverConnection;
    private AsyncTaskExecutor asyncExecutor;
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private List<ItemDto> allItems; // Cache for searching

    @FXML
    public void initialize() {
        serverConnection = new ServerConnection();
        asyncExecutor = new AsyncTaskExecutor();
        
        setupCartTable();
        loadProducts();
        
        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private void setupCartTable() {
        itemNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        qtyCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        priceCol.setCellValueFactory(data -> new SimpleStringProperty(formatLKR(data.getValue().getPrice())));
        totalCol.setCellValueFactory(data -> new SimpleStringProperty(formatLKR(data.getValue().getTotal())));
        
        cartTable.setItems(cartList);
    }

    private void loadProducts() {
        asyncExecutor.executeAsync(
            () -> serverConnection.getAllItems(),
            items -> {
                this.allItems = items;
                Platform.runLater(() -> {
                    populateProductGrid(items);
                });
            },
            error -> System.err.println("Error loading products: " + error.getMessage())
        );
    }

    // Creates the Visual Grid
    private void populateProductGrid(List<ItemDto> items) {
        productGrid.getChildren().clear();
        
        for (ItemDto item : items) {
            // Create the Card
            VBox card = createProductCard(item);
            productGrid.getChildren().add(card);
        }
    }

    private void filterProducts(String query) {
        if (allItems == null) return;
        
        List<ItemDto> filtered = allItems.stream()
            .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
            
        populateProductGrid(filtered);
    }

    // --- VISUAL CARD GENERATOR ---
    private VBox createProductCard(ItemDto item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        
        // 1. Icon (Gmail Style)
        StackPane iconPane = new StackPane();
        Rectangle bg = new Rectangle(80, 80);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(generateRandomPastelColor(item.getName())); // Unique color per name
        
        Label initial = new Label(item.getName().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        iconPane.getChildren().addAll(bg, initial);
        
        // 2. Details
        Label nameLbl = new Label(item.getName());
        nameLbl.getStyleClass().add("product-name");
        
        Label priceLbl = new Label(formatLKR(item.getPrice()));
        priceLbl.getStyleClass().add("product-price");
        
        Label stockLbl = new Label("Stock: " + item.getCurrentStock());
        stockLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(iconPane, nameLbl, priceLbl, stockLbl);
        
        // 3. Click Event
        card.setOnMouseClicked(e -> addToCart(item));
        
        return card;
    }

    private void addToCart(ItemDto item) {
        if (item.getCurrentStock() <= 0) {
            showAlert("Out of Stock", "This item is not available.");
            return;
        }

        // Check if already in cart
        for (CartItem cartItem : cartList) {
            if (cartItem.getItemDto().getName().equals(item.getName())) { // Assuming Name is unique or use ID
                cartItem.incrementQuantity();
                cartTable.refresh();
                updateTotal();
                return;
            }
        }

        // Add new
        cartList.add(new CartItem(item));
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = cartList.stream()
            .map(CartItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalAmountLabel.setText(formatLKR(total));
    }

    @FXML
    private void handleClearCart() {
        cartList.clear();
        updateTotal();
    }

    @FXML
    private void handleCheckout() {
        if (cartList.isEmpty()) {
            showAlert("Empty Cart", "Please select items first.");
            return;
        }
        System.out.println("Processing Checkout... (Implement Server Call Here)");
        // Convert CartItem to BillItemDto and send to server...
        handleClearCart();
        showAlert("Success", "Transaction Completed!");
    }

    // --- HELPERS ---

    private String formatLKR(BigDecimal amount) {
        return String.format("LKR %,.2f", amount);
    }
    
    // Generate a consistent color based on the product name string
    private Color generateRandomPastelColor(String seed) {
        int hash = seed.hashCode();
        Random rng = new Random(hash);
        float hue = rng.nextFloat() * 360;
        return Color.hsb(hue, 0.6, 0.9); // Bright pastel colors
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class for TableView model
    public static class CartItem {
        private final ItemDto itemDto;
        private int quantity;

        public CartItem(ItemDto itemDto) {
            this.itemDto = itemDto;
            this.quantity = 1;
        }

        public void incrementQuantity() { this.quantity++; }
        public String getName() { return itemDto.getName(); }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return itemDto.getPrice(); }
        public BigDecimal getTotal() { return itemDto.getPrice().multiply(BigDecimal.valueOf(quantity)); }
        public ItemDto getItemDto() { return itemDto; }
    }
}