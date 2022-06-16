package com.example.accountservices.config;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.service.LoggerService;
import com.example.accountservices.util.AdminOperation;
import com.example.accountservices.util.LogEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

// Event Logger waits for API requests and then logs the date that it occurred, the action taken,
// the subject (who performed the action), the object (who the action was performed on), and the request path.
@Aspect
@Component
public class EventLogger {

    private final LoggerService loggerService;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    public EventLogger(LoggerService loggerService) {
        this.loggerService = loggerService;
    }


    @AfterReturning(value = "com.example.accountservices.config.CommonJoinPointConfig.registerUser()", returning = "response")
    public void registerUser(Object response) {
        UserResponse userResponse = (UserResponse) response;
        loggerService.log(LogEvent.CREATE_USER, "Anonymous", userResponse.getUsername(),
                request.getRequestURI());
    }

    @After("com.example.accountservices.config.CommonJoinPointConfig.changePass()")
    public void changePass() {
        UserDetails details = getUserDetails();
        loggerService.log(LogEvent.CHANGE_PASSWORD, details.getUsername().toLowerCase(), details.getUsername(),
                request.getRequestURI());
    }

    @After("com.example.accountservices.config.CommonJoinPointConfig.accessDenied()")
    public void accessDenied() {
        UserDetails details = getUserDetails();
        loggerService.log(LogEvent.ACCESS_DENIED, details.getUsername(), request.getRequestURI(), request.getRequestURI());
    }

    @AfterReturning("com.example.accountservices.config.CommonJoinPointConfig.userRoles()")
    public void changeRoles(JoinPoint point) {
        UserDetails details = getUserDetails();

        AdminRequest adminRequest = (AdminRequest) point.getArgs()[0];

        if (Objects.equals(adminRequest.getOperation(), AdminOperation.GRANT)) {
            String object = "Grant role " + adminRequest.getRole() + " to " + adminRequest.getUser().toLowerCase();
            loggerService.log(LogEvent.GRANT_ROLE, details.getUsername(), object, request.getRequestURI());
        } else if (Objects.equals(adminRequest.getOperation(), AdminOperation.REMOVE)) {
            String object = "Remove role " + adminRequest.getRole() + " from " + adminRequest.getUser().toLowerCase();
            loggerService.log(LogEvent.REMOVE_ROLE, details.getUsername(), object, request.getRequestURI());
        }
    }

    @AfterReturning("com.example.accountservices.config.CommonJoinPointConfig.deleteUser()")
    public void deleteUser(JoinPoint point) {
        UserDetails details = getUserDetails();
        String object = point.getArgs()[0].toString();

        loggerService.log(LogEvent.DELETE_USER, details.getUsername(), object, request.getRequestURI());
    }

    @AfterReturning(value = "com.example.accountservices.config.CommonJoinPointConfig.changeAccess()")
    public void changeAccess(JoinPoint point) {
        UserDetails details = getUserDetails();
        AdminRequest changeAccessRequest = (AdminRequest) point.getArgs()[0];
        String user = changeAccessRequest.getUser().toLowerCase();
        String subject = details.getUsername();
        String path = request.getRequestURI();

        if (changeAccessRequest.getOperation().equals(AdminOperation.LOCK)) {
            String object = "Lock user " + user;
            loggerService.log(LogEvent.LOCK_USER, subject, object, path);
        } else {
            String object = "Unlock user " + user;
            loggerService.log(LogEvent.UNLOCK_USER, subject, object, path);
        }
    }


    private UserDetails getUserDetails() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}