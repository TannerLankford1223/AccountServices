package com.example.accountservices.domain.ports.api;

import com.example.accountservices.domain.data.PaymentRequest;
import com.example.accountservices.domain.data.PaymentResponse;
import com.example.accountservices.infrastructure.entity.Payment;

import java.util.List;

public interface PaymentServicePort {
    PaymentResponse postPayroll(List<PaymentRequest> requests);

    void insertPayment(Payment payment);

    PaymentResponse updateSalary(PaymentRequest request);

    List<PaymentResponse> getPayments(String username);

    PaymentResponse getPayment(String username, String period);
}
