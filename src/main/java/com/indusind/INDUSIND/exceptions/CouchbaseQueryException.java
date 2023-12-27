package com.indusind.INDUSIND.exceptions;

public class CouchbaseQueryException extends RuntimeException {
    public CouchbaseQueryException(String message, Throwable cause) {

        super(message, cause);
    }
}
