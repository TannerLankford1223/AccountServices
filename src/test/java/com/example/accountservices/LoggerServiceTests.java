package com.example.accountservices;

import com.example.accountservices.dto.EventLogResponse;
import com.example.accountservices.entity.Event;
import com.example.accountservices.persistence.EventLogRepository;
import com.example.accountservices.service.LoggerService;
import com.example.accountservices.service.LoggerServiceImpl;
import com.example.accountservices.util.LogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LoggerServiceTests {

    @Mock
    private EventLogRepository loggerRepo;

    private LoggerService loggerService;

    @BeforeEach
    void init() {
        this.loggerService = new LoggerServiceImpl(loggerRepo);
    }

    @Test
    public void logEvent() {
        LogEvent action = LogEvent.CREATE_USER;
        String subject = "ANONYMOUS";
        String object = "user@acme.com";
        String path = "/api/auth/signup";

        loggerService.log(action, subject, object, path);

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

        List<EventLogResponse> response = loggerService.getLog();

        assertEquals(4, response.size());
        assertEquals(LogEvent.CREATE_USER.toString(), response.get(0).getAction());
        assertEquals(LogEvent.GRANT_ROLE.toString(), response.get(1).getAction());
        assertEquals(LogEvent.LOCK_USER.toString(), response.get(2).getAction());
        assertEquals(LogEvent.BRUTE_FORCE.toString(), response.get(3).getAction());
    }

    @Test
    public void getLog_NoEvents_ReturnsEmptyListOfEventLogResponse() {
        when(loggerRepo.findAll()).thenReturn(List.of());
        List<EventLogResponse> response = loggerService.getLog();
        assertEquals(0, response.size());
    }
}
