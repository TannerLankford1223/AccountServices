package com.example.accountservices.domain.util;

public class DateValidator {
    private DateValidator(){};

    public static boolean isDateInvalid(String period) {
        return !period.matches("(0[1-9]|1[0-2])-\\d{4}");
    }
}
