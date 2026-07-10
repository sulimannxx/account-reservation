package org.example.accountreservation.service;

import org.example.accountreservation.entity.Client;
import org.example.accountreservation.enums.ClientStatus;
import org.example.accountreservation.exception.ApiException;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.CreateClientRequest;
import org.example.accountreservation.generated.model.ErrorCode;
import org.example.accountreservation.mapper.ClientMapper;
import org.example.accountreservation.repository.AccountRepository;
import org.example.accountreservation.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final UUID CLIENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant NOW = Instant.parse("2024-01-15T10:30:00Z");

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountRepository accountRepository;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository, accountRepository, Mappers.getMapper(ClientMapper.class));
    }

    @Test
    void createClientPersistsActiveClientAndReturnsDto() {
        CreateClientRequest request = new CreateClientRequest(1234567890L, "Ivan", "Petrov", "Sergeevich")
                .citizenship("RU")
                .clientType("PRIVATE");

        when(clientRepository.existsByMdmId(1234567890L)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> persistedClient(invocation.getArgument(0)));

        ClientResponse response = clientService.createClient(request);

        assertThat(response.getId()).isEqualTo(CLIENT_ID);
        assertThat(response.getMdmId()).isEqualTo(1234567890L);
        assertThat(response.getStatus().getValue()).isEqualTo("ACTIVE");
    }

    @Test
    void createClientThrowsConflictWhenMdmIdAlreadyExists() {
        CreateClientRequest request = new CreateClientRequest(1234567890L, "Ivan", "Petrov", "Sergeevich");
        when(clientRepository.existsByMdmId(1234567890L)).thenReturn(true);

        assertThatThrownBy(() -> clientService.createClient(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CLIENT_MDM_ID_ALREADY_EXISTS));
    }

    @Test
    void searchClientsReturnsDtoWithEmptyContent() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(clientRepository.findAll(anyClientSpecification(), eq(pageable))).thenReturn(Page.empty(pageable));

        ClientSearchResponse response = clientService.searchClients(0, 20, null, null);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPageable().getPageNumber()).isZero();
        assertThat(response.getPageable().getPageSize()).isEqualTo(20);
    }

    @Test
    void deleteClientMarksClientAsDeletedWhenNoActiveAccountsExist() {
        Client client = client();
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(accountRepository.existsByClient_IdAndStatus_NameIn(eq(CLIENT_ID), any())).thenReturn(false);

        clientService.deleteClient(CLIENT_ID);

        assertThat(client.getStatus()).isEqualTo(ClientStatus.DELETED);
        verify(clientRepository).save(client);
    }

    @Test
    void deleteClientThrowsConflictWhenActiveAccountsExist() {
        Client client = client();
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(accountRepository.existsByClient_IdAndStatus_NameIn(eq(CLIENT_ID), any())).thenReturn(true);

        assertThatThrownBy(() -> clientService.deleteClient(CLIENT_ID))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CLIENT_HAS_ACTIVE_ACCOUNTS));
    }

    private Client persistedClient(Client client) {
        client.setId(CLIENT_ID);
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
