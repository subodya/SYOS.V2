package com.syos.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryBatchDto implements Serializable {
    private String batchId;
    private String itemCode;
    private int quantity;
    private BigDecimal costPrice;
    private LocalDate expiryDate;
    private String supplierId;

    public InventoryBatchDto() {}

    public InventoryBatchDto(String itemCode, int quantity, 
                             BigDecimal costPrice, LocalDate expiryDate) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.costPrice = costPrice;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
}