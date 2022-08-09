package com.example.accountservices;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(info = @Info(title = "Account Services API", version = "1.0",
        description = "Employee Account and Payroll Services"))
public class AccountServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServicesApplication.class, args);
    }

}
