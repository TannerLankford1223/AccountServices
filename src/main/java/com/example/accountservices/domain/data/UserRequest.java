package com.example.accountservices.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @JsonProperty("lastname")
    private String lastName;

    @NotBlank
    @JsonProperty("email")
    @Pattern(regexp = "\\w+(@acme.com)$", message = "Must be an acme.com email")
    private String username;

    @NotBlank
    @Size(min = 12, message =  "Password must be a minimum of 12 chars!")
    private String password;

    public UserRequest(String name, String lastName, String username, String password) {
        this.name = name;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }
}
