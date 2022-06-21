package com.example.accountservices;

import com.example.accountservices.entity.Payment;
import com.example.accountservices.service.PaymentService;
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

    @Autowired
    private PaymentService paymentService;

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
        List<Payment> payments = getListOfPayments();

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
        List<Payment> payments = getListOfPayments();
        Payment newPayment = new Payment("john@acme.com", "13-Twenty22", 500000);
        payments.add(newPayment);

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
        List<Payment> payments = getListOfPayments();

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.post("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payments));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Sql(scripts = "/insertUsers.sql")
    @Sql(scripts = "/insertPayments.sql")
    public void updateSalary_EmployeeExists_ReturnsPaymentResponse() throws Exception{
        Payment payment = new Payment("paul@acme.com", "05-2021", 8000000);

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

    public List<Payment> getListOfPayments() {
        Payment payment = new Payment("john@acme.com", "05-2021", 5000000);
        Payment payment1 = new Payment("jane@acme.com", "05-2022", 10000000);
        Payment payment2 = new Payment("jane@acme.com", "06-2022", 12500000);
        Payment payment3 = new Payment("jane@acme.com", "07-2022", 13500000);
        Payment payment4 = new Payment("paul@acme.com", "05-2021", 7500000);
        Payment payment5 = new Payment("paul@acme.com", "06-2021", 7000000);

        return new ArrayList<>(List.of(payment, payment1, payment2, payment3, payment4, payment5));
    }
}
