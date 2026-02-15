package com.syos.server.business;

import com.syos.common.dto.*;
import com.syos.server.business.usecases.*;
import com.syos.server.domain.entities.Item;
import com.syos.server.domain.repositories.*;
import java.util.List;

/**
 * Business Facade - coordinates all use cases
 * This is the single entry point to the business layer
 */
public class BusinessFacade {
    private final CheckoutUseCase checkoutUseCase;
    private final InventoryManagementUseCase inventoryUseCase;
    private final IItemRepository itemRepository;

    /**
     * Updated Constructor: Uses the repositories passed from the Server context.
     * This follows Dependency Injection principles.
     */
    public BusinessFacade(IItemRepository itemRepo, 
                         IBillRepository billRepo,
                         ICustomerRepository customerRepo) {
        // Assign the passed-in repositories to the fields
        this.itemRepository = itemRepo; 
        
        // Initialize Use Cases using the same repository instances
        this.checkoutUseCase = new CheckoutUseCase(itemRepo, billRepo, customerRepo);
        this.inventoryUseCase = new InventoryManagementUseCase(itemRepo);
    }

    // --- Product Operations ---

    /**
     * Adds a completely new product to the system.
     */
public void addItem(ItemDto itemDto) {
    // Use the constructor defined in Item.java
    // Order: code, name, desc, price, cat, stock, reorder
    Item item = new Item(
        itemDto.getItemCode(),
        itemDto.getName(),
        itemDto.getDescription(),
        itemDto.getPrice(),
        itemDto.getCategoryCode(),
        0, // currentStock starts at 0 for new items
        itemDto.getReorderLevel()
    );

    // Save to database via repository
    itemRepository.save(item);
}

    // --- Checkout Operations ---

    public BillDto processCheckout(CheckoutRequest request) {
        return checkoutUseCase.execute(request);
    }

    // --- Inventory Operations ---

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