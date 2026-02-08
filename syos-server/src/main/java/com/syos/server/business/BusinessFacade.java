package com.syos.server.business;

import com.syos.common.dto.*;
import com.syos.server.business.usecases.*;
import com.syos.server.domain.repositories.*;

import java.util.List;

/**
 * Business Facade - coordinates all use cases
 * This is the single entry point to the business layer
 */
public class BusinessFacade {
    private final CheckoutUseCase checkoutUseCase;
    private final InventoryManagementUseCase inventoryUseCase;

    public BusinessFacade(IItemRepository itemRepo, 
                         IBillRepository billRepo,
                         ICustomerRepository customerRepo) {
        this.checkoutUseCase = new CheckoutUseCase(itemRepo, billRepo, customerRepo);
        this.inventoryUseCase = new InventoryManagementUseCase(itemRepo);
    }

    // Checkout operations
    public BillDto processCheckout(CheckoutRequest request) {
        return checkoutUseCase.execute(request);
    }

    // Inventory operations
    public void addInventory(InventoryBatchDto batch) {
        inventoryUseCase.addInventory(batch);
    }

    public ItemDto getItem(String itemCode) {
        return inventoryUseCase.getItem(itemCode);
    }

    public List<ItemDto> getAllItems() {
        return inventoryUseCase.getAllItems();
    }

    public List<ItemDto> searchItems(String query) {
        return inventoryUseCase.searchItems(query);
    }

    public List<ItemDto> getLowStockItems() {
        return inventoryUseCase.getLowStockItems();
    }
}