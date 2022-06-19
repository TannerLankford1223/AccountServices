package com.example.accountservices;

import com.example.accountservices.dto.PaymentResponse;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.entity.Payment;
import com.example.accountservices.persistence.PaymentRepository;
import com.example.accountservices.persistence.UserRepository;
import com.example.accountservices.service.EmployeePaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class EmployeePaymentServiceTests {

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private UserRepository userRepo;


    private EmployeePaymentService employeePaymentService;

    private Payment payment;

    private Payment payment1;

    private Payment payment2;

    private Employee user;

    @BeforeEach
    void init() {
        this.employeePaymentService = new EmployeePaymentService(paymentRepo, userRepo);
        this.user = new Employee("firstname", "lastname", "email@acme.com",
                "thisIsAPassword");
        this.payment = new Payment(user.getUsername(), "05-2022", 75000);
        payment.setUser(user);
        this.payment1 = new Payment(user.getUsername(), "08-2022", 55000);
        payment1.setUser(user);
        this.payment2 = new Payment(user.getUsername(), "11-2022", 925000);
        payment2.setUser(user);
    }

    @Test
    public void postPayroll_ReturnsPaymentResponse() {
        List<Payment> payments = List.of(payment, payment1, payment2);
        when(paymentRepo.findPaymentByUsernameAndPeriod(any(String.class), any(String.class)))
                .thenReturn(Optional.empty());
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        PaymentResponse response = employeePaymentService.postPayroll(payments);

        verify(paymentRepo, times(3))
                .findPaymentByUsernameAndPeriod(any(String.class), any(String.class));

        assertEquals("Added successfully", response.getStatus());
    }

    @Test
    public void postPayroll_PaymentHasInvalidDate_ThrowsStatusException() {
        Payment invalidPayment = new Payment(user.getUsername(), "13-2025", 300000);
        List<Payment> payments = List.of(payment, payment1, payment2, invalidPayment);
        when(paymentRepo.findPaymentByUsernameAndPeriod(any(String.class), any(String.class)))
                .thenReturn(Optional.empty());
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> employeePaymentService.postPayroll(payments));
    }

    @Test
    public void postPayroll_PaymentAlreadyPosted_ThrowsStatusException() {
        List<Payment> payments = List.of(payment, payment1, payment2);
        when(paymentRepo.findPaymentByUsernameAndPeriod(payment.getUsername(), payment.getPeriod()))
                .thenReturn(Optional.of(payment));
        assertThrows(ResponseStatusException.class, () -> employeePaymentService.postPayroll(payments));
    }

    @Test
    public void insertValidPayment() {
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(paymentRepo.save(payment)).thenReturn(payment);

        employeePaymentService.insertPayment(payment);
        verify(paymentRepo, times(1)).save(payment);
    }

    @Test
    public void insertPayment_UserNonExistent_ThrowsStatusException() {
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> employeePaymentService.insertPayment(payment));
    }

    @Test
    public void updateSalary_ValidUserAndPayment_ReturnPaymentResponse() {
        when(paymentRepo.findPaymentByUsernameAndPeriod(payment.getUsername(), payment.getPeriod()))
                .thenReturn(Optional.of(payment));
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        doNothing().when(paymentRepo).updatePaymentByEmployeeAndPeriod(payment.getSalary(), payment.getUsername(),
                payment.getPeriod());

        PaymentResponse response = employeePaymentService.updateSalary(payment);

        assertEquals("Updated successfully", response.getStatus());
    }

    @Test
    public void updateSalary_NonExistentUser_ThrowsStatusException() {
        when(paymentRepo.findPaymentByUsernameAndPeriod(payment.getUsername(), payment.getPeriod()))
                .thenReturn(Optional.of(payment));
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> employeePaymentService.updateSalary(payment));
    }

    @Test
    public void updateSalary_InvalidPayment_ThrowsStatusException() {
        when(paymentRepo.findPaymentByUsernameAndPeriod(payment.getUsername(), payment.getPeriod()))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> employeePaymentService.updateSalary(payment));
    }

    @Test
    public void getPayments_returnsListOfPaymentResponses() {
        List<Payment> payments = List.of(payment, payment1, payment2);
        when(paymentRepo.findAllByUsernameOrderByPeriodDesc(user.getUsername()))
                .thenReturn(List.of(payment2, payment1, payment));

        List<PaymentResponse> response = employeePaymentService.getPayments(user.getUsername());

        assertEquals(3, response.size());
        assertEquals("November-2022", response.get(0).getPeriod());
        assertEquals("August-2022", response.get(1).getPeriod());
        assertEquals("May-2022", response.get(2).getPeriod());
    }

    @Test
    public void getPayment_ReturnsPaymentResponse() {
        when(paymentRepo.findPaymentByUsernameAndPeriod(payment2.getUsername(), payment2.getPeriod()))
                .thenReturn(Optional.of(payment2));

        PaymentResponse response = employeePaymentService.getPayment(payment2.getUsername(), payment2.getPeriod());

        assertEquals("firstname", response.getName());
        assertEquals("lastname", response.getLastName());
        assertEquals("9250 dollar(s) 0 cent(s)", response.getSalary());
        assertEquals("November-2022", response.getPeriod());
    }

    @Test
    public void getPayment_InvalidDate_ThrowsStatusException() {
        Payment invalidPayment = new Payment(user.getUsername(), "04-Twenty20", 1000000);
        invalidPayment.setUser(user);
        assertThrows(ResponseStatusException.class, () -> employeePaymentService
                .getPayment(invalidPayment.getUsername(), invalidPayment.getPeriod()));
    }

    @Test
    public void getPayment_NonExistentPayment_ThrowsStatusException() {
        when(paymentRepo.findPaymentByUsernameAndPeriod(payment1.getUsername(), payment1.getPeriod()))
                .thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> employeePaymentService
                .getPayment(payment1.getUsername(), payment1.getPeriod()));
    }
}