package com.syos.server.domain.repositories;

import com.syos.server.domain.entities.Bill;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IBillRepository {
    void save(Bill bill);
    Optional<Bill> findById(String billId);
    List<Bill> findByCustomerId(String customerId);
    List<Bill> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<Bill> findAll();
}