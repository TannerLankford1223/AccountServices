package com.example.accountservices.service;

import com.example.accountservices.dto.EventLogResponse;
import com.example.accountservices.entity.Event;
import com.example.accountservices.persistence.EventLogRepository;
import com.example.accountservices.util.LogEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoggerServiceImpl implements LoggerService {

    private final EventLogRepository loggerRepo;

    public LoggerServiceImpl(EventLogRepository loggerRepo) {
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
