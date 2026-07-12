package org.example.accountreservation.service;

import lombok.RequiredArgsConstructor;
import org.example.accountreservation.entity.Client;
import org.example.accountreservation.enums.AccountStatusCode;
import org.example.accountreservation.enums.ClientStatus;
import org.example.accountreservation.exception.ApiError;
import org.example.accountreservation.exception.ApiException;
import org.example.accountreservation.generated.model.ClientDetailsResponse;
import org.example.accountreservation.generated.model.ClientExistsResponse;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.CreateClientRequest;
import org.example.accountreservation.generated.model.UpdateClientRequest;
import org.example.accountreservation.mapper.ClientMapper;
import org.example.accountreservation.repository.AccountRepository;
import org.example.accountreservation.repository.ClientRepository;
import org.example.accountreservation.specification.ClientSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final ClientMapper clientMapper;
    private final ClientValidationService clientValidationService;

    @Transactional(readOnly = true)
    public ClientSearchResponse searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        clientValidationService.validatePageRequest(page, size);

        Page<Client> clients = clientRepository.findAll(
                ClientSpecifications.byFilters(lastName, mdmId),
                PageRequest.of(page, size)
        );

        return clientMapper.toClientSearchResponse(clients);
    }

    @Transactional(readOnly = true)
    public ClientExistsResponse clientExists(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(clientMapper::toClientExistsResponse)
                .orElseGet(() -> clientMapper.toClientNotExistsResponse(clientId));
    }

    @Transactional(readOnly = true)
    public ClientDetailsResponse getClient(UUID clientId) {
        return clientMapper.toClientDetailsResponse(getRequiredClient(clientId));
    }

    @Transactional
    public void deleteClient(UUID clientId) {
        Client client = getRequiredClient(clientId);

        if (accountRepository.existsByClient_IdAndStatus_NameIn(clientId, AccountStatusCode.activeNames())) {
            throw new ApiException(ApiError.CLIENT_HAS_ACTIVE_ACCOUNTS);
        }

        client.setStatus(ClientStatus.DELETED);
        clientRepository.save(client);
    }

    @Transactional
    public ClientResponse updateClient(UUID clientId, UpdateClientRequest request) {
        clientValidationService.validateUpdateRequest(request);

        Client client = getRequiredClient(clientId);
        clientMapper.updateClient(request, client);

        return clientMapper.toClientResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse createClient(CreateClientRequest request) {
        clientValidationService.validateCreateRequest(request);

        if (clientRepository.existsByMdmId(request.getMdmId())) {
            throw new ApiException(ApiError.CLIENT_MDM_ID_ALREADY_EXISTS);
        }

        Client client = clientMapper.toClient(request);
        client.setStatus(ClientStatus.ACTIVE);

        return clientMapper.toClientResponse(clientRepository.save(client));
    }

    private Client getRequiredClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ApiException(ApiError.CLIENT_NOT_FOUND));
    }
}
