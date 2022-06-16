package com.example.accountservices.service;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.dto.AdminResponse;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.entity.Employee;

import java.util.List;

public interface UserAccountService {
    UserResponse register(Employee user);

    UserResponse changePassword(String password);

    UserResponse changeRole(AdminRequest request);

    List<UserResponse> getUsers();

    AdminResponse deleteUser(String username);

    void increaseFailedAttempts(Employee user);

    void resetFailedAttempts(String username);

    AdminResponse changeAccess(AdminRequest request);
}
