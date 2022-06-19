package com.example.accountservices;

import com.example.accountservices.dto.AdminRequest;
import com.example.accountservices.dto.UserResponse;
import com.example.accountservices.entity.CustomUserDetails;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.entity.EmployeeRole;
import com.example.accountservices.persistence.RoleRepository;
import com.example.accountservices.persistence.UserRepository;
import com.example.accountservices.service.EmployeeAccountService;
import com.example.accountservices.service.UserAccountService;
import com.example.accountservices.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class EmployeeAccountServiceTests {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    private PasswordEncoder encoder;

    private final Authentication authentication = Mockito.mock(Authentication.class);
    private final SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    private UserAccountService userAccountService;

    private Employee user;
    private EmployeeRole adminRole;
    private EmployeeRole userRole;
    private EmployeeRole accountantRole;
    private EmployeeRole auditorRole;

    @BeforeEach
    void init() {
        this.userAccountService = new EmployeeAccountService(userRepo, roleRepo);
        this.user = new Employee("firstname", "lastname", "email@acme.com", "thisIsAPassword");
        this.adminRole = new EmployeeRole();
        this.userRole = new EmployeeRole();
        this.accountantRole = new EmployeeRole();
        this.auditorRole = new EmployeeRole();
        this.encoder = new BCryptPasswordEncoder();

        adminRole.setId(1L);
        adminRole.setAccountRole("ROLE_ADMINISTRATOR");
        adminRole.setGroup(UserRole.ADMINISTRATOR);

        userRole.setId(2L);
        userRole.setAccountRole("ROLE_USER");
        userRole.setGroup(UserRole.USER);

        accountantRole.setId(3L);
        accountantRole.setAccountRole("ROLE_ACCOUNTANT");
        accountantRole.setGroup(UserRole.ACCOUNTANT);

        auditorRole.setId(4L);
        auditorRole.setAccountRole("ROLE_AUDITOR");
        auditorRole.setGroup(UserRole.AUDITOR);

    }

    @Test
    public void registersNewUser_AndGivesRoleAdmin_returnsUserResponse() {
        user.grantRole(adminRole);
        when(userRepo.save(user)).thenReturn(user);
        when(roleRepo.findByGroup(UserRole.ADMINISTRATOR)).thenReturn(Optional.of(adminRole));

        UserResponse response = userAccountService.register(user);

        assertEquals("firstname", response.getName());
        assertEquals("lastname", response.getLastName());
        assertEquals("email@acme.com", response.getUsername());
        assertEquals(Set.of("ROLE_ADMINISTRATOR"), response.getRoles());
    }

    @Test
    public void registersNewUser_AndGivesRoleUser_returnsUserResponse() {
        Employee user1 = new Employee("John", "Doe", "john@acme.com"
                , "testPassword12345");
        user1.grantRole(userRole);
        when(userRepo.save(user1)).thenReturn(user1);
        when(userRepo.findAll()).thenReturn(List.of(user));
        when(roleRepo.findByGroup(UserRole.USER)).thenReturn(Optional.of(userRole));

        UserResponse response = userAccountService.register(user1);

        assertEquals("john@acme.com", response.getUsername());
        assertEquals("John", response.getName());
        assertEquals("Doe", response.getLastName());
        assertEquals(Set.of("ROLE_USER"), response.getRoles());
    }

    @Test
    public void registerExistingUser_throwsError() {
        when(userRepo.findByUsernameIgnoreCase("email@acme.com")).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userAccountService.register(user));
    }

    @Test
    public void registerUserWithBreachedPass_throwsStatusException() {
        Employee badPasswordUser = new Employee("Jane", "Doe", "jane@acme.com", "PasswordForJuly");

        assertThrows(ResponseStatusException.class, () -> userAccountService.register(badPasswordUser));
    }

    @Test
    public void changePassword_ToValidPassword_returnsUserResponse() {
        String newPassword = "testPassword1234";
        setUserDetails(List.of(adminRole));
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        UserResponse response = userAccountService.changePassword(newPassword);
        assertEquals("The password has been updated successfully", response.getStatus());
    }

    @Test
    public void changePassword_ToBreachedPass_throwsStatusException() {
        String newBreachedPassword = "PasswordForNovember";
        setUserDetails(List.of(adminRole));
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changePassword(newBreachedPassword));
    }

    @Test
    public void changePassword_ToSamePass_throwsStatusException() {
        String samePassword = "thisIsAPassword";
        setUserDetails(List.of(userRole));
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changePassword(samePassword));
    }

    @Test
    public void changePassword_UnabletoFindUser_throwsStatusException() {
        String newPassword = "testingNewPasswords";
        setUserDetails(List.of(userRole));
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userAccountService.changePassword(newPassword));
    }

    @Test
    public void grantRoleToUser_returnsUserResponse() {
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "GRANT");
        when(userRepo.findByUsernameIgnoreCase(request.getUser())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(adminRole));

        UserResponse response = userAccountService.changeRole(request);
        assertEquals("firstname", response.getName());
        assertEquals("lastname", response.getLastName());
        assertEquals("email@acme.com", response.getUsername());
        assertEquals(Set.of("ROLE_ADMINISTRATOR"), response.getRoles());
    }

    @Test
    public void removeRoleFromUser_returnsUserResponse() {
        user.grantRole(userRole);
        user.grantRole(accountantRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ACCOUNTANT", "REMOVE");
        when(userRepo.findByUsernameIgnoreCase(request.getUser())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(accountantRole));

        UserResponse response = userAccountService.changeRole(request);
        assertEquals("firstname", response.getName());
        assertEquals("lastname", response.getLastName());
        assertEquals("email@acme.com", response.getUsername());
        assertEquals(Set.of("ROLE_USER"), response.getRoles());
    }

    @Test
    public void changeRole_UserInvalid_throwsStatusException() {
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "GRANT");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.empty());
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(adminRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void changeRoleToInvalidOperation_throwsStatusException() {
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "LOCK");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(adminRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void grantBusinessRoleToAdmin_throwsStatusException() {
        user.grantRole(adminRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ACCOUNTANT", "GRANT");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(accountantRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void removeRole_UserDoesNotHaveRole_throwsStatusException() {
        user.grantRole(adminRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "USER", "REMOVE");
                when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(userRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }


    @Test
    public void removeAdminRole_throwsStatusException() {
        user.grantRole(adminRole);
        user.grantRole(userRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "REMOVE");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase())))
                .thenReturn(Optional.of(adminRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void removeLastRoleFromUser_throwsStatusException() {
        user.grantRole(accountantRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ACCOUNTANT", "REMOVE");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.findByGroup(UserRole.valueOf(request.getRole().toUpperCase())))
                .thenReturn(Optional.of(accountantRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void getListOfUsers_returnsUserResponseList() {
        List<Employee> users = setListOfUsers();
        when(userRepo.findAllByOrderByUserIdAsc()).thenReturn(users);

        List<UserResponse> response = userAccountService.getUsers();
        assertEquals(4, response.size());
    }

    @Test
    public void deleteUserFromDatabase() {
        doNothing().when(userRepo).delete(user);

        userRepo.delete(user);
        verify(userRepo, times(1)).delete(user);
    }

    @Test
    public void deleteNonExistentUser_throwsStatusException() {
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> userAccountService.deleteUser(user.getUsername()));
    }

    @Test
    public void increaseFailedAttemptsForUser() {
        doAnswer(invocation -> {
            user.setFailedAttempt(1);
            return null;
        }).when(userRepo).updateFailedAttempts(user.getFailedAttempt() + 1, user.getUsername());
        userAccountService.increaseFailedAttempts(user);

        verify(userRepo, times(1)).updateFailedAttempts(1, user.getUsername());
        assertEquals(1, user.getFailedAttempt());
    }

    @Test
    public void resetFailedAttemptsForUser() {
        user.setFailedAttempt(4);
        doAnswer(invocation -> {
            user.setFailedAttempt(0);
            return null;
        }).when(userRepo).updateFailedAttempts(0, user.getUsername());
        userAccountService.resetFailedAttempts(user.getUsername());

        verify(userRepo, times(1)).updateFailedAttempts(0, user.getUsername());
        assertEquals(0, user.getFailedAttempt());
    }

    @Test
    public void lockUser() {
        user.grantRole(userRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "LOCK");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        userAccountService.changeAccess(request);
    }

    @Test
    public void unlockUser() {
        user.setAccountNonBlocked(false);
        user.setLockTime(Date.from(Instant.now()));
        user.setFailedAttempt(5);
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        AdminRequest request = new AdminRequest(user.getUsername(), "UNLOCK");

        doAnswer(invocation -> {
            user.setAccountNonBlocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
            return null;
        }).when(userRepo).updateFailedAttempts(0, user.getUsername());

        userAccountService.changeAccess(request);

        assertEquals(0 ,user.getFailedAttempt());
        assertTrue(user.isAccountNonBlocked());
        assertNull(user.getLockTime());
    }

    @Test
    public void changeAccessForNonExistentUser_throwsStatusException() {
        AdminRequest request = new AdminRequest(user.getUsername(), "UNLOCK");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeAccess(request));
    }

    @Test
    public void lockAdmin_throwsStatusException() {
        user.grantRole(adminRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "LOCK");
        when(userRepo.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeAccess(request));
    }

    public List<Employee> setListOfUsers() {
        Employee user1 = new Employee("user", "one", "user1@acme.com", "password1");
        Employee user2 = new Employee("user", "two", "user2@acme.com", "password2");
        Employee user3 = new Employee("user", "three", "use3@acme.com", "password3");

        return List.of(user, user1, user2, user3);
    }

    public void setUserDetails(List<EmployeeRole> roles) {
        for (EmployeeRole role : roles) {
            user.grantRole(role);
        }
        user.setPassword(encoder.encode(user.getPassword()));
        UserDetails userDetails = new CustomUserDetails(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }
}
