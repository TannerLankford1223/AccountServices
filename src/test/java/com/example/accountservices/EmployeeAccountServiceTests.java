package com.example.accountservices;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.UserRequest;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.UserAccountServicePort;
import com.example.accountservices.domain.ports.spi.RolePersistencePort;
import com.example.accountservices.domain.ports.spi.UserPersistencePort;
import com.example.accountservices.domain.service.EmployeeAccountService;
import com.example.accountservices.domain.util.UserRole;
import com.example.accountservices.infrastructure.entity.CustomUserDetails;
import com.example.accountservices.infrastructure.entity.Employee;
import com.example.accountservices.infrastructure.entity.EmployeeRole;
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
    private UserPersistencePort userRepo;

    @Mock
    private RolePersistencePort roleRepo;

//    @Autowired
    private PasswordEncoder encoder;

    private final Authentication authentication = Mockito.mock(Authentication.class);
    private final SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    private UserAccountServicePort userAccountService;

    private Employee user;
    private EmployeeRole adminRole;
    private EmployeeRole userRole;
    private EmployeeRole accountantRole;


    @BeforeEach
    void init() {
        this.encoder = new BCryptPasswordEncoder();
        this.userAccountService = new EmployeeAccountService(userRepo, roleRepo, encoder);
        this.user = new Employee("firstname", "lastname", "email@acme.com",
                "thisIsAPassword");
        this.adminRole = new EmployeeRole();
        this.userRole = new EmployeeRole();
        this.accountantRole = new EmployeeRole();

        adminRole.setId(1L);
        adminRole.setAccountRole("ROLE_ADMINISTRATOR");
        adminRole.setGroup(UserRole.ADMINISTRATOR);

        userRole.setId(2L);
        userRole.setAccountRole("ROLE_USER");
        userRole.setGroup(UserRole.USER);

        accountantRole.setId(3L);
        accountantRole.setAccountRole("ROLE_ACCOUNTANT");
        accountantRole.setGroup(UserRole.ACCOUNTANT);
    }

    @Test
    public void registerExistingUser_throwsError() {
        when(userRepo.find("email@acme.com")).thenReturn(Optional.of(user));
        UserRequest request = new UserRequest(user.getName(), user.getLastName(),
                user.getUsername(), user.getPassword());
        assertThrows(ResponseStatusException.class, () -> userAccountService.register(request));
    }

    @Test
    public void registerUserWithBreachedPass_throwsStatusException() {
        UserRequest badRequest = new UserRequest("Jane", "Doe", "jane@acme.com",
                "PasswordForJuly");

        assertThrows(ResponseStatusException.class, () -> userAccountService.register(badRequest));
    }

    @Test
    public void changePassword_ToValidPassword_returnsUserResponse() {
        String newPassword = "testPassword1234";
        setUserDetails(List.of(adminRole));
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));

        UserResponse response = userAccountService.changePassword(newPassword);
        assertEquals("The password has been updated successfully", response.getStatus());
    }

    @Test
    public void changePassword_ToBreachedPass_throwsStatusException() {
        String newBreachedPassword = "PasswordForNovember";
        setUserDetails(List.of(adminRole));
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changePassword(newBreachedPassword));
    }

    @Test
    public void changePassword_ToSamePass_throwsStatusException() {
        String samePassword = "thisIsAPassword";
        setUserDetails(List.of(userRole));
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changePassword(samePassword));
    }

    @Test
    public void changePassword_UnabletoFindUser_throwsStatusException() {
        String newPassword = "testingNewPasswords";
        setUserDetails(List.of(userRole));
        when(userRepo.find(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userAccountService.changePassword(newPassword));
    }

    @Test
    public void grantRoleToUser_returnsUserResponse() {
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "GRANT");
        when(userRepo.find(request.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(adminRole));

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
        when(userRepo.find(request.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(accountantRole));

        UserResponse response = userAccountService.changeRole(request);
        assertEquals("firstname", response.getName());
        assertEquals("lastname", response.getLastName());
        assertEquals("email@acme.com", response.getUsername());
        assertEquals(Set.of("ROLE_USER"), response.getRoles());
    }

    @Test
    public void changeRole_UserInvalid_throwsStatusException() {
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "GRANT");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.empty());
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(adminRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void changeRoleToInvalidOperation_throwsStatusException() {
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "LOCK");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(adminRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void grantBusinessRoleToAdmin_throwsStatusException() {
        user.grantRole(adminRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ACCOUNTANT", "GRANT");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(accountantRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void removeRole_UserDoesNotHaveRole_throwsStatusException() {
        user.grantRole(adminRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "USER", "REMOVE");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase()))).thenReturn(Optional.of(userRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }


    @Test
    public void removeAdminRole_throwsStatusException() {
        user.grantRole(adminRole);
        user.grantRole(userRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ADMINISTRATOR", "REMOVE");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase())))
                .thenReturn(Optional.of(adminRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void removeLastRoleFromUser_throwsStatusException() {
        user.grantRole(accountantRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "ACCOUNTANT", "REMOVE");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));
        when(roleRepo.find(UserRole.valueOf(request.getRole().toUpperCase())))
                .thenReturn(Optional.of(accountantRole));

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeRole(request));
    }

    @Test
    public void getListOfUsers_returnsUserResponseList() {
        List<Employee> users = setListOfUsers();
        when(userRepo.findAllOrderDesc()).thenReturn(users);

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
        when(userRepo.find(user.getUsername())).thenReturn(Optional.empty());
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
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));

        userAccountService.changeAccess(request);
    }

    @Test
    public void unlockUser() {
        user.setAccountNonBlocked(false);
        user.setLockTime(Date.from(Instant.now()));
        user.setFailedAttempt(5);
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));

        AdminRequest request = new AdminRequest(user.getUsername(), "UNLOCK");

        doAnswer(invocation -> {
            user.setAccountNonBlocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
            return null;
        }).when(userRepo).updateFailedAttempts(0, user.getUsername());

        userAccountService.changeAccess(request);

        assertEquals(0, user.getFailedAttempt());
        assertTrue(user.isAccountNonBlocked());
        assertNull(user.getLockTime());
    }

    @Test
    public void changeAccessForNonExistentUser_throwsStatusException() {
        AdminRequest request = new AdminRequest(user.getUsername(), "UNLOCK");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userAccountService.changeAccess(request));
    }

    @Test
    public void lockAdmin_throwsStatusException() {
        user.grantRole(adminRole);
        AdminRequest request = new AdminRequest(user.getUsername(), "LOCK");
        when(userRepo.find(user.getUsername())).thenReturn(Optional.of(user));

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
