package com.example.accountservices.application.controller;

import com.example.accountservices.domain.data.PaymentRequest;
import com.example.accountservices.domain.data.PaymentResponse;
import com.example.accountservices.domain.ports.api.PaymentServicePort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

// Controller handles endpoints for making payments and updating employees salaries
@Validated
@RestController
@RequestMapping("api/acct")
public class AccountingController {

    private final PaymentServicePort paymentService;

    public AccountingController(PaymentServicePort paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentResponse postPayroll(@RequestBody List<@Valid PaymentRequest> payments) {
        return paymentService.postPayroll(payments);
    }

    @PutMapping("/payments")
    public PaymentResponse updateSalary(@RequestBody @Valid PaymentRequest payment) {
        return paymentService.updateSalary(payment);
    }
}
