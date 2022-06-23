package com.example.accountservices.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.text.DateFormatSymbols;

@Data
@Builder
public class PaymentResponse {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("lastname")
    private final String lastName;

    @JsonProperty("employee")
    private final String email;

    @JsonProperty("period")
    private String period;

    @JsonProperty("salary")
    private String salary;

    @JsonProperty("status")
    private final String status;

    // Custom setters for period and salary
    public static class PaymentResponseBuilder {
        private String period;
        private String Salary;

        public PaymentResponseBuilder period(String period) {
            this.period = parsePeriod(period);
            return this;
        }

        public PaymentResponseBuilder salary(long salary) {
            this.salary = parseSalary(salary);
            return this;
        }

        private static String parsePeriod(String period) {
            String[] date = period.split("-");
            int monthNum = Integer.parseInt(date[0]);
            String month = new DateFormatSymbols().getMonths()[monthNum - 1];
            String year = date[1];

            return month + "-" + year;
        }

        private static String parseSalary(long salary) {
            long cents = salary % 100;
            long dollars = salary / 100;

            return dollars + " dollar(s) " + cents + " cent(s)";
        }
    }
}
