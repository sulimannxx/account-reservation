package org.example.accountreservation.exception;

import lombok.Getter;
import org.example.accountreservation.generated.model.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
public enum ApiError {
    INVALID_CLIENT_DATA(ErrorCode.INVALID_CLIENT_DATA, HttpStatus.BAD_REQUEST, "Invalid client data"),
    CLIENT_MDM_ID_ALREADY_EXISTS(
            ErrorCode.CLIENT_MDM_ID_ALREADY_EXISTS,
            HttpStatus.CONFLICT,
            "Client with this mdmId already exists"
    ),
    CLIENT_NOT_FOUND(ErrorCode.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Client not found"),
    CLIENT_HAS_ACTIVE_ACCOUNTS(
            ErrorCode.CLIENT_HAS_ACTIVE_ACCOUNTS,
            HttpStatus.CONFLICT,
            "Client has active accounts"
    ),
    INTERNAL_SERVER_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error");

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final String description;

    ApiError(ErrorCode errorCode, HttpStatus httpStatus, String description) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.description = description;
    }

    public static ApiError fromErrorCode(ErrorCode errorCode) {
        return Arrays.stream(values())
                .filter(apiErrorCode -> apiErrorCode.errorCode == errorCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported error code: " + errorCode));
    }
}
