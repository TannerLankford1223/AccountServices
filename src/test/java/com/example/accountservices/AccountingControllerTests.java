package com.example.accountservices;

import com.example.accountservices.domain.data.PaymentRequest;
import com.example.accountservices.infrastructure.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Testcontainers
@Sql(scripts = "/insertUsers.sql")
public class AccountingControllerTests {

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
    @Sql(statements = "TRUNCATE TABLE payment")
    public void postPayroll_AllValidPayments_ReturnsPaymentResponse() throws Exception{
        List<PaymentRequest> payments = getListOfPaymentRequests();

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payments));
        mockMvc.perform(request.with(user("john@acme.com").roles("ACCOUNTANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Added successfully")));
    }

    @Test
    @Sql(statements = "TRUNCATE TABLE payment")
    public void postPayroll_PaymentInvalid_ReturnsException() throws Exception {
        List<PaymentRequest> payments = getListOfPaymentRequests();
        PaymentRequest newPaymentRequest = new PaymentRequest("john@acme.com", "13-Twenty22", 500000L);
        payments.add(newPaymentRequest);

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payments));
        mockMvc.perform(request.with(user("john@acme.com").roles("ACCOUNTANT")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void postPayroll_UserIsNotAccountant_ReturnsException() throws Exception{
        List<PaymentRequest> payments = getListOfPaymentRequests();

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payments));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Sql(scripts = { "/insertUsers.sql", "/insertPayments.sql"})
    public void updateSalary_EmployeeExists_ReturnsPaymentResponse() throws Exception{
        PaymentRequest payment = new PaymentRequest("paul@acme.com", "05-2021", 8000000L);

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment));
        mockMvc.perform(request.with(user("john@acme.com").roles("ACCOUNTANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Updated successfully")));
    }

    @Test
    @Sql(scripts = "/insertPayments.sql")
    public void updateSalary_NonExistentEmployee_ReturnsException() throws Exception {
        Payment payment = new Payment("fakeUser@acme.com", "05-2021", 8000000);

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment));
        mockMvc.perform(request.with(user("john@acme.com").roles("ACCOUNTANT")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Sql(scripts = "/insertPayments.sql")
    public void updateSalary_UserIsNotAccountant_ReturnsException() throws Exception{
        Payment payment = new Payment("paul@acme.com", "05-2021", 8000000);

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment));
        mockMvc.perform(request.with(user("john@acme.com").roles("AUDITOR")))
                .andExpect(status().is4xxClientError());
    }

    public List<PaymentRequest> getListOfPaymentRequests() {
        PaymentRequest payment = new PaymentRequest("john@acme.com", "05-2021", 5000000L);
        PaymentRequest payment1 = new PaymentRequest("jane@acme.com", "05-2022", 10000000L);
        PaymentRequest payment2 = new PaymentRequest("jane@acme.com", "06-2022", 12500000L);
        PaymentRequest payment3 = new PaymentRequest("jane@acme.com", "07-2022", 13500000L);
        PaymentRequest payment4 = new PaymentRequest("paul@acme.com", "05-2021", 7500000L);
        PaymentRequest payment5 = new PaymentRequest("paul@acme.com", "06-2021", 7000000L);

        return new ArrayList<>(List.of(payment, payment1, payment2, payment3, payment4, payment5));
    }
}
