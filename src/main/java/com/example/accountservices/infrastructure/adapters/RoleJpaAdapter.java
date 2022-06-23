package com.example.accountservices.infrastructure.adapters;

import com.example.accountservices.domain.ports.spi.RolePersistencePort;
import com.example.accountservices.domain.util.UserRole;
import com.example.accountservices.infrastructure.entity.EmployeeRole;
import com.example.accountservices.infrastructure.persistence.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleJpaAdapter implements RolePersistencePort {

    private final RoleRepository roleRepo;

    public RoleJpaAdapter(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public Optional<EmployeeRole> find(UserRole group) {
        return roleRepo.findByGroup(group);
    }
}
