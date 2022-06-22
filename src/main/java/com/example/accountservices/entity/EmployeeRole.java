package com.example.accountservices.entity;

import com.example.accountservices.util.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "principle_groups")
public class EmployeeRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @JsonIgnore
    private Long id;

    @Column(name = "roles", unique = true, nullable = false)
    private String accountRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "code")
    private UserRole group;

    @ManyToMany(mappedBy = "roles")
    private Set<Employee> users;

    public EmployeeRole(String accountRole, UserRole group) {
        this.accountRole = accountRole;
        this.group = group;
    }

    @Override
    public String toString() {
        return "EmployeeRole{" +
                "id=" + id +
                ", accountRole='" + accountRole + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
