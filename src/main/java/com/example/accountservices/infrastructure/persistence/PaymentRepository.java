package com.example.accountservices.infrastructure.persistence;

import com.example.accountservices.infrastructure.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUsernameOrderByPeriodDesc(String employee);

    Optional<Payment> findPaymentByUsernameAndPeriod(String employee, String period);

    @Modifying
    @Transactional
    @Query(value = "UPDATE payment " +
            "SET salary = ?1 " +
            "WHERE user_id = (SELECT user_id FROM users WHERE users.username = ?2) AND period = ?3",
            nativeQuery = true)
    void updatePaymentByEmployeeAndPeriod(long salary, String username, String period);
}
