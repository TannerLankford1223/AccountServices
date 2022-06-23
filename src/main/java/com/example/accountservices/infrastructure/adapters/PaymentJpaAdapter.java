package com.example.accountservices.infrastructure.adapters;

import com.example.accountservices.domain.ports.spi.PaymentPersistencePort;
import com.example.accountservices.infrastructure.entity.Payment;
import com.example.accountservices.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentJpaAdapter implements PaymentPersistencePort {

    private final PaymentRepository paymentRepo;

    public PaymentJpaAdapter(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepo.save(payment);
    }

    @Override
    public List<Payment> findAll(String employee) {
        return paymentRepo.findAllByUsernameOrderByPeriodDesc(employee);
    }

    @Override
    public Optional<Payment> find(String employee, String period) {
        return paymentRepo.findPaymentByUsernameAndPeriod(employee, period);
    }

    @Override
    public void updatePayment(long salary, String username, String period) {
        paymentRepo.updatePaymentByEmployeeAndPeriod(salary, username, period);
    }
}
