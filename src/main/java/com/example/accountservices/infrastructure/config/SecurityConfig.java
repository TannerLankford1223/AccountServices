package com.example.accountservices.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(UserDetailsService userDetailsService,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    private static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/webjars/**"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handle auth errors
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman
                .and()
                .authorizeRequests() // manage access
                .antMatchers("/actuator/**").permitAll()
                .antMatchers(SWAGGER_ENDPOINTS).permitAll() // Allow access to Swagger endpoints for any user
                .antMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/changepass").authenticated()
                .antMatchers(HttpMethod.GET, "/api/empl/payment").hasRole("USER")
                .antMatchers(HttpMethod.POST, "/api/acct/payments").hasRole("ACCOUNTANT")
                .antMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole("ACCOUNTANT")
                .antMatchers(HttpMethod.GET, "/api/admin/user/").hasRole("ADMINISTRATOR")
                .antMatchers(HttpMethod.PUT, "/api/admin/user/role").hasRole("ADMINISTRATOR")
                .antMatchers(HttpMethod.DELETE, "/api/admin/user/*").hasRole("ADMINISTRATOR")
                .antMatchers(HttpMethod.GET, "/api/security/events/").hasRole("AUDITOR")
                .antMatchers(HttpMethod.PUT, "/api/admin/user/access").hasRole("ADMINISTRATOR")
                .anyRequest().denyAll()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
