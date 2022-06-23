package com.example.accountservices.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long paymentId;

    @NotBlank
    @JsonProperty("employee")
    private String username;

    @JsonProperty("period")
    @Pattern(regexp = "(0[1-9]|1[0-2])-\\d{4}", message = "Invalid date!")
    private String period;

    @JsonProperty("salary")
    @Positive
    private long salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Employee user;

    public Payment(String username, String period, long salary) {
        this.username = username;
        this.period = period;
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", employee='" + username + '\'' +
                ", period='" + period + '\'' +
                ", salary=" + salary +
                ", user=" + user +
                '}';
    }
}
