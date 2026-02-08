package com.syos.server.infrastructure.repositories;

import com.syos.server.domain.entities.Customer;
import com.syos.server.domain.repositories.ICustomerRepository;
import com.syos.server.infrastructure.database.DatabaseConnection;

import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CustomerRepositoryImpl implements ICustomerRepository {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void save(Customer customer) {
        lock.writeLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO customers (customer_id, name, email, phone, address) " +
                 "VALUES (?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        lock.readLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM customers WHERE customer_id = ?")) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                ));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Customer> findAll() {
        lock.readLock().lock();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                ));
            }
            return customers;
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void update(Customer customer) {
        // Similar to save but with UPDATE
    }

    @Override
    public void delete(String customerId) {
        // Similar pattern
    }
}