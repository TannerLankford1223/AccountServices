package com.example.accountservices.domain.service;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.AdminResponse;
import com.example.accountservices.domain.data.UserRequest;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.UserAccountServicePort;
import com.example.accountservices.domain.ports.spi.RolePersistencePort;
import com.example.accountservices.domain.ports.spi.UserPersistencePort;
import com.example.accountservices.domain.util.AdminOperation;
import com.example.accountservices.domain.util.UserRole;
import com.example.accountservices.infrastructure.entity.Employee;
import com.example.accountservices.infrastructure.entity.EmployeeRole;
import com.example.accountservices.infrastructure.persistence.BreachedPasswords;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeAccountService implements UserAccountServicePort {
    public static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserPersistencePort userRepo;
    private final RolePersistencePort roleRepo;
    private final PasswordEncoder encoder;

    public EmployeeAccountService(UserPersistencePort userRepo,
                                  RolePersistencePort roleRepo,
                                  PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Transactional
    @Override
    public UserResponse register(UserRequest request) {
        if (userRepo.find(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exists");
        } else if (BreachedPasswords.isBreached(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is a known breached password");
        }

        Employee user = new Employee(request.getName(), request.getLastName(),
                request.getUsername(), request.getPassword());
        user.setPassword(encoder.encode(user.getPassword()));
        initRole(user);

        Employee returnedUser = userRepo.save(user);

        return UserResponse.builder()
                .userId(returnedUser.getUserId())
                .name(returnedUser.getName())
                .lastName(returnedUser.getLastName())
                .username(returnedUser.getUsername())
                .roles(returnedUser.getRoles())
                .build();
    }

    // Initializes the user's original role to Administrator, if no other user's exist, else it sets the role
    // to User
    private void initRole(Employee user) {
        Optional<EmployeeRole> roleOpt = userRepo.findAll().size() == 0 ? roleRepo.find(UserRole.ADMINISTRATOR) :
                roleRepo.find(UserRole.USER);

        if (roleOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find role");
        }

        user.grantRole(roleOpt.get());
    }

    // Change the employee's role
    @Override
    public UserResponse changeRole(AdminRequest request) {
        Optional<Employee> userOpt = userRepo.find(request.getUsername());
        Optional<EmployeeRole> roleOpt = roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()));
        AdminOperation operation = AdminOperation.valueOf(request.getOperation().toUpperCase());
        Employee user;
        EmployeeRole role;
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        } else if (roleOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        } else if (operation != AdminOperation.GRANT && operation != AdminOperation.REMOVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operation");
        }
        user = userOpt.get();
        role = roleOpt.get();

        if (Objects.equals(operation, AdminOperation.GRANT)) {
            grantRole(role, user);
        } else {
            removeRole(role, user);
        }

        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }

    @Transactional
    public void grantRole(EmployeeRole role, Employee user) {

        // Administrators cannot be Accountants or Auditors.
        if (user.hasRole(UserRole.ADMINISTRATOR) ||
                ((user.hasRole(UserRole.ACCOUNTANT) ||
                        user.hasRole(UserRole.AUDITOR)) &&
                        role.getGroup() == UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        }

        user.grantRole(role);
        userRepo.save(user);
    }

    @Transactional
    public void removeRole(EmployeeRole role, Employee employee) {
        if (!employee.hasRole(role.getGroup())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have the role provided");
        } else if (!roleExists(role.getGroup())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        } else if (Objects.equals(role.getGroup(), UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role");
        } else if (employee.getRoles().size() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role");
        } else {
            employee.removeRole(role);
            userRepo.save(employee);
        }
    }

    private boolean roleExists(UserRole role) {
        for (UserRole empRole : UserRole.values()) {
            if (role == empRole) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    @Override
    public UserResponse changePassword(String password) {
        UserDetails details = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Employee> userOpt = userRepo.find(details.getUsername());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Employee user = userOpt.get();
        if (BreachedPasswords.isBreached(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        } else if (encoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be different");
        }

        user.setPassword(encoder.encode(password));
        userRepo.save(user);

        return UserResponse.builder()
                .username(user.getUsername())
                .status("The password has been updated successfully")
                .build();

    }

    @Override
    public List<UserResponse> getUsers() {
        List<Employee> employeeList = userRepo.findAllOrderDesc();

        return employeeList.stream().map(employee -> UserResponse.builder()
                .userId(employee.getUserId())
                .name(employee.getName())
                .lastName(employee.getLastName())
                .username(employee.getUsername())
                .roles(employee.getRoles())
                .build()).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AdminResponse deleteUser(String username) {
        Optional<Employee> userOpt = userRepo.find(username);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Employee user = userOpt.get();
        if (user.hasRole(UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR");
        } else {
            userRepo.delete(user);
        }

        return AdminResponse.builder()
                .user(user.getUsername())
                .status("Deleted successfully")
                .build();
    }

    @Transactional
    @Override
    public void increaseFailedAttempts(Employee user) {
        int failedAttempts = user.getFailedAttempt() + 1;
        userRepo.updateFailedAttempts(failedAttempts, user.getUsername());
    }

    @Transactional
    @Override
    public void resetFailedAttempts(String username) {
        userRepo.updateFailedAttempts(0, username);
    }

    @Override
    public AdminResponse changeAccess(AdminRequest request) {
        Optional<Employee> userOpt = userRepo.find(request.getUsername());
        AdminOperation operation = AdminOperation.valueOf(request.getOperation().toUpperCase());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Employee user = userOpt.get();
        if (operation.equals(AdminOperation.LOCK)) {
            lock(user);
        } else if (operation.equals(AdminOperation.UNLOCK)){
            unlock(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operation");
        }

        return AdminResponse.builder().status("User " +
                user.getUsername() + " " + request.getOperation().toLowerCase() + "ed").build();
    }

    @Transactional
    void lock(Employee user) {
        if (user.hasRole(UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR");
        }
        user.setAccountNonBlocked(false);
        user.setLockTime(new Date());

        userRepo.save(user);
    }

    @Transactional
    void unlock(Employee user) {
        user.setAccountNonBlocked(true);
        user.setLockTime(null);
        resetFailedAttempts(user.getUsername());

        userRepo.save(user);
    }
}
