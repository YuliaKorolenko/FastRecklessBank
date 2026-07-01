package org.example.fastrecklessbank.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BankApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private UUID createAccount(String name, String surname, long initialDeposit) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","surname":"%s","initialDepositCents":%d}
                                """.formatted(name, surname, initialDeposit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.balanceCents").value(initialDeposit))
                .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return UUID.fromString(id);
    }

    @Test
    void createDepositWithdrawFlow() throws Exception {
        UUID id = createAccount("Alice", "Smith", 1_000);

        mockMvc.perform(post("/api/accounts/{id}/deposit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountCents\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(1_500));

        mockMvc.perform(post("/api/accounts/{id}/withdraw", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountCents\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(1_300));
    }

    @Test
    void transferFlowAndHistory() throws Exception {
        UUID from = createAccount("Alice", "Smith", 1_000);
        UUID to = createAccount("Bob", "Jones", 0);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":"%s","toAccountId":"%s","amountCents":400}
                                """.formatted(from, to)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(600));

        mockMvc.perform(get("/api/accounts/{id}/transfers", from))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].toAccountId").value(to.toString()))
                .andExpect(jsonPath("$[0].amountCents").value(400));
    }

    @Test
    void withdrawBeyondBalanceReturns409() throws Exception {
        UUID id = createAccount("Alice", "Smith", 100);
        mockMvc.perform(post("/api/accounts/{id}/withdraw", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountCents\":500}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void invalidAmountsReturn400() throws Exception {
        UUID id = createAccount("Alice", "Smith", 100);
        // zero
        mockMvc.perform(post("/api/accounts/{id}/deposit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountCents\":0}"))
                .andExpect(status().isBadRequest());
        // negative
        mockMvc.perform(post("/api/accounts/{id}/deposit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountCents\":-10}"))
                .andExpect(status().isBadRequest());
        // non-numeric ("abc")
        mockMvc.perform(post("/api/accounts/{id}/deposit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountCents\":\"abc\"}"))
                .andExpect(status().isBadRequest());
        // missing amount (null)
        mockMvc.perform(post("/api/accounts/{id}/deposit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferToSelfReturns400() throws Exception {
        UUID id = createAccount("Alice", "Smith", 1_000);
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":"%s","toAccountId":"%s","amountCents":100}
                                """.formatted(id, id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
    }

    @Test
    void createAccountWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"surname\":\"Smith\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAccountsReturnsCreated() throws Exception {
        createAccount("Alice", "Smith", 10);
        createAccount("Bob", "Jones", 20);
        MvcResult result = mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andReturn();
        int size = JsonPath.read(result.getResponse().getContentAsString(), "$.length()");
        assertThat(size).isGreaterThanOrEqualTo(2);
    }
}
