package com.simon.code_lab.exception;

import jakarta.persistence.EntityNotFoundException;

public class TodoListNotFoundException extends EntityNotFoundException {
    public TodoListNotFoundException(Long id) {
        super("Todo list not found with id " + id);
    }
}