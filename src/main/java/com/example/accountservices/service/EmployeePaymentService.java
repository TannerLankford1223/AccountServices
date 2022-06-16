package com.example.accountservices.service;

import com.example.accountservices.dto.PaymentResponse;
import com.example.accountservices.entity.Payment;
import com.example.accountservices.persistence.PaymentRepository;
import com.example.accountservices.persistence.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class EmployeePaymentService implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;

    public EmployeePaymentService(PaymentRepository paymentRepo, UserRepository userRepo) {
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
    }


    @Transactional
    @Override
    public PaymentResponse postPayroll(List<Payment> payments) {
        return null;
    }

    @Transactional
    @Override
    public void insertPayment(Payment payment) {
    }

    @CachePut("payments")
    @Transactional
    @Override
    public PaymentResponse updateSalary(Payment payment) {
        return null;
    }

    @CacheEvict("payments")
    @Override
    public List<PaymentResponse> getPayments(String username) {
        return null;
    }

    @Cacheable("payments")
    @Override
    public PaymentResponse getPayment(String username, String period) {
        return null;
    }

}
