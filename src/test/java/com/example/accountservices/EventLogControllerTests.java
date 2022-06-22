package com.example.accountservices;

import com.example.accountservices.persistence.EventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Testcontainers
@Sql(scripts = "/insertLogEvents.sql")
public class EventLogControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private EventLogRepository eventLogRepo;

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
    @WithMockUser(username = "john@acme.com", roles = {"AUDITOR"})
    public void getLog_UserIsAuditor_ReturnsListOfPaymentResponses() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/security/events/")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(6)))
                .andDo(document("get-event-log"));
    }

    @Test
    @WithAnonymousUser
    public void getLog_UserIsNotAuditor_ReturnsException() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/security/events/")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }
}
