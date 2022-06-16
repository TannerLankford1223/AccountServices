package com.example.accountservices.service;

import com.example.accountservices.dto.EventLogResponse;
import com.example.accountservices.persistence.EventLogRepository;
import com.example.accountservices.util.LogEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

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
    }


    @CachePut("eventLog")
    @Override
    public List<EventLogResponse> getLog() {
        return null;
    }
}
