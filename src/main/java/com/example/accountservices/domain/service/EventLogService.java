package com.example.accountservices.domain.service;

import com.example.accountservices.domain.data.EventLogResponse;
import com.example.accountservices.domain.ports.api.EventLogServicePort;
import com.example.accountservices.domain.ports.spi.EventLogPersistencePort;
import com.example.accountservices.domain.util.LogEvent;
import com.example.accountservices.infrastructure.entity.Event;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventLogService implements EventLogServicePort {

    private final EventLogPersistencePort loggerRepo;

    public EventLogService(EventLogPersistencePort loggerRepo) {
        this.loggerRepo = loggerRepo;
    }

    @CacheEvict("eventLog")
    @Transactional
    @Override
    public void log(LogEvent action, String subject, String object, String path) {
        Event event = new Event(action, subject, object, path);
        loggerRepo.save(event);
    }


    @CachePut("eventLog")
    @Override
    public List<EventLogResponse> getLog() {
        return loggerRepo.findAll().stream()
                .map(event ->
                    new EventLogResponse(event.getDate(), event.getAction(),
                            event.getSubject(), event.getObject(), event.getPath())
                ).collect(Collectors.toList());
    }
}
