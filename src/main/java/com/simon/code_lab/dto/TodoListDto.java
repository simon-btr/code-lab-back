package com.simon.code_lab.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TodoListDto {
    private Long id;
    private String title;
    private UserDto owner;
    private List<UserDto> members;
    private List<TaskDto> tasks;
}