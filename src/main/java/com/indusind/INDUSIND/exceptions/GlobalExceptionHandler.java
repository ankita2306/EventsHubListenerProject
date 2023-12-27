package com.indusind.INDUSIND.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CouchbaseQueryException.class)
    public ResponseEntity<Object> handleCouchbaseQueryException(CouchbaseQueryException ex) {
        logger.error("Exception : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("Exception", ex.getMessage()));
    }

    // Example for handling generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        // Customize the response for generic exceptions
        return new ResponseEntity<>("An error occurred, please try again later", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

