package com.example.accountservices.infrastructure.persistence;

import com.example.accountservices.infrastructure.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends JpaRepository<Event, Long> {
}
