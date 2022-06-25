package com.example.accountservices.infrastructure;

import com.example.accountservices.domain.util.UserRole;
import com.example.accountservices.infrastructure.entity.EmployeeRole;
import com.example.accountservices.infrastructure.persistence.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrincipleGroupLoader implements CommandLineRunner {

    private final RoleRepository roleRepo;

    public PrincipleGroupLoader(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        EmployeeRole adminRole = new EmployeeRole("ROLE_ADMINISTRATOR", UserRole.ADMINISTRATOR);
        EmployeeRole accountantRole = new EmployeeRole("ROLE_ACCOUNTANT", UserRole.ACCOUNTANT);
        EmployeeRole auditorRole = new EmployeeRole("ROLE_AUDITOR", UserRole.AUDITOR);
        EmployeeRole userRole = new EmployeeRole("ROLE_USER", UserRole.USER);

        roleRepo.saveAll(List.of(adminRole, accountantRole, auditorRole, userRole));
    }
}
