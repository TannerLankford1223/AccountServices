package com.example.accountservices.domain.ports.api;


import com.example.accountservices.domain.data.EventLogResponse;
import com.example.accountservices.domain.util.LogEvent;

import java.util.List;

public interface EventLogServicePort {

    void log(LogEvent action, String subject, String object, String path);

    List<EventLogResponse> getLog();
}
