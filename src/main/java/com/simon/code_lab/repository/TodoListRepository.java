package com.simon.code_lab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.simon.code_lab.model.TodoList;
import com.simon.code_lab.model.User;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long> {
    List<TodoList> findByOwner(User owner);

    @Query("SELECT l FROM TodoList l JOIN l.members m WHERE m = :user")
    List<TodoList> findByMember(@Param("user") User user);
}
