package org.example.accountreservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.accountreservation.generated.api.ClientsApi;
import org.example.accountreservation.generated.model.ClientDetailsResponse;
import org.example.accountreservation.generated.model.ClientExistsResponse;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.CreateClientRequest;
import org.example.accountreservation.generated.model.UpdateClientRequest;
import org.example.accountreservation.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final ClientService clientService;

    @Override
    public ResponseEntity<ClientSearchResponse> searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        return ResponseEntity.ok(clientService.searchClients(page, size, lastName, mdmId));
    }

    @Override
    public ResponseEntity<ClientExistsResponse> clientExists(UUID clientId) {
        return ResponseEntity.ok(clientService.clientExists(clientId));
    }

    @Override
    public ResponseEntity<ClientDetailsResponse> getClient(UUID clientId) {
        return ResponseEntity.ok(clientService.getClient(clientId));
    }

    @Override
    public ResponseEntity<Void> deleteClient(UUID clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClientResponse> updateClient(UUID clientId, UpdateClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(clientId, request));
    }

    @Override
    public ResponseEntity<ClientResponse> createClient(CreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
    }
}
