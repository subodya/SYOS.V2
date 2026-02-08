package com.syos.server.domain.repositories;

import com.syos.server.domain.entities.Item;
import java.util.List;
import java.util.Optional;

public interface IItemRepository {
    Optional<Item> findByCode(String itemCode);
    List<Item> findAll();
    List<Item> searchByName(String name);
    List<Item> findLowStockItems();
    void save(Item item);
    void update(Item item);
    void delete(String itemCode);
}