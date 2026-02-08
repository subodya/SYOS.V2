package com.syos.client.presentation.controllers;

import com.syos.client.concurrency.AsyncTaskExecutor;
import com.syos.client.services.ServerConnection;
import com.syos.common.dto.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class InventoryController {
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> stockStatusFilter;
    
    @FXML private Label totalItemsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label recordCountLabel;
    
    @FXML private TableView<ItemDto> inventoryTable;
    @FXML private TableColumn<ItemDto, String> itemCodeColumn;
    @FXML private TableColumn<ItemDto, String> itemNameColumn;
    @FXML private TableColumn<ItemDto, String> categoryColumn;
    @FXML private TableColumn<ItemDto, BigDecimal> priceColumn;
    @FXML private TableColumn<ItemDto, Integer> stockColumn;
    @FXML private TableColumn<ItemDto, Integer> reorderColumn;
    @FXML private TableColumn<ItemDto, String> statusColumn;
    @FXML private TableColumn<ItemDto, Void> actionsColumn;
    
    @FXML private ProgressIndicator loadingProgress;

    private ObservableList<ItemDto> allItems;
    private ObservableList<ItemDto> filteredItems;
    private ServerConnection serverConnection;
    private AsyncTaskExecutor asyncExecutor;

    @FXML
    public void initialize() {
        System.out.println("InventoryController initialized");
        
        serverConnection = new ServerConnection();
        asyncExecutor = new AsyncTaskExecutor();
        allItems = FXCollections.observableArrayList();
        filteredItems = FXCollections.observableArrayList();
        
        setupTable();
        setupFilters();
        loadInventory();
    }

    private void setupTable() {
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryCode"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        reorderColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        
        // Status column with color coding
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    ItemDto itemDto = getTableRow().getItem();
                    String status;
                    String styleClass;
                    
                    if (itemDto.getCurrentStock() == 0) {
                        status = "OUT OF STOCK";
                        styleClass = "table-cell-out-of-stock";
                    } else if (itemDto.isLowStock()) {
                        status = "LOW STOCK";
                        styleClass = "table-cell-low-stock";
                    } else {
                        status = "IN STOCK";
                        styleClass = "table-cell-in-stock";
                    }
                    
                    setText(status);
                    getStyleClass().removeAll("table-cell-out-of-stock", 
                                            "table-cell-low-stock", 
                                            "table-cell-in-stock");
                    getStyleClass().add(styleClass);
                }
            }
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            final Button viewBtn = new Button("View");
            final Button addBtn = new Button("+ Stock");
            
            {
                viewBtn.setOnAction(event -> {
                    ItemDto item = getTableView().getItems().get(getIndex());
                    handleViewDetails(item);
                });
                addBtn.setOnAction(event -> {
                    ItemDto item = getTableView().getItems().get(getIndex());
                    handleAddStockToItem(item);
                });
                addBtn.getStyleClass().add("button-success");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, viewBtn, addBtn);
                    setGraphic(buttons);
                }
            }
        });
        
        inventoryTable.setItems(filteredItems);
    }

    private void setupFilters() {
        // Category filter
        categoryFilter.setItems(FXCollections.observableArrayList(
            "All Categories", "ELECTRONICS", "ACCESSORIES", "FOOD", "OTHER"
        ));
        categoryFilter.setValue("All Categories");
        
        // Stock status filter
        stockStatusFilter.setItems(FXCollections.observableArrayList(
            "All Status", "In Stock", "Low Stock", "Out of Stock"
        ));
        stockStatusFilter.setValue("All Status");
        
        // Search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        stockStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadInventory() {
        System.out.println("Loading inventory...");
        loadingProgress.setVisible(true);
        
        asyncExecutor.executeAsync(
            () -> serverConnection.getAllItems(),
            items -> {
                System.out.println("Loaded " + items.size() + " items");
                allItems.setAll(items);
                applyFilters();
                updateMetrics();
                loadingProgress.setVisible(false);
            },
            error -> {
                System.err.println("Failed to load inventory: " + error.getMessage());
                error.printStackTrace();
                loadingProgress.setVisible(false);
                showError("Load Failed", error.getMessage());
            }
        );
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();
        String status = stockStatusFilter.getValue();
        
        var filtered = allItems.stream()
            .filter(item -> {
                // Search filter
                if (!searchText.isEmpty()) {
                    return item.getItemCode().toLowerCase().contains(searchText) ||
                           item.getName().toLowerCase().contains(searchText);
                }
                return true;
            })
            .filter(item -> {
                // Category filter
                if (!"All Categories".equals(category)) {
                    return item.getCategoryCode().equals(category);
                }
                return true;
            })
            .filter(item -> {
                // Status filter
                return switch (status) {
                    case "In Stock" -> item.getCurrentStock() > item.getReorderLevel();
                    case "Low Stock" -> item.isLowStock() && item.getCurrentStock() > 0;
                    case "Out of Stock" -> item.getCurrentStock() == 0;
                    default -> true;
                };
            })
            .collect(Collectors.toList());
        
        filteredItems.setAll(filtered);
        recordCountLabel.setText("Showing " + filtered.size() + " of " + allItems.size() + " items");
    }

    private void updateMetrics() {
        int total = allItems.size();
        int lowStock = (int) allItems.stream().filter(ItemDto::isLowStock).count();
        int outOfStock = (int) allItems.stream()
            .filter(item -> item.getCurrentStock() == 0).count();
        
        BigDecimal totalValue = allItems.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCurrentStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalItemsLabel.setText(String.valueOf(total));
        lowStockLabel.setText(String.valueOf(lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStock));
        totalValueLabel.setText(String.format("Rs. %.2f", totalValue));
    }

    @FXML
    private void handleRefresh() {
        loadInventory();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        stockStatusFilter.setValue("All Status");
    }

    @FXML
    private void handleAddNewItem() {
        showInfo("Add New Item", "Feature coming soon...");
    }

    @FXML
    private void handleAddStock() {
        showInfo("Add Stock", "Feature coming soon...");
    }

    @FXML
    private void handleExport() {
        showInfo("Export", "Export feature coming soon...");
    }

    @FXML
    private void handleViewDetails() {
        ItemDto selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleViewDetails(selected);
        }
    }

    private void handleViewDetails(ItemDto item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(item.getName());
        alert.setContentText(
            "Code: " + item.getItemCode() + "\n" +
            "Category: " + item.getCategoryCode() + "\n" +
            "Price: Rs" + item.getPrice() + "\n" +
            "Stock: " + item.getCurrentStock() + "\n" +
            "Reorder Level: " + item.getReorderLevel() + "\n" +
            "Description: " + item.getDescription()
        );
        alert.showAndWait();
    }

    @FXML
    private void handleAddStockToItem() {
        ItemDto selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleAddStockToItem(selected);
        }
    }

    private void handleAddStockToItem(ItemDto item) {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock for: " + item.getName());
        dialog.setContentText("Quantity:");
        
        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int quantity = Integer.parseInt(qtyStr);
                if (quantity > 0) {
                    addStockToItem(item, quantity);
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid number");
            }
        });
    }

    private void addStockToItem(ItemDto item, int quantity) {
        InventoryBatchDto batch = new InventoryBatchDto(
            item.getItemCode(),
            quantity,
            item.getPrice(),
            java.time.LocalDate.now().plusMonths(6)
        );
        
        loadingProgress.setVisible(true);
        
        asyncExecutor.executeAsync(
            () -> {
                serverConnection.addInventory(batch);
                return null;
            },
            result -> {
                showSuccess("Stock Added", 
                    "Added " + quantity + " units to " + item.getName());
                loadInventory(); // Reload
                loadingProgress.setVisible(false);
            },
            error -> {
                showError("Failed to Add Stock", error.getMessage());
                loadingProgress.setVisible(false);
            }
        );
    }

    @FXML
    private void handleEditItem() {
        showInfo("Edit Item", "Feature coming soon...");
    }

    @FXML
    private void handleDeleteItem() {
        showInfo("Delete Item", "Feature coming soon...");
    }

    // Helper Methods
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }}