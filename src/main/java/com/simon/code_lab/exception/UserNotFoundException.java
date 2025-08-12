package com.simon.code_lab.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(String email) {
        super("User not found with email " + email);
    }
}