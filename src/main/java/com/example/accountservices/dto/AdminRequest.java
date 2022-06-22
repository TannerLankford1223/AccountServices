package com.example.accountservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequest {

    @NotBlank
    private String username;

    @Pattern(regexp = "ADMINSTRATOR|ACCOUNTANT|AUDITOR|USER", message = "Invalid role")
    private String role;

    @Pattern(regexp = "GRANT|REMOVE|LOCK|UNLOCK", message = "Invalid administer operation")
    private String operation;

    public AdminRequest(String username, String operation) {
        this.username = username;
        this.operation = operation;
    }

}
