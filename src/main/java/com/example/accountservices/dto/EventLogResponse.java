package com.example.accountservices.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class EventLogResponse {

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("action")
    private String action;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("object")
    private String object;

    @JsonProperty("path")
    private String path;

}
