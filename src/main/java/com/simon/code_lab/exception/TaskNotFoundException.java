package com.simon.code_lab.exception;

import jakarta.persistence.EntityNotFoundException;

public class TaskNotFoundException extends EntityNotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task not found with id " + id);
    }
}