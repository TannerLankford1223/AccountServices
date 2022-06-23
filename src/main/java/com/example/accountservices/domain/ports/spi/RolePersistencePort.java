package com.example.accountservices.domain.ports.spi;

import com.example.accountservices.domain.util.UserRole;
import com.example.accountservices.infrastructure.entity.EmployeeRole;

import java.util.Optional;

public interface RolePersistencePort {

    Optional<EmployeeRole> find(UserRole group);
}
