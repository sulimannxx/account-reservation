package org.example.accountreservation.mapper;

import org.example.accountreservation.entity.Client;
import org.example.accountreservation.enums.ClientStatus;
import org.example.accountreservation.generated.model.ClientDetailsResponse;
import org.example.accountreservation.generated.model.ClientExistsResponse;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchItemResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.PageableResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class ClientMapper {

    public ClientResponse toClientResponse(Client client) {
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

    public ClientDetailsResponse toClientDetailsResponse(Client client) {
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

    public ClientExistsResponse toClientExistsResponse(Client client) {
        return new ClientExistsResponse(true, client.getId()).status(toApiStatus(client.getStatus()));
    }

    public ClientExistsResponse toClientNotExistsResponse(UUID clientId) {
        return new ClientExistsResponse(false, clientId);
    }

    public ClientSearchResponse toClientSearchResponse(Page<Client> clients) {
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

    private org.example.accountreservation.generated.model.ClientStatus toApiStatus(ClientStatus status) {
        return status == null
                ? null
                : org.example.accountreservation.generated.model.ClientStatus.fromValue(status.name());
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
