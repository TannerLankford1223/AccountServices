package com.example.accountservices.entity;

import com.example.accountservices.util.LogEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
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

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", date=" + date +
                ", action='" + action + '\'' +
                ", subject='" + subject + '\'' +
                ", object='" + object + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
