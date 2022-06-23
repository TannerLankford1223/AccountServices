package com.example.accountservices.infrastructure.adapters;

import com.example.accountservices.domain.ports.spi.EventLogPersistencePort;
import com.example.accountservices.infrastructure.entity.Event;
import com.example.accountservices.infrastructure.persistence.EventLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventLogJpaAdapter implements EventLogPersistencePort {

    private final EventLogRepository eventLogRepo;

    public EventLogJpaAdapter(EventLogRepository eventLogRepo) {
        this.eventLogRepo = eventLogRepo;
    }

    @Override
    public Event save(Event event) {
        return eventLogRepo.save(event);
    }

    @Override
    public List<Event> findAll() {
        return eventLogRepo.findAll();
    }
}
