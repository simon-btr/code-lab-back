package com.simon.code_lab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simon.code_lab.model.Task;
import com.simon.code_lab.model.TodoList;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTodoList(TodoList todoList);
}
