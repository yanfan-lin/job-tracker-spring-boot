package com.yanfan.jobtracker.exception;

// exception used when an email address is already registered
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

}
