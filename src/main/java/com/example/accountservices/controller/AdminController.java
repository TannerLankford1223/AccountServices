package com.example.accountservices.controller;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.dto.AdminResponse;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.service.UserAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller handles endpoints pertaining to the Administrators' services, i.e. deleting users, changing Roles
// and granting accesses
@RestController
@RequestMapping("api/admin/user")
public class AdminController {

    private final UserAccountService userAccountService;

    public AdminController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PutMapping("/role")
    public UserResponse changeRoles(@RequestBody AdminRequest request) {
        return null;
    }

    @DeleteMapping("/{email}")
    public AdminResponse deleteUser(@PathVariable String email) {
        return null;
    }

    @GetMapping("/")
    public List<UserResponse> getUsersInfo() {
        return null;
    }

    @PutMapping("/access")
    public AdminResponse changeAccess(@RequestBody AdminRequest request) {
        return null;
    }

}
