package com.example.accountservices.infrastructure.adapters;

import com.example.accountservices.domain.ports.spi.UserPersistencePort;
import com.example.accountservices.infrastructure.entity.Employee;
import com.example.accountservices.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserJpaAdapter implements UserPersistencePort {

    private final UserRepository userRepo;

    public UserJpaAdapter(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Employee save(Employee user) {
        return userRepo.save(user);
    }

    @Override
    public Optional<Employee> find(String username) {
        return userRepo.findByUsernameIgnoreCase(username);
    }

    @Override
    public List<Employee> findAllOrderDesc() {
        return userRepo.findAllByOrderByUserIdAsc();
    }

    @Override
    public List<Employee> findAll() {
        return userRepo.findAll();
    }

    @Override
    public void delete(Employee user) {
        userRepo.delete(user);
    }

    @Override
    public void updateFailedAttempts(int failAttempts, String email) {
        userRepo.updateFailedAttempts(failAttempts, email);
    }
}
