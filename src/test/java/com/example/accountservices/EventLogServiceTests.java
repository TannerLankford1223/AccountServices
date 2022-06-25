package com.example.accountservices;

import com.example.accountservices.domain.data.EventLogResponse;
import com.example.accountservices.domain.ports.api.EventLogServicePort;
import com.example.accountservices.domain.ports.spi.EventLogPersistencePort;
import com.example.accountservices.domain.service.EventLogService;
import com.example.accountservices.domain.util.LogEvent;
import com.example.accountservices.infrastructure.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventLogServiceTests {

    @Mock
    private EventLogPersistencePort loggerRepo;

    private EventLogServicePort eventLogServicePort;

    @BeforeEach
    void init() {
        this.eventLogServicePort = new EventLogService(loggerRepo);
    }

    @Test
    public void logEvent() {
        LogEvent action = LogEvent.CREATE_USER;
        String subject = "ANONYMOUS";
        String object = "user@acme.com";
        String path = "/api/auth/signup";

        eventLogServicePort.log(action, subject, object, path);

        verify(loggerRepo, times(1)).save(any(Event.class));
    }


    @Test
    public void getLog_ReturnsListOfEventLogResponse() {
        Event event = new Event(LogEvent.CREATE_USER, "ANONYMOUS", "user@acme.com",
                "/api/auth/signup");
        Event event1 = new Event(LogEvent.GRANT_ROLE, "john@acme.com", "user@acme.com",
                "/api/admin/user/role");
        Event event2 = new Event(LogEvent.LOCK_USER, "Lock user maxmustermann@acme.com",
                "maxmustermann@acme.com", "/api/admin/user/access");
        Event event3 = new Event(LogEvent.BRUTE_FORCE, "maxmustermann@acme.com", "/api/empl/payment",
                "/api/empl/payment");

        List<Event> events = List.of(event, event1, event2, event3);
        when(loggerRepo.findAll()).thenReturn(events);

        List<EventLogResponse> response = eventLogServicePort.getLog();

        assertEquals(4, response.size());
        assertEquals(LogEvent.CREATE_USER.toString(), response.get(0).getAction());
        assertEquals(LogEvent.GRANT_ROLE.toString(), response.get(1).getAction());
        assertEquals(LogEvent.LOCK_USER.toString(), response.get(2).getAction());
        assertEquals(LogEvent.BRUTE_FORCE.toString(), response.get(3).getAction());
    }

    @Test
    public void getLog_NoEvents_ReturnsEmptyListOfEventLogResponse() {
        when(loggerRepo.findAll()).thenReturn(List.of());
        List<EventLogResponse> response = eventLogServicePort.getLog();
        assertEquals(0, response.size());
    }
}
