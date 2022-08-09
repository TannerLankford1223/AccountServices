package com.example.accountservices.application.controller;

import com.example.accountservices.domain.data.EventLogResponse;
import com.example.accountservices.domain.ports.api.EventLogServicePort;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller allows the auditor to view event logs
@RestController
@RequestMapping("api/security")
public class EventLogController {
    private final EventLogServicePort eventLogService;

    public EventLogController(EventLogServicePort eventLogService) {
        this.eventLogService = eventLogService;
    }

    @GetMapping("/events")
    @Operation(summary = "Allows an auditor to view the security event log")
    public List<EventLogResponse> getLog() {
        return eventLogService.getLog();
    }
}
