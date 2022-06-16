package com.example.accountservices.controller;

import com.example.accountservices.dto.EventLogResponse;
import com.example.accountservices.service.LoggerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller allows the auditor to view event logs
@RestController
@RequestMapping("api/security")
public class EventLogController {
    private final LoggerService loggerService;

    public EventLogController(LoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @GetMapping("/events")
    public List<EventLogResponse> getLog() {
        return null;
    }
}
