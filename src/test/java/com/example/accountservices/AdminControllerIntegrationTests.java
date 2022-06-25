package com.example.accountservices;

import com.example.accountservices.domain.data.AdminRequest;
import com.example.accountservices.domain.data.UserResponse;
import com.example.accountservices.domain.ports.api.UserAccountServicePort;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Testcontainers
@Sql(scripts = "/insertUsers.sql")
public class AdminControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private UserAccountServicePort accountService;

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
    public void changeUserRole_GrantValidRole_ReturnsAdminResponse() throws Exception {
        AdminRequest grantRole = new AdminRequest("jane@acme.com", "ACCOUNTANT","GRANT");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("jane@acme.com")))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_ACCOUNTANT")));
    }

    @Test
    public void changeUserRole_RemoveValidRole_ReturnsAdminResponse() throws Exception {
        UserResponse response = accountService.getUsers().get(2);
        accountService.changeRole(new AdminRequest(response.getUsername(), "ACCOUNTANT", "GRANT"));
        accountService.changeRole(new AdminRequest(response.getUsername(), "USER", "GRANT"));

        AdminRequest removeRole = new AdminRequest(response.getUsername(), "ACCOUNTANT","REMOVE");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("paul@acme.com")))
                .andExpect(jsonPath("$.roles.size()", is(1)))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_USER")));
    }

    @Test
    public void changeUserRole_UserNonExistent_ReturnsException() throws Exception {
        AdminRequest grantRole = new AdminRequest("fakeUser@acme.com", "ACCOUNTANT","GRANT");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeUserRole_InvalidAdminOperation_ReturnsException() throws Exception {
        AdminRequest grantRole = new AdminRequest("jane@acme.com", "ACCOUNTANT","LOCK");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeUserRole_UserIsNotAdmin_ReturnsException() throws Exception {
        AdminRequest grantRole = new AdminRequest("paul@acme.com", "ACCOUNTANT","GRANT");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("jane@acme.com").roles("AUDITOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void deleteUser_UserExists_ReturnsAdminResponse() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.delete("/api/admin/user/paul@acme.com")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Deleted successfully")));
    }

    @Test
    public void deleteUser_UserNonExistent_ReturnsException() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.delete("/api/admin/user/fakeUser@acme.com")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getUserInfo_ReturnsListOfUserResponses() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/admin/user/")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(3)));
    }

    @Test
    public void getUserInfo_UserIsNotAdmin_ReturnsException() throws Exception {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.get("/api/admin/user/")
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request.with(user("jane@acme.com").roles("ACCOUNTANT")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeAccess_LockUser_ReturnsAdminResponse() throws Exception {
        AdminRequest grantRole = new AdminRequest("jane@acme.com","LOCK");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("User jane@acme.com locked")));
    }

    @Test
    public void changeAccess_UnlockUser_ReturnsAdminResponse() throws Exception {
        AdminRequest grantRole = new AdminRequest("jane@acme.com","UNLOCK");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("User jane@acme.com unlocked")));
    }

    @Test
    public void changeAccess_UserNonExistent_ReturnsException() throws Exception {
        AdminRequest grantRole = new AdminRequest("fakeUser@acme.com","UNLOCK");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeAccess_InvalidAdminOperation_ReturnsException() throws Exception {
        AdminRequest grantRole = new AdminRequest("paul@acme.com","GRANT");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("john@acme.com").roles("ADMINISTRATOR")))
                .andExpect(status().is4xxClientError());
    }

    @Test void changeAccess_UserIsNotAdmin_ReturnsException() throws Exception {
        AdminRequest grantRole = new AdminRequest("paul@acme.com","LOCK");

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.put("/api/admin/user/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantRole));
        mockMvc.perform(request.with(user("jane@acme.com").roles("AUDITOR")))
                .andExpect(status().is4xxClientError());
    }
}
