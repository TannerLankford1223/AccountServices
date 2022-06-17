package com.example.accountservices.service;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.dto.AdminResponse;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.entity.EmployeeRole;
import com.example.accountservices.persistence.BreachedPasswords;
import com.example.accountservices.persistence.RoleRepository;
import com.example.accountservices.persistence.UserRepository;
import com.example.accountservices.util.AdminOperation;
import com.example.accountservices.util.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeAccountService implements UserAccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder encoder;

    public EmployeeAccountService(UserRepository userRepo,
                                  RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = new BCryptPasswordEncoder();
    }

    @Transactional
    @Override
    public UserResponse register(Employee user) {
        if (userRepo.findByUsernameIgnoreCase(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exists");
        } else if (BreachedPasswords.isBreached(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is a known breached password");
        }

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
        Optional<EmployeeRole> roleOpt = userRepo.findAll().size() == 0 ? roleRepo.findByGroup(UserRole.ADMINISTRATOR) :
                roleRepo.findByGroup(UserRole.USER);

        if (roleOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find role");
        }

        user.grantRole(roleOpt.get());
    }

    // Change the employee's role
    @Override
    public UserResponse changeRole(AdminRequest request) {
        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(request.getUser());
        Optional<EmployeeRole> roleOpt = roleRepo.findByGroup(request.getRole());
        Employee user;
        EmployeeRole role;
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        } else if (roleOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        } else if (request.getOperation() != AdminOperation.GRANT && request.getOperation() != AdminOperation.REMOVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operation");
        }
        user = userOpt.get();
        role = roleOpt.get();
        if (Objects.equals(request.getOperation(), AdminOperation.GRANT)) {
            grantRole(role, user);
        } else if (Objects.equals(request.getOperation(), AdminOperation.REMOVE)) {
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

        // Administrators cannot be Accountants or Auditors. There can be only one Auditor.
        if (user.hasRole(UserRole.ADMINISTRATOR) ||
                ((user.hasRole(UserRole.ACCOUNTANT) ||
                        user.hasRole(UserRole.AUDITOR)) &&
                        role.getGroup() == UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        } else if (role.getGroup() == UserRole.AUDITOR && role.getUsers().size() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There can be only one auditor");
        }

        user.grantRole(role);
        userRepo.save(user);
    }

    @Transactional
    public void removeRole(EmployeeRole role, Employee employee) {
        if (!employee.hasRole(role.getGroup())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
        } else if (!roleExists(role.getGroup())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        } else if (Objects.equals(role.getGroup(), UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        } else if (employee.getRoles().size() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
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

        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(details.getUsername());
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
        System.out.println("In get users");
        List<Employee> employeeList = userRepo.findAllByOrderByUserIdAsc();

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
        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(username);
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
                .status("Deleted successfully!")
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
        Optional<Employee> user = userRepo.findByUsernameIgnoreCase(username);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepo.updateFailedAttempts(0, user.get().getUsername());
    }

    @Override
    public AdminResponse changeAccess(AdminRequest request) {
        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(request.getUser());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Employee user = userOpt.get();
        if (request.getOperation().equals(AdminOperation.LOCK)) {
            lock(user);
        } else {
            unlock(user);
        }

        return AdminResponse.builder().status("User " +
                user.getUsername() + " " + request.getOperation().toString().toLowerCase() + "ed!").build();
    }

    @Transactional
    void lock(Employee user) {
        if (user.hasRole(UserRole.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
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
