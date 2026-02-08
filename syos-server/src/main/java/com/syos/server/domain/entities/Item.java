package com.syos.server.domain.entities;

import java.math.BigDecimal;

public class Item {
    private String itemCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryCode;
    private int currentStock;
    private int reorderLevel;

    public Item(String itemCode, String name, String description, 
                BigDecimal price, String categoryCode, 
                int currentStock, int reorderLevel) {
        this.itemCode = itemCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryCode = categoryCode;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
    }

    // Business methods
    public boolean isLowStock() {
        return currentStock <= reorderLevel;
    }

    public void reduceStock(int quantity) {
        if (quantity > currentStock) {
            throw new IllegalStateException("Insufficient stock for item: " + itemCode);
        }
        this.currentStock -= quantity;
    }

    public void addStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.currentStock += quantity;
    }

    // Getters
    public String getItemCode() { return itemCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategoryCode() { return categoryCode; }
    public int getCurrentStock() { return currentStock; }
    public int getReorderLevel() { return reorderLevel; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
}