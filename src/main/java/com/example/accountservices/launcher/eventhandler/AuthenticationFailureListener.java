package com.example.accountservices.launcher.eventhandler;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.infrastructure.entity.Employee;
import com.example.accountservices.infrastructure.persistence.UserRepository;
import com.example.accountservices.domain.service.EmployeeAccountService;
import com.example.accountservices.domain.ports.api.EventLogServicePort;
import com.example.accountservices.domain.util.LogEvent;
import com.example.accountservices.domain.util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final EventLogServicePort eventLogServicePort;
    private final EmployeeAccountService employeeAccountService;
    private final UserRepository userRepo;

    private final HttpServletRequest request;

    @Autowired
    public AuthenticationFailureListener(EventLogServicePort eventLogServicePort, EmployeeAccountService employeeAccountService,
                                         UserRepository userRepo, HttpServletRequest request) {
        this.eventLogServicePort = eventLogServicePort;
        this.employeeAccountService = employeeAccountService;
        this.userRepo = userRepo;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = Optional.of(event.getAuthentication().getName()).orElse("Anonymous");
        String path = request.getRequestURI();

        eventLogServicePort.log(LogEvent.LOGIN_FAILED, username, path, path);

        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(username);
        Employee user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (!userOpt.get().hasRole(UserRole.ADMINISTRATOR)) {
            int loginAttempt = user.getFailedAttempt() + 1;
            if (loginAttempt >= EmployeeAccountService.MAX_FAILED_ATTEMPTS) {
                AdminRequest AdminRequest = new AdminRequest(username, "LOCK");
                employeeAccountService.changeAccess(AdminRequest);
                eventLogServicePort.log(LogEvent.BRUTE_FORCE, username, path, path);
                eventLogServicePort.log(LogEvent.LOCK_USER, username, "Lock user " + username, "/api/admin/user/access");
            }
            employeeAccountService.increaseFailedAttempts(user);
        }

    }
}
