package com.example.accountservices.domain.ports.spi;

import com.example.accountservices.infrastructure.entity.Event;

import java.util.List;

public interface EventLogPersistencePort {

    Event save(Event event);

    List<Event> findAll();
}
