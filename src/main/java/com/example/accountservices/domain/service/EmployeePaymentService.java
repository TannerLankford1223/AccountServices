package com.example.accountservices.domain.service;

import com.example.accountservices.domain.data.PaymentRequest;
import com.example.accountservices.domain.data.PaymentResponse;
import com.example.accountservices.domain.ports.api.PaymentServicePort;
import com.example.accountservices.domain.ports.spi.PaymentPersistencePort;
import com.example.accountservices.domain.ports.spi.UserPersistencePort;
import com.example.accountservices.domain.util.DateValidator;
import com.example.accountservices.infrastructure.entity.Employee;
import com.example.accountservices.infrastructure.entity.Payment;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeePaymentService implements PaymentServicePort {

    private final PaymentPersistencePort paymentRepo;
    private final UserPersistencePort userRepo;

    public EmployeePaymentService(PaymentPersistencePort paymentRepo, UserPersistencePort userRepo) {
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
    }


    @Transactional
    @Override
    public PaymentResponse postPayroll(List<PaymentRequest> payments) {
        for (PaymentRequest request : payments) {
            if (DateValidator.isDateInvalid(request.getPeriod())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date");
            } else if (paymentRepo.find(request.getEmail(),
                    request.getPeriod()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment already exists");
            } else {
                Payment payment = new Payment(request.getEmail(), request.getPeriod(), request.getSalary());
                insertPayment(payment);
            }
        }

        return PaymentResponse.builder().status("Added successfully").build();
    }

    @Transactional
    @Override
    public void insertPayment(Payment payment) {
        Optional<Employee> userOpt = userRepo.find(payment.getUsername());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        payment.setUser(userOpt.get());
        paymentRepo.save(payment);
    }

    @CachePut("payments")
    @Transactional
    @Override
    public PaymentResponse updateSalary(PaymentRequest request) {
        if (paymentRepo.find(request.getEmail(),
                request.getPeriod()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        } else if (userRepo.find(request.getEmail()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }else {
            paymentRepo.updatePayment(request.getSalary(),
                    request.getEmail(),
                    request.getPeriod());
        }

        return PaymentResponse.builder().status("Updated successfully").build();
    }

    @CacheEvict("payments")
    @Override
    public List<PaymentResponse> getPayments(String username) {
        List<Payment> payments = paymentRepo.findAll(username);

        return payments.stream().map(payment -> PaymentResponse.builder()
                .name(payment.getUser().getName())
                .lastName(payment.getUser().getLastName())
                .period(payment.getPeriod())
                .salary(payment.getSalary())
                .build()).collect(Collectors.toList());
    }

    @Cacheable("payments")
    @Override
    public PaymentResponse getPayment(String username, String period) {
        if (DateValidator.isDateInvalid(period)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date");
        }

        Optional<Payment> paymentOpt = paymentRepo.find(username, period);

        if (paymentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        Payment payment = paymentOpt.get();

        return PaymentResponse.builder()
                .name(payment.getUser().getName())
                .lastName(payment.getUser().getLastName())
                .period(payment.getPeriod())
                .salary(payment.getSalary())
                .build();
    }

}
