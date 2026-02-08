package com.syos.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application
 * SYOS - Point of Sale & Inventory Management System
 * Assignment 2 - Modern GUI with Concurrent Programming
 */
public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting SYOS Client Application...");
            
            // Load main FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Main.fxml"));
            
            Parent root = loader.load();
            
            // Create scene with stylesheet
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(
                getClass().getResource("/css/modern-styles.css").toExternalForm());
            
            // Configure stage
            primaryStage.setTitle("SYOS - Point of Sale & Inventory Management");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
            System.out.println("✓ Application started successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to start application:");
            e.printStackTrace();
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        System.out.println("Application shutting down...");
        // Cleanup resources if needed
    }

    public static void main(String[] args) {
        launch(args);
    }
}