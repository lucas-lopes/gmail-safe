package com.axcient.gmailsafe.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StandardError> throwUnauthorizedException(UnauthorizedException e) {
        StandardError err = buildStandardError(HttpStatus.UNAUTHORIZED, e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    private StandardError buildStandardError(HttpStatus httpStatus, RuntimeException e) {
        return StandardError.builder()
            .status(httpStatus.value())
            .message(e.getMessage())
            .error(httpStatus.name())
            .build();
    }

}
