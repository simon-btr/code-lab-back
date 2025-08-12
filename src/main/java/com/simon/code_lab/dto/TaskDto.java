package com.simon.code_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
}
