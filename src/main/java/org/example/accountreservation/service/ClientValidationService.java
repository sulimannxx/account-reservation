package org.example.accountreservation.service;

import org.example.accountreservation.exception.ApiException;
import org.example.accountreservation.generated.model.CreateClientRequest;
import org.example.accountreservation.generated.model.ErrorCode;
import org.example.accountreservation.generated.model.UpdateClientRequest;
import org.springframework.stereotype.Service;

@Service
public class ClientValidationService {

    public void validateCreateRequest(CreateClientRequest request) {
        if (request == null
                || request.getMdmId() == null
                || request.getMdmId() < 1
                || hasInvalidName(request.getFirstName(), request.getLastName(), request.getMiddleName())) {
            throw new ApiException(ErrorCode.INVALID_CLIENT_DATA);
        }
    }

    public void validateUpdateRequest(UpdateClientRequest request) {
        if (request == null || hasInvalidName(request.getFirstName(), request.getLastName(), request.getMiddleName())) {
            throw new ApiException(ErrorCode.INVALID_CLIENT_DATA);
        }
    }

    public void validatePageRequest(Integer page, Integer size) {
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
}
