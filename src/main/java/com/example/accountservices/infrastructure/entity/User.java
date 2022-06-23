package com.example.accountservices.infrastructure.entity;

import com.example.accountservices.domain.util.UserRole;

public interface User {
    boolean hasRole(UserRole roleName);

    void grantRole(EmployeeRole role);

    void removeRole(EmployeeRole role);

    void setAccountNonBlocked(boolean accountNonBlocked);
}
