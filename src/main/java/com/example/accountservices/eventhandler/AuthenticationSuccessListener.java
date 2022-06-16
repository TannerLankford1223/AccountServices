package com.example.accountservices.eventhandler;

import com.example.accountservices.service.EmployeeAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final EmployeeAccountService employeeAccountService;

    @Autowired
    public AuthenticationSuccessListener(EmployeeAccountService employeeAccountService) {
        this.employeeAccountService = employeeAccountService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        employeeAccountService.resetFailedAttempts(event.getAuthentication().getName());
    }
}
