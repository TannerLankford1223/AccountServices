package com.example.accountservices;

import com.example.accountservices.domain.data.NewPassword;
import com.example.accountservices.domain.data.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Testcontainers
@Sql(scripts = "/insertUsers.sql")
public class AccountServiceControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer("postgres")
            .withDatabaseName("testPostgres")
            .withUsername("user")
            .withPassword("testPass");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
    }

    @Test
    public void signup_ValidUser_ReturnsUserResponse() throws Exception {
        UserRequest userRequest = new UserRequest("George", "Harrison",
                "george@acme.com", "newPassword5");
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest));
        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void changePassword_ValidUser_ReturnsUserResponse() throws Exception {
        NewPassword password = new NewPassword("ThisIsAPassword123");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/auth/changepass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password.getPassword()));
        mockMvc.perform(request.with(user("jane@acme.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("jane@acme.com")))
                .andExpect(jsonPath("$.status", is("The password has been updated successfully")));
    }

    @Test
    public void changePassword_UserHasInvalidRole_ReturnsException() throws Exception {
        NewPassword password = new NewPassword("ThisIsAPassword123");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/auth/changepass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password.getPassword()));
        mockMvc.perform(request.with(user("fakeUser@acme.com")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Sql(scripts = "/insertPayments.sql")
    public void getUserPayment_UserDoesntProvidePeriod_ReturnAllPaymentsForUser() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/empl/payment")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request.with(user("jane@acme.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(3)));
    }

    @Test
    @Sql(scripts = "/insertPayments.sql")
    public void getUserPayment_UserProvidesPeriod_ReturnUserPaymentForPeriod() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/empl/payment?period=06-2021")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request.with(user("paul@acme.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastname", is("McCartney")))
                .andExpect(jsonPath("$.period", is("June-2021")));
    }

    @Test
    @WithAnonymousUser
    public void getUserPayment_WithAnonymousUser_ReturnsAccessDenied() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/empl/payment")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }
}
