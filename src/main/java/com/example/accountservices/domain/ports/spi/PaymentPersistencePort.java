package com.example.accountservices.domain.ports.spi;

import com.example.accountservices.infrastructure.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentPersistencePort {

    Payment save(Payment payment);

    List<Payment> findAll(String employee);

    Optional<Payment> find(String employee, String period);

    void updatePayment(long salary, String username, String period);
}
