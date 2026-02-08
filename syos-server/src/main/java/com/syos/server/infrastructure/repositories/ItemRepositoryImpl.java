package com.syos.server.infrastructure.repositories;

import com.syos.server.domain.entities.Item;
import com.syos.server.domain.repositories.IItemRepository;
import com.syos.server.infrastructure.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ItemRepositoryImpl implements IItemRepository {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Item> findByCode(String itemCode) {
        lock.readLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM items WHERE item_code = ?")) {
            
            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToItem(rs));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Item> findAll() {
        lock.readLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM items")) {
            
            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
            return items;
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Item> searchByName(String name) {
        lock.readLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM items WHERE name LIKE ?")) {
            
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            
            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
            return items;
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Item> findLowStockItems() {
        lock.readLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT * FROM items WHERE current_stock <= reorder_level")) {
            
            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
            return items;
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(Item item) {
        lock.writeLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO items (item_code, name, description, price, " +
                 "category_code, current_stock, reorder_level) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getDescription());
            stmt.setBigDecimal(4, item.getPrice());
            stmt.setString(5, item.getCategoryCode());
            stmt.setInt(6, item.getCurrentStock());
            stmt.setInt(7, item.getReorderLevel());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(Item item) {
        lock.writeLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE items SET name = ?, description = ?, price = ?, " +
                 "current_stock = ?, reorder_level = ? WHERE item_code = ?")) {
            
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setInt(4, item.getCurrentStock());
            stmt.setInt(5, item.getReorderLevel());
            stmt.setString(6, item.getItemCode());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(String itemCode) {
        lock.writeLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM items WHERE item_code = ?")) {
            
            stmt.setString(1, itemCode);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        return new Item(
            rs.getString("item_code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getString("category_code"),
            rs.getInt("current_stock"),
            rs.getInt("reorder_level")
        );
    }
}