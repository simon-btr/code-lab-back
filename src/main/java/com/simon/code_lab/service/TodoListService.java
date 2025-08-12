package com.simon.code_lab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.simon.code_lab.dto.TodoListDto;
import com.simon.code_lab.dto.mapper.TodoListMapper;
import com.simon.code_lab.exception.AccessDeniedException;
import com.simon.code_lab.exception.TodoListNotFoundException;
import com.simon.code_lab.exception.UserNotFoundException;
import com.simon.code_lab.model.TodoList;
import com.simon.code_lab.model.User;
import com.simon.code_lab.repository.TodoListRepository;
import com.simon.code_lab.repository.UserRepository;
import com.simon.code_lab.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final UserRepository userRepository;

    private User getCurrentUserOrThrow() {
        String email = SecurityUtil.getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private TodoList getListIfMember(Long listId, User user) {
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new TodoListNotFoundException(listId));

        if (!list.getMembers().contains(user)) {
            throw new AccessDeniedException("User is not a member of this list");
        }
        return list;
    }

    public TodoListDto createTodoList(String title) {
        User owner = getCurrentUserOrThrow();

        TodoList list = new TodoList();
        list.setTitle(title);
        list.setOwner(owner);
        list.getMembers().add(owner);

        return TodoListMapper.toTodoListDto(todoListRepository.save(list));
    }

    public List<TodoListDto> getTodoListsForCurrentUser() {
        User user = getCurrentUserOrThrow();
        return todoListRepository.findByMember(user)
                .stream()
                .map(TodoListMapper::toTodoListDto)
                .collect(Collectors.toList());
    }

    public TodoListDto getTodoListById(Long id) {
        User user = getCurrentUserOrThrow();
        return TodoListMapper.toTodoListDto(getListIfMember(id, user));
    }

    public TodoListDto addMember(Long listId, String memberEmailToAdd) {
        User requester = getCurrentUserOrThrow();
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new TodoListNotFoundException(listId));

        if (!list.getOwner().equals(requester)) {
            throw new AccessDeniedException("Only the owner can add members");
        }

        User newMember = userRepository.findByEmail(memberEmailToAdd)
                .orElseThrow(() -> new UserNotFoundException(memberEmailToAdd));

        if (list.getMembers().contains(newMember)) {
            throw new RuntimeException("User already a member");
        }

        list.getMembers().add(newMember);
        return TodoListMapper.toTodoListDto(todoListRepository.save(list));
    }

    public TodoListDto removeMember(Long listId, String memberEmailToRemove) {
        User requester = getCurrentUserOrThrow();
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new TodoListNotFoundException(listId));

        if (!list.getOwner().equals(requester)) {
            throw new AccessDeniedException("Only the owner can remove members");
        }

        User member = userRepository.findByEmail(memberEmailToRemove)
                .orElseThrow(() -> new UserNotFoundException(memberEmailToRemove));

        if (!list.getMembers().contains(member)) {
            throw new AccessDeniedException("User is not a member of this list");
        }

        if (list.getOwner().equals(member)) {
            throw new AccessDeniedException("Owner cannot be removed");
        }

        list.getMembers().remove(member);
        return TodoListMapper.toTodoListDto(todoListRepository.save(list));
    }

    public TodoListDto updateTitle(Long listId, String newTitle) {
        User requester = getCurrentUserOrThrow();
        TodoList list = getListIfMember(listId, requester);

        if (!list.getOwner().equals(requester)) {
            throw new AccessDeniedException("Only the owner can update the title");
        }

        list.setTitle(newTitle);
        return TodoListMapper.toTodoListDto(todoListRepository.save(list));
    }

    public void deleteTodoList(Long listId) {
        User requester = getCurrentUserOrThrow();
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new TodoListNotFoundException(listId));

        if (!list.getOwner().equals(requester)) {
            throw new AccessDeniedException("Only the owner can delete the list");
        }

        todoListRepository.delete(list);
    }
}
