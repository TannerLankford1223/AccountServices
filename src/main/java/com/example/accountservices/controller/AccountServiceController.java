package com.example.accountservices.controller;

import com.example.accountservices.dto.NewPassword;
import com.example.accountservices.dto.UserRequest;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.service.PaymentService;
import com.example.accountservices.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

// Controller handles endpoints for registering, viewing, and updating employee accounts as well as allowing
// employees to view their previous payments
@Validated
@RestController
@RequestMapping("/api")
public class AccountServiceController {

    private final UserAccountService userAccountService;
    private final PaymentService paymentService;

    public AccountServiceController(UserAccountService employeeAccountService, PaymentService paymentService) {
        this.userAccountService = employeeAccountService;
        this.paymentService = paymentService;
    }

    @PostMapping("/auth/signup")
    public UserResponse signup(@Valid @RequestBody UserRequest request) {
        return userAccountService.register(request);
    }

    @PostMapping("/auth/changepass")
    public UserResponse changePassword(@Valid @RequestBody NewPassword newPassword) {
        return userAccountService.changePassword(newPassword.getPassword());
    }


    @GetMapping("/empl/payment")
    public ResponseEntity<?> getEmplPayment(@Valid @AuthenticationPrincipal UserDetails details,
                                            @RequestParam(value = "period", required = false) Optional<String> period) {
        if (period.isEmpty()) {
            return new ResponseEntity<>(paymentService.getPayments(details.getUsername()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(paymentService.getPayment(details.getUsername(), period.get()),
                    HttpStatus.OK);
        }
    }
}
