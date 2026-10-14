package com.vit.parkingappeal.service;

/** Raised when a reviewer action is not allowed in the appeal's current status. */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}
