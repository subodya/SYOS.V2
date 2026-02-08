package com.syos.client.presentation.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {
    
    @FXML private TabPane mainTabPane;
    @FXML private Label userLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionLabel;
    @FXML private Label versionLabel;

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        System.out.println("MainController initialized");
        
        setupClock();
        
        if (userLabel != null) userLabel.setText("User: Admin");
        if (versionLabel != null) versionLabel.setText("Version 2.0.0");
        
        checkServerConnection();
        updateStatus("Ready");
    }

    private void setupClock() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (dateTimeLabel != null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                dateTimeLabel.setText(now.format(formatter));
            }
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void checkServerConnection() {
        // Run network check in a background thread to prevent UI freezing
        new Thread(() -> {
            boolean isConnected = false;
            try {
                java.net.Socket socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress("localhost", 8080), 1000);
                socket.close();
                isConnected = true;
            } catch (Exception e) {
                isConnected = false;
            }

            final boolean status = isConnected;
            Platform.runLater(() -> {
                if (connectionLabel != null) {
                    if (status) {
                        connectionLabel.setText("🟢 Connected to Server");
                        connectionLabel.setStyle("-fx-text-fill: #4CAF50;");
                    } else {
                        connectionLabel.setText("🔴 Server Offline");
                        connectionLabel.setStyle("-fx-text-fill: #F44336;");
                    }
                }
            });
        }).start();
    }

    public void updateStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText("Status: " + status);
        }
    }

    public void cleanup() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}