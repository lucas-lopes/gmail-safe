package com.axcient.gmailsafe.controller.exception;

import com.axcient.gmailsafe.service.exception.AcceptedException;
import com.axcient.gmailsafe.service.exception.BadGatewayException;
import com.axcient.gmailsafe.service.exception.BadRequestException;
import com.axcient.gmailsafe.service.exception.FileException;
import com.axcient.gmailsafe.service.exception.ObjectNotFoundException;
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

    @ExceptionHandler(AcceptedException.class)
    public ResponseEntity<StandardError> throwAcceptException(AcceptedException e) {
        StandardError err = buildStandardError(HttpStatus.ACCEPTED, e);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(err);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardError> throwBadRequestException(BadRequestException e) {
        StandardError err = buildStandardError(HttpStatus.BAD_REQUEST, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> throwObjectNotFoundException(ObjectNotFoundException e) {
        StandardError err = buildStandardError(HttpStatus.NOT_FOUND, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<StandardError> throwUnauthorizedException(BadGatewayException e) {
        StandardError err = buildStandardError(HttpStatus.BAD_GATEWAY, e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(err);
    }

    private StandardError buildStandardError(HttpStatus httpStatus, RuntimeException e) {
        return StandardError.builder()
            .status(httpStatus.value())
            .message(e.getMessage())
            .error(httpStatus.name())
            .build();
    }

}
