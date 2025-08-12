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

import com.simon.code_lab.dto.TodoListDto;
import com.simon.code_lab.service.TodoListService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/todolists")
@RequiredArgsConstructor
public class TodoListController {
    private final TodoListService todoListService;

    @PostMapping
    public ResponseEntity<TodoListDto> createTodoList(@RequestParam String title) {
        return ResponseEntity.ok(todoListService.createTodoList(title));
    }

    @GetMapping
    public ResponseEntity<List<TodoListDto>> getTodoLists() {
        return ResponseEntity.ok(todoListService.getTodoListsForCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoListDto> getTodoListById(@PathVariable Long id) {
        return ResponseEntity.ok(todoListService.getTodoListById(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<TodoListDto> addMember(
            @PathVariable Long id,
            @RequestParam String memberEmail) {
        return ResponseEntity.ok(todoListService.addMember(id, memberEmail));
    }

    @DeleteMapping("/{id}/members/{memberEmail}")
    public ResponseEntity<TodoListDto> removeMember(
            @PathVariable Long id,
            @PathVariable String memberEmail) {
        return ResponseEntity.ok(todoListService.removeMember(id, memberEmail));
    }
    
    @PutMapping("/{id}/title")
    public ResponseEntity<TodoListDto> updateTitle(
            @PathVariable Long id,
            @RequestParam String title) {
        return ResponseEntity.ok(todoListService.updateTitle(id, title));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable Long id) {
        todoListService.deleteTodoList(id);
        return ResponseEntity.noContent().build();
    }
}
