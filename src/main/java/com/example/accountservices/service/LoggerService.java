package com.example.accountservices.service;


import com.example.accountservices.dto.EventLogResponse;
import com.example.accountservices.util.LogEvent;

import java.util.List;

public interface LoggerService {

    void log(LogEvent action, String subject, String object, String path);

    List<EventLogResponse> getLog();
}
