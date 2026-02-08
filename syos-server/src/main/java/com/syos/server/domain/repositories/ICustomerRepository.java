package com.syos.server.domain.repositories;

import com.syos.server.domain.entities.Customer;
import java.util.List;
import java.util.Optional;

public interface ICustomerRepository {
    void save(Customer customer);
    Optional<Customer> findById(String customerId);
    List<Customer> findAll();
    void update(Customer customer);
    void delete(String customerId);
}