package com.example.accountservices.controller;

import com.example.accountservices.dto.PaymentRequest;
import com.example.accountservices.dto.PaymentResponse;
import com.example.accountservices.service.PaymentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

// Controller handles endpoints for making payments and updating employees salaries
@Validated
@RestController
@RequestMapping("api/acct")
public class AccountingController {

    private final PaymentService paymentService;

    public AccountingController(PaymentService paymentService) {
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
