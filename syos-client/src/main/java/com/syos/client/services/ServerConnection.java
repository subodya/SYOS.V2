package com.syos.client.services;

import com.syos.common.dto.*;
import com.syos.common.util.JsonUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Handles HTTP communication with server
 */
public class ServerConnection {
    
    private static final String SERVER_BASE_URL = "http://localhost:8080/api";

    public boolean createNewItem(ItemDto item) throws Exception {
        String url = SERVER_BASE_URL + "/inventory?type=NEW_ITEM";
        String json = JsonUtil.toJson(item);
        
        try {
            String response = sendPostRequest(url, json);
            // Returns true if the server response contains success:true
            return response != null && response.contains("\"success\":true");
        } catch (Exception e) {
            System.err.println("Failed to create new item: " + e.getMessage());
            return false;
        }
    }

    public BillDto processCheckout(CheckoutRequest request) throws Exception {
        String url = SERVER_BASE_URL + "/checkout";
        String jsonRequest = JsonUtil.toJson(request);
        
        String response = sendPostRequest(url, jsonRequest);
        return JsonUtil.fromJson(response, BillDto.class);
    }

    public void addInventory(InventoryBatchDto batch) throws Exception {
        String url = SERVER_BASE_URL + "/inventory"; 
        String jsonRequest = JsonUtil.toJson(batch);
        
        sendPostRequest(url, jsonRequest);
    }

    public List<ItemDto> getAllItems() throws Exception {
        String url = SERVER_BASE_URL + "/items?action=getAll";
        String response = sendGetRequest(url);
        
        return JsonUtil.fromJson(response, 
            new com.google.gson.reflect.TypeToken<List<ItemDto>>(){}.getType());
    }

    public List<ItemDto> searchItems(String query) throws Exception {
        String url = SERVER_BASE_URL + "/items?action=search&q=" + 
            java.net.URLEncoder.encode(query, "UTF-8");
        String response = sendGetRequest(url);
        
        return JsonUtil.fromJson(response,
            new com.google.gson.reflect.TypeToken<List<ItemDto>>(){}.getType());
    }

    
    private String sendPostRequest(String urlString, String jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");  // FIXED
        conn.setDoOutput(true);
    
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }
    
        return readResponse(conn);
    }

    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return readResponse(conn);
    }


    private String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        
        // Handle both 200 OK and 201 Created
        if (responseCode >= 200 && responseCode <= 299) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            // Read error stream for better debugging
            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while((line = in.readLine()) != null) errorResponse.append(line);
            
            throw new Exception("HTTP " + responseCode + " Error: " + errorResponse.toString());
        }
    }
}