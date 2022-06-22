package com.example.accountservices.service;

import com.example.accountservices.dto.PaymentRequest;
import com.example.accountservices.dto.PaymentResponse;
import com.example.accountservices.entity.Payment;

import java.util.List;

public interface PaymentService {
    PaymentResponse postPayroll(List<PaymentRequest> requests);

    void insertPayment(Payment payment);

    PaymentResponse updateSalary(PaymentRequest request);

    List<PaymentResponse> getPayments(String username);

    PaymentResponse getPayment(String username, String period);
}
