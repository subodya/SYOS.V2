package com.syos.client.presentation.controllers;

import com.syos.client.concurrency.AsyncTaskExecutor;
import com.syos.client.services.ServerConnection;
import com.syos.common.dto.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReportsController {
    
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private Label todaySalesLabel;
    @FXML private Label weekSalesLabel;
    @FXML private Label monthSalesLabel;
    @FXML private Label transactionCountLabel;
    
    @FXML private LineChart<String, Number> salesChart;
    @FXML private TableView<ItemDto> topItemsTable;
    @FXML private TableColumn<ItemDto, Integer> rankColumn;
    @FXML private TableColumn<ItemDto, String> topItemCodeColumn;
    @FXML private TableColumn<ItemDto, String> topItemNameColumn;
    @FXML private TableColumn<ItemDto, Integer> qtySoldColumn;
    @FXML private TableColumn<ItemDto, BigDecimal> revenueColumn;
    
    @FXML private TableView<ItemDto> lowStockTable;
    @FXML private TableColumn<ItemDto, String> lowStockCodeColumn;
    @FXML private TableColumn<ItemDto, String> lowStockNameColumn;
    @FXML private TableColumn<ItemDto, Integer> currentStockColumn;
    @FXML private TableColumn<ItemDto, Integer> reorderLevelColumn;
    @FXML private TableColumn<ItemDto, Integer> shortageColumn;
    
    @FXML private ProgressIndicator reportProgress;

    private ServerConnection serverConnection;
    private AsyncTaskExecutor asyncExecutor;

    @FXML
    public void initialize() {
        System.out.println("ReportsController initialized");
        
        serverConnection = new ServerConnection();
        asyncExecutor = new AsyncTaskExecutor();
        
        setupReportTypes();
        setupDatePickers();
        setupTables();
        loadReportData();
    }

    private void setupReportTypes() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
            "Daily Sales Report",
            "Weekly Sales Report",
            "Monthly Sales Report",
            "Inventory Report",
            "Low Stock Report",
            "Top Selling Items"
        ));
        reportTypeCombo.setValue("Daily Sales Report");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupTables() {
        // Top Items Table
        rankColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(
                topItemsTable.getItems().indexOf(data.getValue()) + 1).asObject());
        topItemCodeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getItemCode()));
        topItemNameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        
        // Low Stock Table
        lowStockCodeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getItemCode()));
        lowStockNameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        currentStockColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getCurrentStock()).asObject());
        reorderLevelColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getReorderLevel()).asObject());
        shortageColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(
                Math.max(0, data.getValue().getReorderLevel() - 
                           data.getValue().getCurrentStock())).asObject());
    }

    private void loadReportData() {
        System.out.println("Loading report data...");
        
        // Load sales metrics
        todaySalesLabel.setText("Rs 0.00");
        weekSalesLabel.setText("Rs 0.00");
        monthSalesLabel.setText("Rs 0.00");
        transactionCountLabel.setText("0");
        
        // Load low stock items
        loadLowStockReport();
        
        // Load sample sales chart
        loadSalesChart();
    }

    private void loadLowStockReport() {
        asyncExecutor.executeAsync(
            () -> serverConnection.getAllItems(),
            items -> {
                var lowStockItems = items.stream()
                    .filter(ItemDto::isLowStock)
                    .collect(java.util.stream.Collectors.toList());
                
                lowStockTable.setItems(FXCollections.observableArrayList(lowStockItems));
            },
            error -> {
                System.err.println("Failed to load low stock report: " + error.getMessage());
            }
        );
    }

    private void loadSalesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        
        // Sample data - replace with real data from server
        series.getData().add(new XYChart.Data<>("Jan 13", 1250));
        series.getData().add(new XYChart.Data<>("Jan 14", 1580));
        series.getData().add(new XYChart.Data<>("Jan 15", 1320));
        series.getData().add(new XYChart.Data<>("Jan 16", 1890));
        series.getData().add(new XYChart.Data<>("Jan 17", 2100));
        
        salesChart.getData().add(series);
    }

    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeCombo.getValue();
        System.out.println("Generating report: " + reportType);
        
        showInfo("Generate Report", 
            "Generating " + reportType + "...\n" +
            "From: " + startDatePicker.getValue() + "\n" +
            "To: " + endDatePicker.getValue());
    }

    @FXML
    private void handleExportPDF() {
        showInfo("Export PDF", "PDF export feature coming soon...");
    }

    @FXML
    private void handleRefresh() {
        loadReportData();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}