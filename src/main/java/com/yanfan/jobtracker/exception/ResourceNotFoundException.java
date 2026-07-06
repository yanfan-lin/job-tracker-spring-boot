package com.yanfan.jobtracker.exception;

// Custom exception used when a requested database record does not exist.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
