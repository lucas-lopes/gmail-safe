package com.axcient.gmailsafe.controller.exception;

import com.axcient.gmailsafe.service.exception.FileException;
import com.axcient.gmailsafe.service.exception.UnauthorizedException;
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

    @ExceptionHandler(FileException.class)
    public ResponseEntity<StandardError> throwConflictException(FileException e) {
        StandardError err = buildStandardError(HttpStatus.INTERNAL_SERVER_ERROR, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    private StandardError buildStandardError(HttpStatus httpStatus, RuntimeException e) {
        return StandardError.builder()
            .status(httpStatus.value())
            .message(e.getMessage())
            .error(httpStatus.name())
            .build();
    }

}
