package com.syos.client.presentation.controllers;

import com.syos.client.concurrency.AsyncTaskExecutor;
import com.syos.client.services.ServerConnection;
import com.syos.common.dto.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

public class DashboardController {
    
    @FXML private Label todaySalesValue;
    @FXML private Label todaySalesSubtitle;
    @FXML private Label lowStockValue;
    @FXML private Label expiringSoonValue;
    @FXML private Label inventoryValue;
    @FXML private Label transactionCount;
    @FXML WebSocket webSocket;
    @FXML private TableView<BillDto> recentTransactionsTable;
    @FXML private TableColumn<BillDto, String> billIdColumn;
    @FXML private TableColumn<BillDto, String> dateTimeColumn;
    @FXML private TableColumn<BillDto, String> customerColumn;
    @FXML private TableColumn<BillDto, Integer> itemsColumn;
    @FXML private TableColumn<BillDto, String> totalColumn; // Changed to String for formatted LKR
    @FXML private TableColumn<BillDto, String> paymentColumn;
    
    @FXML private BarChart<String, Number> stockLevelChart;

    private ServerConnection serverConnection;
    private AsyncTaskExecutor asyncExecutor;

    @FXML
    public void initialize() {
        serverConnection = new ServerConnection();
        asyncExecutor = new AsyncTaskExecutor();
        
        setupTableColumns();
        loadDashboardData();
        connectWebSocket();

    }
    private void connectWebSocket() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8080/ws/dashboard"), new WebSocketListener())
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("Connected to WebSocket for real-time updates");
                });
        } catch (Exception e) {
            System.err.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

        // Inner class to handle incoming messages
        private class WebSocketListener implements WebSocket.Listener {
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("WebSocket Update Received: " + data);
            
            if ("REFRESH_DASHBOARD".equals(data.toString())) {
                // Must update UI on JavaFX Thread
                Platform.runLater(() -> {
                    System.out.println("Refreshing Dashboard Data...");
                    loadDashboardData(); // Your existing refresh method
                });
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }

        // Optional: Close socket on app exit
        public void shutdown() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "App Closing");
        }
    }

    private void setupTableColumns() {
        billIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getBillId()));
            
        dateTimeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTimestamp().toLocalDate().toString())); // Simplified date
            
        customerColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCustomerName()));
            
        itemsColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getItems().size()).asObject());
            
        // Format Total as LKR
        totalColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(formatCurrency(data.getValue().getTotal())));
            
        paymentColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPaymentMethod()));
    }

    private void loadDashboardData() {
        loadSalesMetrics();
        loadInventoryMetrics();
        loadStockChart();
    }

    private void loadSalesMetrics() {
        // Mock data logic for now
        if(todaySalesValue != null) {
            todaySalesValue.setText(formatCurrency(BigDecimal.ZERO));
            todaySalesSubtitle.setText("No sales today");
        }
    }

    private void loadInventoryMetrics() {
        asyncExecutor.executeAsync(
            () -> serverConnection.getAllItems(),
            items -> {
                int lowStock = (items == null) ? 0 : (int) items.stream().filter(ItemDto::isLowStock).count();
                int outOfStock = (items == null) ? 0 : (int) items.stream().filter(item -> item.getCurrentStock() == 0).count();
                
                BigDecimal totalValue = (items == null) ? BigDecimal.ZERO : items.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCurrentStock())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Platform.runLater(() -> {
                    if (lowStockValue != null) lowStockValue.setText(String.valueOf(lowStock));
                    if (expiringSoonValue != null) expiringSoonValue.setText(String.valueOf(outOfStock));
                    if (inventoryValue != null) inventoryValue.setText(formatCurrency(totalValue));
                });
            },
            error -> System.err.println("Failed to load inventory metrics")
        );
    }

    private void loadStockChart() {
        if (stockLevelChart == null) return;
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Current Stock");
        series.getData().add(new XYChart.Data<>("Beverages", 120));
        series.getData().add(new XYChart.Data<>("Snacks", 320));
        series.getData().add(new XYChart.Data<>("Household", 80));
        series.getData().add(new XYChart.Data<>("Fresh", 200));
        
        stockLevelChart.getData().clear();
        stockLevelChart.getData().add(series);
    }

    // Helper to format LKR Currency
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "LKR 0.00";
        // Create a custom format for Sri Lanka
        // Java's default Locale for SL might use "Rs.", but you requested "LKR" or specific styling.
        // We will manually force "LKR " prefix.
        return String.format("LKR %,.2f", amount);
    }

    @FXML private void handleRefresh() { loadDashboardData(); }
    @FXML private void handleNewSale() { 
        // Logic to switch tabs would go here
        System.out.println("Navigating to POS..."); 
    }
    @FXML private void handleAddStock() { System.out.println("Add Stock..."); }
    @FXML private void handleViewReports() { System.out.println("Reports..."); }
    @FXML private void handleCheckLowStock() { System.out.println("Checking low stock..."); }
}