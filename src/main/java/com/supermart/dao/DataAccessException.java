package com.supermart.dao;

/** Unchecked wrapper so the service/web layers never handle raw {@code SQLException}. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
