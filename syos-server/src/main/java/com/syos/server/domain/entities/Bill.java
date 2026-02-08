package com.syos.server.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {
    private String billId;
    private String customerId;
    private List<BillItem> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String paymentMethod;
    private LocalDateTime timestamp;
    private String cashierId;

    public Bill(String billId, String customerId, String cashierId, String paymentMethod) {
        this.billId = billId;
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.paymentMethod = paymentMethod;
        this.items = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public void addItem(BillItem item) {
        items.add(item);
        calculateTotals();
    }

    private void calculateTotals() {
        subtotal = items.stream()
            .map(BillItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax
        total = subtotal.add(tax);
    }

    // Getters
    public String getBillId() { return billId; }
    public String getCustomerId() { return customerId; }
    public List<BillItem> getItems() { return new ArrayList<>(items); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getCashierId() { return cashierId; }
}