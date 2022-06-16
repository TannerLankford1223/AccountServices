package com.example.accountservices.persistence;

import com.example.accountservices.entity.EmployeeRole;
import com.example.accountservices.util.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<EmployeeRole, Long> {
    EmployeeRole findByGroup(UserRole group);
}
