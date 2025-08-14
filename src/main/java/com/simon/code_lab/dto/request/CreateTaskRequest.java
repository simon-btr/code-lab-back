package com.simon.code_lab.dto.request;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private Long listId;
    private String title;
    private String description;
}
