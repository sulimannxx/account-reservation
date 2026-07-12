package org.example.accountreservation.mapper;

import org.example.accountreservation.entity.Client;
import org.example.accountreservation.enums.ClientStatus;
import org.example.accountreservation.generated.model.ClientDetailsResponse;
import org.example.accountreservation.generated.model.ClientExistsResponse;
import org.example.accountreservation.generated.model.ClientResponse;
import org.example.accountreservation.generated.model.ClientSearchItemResponse;
import org.example.accountreservation.generated.model.ClientSearchResponse;
import org.example.accountreservation.generated.model.CreateClientRequest;
import org.example.accountreservation.generated.model.PageableResponse;
import org.example.accountreservation.generated.model.UpdateClientRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse toClientResponse(Client client);

    @Mapping(target = "hasAccounts", constant = "false")
    ClientDetailsResponse toClientDetailsResponse(Client client);

    @Mapping(target = "exists", constant = "true")
    @Mapping(target = "clientId", source = "id")
    ClientExistsResponse toClientExistsResponse(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toClient(CreateClientRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "middleName", source = "middleName")
    void updateClient(UpdateClientRequest request, @MappingTarget Client client);

    List<ClientSearchItemResponse> toClientSearchItemResponses(List<Client> clients);

    default ClientExistsResponse toClientNotExistsResponse(UUID clientId) {
        return new ClientExistsResponse(false, clientId);
    }

    default ClientSearchResponse toClientSearchResponse(org.springframework.data.domain.Page<Client> clients) {
        PageableResponse pageable = new PageableResponse(
                clients.getNumber(),
                clients.getSize(),
                clients.getTotalPages(),
                clients.getTotalElements()
        );

        return new ClientSearchResponse(toClientSearchItemResponses(clients.getContent()), pageable);
    }

    default org.example.accountreservation.generated.model.ClientStatus toApiStatus(ClientStatus status) {
        return status == null
                ? null
                : org.example.accountreservation.generated.model.ClientStatus.fromValue(status.name());
    }

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
