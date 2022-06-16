package com.example.accountservices.entity;

import com.example.accountservices.util.UserRole;

public interface User {
    boolean hasRole(UserRole roleName);

    void grantRole(EmployeeRole role);

    void removeRole(EmployeeRole role);

    void setAccountNonBlocked(boolean accountNonBlocked);
}
