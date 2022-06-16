package com.example.accountservices.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
public class NewPassword {

    @JsonProperty("new_password")
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String password;
}
