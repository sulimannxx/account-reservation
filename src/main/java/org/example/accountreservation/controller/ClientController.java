package org.example.accountreservation.controller;

import org.example.accountreservation.entity.Client;
import org.example.accountreservation.enums.AccountStatusCode;
import org.example.accountreservation.enums.ClientStatus;
import org.example.accountreservation.exception.ApiException;
import org.example.accountreservation.generated.api.ClientsApi;
import org.example.accountreservation.generated.model.ClientDetailsResponse;
import org.example.accountreservation.generated.model.ClientExistsResponse;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchItemResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.CreateClientRequest;
import org.example.accountreservation.generated.model.ErrorCode;
import org.example.accountreservation.generated.model.PageableResponse;
import org.example.accountreservation.generated.model.UpdateClientRequest;
import org.example.accountreservation.repository.AccountRepository;
import org.example.accountreservation.repository.ClientRepository;
import org.example.accountreservation.specification.ClientSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
public class ClientController implements ClientsApi {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    public ClientController(ClientRepository clientRepository, AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ResponseEntity<ClientSearchResponse> searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        validatePageRequest(page, size);

        Page<Client> clients = clientRepository.findAll(
                ClientSpecifications.byFilters(lastName, mdmId),
                PageRequest.of(page, size)
        );

        return ResponseEntity.ok(toClientSearchResponse(clients));
    }

    @Override
    public ResponseEntity<ClientExistsResponse> clientExists(UUID clientId) {
        ClientExistsResponse response = clientRepository.findById(clientId)
                .map(client -> new ClientExistsResponse(true, client.getId()).status(toApiStatus(client.getStatus())))
                .orElseGet(() -> new ClientExistsResponse(false, clientId));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClientDetailsResponse> getClient(UUID clientId) {
        return ResponseEntity.ok(toClientDetailsResponse(getRequiredClient(clientId)));
    }

    @Override
    public ResponseEntity<Void> deleteClient(UUID clientId) {
        Client client = getRequiredClient(clientId);

        if (accountRepository.existsByClient_IdAndStatus_NameIn(clientId, AccountStatusCode.activeNames())) {
            throw new ApiException(ErrorCode.CLIENT_HAS_ACTIVE_ACCOUNTS);
        }

        client.setStatus(ClientStatus.DELETED);
        clientRepository.save(client);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClientResponse> updateClient(UUID clientId, UpdateClientRequest request) {
        validateUpdateRequest(request);

        Client client = getRequiredClient(clientId);
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setMiddleName(request.getMiddleName());

        return ResponseEntity.ok(toClientResponse(clientRepository.save(client)));
    }

    @Override
    public ResponseEntity<ClientResponse> createClient(CreateClientRequest request) {
        validateCreateRequest(request);

        if (clientRepository.existsByMdmId(request.getMdmId())) {
            throw new ApiException(ErrorCode.CLIENT_MDM_ID_ALREADY_EXISTS);
        }

        Client client = new Client();
        client.setMdmId(request.getMdmId());
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setMiddleName(request.getMiddleName());
        client.setCitizenship(request.getCitizenship());
        client.setClientType(request.getClientType());
        client.setDocumentNumber(request.getDocumentNumber());
        client.setDocumentSeries(request.getDocumentSeries());
        client.setDocumentType(request.getDocumentType());
        client.setStatus(ClientStatus.ACTIVE);

        return ResponseEntity.status(201).body(toClientResponse(clientRepository.save(client)));
    }

    private ClientResponse toClientResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getMdmId(),
                client.getFirstName(),
                client.getLastName(),
                client.getMiddleName(),
                toApiStatus(client.getStatus()),
                toOffsetDateTime(client.getCreatedAt()),
                toOffsetDateTime(client.getUpdatedAt())
        );
    }

    private ClientDetailsResponse toClientDetailsResponse(Client client) {
        return new ClientDetailsResponse(
                client.getId(),
                client.getMdmId(),
                client.getFirstName(),
                client.getLastName(),
                client.getMiddleName(),
                toApiStatus(client.getStatus()),
                toOffsetDateTime(client.getCreatedAt()),
                toOffsetDateTime(client.getUpdatedAt()),
                false
        );
    }

    private ClientSearchResponse toClientSearchResponse(Page<Client> clients) {
        List<ClientSearchItemResponse> content = clients.getContent().stream()
                .map(this::toClientSearchItemResponse)
                .toList();

        PageableResponse pageable = new PageableResponse(
                clients.getNumber(),
                clients.getSize(),
                clients.getTotalPages(),
                clients.getTotalElements()
        );

        return new ClientSearchResponse(content, pageable);
    }

    private ClientSearchItemResponse toClientSearchItemResponse(Client client) {
        return new ClientSearchItemResponse(
                client.getId(),
                client.getMdmId(),
                client.getFirstName(),
                client.getLastName(),
                client.getMiddleName(),
                toApiStatus(client.getStatus())
        );
    }

    private Client getRequiredClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLIENT_NOT_FOUND));
    }

    private void validateCreateRequest(CreateClientRequest request) {
        if (request == null
                || request.getMdmId() == null
                || request.getMdmId() < 1
                || hasInvalidName(request.getFirstName(), request.getLastName(), request.getMiddleName())) {
            throw new ApiException(ErrorCode.INVALID_CLIENT_DATA);
        }
    }

    private void validateUpdateRequest(UpdateClientRequest request) {
        if (request == null || hasInvalidName(request.getFirstName(), request.getLastName(), request.getMiddleName())) {
            throw new ApiException(ErrorCode.INVALID_CLIENT_DATA);
        }
    }

    private void validatePageRequest(Integer page, Integer size) {
        if (page == null || page < 0 || size == null || size < 1 || size > 200) {
            throw new ApiException(ErrorCode.INVALID_CLIENT_DATA);
        }
    }

    private boolean hasInvalidName(String firstName, String lastName, String middleName) {
        return isBlank(firstName) || isBlank(lastName) || isBlank(middleName);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private org.example.accountreservation.generated.model.ClientStatus toApiStatus(ClientStatus status) {
        return status == null
                ? null
                : org.example.accountreservation.generated.model.ClientStatus.fromValue(status.name());
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
