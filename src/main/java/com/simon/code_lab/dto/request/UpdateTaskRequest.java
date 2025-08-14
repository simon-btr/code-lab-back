package com.simon.code_lab.dto.request;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private boolean completed;
}