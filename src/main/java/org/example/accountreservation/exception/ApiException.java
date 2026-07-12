package org.example.accountreservation.exception;

import lombok.Getter;
import org.example.accountreservation.generated.model.ErrorCode;

public class ApiException extends RuntimeException {

    @Getter
    private final ApiError apiErrorCode;

    public ApiException(ApiError apiErrorCode) {
        super(apiErrorCode.getErrorCode().getValue());
        this.apiErrorCode = apiErrorCode;
    }

    public ApiException(ErrorCode errorCode) {
        this(ApiError.fromErrorCode(errorCode));
    }

    public ErrorCode getErrorCode() {
        return apiErrorCode.getErrorCode();
    }
}
