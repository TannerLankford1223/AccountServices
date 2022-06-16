package com.example.accountservices.service;

import com.example.accountservices.dto.PaymentResponse;
import com.example.accountservices.entity.Payment;

import java.util.List;

public interface PaymentService {
    PaymentResponse postPayroll(List<Payment> requests);

    void insertPayment(Payment request);

    PaymentResponse updateSalary(Payment request);

    List<PaymentResponse> getPayments(String username);

    PaymentResponse getPayment(String username, String period);
}
