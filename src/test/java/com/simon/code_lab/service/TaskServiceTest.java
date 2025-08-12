package com.simon.code_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.simon.code_lab.dto.TaskDto;
import com.simon.code_lab.exception.AccessDeniedException;
import com.simon.code_lab.exception.TaskNotFoundException;
import com.simon.code_lab.exception.TodoListNotFoundException;
import com.simon.code_lab.model.Task;
import com.simon.code_lab.model.TodoList;
import com.simon.code_lab.model.User;
import com.simon.code_lab.repository.TaskRepository;
import com.simon.code_lab.repository.TodoListRepository;
import com.simon.code_lab.repository.UserRepository;
import com.simon.code_lab.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TodoListRepository todoListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Mock
    private static SecurityUtil securityUtilMock;

    private User user;
    private TodoList todoList;
    private Task task1, task2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        todoList = new TodoList();
        todoList.setId(10L);
        todoList.setTitle("My TodoList");
        todoList.getMembers().add(user);

        task1 = new Task();
        task1.setId(100L);
        task1.setTitle("Task 1");
        task1.setDescription("desc1");
        task1.setCompleted(false);
        task1.setTodoList(todoList);

        task2 = new Task();
        task2.setId(101L);
        task2.setTitle("Task 2");
        task2.setDescription("desc2");
        task2.setCompleted(false);
        task2.setTodoList(todoList);
    }

    @Test
    void addTask_shouldCreateTask_whenUserIsMember() {
        // Mock SecurityUtil static method to return current email
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(todoList.getId())).thenReturn(Optional.of(todoList));

            when(taskRepository.save(any(Task.class))).thenReturn(task1);

            TaskDto result = taskService.addTask(todoList.getId(), "Task 1", "desc1");

            assertNotNull(result);
            assertEquals("Task 1", result.getTitle());
            assertEquals("desc1", result.getDescription());

            verify(taskRepository).save(any(Task.class));
        }
    }

    @Test
    void addTask_shouldThrowAccessDenied_whenUserNotMember() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(otherUser.getEmail());

            when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
            when(todoListRepository.findById(todoList.getId())).thenReturn(Optional.of(todoList));

            AccessDeniedException thrown = assertThrows(AccessDeniedException.class,
                    () -> taskService.addTask(todoList.getId(), "Titre", "desc"));

            assertEquals("User is not a member of this list", thrown.getMessage());
            verify(taskRepository, never()).save(any());
        }
    }

    @Test
    void addTask_shouldThrowTodoListNotFoundException_whenTodoListDoesNotExist() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(anyLong())).thenReturn(Optional.empty());

            TodoListNotFoundException exception = assertThrows(TodoListNotFoundException.class,
                    () -> taskService.addTask(999L, "Titre", "desc"));

            assertEquals("Todo list not found with id 999", exception.getMessage());
            verify(taskRepository, never()).save(any());
        }
    }

    @Test
    void getTasksForList_shouldReturnTasks_whenUserIsMember() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(todoList.getId())).thenReturn(Optional.of(todoList));
            when(taskRepository.findByTodoList(todoList)).thenReturn(List.of(task1, task2));

            List<TaskDto> result = taskService.getTasksForList(todoList.getId());

            assertEquals(2, result.size());
            assertEquals("Task 1", result.get(0).getTitle());
            verify(taskRepository).findByTodoList(todoList);
        }
    }

    @Test
    void getTasksForList_shouldThrowTodoListNotFoundException_whenListDoesNotExist() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(TodoListNotFoundException.class, () -> taskService.getTasksForList(999L));

            verify(taskRepository, never()).findByTodoList(any());
        }
    }

    @Test
    void getTasksForList_shouldThrowAccessDeniedException_whenUserIsNotMember() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@example.com");

        todoList.setMembers(new HashSet<>(List.of(anotherUser)));

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(todoList.getId())).thenReturn(Optional.of(todoList));

            assertThrows(AccessDeniedException.class, () -> taskService.getTasksForList(todoList.getId()));

            verify(taskRepository, never()).findByTodoList(any());
        }
    }

    @Test
    void updateTask_shouldReturnUpdatedTask_whenUserIsMember() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TaskDto updated = taskService.updateTask(task1.getId(), "Nouveau titre", "Nouvelle description", true);

            assertEquals("Nouveau titre", updated.getTitle());
            assertEquals("Nouvelle description", updated.getDescription());
            assertTrue(updated.isCompleted());

            verify(taskRepository).save(task1);
        }
    }

    @Test
    void updateTask_shouldThrowTaskNotFoundException_whenTaskDoesNotExist() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(999L, "Titre", "Desc", false));

            verify(taskRepository, never()).save(any());
        }
    }

    @Test
    void updateTask_shouldThrowAccessDeniedException_whenUserIsNotMember() {
        User autreUser = new User();
        autreUser.setId(2L);
        autreUser.setEmail("other@example.com");

        todoList.setMembers(new HashSet<>(List.of(autreUser))); // l'utilisateur actuel n'est pas membre

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));

            assertThrows(AccessDeniedException.class,
                    () -> taskService.updateTask(task1.getId(), "Titre", "Desc", false));

            verify(taskRepository, never()).save(any());
        }
    }

    @Test
    void deleteTask_shouldDelete_whenUserIsMember() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));

            taskService.deleteTask(task1.getId());

            verify(taskRepository).delete(task1);
        }
    }

    @Test
    void deleteTask_shouldThrowTaskNotFoundException_whenTaskDoesNotExist() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(999L));

            verify(taskRepository, never()).delete(any());
        }
    }

    @Test
    void deleteTask_shouldThrowAccessDeniedException_whenUserIsNotMember() {
        User autreUser = new User();
        autreUser.setId(2L);
        autreUser.setEmail("other@example.com");

        todoList.setMembers(new HashSet<>(List.of(autreUser))); // user actuel pas membre

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));

            assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(task1.getId()));

            verify(taskRepository, never()).delete(any());
        }
    }
}