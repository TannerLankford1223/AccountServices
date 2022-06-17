package com.example.accountservices.service;

import com.example.accountservices.entity.CustomUserDetails;
import com.example.accountservices.entity.Employee;
import com.example.accountservices.entity.EmployeeRole;
import com.example.accountservices.persistence.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Employee> userOpt = userRepo.findByUsernameIgnoreCase(username.toLowerCase());

        if (userOpt.isPresent()) {
            return new CustomUserDetails(userOpt.get());
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    private Collection<GrantedAuthority> getAuthorities(Employee employee) {
        Set<EmployeeRole> roles = employee.getRoles();
        Collection<GrantedAuthority> authorities = new ArrayList<>(roles.size());
        for (EmployeeRole role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getAccountRole()));
        }

        return authorities;
    }
}
