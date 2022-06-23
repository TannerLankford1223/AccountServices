package com.example.accountservices.infrastructure.persistence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BreachedPasswords {

    // implement hashset for fast lookup
    private final static Set<String> breachedPasswords = new HashSet<>(
            Arrays.asList("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"));

    private BreachedPasswords() {

    }

    public static Boolean isBreached(String password) {
        return breachedPasswords.contains(password);
    }
}
