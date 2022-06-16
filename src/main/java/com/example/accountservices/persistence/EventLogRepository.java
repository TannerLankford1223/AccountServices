package com.example.accountservices.persistence;

import com.example.accountservices.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<Event, Long> {
    List<Event> findAll();
}
