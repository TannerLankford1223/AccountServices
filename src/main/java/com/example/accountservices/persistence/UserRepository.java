package com.example.accountservices.persistence;

import com.example.accountservices.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsernameIgnoreCase(String username);

    List<Employee> findAllByOrderByUserIdAsc();

    void delete(Employee employee);

    @Query("UPDATE Employee emp SET emp.failedAttempt = ?1 WHERE emp.username = ?2")
    @Transactional
    @Modifying
    void updateFailedAttempts(int failAttempts, String email);
}
