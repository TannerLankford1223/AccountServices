package com.example.accountservices.application.controller;

import com.example.accountservices.domain.data.NewPassword;
import com.example.accountservices.domain.data.UserRequest;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.PaymentServicePort;
import com.example.accountservices.domain.ports.api.UserAccountServicePort;
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

    private final UserAccountServicePort userAccountService;
    private final PaymentServicePort paymentService;

    public AccountServiceController(UserAccountServicePort userAccountService, PaymentServicePort paymentService) {
        this.userAccountService = userAccountService;
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
