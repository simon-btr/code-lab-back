package com.simon.code_lab.dto.mapper;

import java.util.stream.Collectors;

import com.simon.code_lab.dto.TaskDto;
import com.simon.code_lab.dto.TodoListDto;
import com.simon.code_lab.dto.UserDto;
import com.simon.code_lab.model.Task;
import com.simon.code_lab.model.TodoList;
import com.simon.code_lab.model.User;

public class TodoListMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    public static TaskDto toTaskDto(Task task) {
        return new TaskDto(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted());
    }

    public static TodoListDto toTodoListDto(TodoList list) {
        return new TodoListDto(
                list.getId(),
                list.getTitle(),
                toUserDto(list.getOwner()),
                list.getMembers().stream().map(TodoListMapper::toUserDto).collect(Collectors.toList()),
                list.getTasks().stream().map(TodoListMapper::toTaskDto).collect(Collectors.toList())
        );
    }
}
