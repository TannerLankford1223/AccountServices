package com.example.accountservices.infrastructure.entity;

import com.example.accountservices.domain.util.LogEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "event_log")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDate date;

    private String action;

    private String subject;

    private String object;

    private String path;

    public Event(LogEvent action, String subject, String object, String path) {
        this.date = LocalDate.now();
        this.action = action.toString();
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
}
