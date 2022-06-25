package com.example.accountservices;

import com.example.accountservices.infrastructure.entity.Employee;
import com.example.accountservices.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(scripts = {"/insertUsers-h2.sql"})
public class UserRepositoryUnitTests {

    @Autowired
    private UserRepository userRepo;

    @Test
    public void findByUsername_UserExists_ReturnsOptionalOfUser() {
        Optional<Employee> user = userRepo.findByUsernameIgnoreCase("john@acme.com");
        assertTrue(user.isPresent());
        assertEquals("john@acme.com", user.get().getUsername());
    }

    @Test
    public void findByUsername_UserNonExistent_ReturnsEmptyOptional() {
        Optional<Employee> user = userRepo.findByUsernameIgnoreCase("fakeUser@acme.com");
        assertTrue(user.isEmpty());
    }

    @Test
    public void findAllOrderedByUserId_ReturnsListOfUsers() {
        List<Employee> users = userRepo.findAllByOrderByUserIdAsc();
        assertEquals(3, users.size());
    }

    @Test
    public void updateFailedAttempts_IncrementAttemps() {
        userRepo.updateFailedAttempts(3, "john@acme.com");
        Optional<Employee> user = userRepo.findByUsernameIgnoreCase("john@acme.com");
        assertTrue(user.isPresent());
        assertEquals(3, user.get().getFailedAttempt());
    }
}
