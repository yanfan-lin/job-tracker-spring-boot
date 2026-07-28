package com.yanfan.jobtracker.exception;

// Represent a requested record that cannot be found
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
