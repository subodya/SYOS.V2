package com.syos.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemDto implements Serializable {
    private String itemCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryCode;
    private int currentStock;
    private int reorderLevel;

    public ItemDto() {}

    public ItemDto(String itemCode, String name, String description, 
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

    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    
    public boolean isLowStock() {
        return currentStock <= reorderLevel;
    }
}