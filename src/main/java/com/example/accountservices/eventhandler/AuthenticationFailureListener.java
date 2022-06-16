package com.example.accountservices.eventhandler;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.persistence.UserRepository;
import com.example.accountservices.service.EmployeeAccountService;
import com.example.accountservices.service.LoggerService;
import com.example.accountservices.util.AdminOperation;
import com.example.accountservices.util.LogEvent;
import com.example.accountservices.util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final LoggerService loggerService;
    private final EmployeeAccountService employeeAccountService;
    private final UserRepository userRepo;

    private final HttpServletRequest request;

    @Autowired
    public AuthenticationFailureListener(LoggerService loggerService, EmployeeAccountService employeeAccountService,
                                         UserRepository userRepo, HttpServletRequest request) {
        this.loggerService = loggerService;
        this.employeeAccountService = employeeAccountService;
        this.userRepo = userRepo;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = Optional.of(event.getAuthentication().getName()).orElse("Anonymous");
        String path = request.getRequestURI();

        loggerService.log(LogEvent.LOGIN_FAILED, username, path, path);

        Employee user = userRepo.findByUsernameIgnoreCase(username);
        if (user != null && !user.hasRole(UserRole.ADMINISTRATOR)) {
            int loginAttempt = user.getFailedAttempt() + 1;
            if (loginAttempt >= EmployeeAccountService.MAX_FAILED_ATTEMPTS) {
                AdminRequest AdminRequest = new AdminRequest(username, AdminOperation.LOCK);
                employeeAccountService.changeAccess(AdminRequest);
                loggerService.log(LogEvent.BRUTE_FORCE, username, path, path);
                loggerService.log(LogEvent.LOCK_USER, username, "Lock user " + username, "/api/admin/user/access");
            }
            employeeAccountService.increaseFailedAttempts(user);
        }

    }
}
