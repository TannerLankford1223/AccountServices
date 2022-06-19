package com.example.accountservices.entity;

import com.example.accountservices.util.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class Employee implements User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long userId;

    @NotBlank
    private String name;

    @NotBlank
    @JsonProperty("lastname")
    private String lastName;

    @NotBlank
    @JsonProperty("email")
    @Pattern(regexp = "\\w+(@acme.com)$", message = "Must be an acme.com email")
    private String username;

    @NotBlank
//    @Min(value = 12, message =  "Password must be a minimum of 12 chars!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @OneToMany(mappedBy = "user")
    private Set<Payment> payments = new HashSet<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    protected Set<EmployeeRole> roles = new HashSet<>();

    private boolean accountNonBlocked = true;

    private int failedAttempt = 0;

    private Date lockTime;

    public Employee(String name, String lastName, String username, String password) {
        this.name = name;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean hasRole(UserRole group) {
        for (EmployeeRole role : roles) {
            if (Objects.equals(role.getGroup(), group)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void grantRole(EmployeeRole role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }

    @Override
    public void removeRole(EmployeeRole role) {
        roles.remove(role);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                '}';
    }
}
