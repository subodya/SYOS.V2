package com.syos.common.dto;

import java.io.Serializable;
import java.util.List;

public class CheckoutRequest implements Serializable {
    private String customerId;
    private List<BillItemDto> items;
    private String paymentMethod; // "CASH" or "CARD"
    private String cashierId;

    public CheckoutRequest() {}

    public CheckoutRequest(String customerId, List<BillItemDto> items, 
                          String paymentMethod, String cashierId) {
        this.customerId = customerId;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.cashierId = cashierId;
    }

    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public List<BillItemDto> getItems() { return items; }
    public void setItems(List<BillItemDto> items) { this.items = items; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
}