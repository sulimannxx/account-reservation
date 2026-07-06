package org.example.accountreservation.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.accountreservation.generated.model.ErrorCode;
import org.example.accountreservation.generated.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        log.warn("Client API business error: {}", exception.getErrorCode());
        return errorResponse(exception.getErrorCode());
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception exception) {
        log.warn("Invalid client API request", exception);
        return errorResponse(ErrorCode.INVALID_CLIENT_DATA);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        log.error("Unexpected client API error", exception);
        return errorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> errorResponse(ErrorCode errorCode) {
        HttpStatus status = status(errorCode);
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(errorCode, description(errorCode), status.value()));
    }

    private HttpStatus status(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_CLIENT_DATA -> HttpStatus.BAD_REQUEST;
            case CLIENT_MDM_ID_ALREADY_EXISTS, CLIENT_HAS_ACTIVE_ACCOUNTS -> HttpStatus.CONFLICT;
            case CLIENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String description(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_CLIENT_DATA -> "Invalid client data";
            case CLIENT_MDM_ID_ALREADY_EXISTS -> "Client with this mdmId already exists";
            case CLIENT_NOT_FOUND -> "Client not found";
            case CLIENT_HAS_ACTIVE_ACCOUNTS -> "Client has active accounts";
            case INTERNAL_SERVER_ERROR -> "Unexpected internal error";
        };
    }
}
