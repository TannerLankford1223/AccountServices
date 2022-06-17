package com.example.accountservices.dto;

import com.example.accountservices.util.AdminOperation;
import com.example.accountservices.util.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class AdminRequest {

    @NotBlank
    private String user;

    private UserRole role;

    private AdminOperation operation;

    public AdminRequest(String user, AdminOperation operation) {
        this.user = user;
        this.operation = operation;
    }

}
