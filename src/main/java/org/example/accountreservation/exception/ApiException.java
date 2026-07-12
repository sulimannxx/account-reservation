package org.example.accountreservation.exception;

import org.example.accountreservation.generated.model.ErrorCode;

public class ApiException extends RuntimeException {

    private final ApiError apiError;

    public ApiException(ApiError apiError) {
        super(apiError.getErrorCode().getValue());
        this.apiError = apiError;
    }

    public ApiException(ErrorCode errorCode) {
        this(ApiError.fromErrorCode(errorCode));
    }

    public ErrorCode getErrorCode() {
        return apiError.getErrorCode();
    }

    public ApiError getApiErrorCode() {
        return apiError;
    }
}
