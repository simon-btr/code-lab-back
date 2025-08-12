package com.simon.code_lab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.simon.code_lab.dto.TaskDto;
import com.simon.code_lab.dto.mapper.TodoListMapper;
import com.simon.code_lab.exception.AccessDeniedException;
import com.simon.code_lab.exception.TaskNotFoundException;
import com.simon.code_lab.exception.TodoListNotFoundException;
import com.simon.code_lab.exception.UserNotFoundException;
import com.simon.code_lab.model.Task;
import com.simon.code_lab.model.TodoList;
import com.simon.code_lab.model.User;
import com.simon.code_lab.repository.TaskRepository;
import com.simon.code_lab.repository.TodoListRepository;
import com.simon.code_lab.repository.UserRepository;
import com.simon.code_lab.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TodoListRepository todoListRepository;
    private final UserRepository userRepository;

    private User getCurrentUserOrThrow() {
        String email = SecurityUtil.getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private void ensureUserIsMember(TodoList list, User user) {
        if (!list.getMembers().contains(user)) {
            throw new AccessDeniedException("User is not a member of this list");
        }
    }

    public TaskDto addTask(Long todoListId, String title, String description) {
        User user = getCurrentUserOrThrow();
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new TodoListNotFoundException(todoListId));

        ensureUserIsMember(todoList, user);

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setTodoList(todoList);

        return TodoListMapper.toTaskDto(taskRepository.save(task));
    }

    public List<TaskDto> getTasksForList(Long todoListId) {
        User user = getCurrentUserOrThrow();
        TodoList todoList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new TodoListNotFoundException(todoListId));

        ensureUserIsMember(todoList, user);

        return taskRepository.findByTodoList(todoList)
                .stream()
                .map(TodoListMapper::toTaskDto)
                .collect(Collectors.toList());
    }

    public TaskDto updateTask(Long taskId, String title, String description, boolean completed) {
        User user = getCurrentUserOrThrow();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TodoList todoList = task.getTodoList();
        ensureUserIsMember(todoList, user);

        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);

        return TodoListMapper.toTaskDto(taskRepository.save(task));
    }

    public void deleteTask(Long taskId) {
        User user = getCurrentUserOrThrow();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TodoList todoList = task.getTodoList();
        ensureUserIsMember(todoList, user);

        taskRepository.delete(task);
    }
}
