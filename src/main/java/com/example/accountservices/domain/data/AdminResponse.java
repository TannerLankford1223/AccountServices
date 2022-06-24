package com.example.accountservices.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Builder
public class AdminResponse {

    @JsonProperty("username")
    private final String user;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("role")
    private String role;

    @JsonProperty("operation")
    private String operation;

    @JsonProperty("new_password")
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String password;
}
