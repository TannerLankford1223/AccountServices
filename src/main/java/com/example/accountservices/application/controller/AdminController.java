package com.example.accountservices.application.controller;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.AdminResponse;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.UserAccountServicePort;
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
    public UserResponse changeRoles(@RequestBody AdminRequest request) {
        return userAccountService.changeRole(request);
    }

    @DeleteMapping("/{email}")
    public AdminResponse deleteUser(@PathVariable String email) {
        return userAccountService.deleteUser(email);
    }

    @GetMapping("/")
    public List<UserResponse> getUsersInfo() {
        return userAccountService.getUsers();
    }

    @PutMapping("/access")
    public AdminResponse changeAccess(@RequestBody AdminRequest request) {
        return userAccountService.changeAccess(request);
    }

}
