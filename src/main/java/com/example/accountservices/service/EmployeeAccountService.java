package com.example.accountservices.service;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.dto.AdminResponse;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.entity.EmployeeRole;
import com.example.accountservices.persistence.RoleRepository;
import com.example.accountservices.persistence.UserRepository;
import com.example.accountservices.util.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class EmployeeAccountService implements UserAccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder encoder;

    public EmployeeAccountService(UserRepository userRepo,
                                  RoleRepository roleRepo,
                                  BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Transactional
    @Override
    public UserResponse register(Employee user) {
        return null;
    }

    // Initializes the user's original role to Administrator, if no other user's exist, else it sets the role
    // to User
    private void initRole(Employee user) {

    }

    // Change the employee's role
    @Override
    public UserResponse changeRole(AdminRequest request) {
        return null;
    }

    @Transactional
    public void grantRole(EmployeeRole role, Employee user) {

    }

    @Transactional
    public void removeRole(EmployeeRole role, Employee employee) {

    }

    private boolean roleExists(UserRole role) {
        return false;
    }

    @Transactional
    @Override
    public UserResponse changePassword(String password) {
        return null;
    }

    @Override
    public List<UserResponse> getUsers() {
        return null;
    }

    @Transactional
    @Override
    public AdminResponse deleteUser(String username) {
        return null;
    }

    @Transactional
    @Override
    public void increaseFailedAttempts(Employee user) {
    }

    @Transactional
    @Override
    public void resetFailedAttempts(String username) {
    }

    @Override
    public AdminResponse changeAccess(AdminRequest request) {
        return null;
    }

    @Transactional
    void lock(Employee user) {
    }


    @Transactional
    void unlock(Employee user) {

    }
}
