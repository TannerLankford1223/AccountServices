package com.example.accountservices.domain.ports.api;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.AdminResponse;
import com.example.accountservices.domain.data.UserRequest;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.infrastructure.entity.Employee;

import java.util.List;

public interface UserAccountServicePort {
    UserResponse register(UserRequest request);

    UserResponse changePassword(String password);

    UserResponse changeRole(AdminRequest request);

    List<UserResponse> getUsers();

    AdminResponse deleteUser(String username);

    void increaseFailedAttempts(Employee user);

    void resetFailedAttempts(String username);

    AdminResponse changeAccess(AdminRequest request);
}
