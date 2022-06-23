package com.example.accountservices.launcher.config;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.EventLogServicePort;
import com.example.accountservices.domain.util.AdminOperation;
import com.example.accountservices.domain.util.LogEvent;
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

    private final EventLogServicePort eventLogServicePort;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    public EventLogger(EventLogServicePort eventLogServicePort) {
        this.eventLogServicePort = eventLogServicePort;
    }


    @AfterReturning(value = "com.example.accountservices.launcher.config.CommonJoinPointConfig.registerUser()", returning = "response")
    public void registerUser(Object response) {
        UserResponse userResponse = (UserResponse) response;
        eventLogServicePort.log(LogEvent.CREATE_USER, "Anonymous", userResponse.getUsername(),
                request.getRequestURI());
    }

    @After("com.example.accountservices.launcher.config.CommonJoinPointConfig.changePass()")
    public void changePass() {
        UserDetails details = getUserDetails();
        eventLogServicePort.log(LogEvent.CHANGE_PASSWORD, details.getUsername().toLowerCase(), details.getUsername(),
                request.getRequestURI());
    }

    @After("com.example.accountservices.launcher.config.CommonJoinPointConfig.accessDenied()")
    public void accessDenied() {
        UserDetails details = getUserDetails();
        eventLogServicePort.log(LogEvent.ACCESS_DENIED, details.getUsername(), request.getRequestURI(), request.getRequestURI());
    }

    @AfterReturning("com.example.accountservices.launcher.config.CommonJoinPointConfig.userRoles()")
    public void changeRoles(JoinPoint point) {
        UserDetails details = getUserDetails();
        AdminRequest adminRequest = (AdminRequest) point.getArgs()[0];
        AdminOperation operation = AdminOperation.valueOf(adminRequest.getOperation().toUpperCase());
        if (Objects.equals(operation, AdminOperation.GRANT)) {
            String object = "Grant role " + adminRequest.getRole() + " to " + adminRequest.getUsername().toLowerCase();
            eventLogServicePort.log(LogEvent.GRANT_ROLE, details.getUsername(), object, request.getRequestURI());
        } else if (Objects.equals(operation, AdminOperation.REMOVE)) {
            String object = "Remove role " + adminRequest.getRole() + " from " + adminRequest.getUsername().toLowerCase();
            eventLogServicePort.log(LogEvent.REMOVE_ROLE, details.getUsername(), object, request.getRequestURI());
        }
    }

    @AfterReturning("com.example.accountservices.launcher.config.CommonJoinPointConfig.deleteUser()")
    public void deleteUser(JoinPoint point) {
        UserDetails details = getUserDetails();
        String object = point.getArgs()[0].toString();

        eventLogServicePort.log(LogEvent.DELETE_USER, details.getUsername(), object, request.getRequestURI());
    }

    @AfterReturning(value = "com.example.accountservices.launcher.config.CommonJoinPointConfig.changeAccess()")
    public void changeAccess(JoinPoint point) {
        UserDetails details = getUserDetails();
        AdminRequest changeAccessRequest = (AdminRequest) point.getArgs()[0];
        AdminOperation operation = AdminOperation.valueOf(changeAccessRequest
                .getOperation().toUpperCase());
        String user = changeAccessRequest.getUsername().toLowerCase();
        String subject = details.getUsername();
        String path = request.getRequestURI();

        if (operation.equals(AdminOperation.LOCK)) {
            String object = "Lock user " + user;
            eventLogServicePort.log(LogEvent.LOCK_USER, subject, object, path);
        } else {
            String object = "Unlock user " + user;
            eventLogServicePort.log(LogEvent.UNLOCK_USER, subject, object, path);
        }
    }


    private UserDetails getUserDetails() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
