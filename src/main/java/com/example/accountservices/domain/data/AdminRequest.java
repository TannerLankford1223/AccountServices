package com.example.accountservices.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequest {

    @JsonProperty("username")
    @NotBlank
    private String username;

    @JsonProperty("role")
    @Pattern(regexp = "ADMINSTRATOR|ACCOUNTANT|AUDITOR|USER", message = "Invalid role")
    private String role;

    @JsonProperty("operation")
    @Pattern(regexp = "GRANT|REMOVE|LOCK|UNLOCK", message = "Invalid administer operation")
    private String operation;

    public AdminRequest(String username, String operation) {
        this.username = username;
        this.operation = operation;
    }

}
