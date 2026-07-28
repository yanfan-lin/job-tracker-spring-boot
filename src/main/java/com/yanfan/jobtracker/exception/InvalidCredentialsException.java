package com.yanfan.jobtracker.exception;

// Represent a login attempt with invalid credentials
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

}
