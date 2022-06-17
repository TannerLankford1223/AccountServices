package com.example.accountservices.service;

import com.example.accountservices.dto.PaymentResponse;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.entity.Payment;
import com.example.accountservices.persistence.PaymentRepository;
import com.example.accountservices.persistence.UserRepository;
import com.example.accountservices.util.DateValidator;
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
        for (Payment payment : payments) {
            if (DateValidator.isDateInvalid(payment.getPeriod())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date");
            } else if (paymentRepo.findPaymentByUsernameAndPeriod(payment.getUsername(),
                    payment.getPeriod()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment already exists");
            } else {
                insertPayment(payment);
            }
        }

        return PaymentResponse.builder().status("Added successfully").build();
    }

    @Transactional
    @Override
    public void insertPayment(Payment payment) {
        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(payment.getUsername());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        payment.setUser(userOpt.get());
        paymentRepo.save(payment);
    }

    @CachePut("payments")
    @Transactional
    @Override
    public PaymentResponse updateSalary(Payment payment) {
        if (paymentRepo.findPaymentByUsernameAndPeriod(payment.getUsername(),
                payment.getPeriod()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        } else {
            paymentRepo.updatePaymentByEmployeeAndPeriod(payment.getSalary(),
                    payment.getUsername(),
                    payment.getPeriod());
        }

        return PaymentResponse.builder().status("Updated successfully").build();
    }

    @CacheEvict("payments")
    @Override
    public List<PaymentResponse> getPayments(String username) {
        List<Payment> payments = paymentRepo.findAllByUsernameOrderByPeriodDesc(username);

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

        Optional<Payment> paymentOpt = paymentRepo.findPaymentByUsernameAndPeriod(username, period);

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
