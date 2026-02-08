package com.syos.server.infrastructure.repositories;

import com.syos.server.domain.entities.Bill;
import com.syos.server.domain.entities.BillItem;
import com.syos.server.domain.repositories.IBillRepository;
import com.syos.server.infrastructure.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BillRepositoryImpl implements IBillRepository {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void save(Bill bill) {
        lock.writeLock().lock();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO bills (bill_id, customer_id, cashier_id, payment_method, " +
                "subtotal, tax, total, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, bill.getBillId());
                stmt.setString(2, bill.getCustomerId());
                stmt.setString(3, bill.getCashierId());
                stmt.setString(4, bill.getPaymentMethod());
                stmt.setBigDecimal(5, bill.getSubtotal());
                stmt.setBigDecimal(6, bill.getTax());
                stmt.setBigDecimal(7, bill.getTotal());
                stmt.setTimestamp(8, Timestamp.valueOf(bill.getTimestamp()));
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO bill_items (bill_id, item_code, item_name, quantity, " +
                "unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)")) {
                
                for (BillItem item : bill.getItems()) {
                    stmt.setString(1, bill.getBillId());
                    stmt.setString(2, item.getItemCode());
                    stmt.setString(3, item.getItemName());
                    stmt.setInt(4, item.getQuantity());
                    stmt.setBigDecimal(5, item.getUnitPrice());
                    stmt.setBigDecimal(6, item.getSubtotal());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Bill> findById(String billId) {
        lock.readLock().lock();
        try {
            // Implementation similar to save but with SELECT
            return Optional.empty(); // Simplified for brevity
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Bill> findByCustomerId(String customerId) {
        return new ArrayList<>(); // Simplified
    }

    @Override
    public List<Bill> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return new ArrayList<>(); // Simplified
    }

    @Override
    public List<Bill> findAll() {
        return new ArrayList<>(); // Simplified
    }
}