package com.example.accountservices.domain.data;

import com.example.accountservices.infrastructure.entity.EmployeeRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.SortNatural;

import java.util.Set;
import java.util.TreeSet;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserResponse {

    @JsonProperty("id")
    private long userId = 0;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("lastname")
    private final String lastName;

    @JsonProperty("username")
    private final String username;

    private final String password;

    @JsonProperty("roles")
    @SortNatural()
    private Set<String> roles;

    @JsonProperty("status")
    private final String status;

        // Custom setter for roles
        public static class UserResponseBuilder {
            private Set<String> roles;

            // Place roles in TreeSet to ensure proper ordering
            public UserResponseBuilder roles(Set<EmployeeRole> vals) {
                if (roles == null) {
                    roles = new TreeSet<>();
                }

                for (EmployeeRole role : vals) {
                    roles.add(role.getAccountRole());
                }

                return this;
            }
        }
}
