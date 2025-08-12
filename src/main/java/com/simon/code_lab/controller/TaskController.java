package com.simon.code_lab.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.simon.code_lab.dto.TaskDto;
import com.simon.code_lab.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @RequestParam Long listId,
            @RequestParam String title,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(taskService.addTask(listId, title, description));
    }


    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(@RequestParam Long listId) {
        return ResponseEntity.ok(taskService.getTasksForList(listId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam boolean completed) {
        return ResponseEntity.ok(taskService.updateTask(taskId, title, description, completed));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
