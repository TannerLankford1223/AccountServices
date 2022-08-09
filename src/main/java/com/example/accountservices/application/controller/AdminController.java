package com.example.accountservices.application.controller;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.AdminResponse;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.UserAccountServicePort;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller handles endpoints pertaining to the Administrators' services, i.e. deleting users, changing Roles
// and granting accesses
@RestController
@RequestMapping("api/admin/user")
public class AdminController {

    private final UserAccountServicePort userAccountService;

    public AdminController(UserAccountServicePort userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PutMapping("/role")
    @Operation(summary = "Allows an admin to grant or revoke an employee role")
    public UserResponse changeRoles(@RequestBody AdminRequest request) {
        return userAccountService.changeRole(request);
    }

    @DeleteMapping("/{email}")
    @Operation(summary = "Allows an admin to delete an employee")
    public AdminResponse deleteUser(@PathVariable String email) {
        return userAccountService.deleteUser(email);
    }

    @GetMapping("/")
    @Operation(summary = "Returns user information for all employees")
    public List<UserResponse> getUsersInfo() {
        return userAccountService.getUsers();
    }

    @PutMapping("/access")
    @Operation(summary = "Allows an admin to lock or unlock a user's account")
    public AdminResponse changeAccess(@RequestBody AdminRequest request) {
        return userAccountService.changeAccess(request);
    }

}
