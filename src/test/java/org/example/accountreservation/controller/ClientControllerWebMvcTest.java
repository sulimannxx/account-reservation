package org.example.accountreservation.controller;

import org.example.accountreservation.exception.ApiException;
import org.example.accountreservation.exception.ApiExceptionHandler;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.ErrorCode;
import org.example.accountreservation.generated.model.PageableResponse;
import org.example.accountreservation.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
    private ClientService clientService;

    @Test
    void createClientReturnsCreatedResponse() throws Exception {
        when(clientService.createClient(any())).thenReturn(clientResponse());

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
        when(clientService.createClient(any())).thenThrow(new ApiException(ErrorCode.INVALID_CLIENT_DATA));

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
        when(clientService.createClient(any())).thenThrow(new ApiException(ErrorCode.CLIENT_MDM_ID_ALREADY_EXISTS));

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
        when(clientService.searchClients(eq(0), eq(20), eq(null), eq(null)))
                .thenReturn(new ClientSearchResponse(List.of(), new PageableResponse(0, 20, 0, 0L)));

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
        when(clientService.getClient(CLIENT_ID)).thenThrow(new ApiException(ErrorCode.CLIENT_NOT_FOUND));

        mockMvc.perform(get(API_PREFIX + "/clients/{clientId}", CLIENT_ID)
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void deleteClientReturnsConflictWhenActiveAccountsExist() throws Exception {
        doThrow(new ApiException(ErrorCode.CLIENT_HAS_ACTIVE_ACCOUNTS)).when(clientService).deleteClient(CLIENT_ID);

        mockMvc.perform(delete(API_PREFIX + "/clients/{clientId}", CLIENT_ID)
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_HAS_ACTIVE_ACCOUNTS"))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void unexpectedExceptionReturnsInternalServerErrorBody() throws Exception {
        when(clientService.getClient(CLIENT_ID)).thenThrow(new IllegalStateException("database is unavailable"));

        mockMvc.perform(get(API_PREFIX + "/clients/{clientId}", CLIENT_ID)
                        .contextPath(API_PREFIX)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    private ClientResponse clientResponse() {
        OffsetDateTime now = NOW.atOffset(ZoneOffset.UTC);
        return new ClientResponse(
                CLIENT_ID,
                1234567890L,
                "Ivan",
                "Petrov",
                "Sergeevich",
                org.example.accountreservation.generated.model.ClientStatus.ACTIVE,
                now,
                now
        );
    }
}
