package com.example.accountservices.persistence;

import com.example.accountservices.entity.EmployeeRole;
import com.example.accountservices.util.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<EmployeeRole, Long> {

    Optional<EmployeeRole> findByGroup(UserRole group);
}
