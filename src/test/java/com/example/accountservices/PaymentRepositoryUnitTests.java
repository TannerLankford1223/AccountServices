package com.example.accountservices;

import com.example.accountservices.infrastructure.entity.Payment;
import com.example.accountservices.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(scripts = {"/insertUsers-h2.sql", "/insertPayments-h2.sql"})
public class PaymentRepositoryUnitTests {

    @Autowired
    PaymentRepository paymentRepo;

    @Test
    public void findAllPaymentsForUser_UserExists_ReturnsListOfPaymentsForUser() {
        List<Payment> payments = paymentRepo.findAllByUsernameOrderByPeriodDesc("jane@acme.com");
        assertEquals(3, payments.size());
    }

    @Test
    public void findAllPaymentsForUser_UserNonExistent_ReturnsEmptyList() {
        List<Payment> payments = paymentRepo.findAllByUsernameOrderByPeriodDesc("fakeUser@acme.com");
        assertEquals(0, payments.size());
    }

    @Test
    public void findSpecificUserPayment_UserExist_ReturnsOptionalOfPayment() {
        Optional<Payment> payment = paymentRepo.findPaymentByUsernameAndPeriod("john@acme.com", "05-2021");
        assertTrue(payment.isPresent());
        assertEquals(5000000, payment.get().getSalary());
    }

    @Test
    public void findSpecificUserPayment_UserNonExistent_ReturnsEmptyOptional() {
        Optional<Payment> payment = paymentRepo.findPaymentByUsernameAndPeriod("fakeUser@acme.com", "05-2021");
        assertTrue(payment.isEmpty());
    }

    @Test
    public void findSpecificUserPayment_PaymentNonExistent_ReturnsEmptyOptional() {
        Optional<Payment> payment = paymentRepo.findPaymentByUsernameAndPeriod("jane@acme.com", "03-2005");
        assertTrue(payment.isEmpty());
    }

    @Test
    public void updateUserPayment_UserExists_ReturnsTrue() {
        paymentRepo.updatePaymentByEmployeeAndPeriod(300000, "paul@acme.com", "06-2021");
        Optional<Payment> updatedPayment = paymentRepo.findPaymentByUsernameAndPeriod("paul@acme.com", "06-2021");
        assertTrue(updatedPayment.isPresent());
        assertEquals(300000, updatedPayment.get().getSalary());
    }
}
