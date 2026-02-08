package com.syos.server.business.usecases;

import com.syos.common.dto.*;
import com.syos.server.domain.entities.Item;
import com.syos.server.domain.repositories.IItemRepository;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryManagementUseCase {
    private final IItemRepository itemRepository;

    public InventoryManagementUseCase(IItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void addInventory(InventoryBatchDto batch) {
        Item item = itemRepository.findByCode(batch.getItemCode())
            .orElseThrow(() -> new RuntimeException("Item not found: " + batch.getItemCode()));

        item.addStock(batch.getQuantity());
        itemRepository.update(item);
    }

    public ItemDto getItem(String itemCode) {
        Item item = itemRepository.findByCode(itemCode)
            .orElseThrow(() -> new RuntimeException("Item not found: " + itemCode));
        
        return convertToDto(item);
    }

    public List<ItemDto> getAllItems() {
        return itemRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String query) {
        return itemRepository.searchByName(query).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<ItemDto> getLowStockItems() {
        return itemRepository.findLowStockItems().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private ItemDto convertToDto(Item item) {
        return new ItemDto(
            item.getItemCode(),
            item.getName(),
            item.getDescription(),
            item.getPrice(),
            item.getCategoryCode(),
            item.getCurrentStock(),
            item.getReorderLevel()
        );
    }
}