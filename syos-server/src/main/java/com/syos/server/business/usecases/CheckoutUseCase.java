package com.syos.server.business.usecases;

import com.syos.common.dto.*;
import com.syos.server.domain.entities.*;
import com.syos.server.domain.repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutUseCase {
    private final IItemRepository itemRepository;
    private final IBillRepository billRepository;
    private final ICustomerRepository customerRepository;

    public CheckoutUseCase(IItemRepository itemRepository, 
                          IBillRepository billRepository,
                          ICustomerRepository customerRepository) {
        this.itemRepository = itemRepository;
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
    }

    public BillDto execute(CheckoutRequest request) {
        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Create bill
        String billId = "BILL-" + UUID.randomUUID().toString().substring(0, 8);
        Bill bill = new Bill(billId, customer.getCustomerId(), 
                            request.getCashierId(), request.getPaymentMethod());

        // Process each item
        for (BillItemDto itemDto : request.getItems()) {
            Item item = itemRepository.findByCode(itemDto.getItemCode())
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemDto.getItemCode()));

            // Check stock
            if (item.getCurrentStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName());
            }

            // Reduce stock
            item.reduceStock(itemDto.getQuantity());
            itemRepository.update(item);

            // Add to bill
            BillItem billItem = new BillItem(
                item.getItemCode(),
                item.getName(),
                itemDto.getQuantity(),
                item.getPrice()
            );
            bill.addItem(billItem);
        }

        // Save bill
        billRepository.save(bill);

        // Convert to DTO
        return convertToDto(bill, customer.getName());
    }

    private BillDto convertToDto(Bill bill, String customerName) {
        BillDto dto = new BillDto();
        dto.setBillId(bill.getBillId());
        dto.setCustomerId(bill.getCustomerId());
        dto.setCustomerName(customerName);
        dto.setCashierId(bill.getCashierId());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setSubtotal(bill.getSubtotal());
        dto.setTax(bill.getTax());
        dto.setTotal(bill.getTotal());
        dto.setTimestamp(bill.getTimestamp());

        List<BillItemDto> itemDtos = new ArrayList<>();
        for (BillItem item : bill.getItems()) {
            BillItemDto itemDto = new BillItemDto(
                item.getItemCode(),
                item.getItemName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            );
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);

        return dto;
    }
}