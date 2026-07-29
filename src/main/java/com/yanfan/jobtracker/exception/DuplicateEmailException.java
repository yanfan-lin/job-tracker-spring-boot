package com.yanfan.jobtracker.exception;

// Represent an attempt to register an existing email address
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

}
