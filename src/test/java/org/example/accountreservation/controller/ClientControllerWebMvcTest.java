package org.example.accountreservation.controller;

import org.example.accountreservation.entity.Client;
import org.example.accountreservation.enums.ClientStatus;
import org.example.accountreservation.exception.ApiExceptionHandler;
import org.example.accountreservation.repository.AccountRepository;
import org.example.accountreservation.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import(ApiExceptionHandler.class)
class ClientControllerWebMvcTest {

    private static final String API_PREFIX = "/api/v1";
    private static final UUID CLIENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant NOW = Instant.parse("2024-01-15T10:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientRepository clientRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void createClientReturnsCreatedResponse() throws Exception {
        when(clientRepository.existsByMdmId(1234567890L)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> persistedClient(invocation.getArgument(0)));

        mockMvc.perform(post(API_PREFIX + "/clients")
                        .contextPath(API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content("""
                                {
                                  "mdmId": 1234567890,
                                  "firstName": "Ivan",
                                  "lastName": "Petrov",
                                  "middleName": "Sergeevich"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$.mdmId").value(1234567890L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createClientReturnsBadRequestForInvalidBody() throws Exception {
        mockMvc.perform(post(API_PREFIX + "/clients")
                        .contextPath(API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mdmId": 1234567890,
                                  "lastName": "Petrov",
                                  "middleName": "Sergeevich"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CLIENT_DATA"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void createClientReturnsConflictForDuplicateMdmId() throws Exception {
        when(clientRepository.existsByMdmId(1234567890L)).thenReturn(true);

        mockMvc.perform(post(API_PREFIX + "/clients")
                        .contextPath(API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mdmId": 1234567890,
                                  "firstName": "Ivan",
                                  "lastName": "Petrov",
                                  "middleName": "Sergeevich"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_MDM_ID_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void searchClientsReturnsEmptyPage() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        when(clientRepository.findAll(anyClientSpecification(), eq(pageable))).thenReturn(Page.empty(pageable));

        mockMvc.perform(get(API_PREFIX + "/clients")
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));
    }

    @Test
    void getClientReturnsNotFoundErrorBody() throws Exception {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_PREFIX + "/clients/{clientId}", CLIENT_ID)
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void deleteClientReturnsConflictWhenActiveAccountsExist() throws Exception {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client()));
        when(accountRepository.existsByClient_IdAndStatus_NameIn(eq(CLIENT_ID), any())).thenReturn(true);

        mockMvc.perform(delete(API_PREFIX + "/clients/{clientId}", CLIENT_ID)
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_HAS_ACTIVE_ACCOUNTS"))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void unexpectedExceptionReturnsInternalServerErrorBody() throws Exception {
        when(clientRepository.findById(CLIENT_ID)).thenThrow(new IllegalStateException("database is unavailable"));

        mockMvc.perform(get(API_PREFIX + "/clients/{clientId}", CLIENT_ID)
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    private Client persistedClient(Client client) {
        client.setId(CLIENT_ID);
        client.setStatus(ClientStatus.ACTIVE);
        client.setCreatedAt(NOW);
        client.setUpdatedAt(NOW);
        return client;
    }

    private Client client() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setMdmId(1234567890L);
        client.setFirstName("Ivan");
        client.setLastName("Petrov");
        client.setMiddleName("Sergeevich");
        client.setStatus(ClientStatus.ACTIVE);
        client.setCreatedAt(NOW);
        client.setUpdatedAt(NOW);
        return client;
    }

    private Specification<Client> anyClientSpecification() {
        return any();
    }
}
