package com.example.accountservices.domain.ports.spi;

import com.example.accountservices.infrastructure.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {

    Employee save(Employee user);

    Optional<Employee> find(String username);

    List<Employee> findAllOrderDesc();

    List<Employee> findAll();

    void delete(Employee user);

    void updateFailedAttempts(int failAttempts, String email);
}
