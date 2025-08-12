package com.simon.code_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.simon.code_lab.dto.TodoListDto;
import com.simon.code_lab.exception.AccessDeniedException;
import com.simon.code_lab.exception.TodoListNotFoundException;
import com.simon.code_lab.exception.UserNotFoundException;
import com.simon.code_lab.model.TodoList;
import com.simon.code_lab.model.User;
import com.simon.code_lab.repository.TodoListRepository;
import com.simon.code_lab.repository.UserRepository;
import com.simon.code_lab.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class TodoListServiceTest {

    @Mock
    private TodoListRepository todoListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoListService todoListService;

    private User user, newMember, notOwner;
    private TodoList list1, list2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        newMember = new User();
        newMember.setId(2L);
        newMember.setEmail("newmember@example.com");

        notOwner = new User();
        notOwner.setId(3L);
        notOwner.setEmail("notowner@example.com");

        list1 = new TodoList();
        list1.setId(10L);
        list1.setTitle("List 1");
        list1.setOwner(user);
        list1.getMembers().add(user);

        list2 = new TodoList();
        list2.setId(20L);
        list2.setTitle("List 2");
        list2.setOwner(user);
        list2.getMembers().add(user);
        list2.getMembers().add(newMember);
    }

    @Test
    void createTodoList_shouldCreateListWithCurrentUserAsOwnerAndMember() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            ArgumentCaptor<TodoList> todoListCaptor = ArgumentCaptor.forClass(TodoList.class);
            when(todoListRepository.save(todoListCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            String title = "Nouvelle liste";

            TodoListDto result = todoListService.createTodoList(title);

            TodoList savedList = todoListCaptor.getValue();

            assertEquals(title, savedList.getTitle());
            assertEquals(user, savedList.getOwner());
            assertTrue(savedList.getMembers().contains(user));

            assertEquals(savedList.getId(), result.getId());
            assertEquals(savedList.getTitle(), result.getTitle());
        }
    }

    @Test
    void createTodoList_shouldThrowUserNotFoundException_whenUserNotFound() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn("unknown@example.com");
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> todoListService.createTodoList("Titre"));
            verify(todoListRepository, never()).save(any());
        }
    }

    @Test
    void getTodoListsForCurrentUser_shouldReturnTodoListsForUser() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            List<TodoList> lists = List.of(list1, list2);
            when(todoListRepository.findByMember(user)).thenReturn(lists);

            List<TodoListDto> result = todoListService.getTodoListsForCurrentUser();

            assertEquals(2, result.size());
            assertTrue(result.stream()
                    .anyMatch(dto -> dto.getId().equals(list1.getId()) && dto.getTitle().equals(list1.getTitle())));
            assertTrue(result.stream()
                    .anyMatch(dto -> dto.getId().equals(list2.getId()) && dto.getTitle().equals(list2.getTitle())));
        }
    }

    @Test
    void getTodoListsForCurrentUser_shouldThrowUserNotFoundException_whenUserNotFound() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn("unknown@example.com");
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> todoListService.getTodoListsForCurrentUser());
            verify(todoListRepository, never()).findByMember(any());
        }
    }

    @Test
    void getTodoListById_shouldReturnTodoListDto_whenUserIsMember() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));

            TodoListDto result = todoListService.getTodoListById(list1.getId());

            assertNotNull(result);
            assertEquals(list1.getId(), result.getId());
            assertEquals(list1.getTitle(), result.getTitle());
        }
    }

    @Test
    void getTodoListById_shouldThrowTodoListNotFoundException_whenListNotFound() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.empty());

            assertThrows(TodoListNotFoundException.class, () -> todoListService.getTodoListById(list1.getId()));
        }
    }

    @Test
    void getTodoListById_shouldThrowAccessDeniedException_whenUserIsNotMember() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        TodoList listWithoutUser = new TodoList();
        listWithoutUser.setId(list1.getId());
        listWithoutUser.setTitle("Other List");
        listWithoutUser.setOwner(otherUser);
        listWithoutUser.getMembers().add(otherUser);

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(listWithoutUser));

            assertThrows(AccessDeniedException.class, () -> todoListService.getTodoListById(list1.getId()));
        }
    }

    @Test
    void addMember_shouldAddNewMember_whenRequesterIsOwnerAndMemberNotPresent() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));
            when(userRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.of(newMember));
            when(todoListRepository.save(any(TodoList.class))).thenAnswer(i -> i.getArguments()[0]);

            TodoListDto result = todoListService.addMember(list1.getId(), newMember.getEmail());

            assertNotNull(result);
            assertTrue(list1.getMembers().contains(newMember));
            assertEquals(list1.getId(), result.getId());
        }
    }

    @Test
    void addMember_shouldThrowAccessDeniedException_whenRequesterIsNotOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(notOwner.getEmail());

            when(userRepository.findByEmail(notOwner.getEmail())).thenReturn(Optional.of(notOwner));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> todoListService.addMember(list1.getId(), newMember.getEmail()));

            assertEquals("Only the owner can add members", exception.getMessage());
        }
    }

    @Test
    void addMember_shouldThrowUserNotFoundException_whenNewMemberNotFound() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));
            when(userRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> todoListService.addMember(list1.getId(), newMember.getEmail()));
        }
    }

    @Test
    void addMember_shouldThrowRuntimeException_whenUserAlreadyMember() {
        list1.getMembers().add(newMember);

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));
            when(userRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.of(newMember));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> todoListService.addMember(list1.getId(), newMember.getEmail()));

            assertEquals("User already a member", exception.getMessage());
        }
    }

    @Test
    void removeMember_shouldRemoveMember_whenRequesterIsOwnerAndMemberIsPresent() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list2.getId())).thenReturn(Optional.of(list2));
            when(userRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.of(newMember));
            when(todoListRepository.save(any(TodoList.class))).thenAnswer(i -> i.getArguments()[0]);

            TodoListDto result = todoListService.removeMember(list2.getId(), newMember.getEmail());

            assertNotNull(result);
            assertFalse(list2.getMembers().contains(newMember));
            assertEquals(list2.getId(), result.getId());
        }
    }

    @Test
    void removeMember_shouldThrowAccessDeniedException_whenRequesterIsNotOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(notOwner.getEmail());

            when(userRepository.findByEmail(notOwner.getEmail())).thenReturn(Optional.of(notOwner));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> todoListService.removeMember(list1.getId(), newMember.getEmail()));

            assertEquals("Only the owner can remove members", exception.getMessage());
        }
    }

    @Test
    void removeMember_shouldThrowUserNotFoundException_whenMemberToRemoveNotFound() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));
            when(userRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> todoListService.removeMember(list1.getId(), newMember.getEmail()));
        }
    }

    @Test
    void removeMember_shouldThrowAccessDeniedException_whenMemberNotInList() {
        User notMember = new User();
        notMember.setId(4L);
        notMember.setEmail("notmember@example.com");

        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));
            when(userRepository.findByEmail(notMember.getEmail())).thenReturn(Optional.of(notMember));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> todoListService.removeMember(list1.getId(), notMember.getEmail()));

            assertEquals("User is not a member of this list", exception.getMessage());
        }
    }

    @Test
    void removeMember_shouldThrowAccessDeniedException_whenTryingToRemoveOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> todoListService.removeMember(list1.getId(), user.getEmail()));

            assertEquals("Owner cannot be removed", exception.getMessage());
        }
    }

    @Test
    void updateTitle_shouldUpdateTitle_whenRequesterIsOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));
            when(todoListRepository.save(any(TodoList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            String newTitle = "New Title";

            TodoListDto result = todoListService.updateTitle(list1.getId(), newTitle);

            assertNotNull(result);
            assertEquals(newTitle, list1.getTitle());
            assertEquals(list1.getId(), result.getId());
        }
    }

    @Test
    void updateTitle_shouldThrowAccessDeniedException_whenRequesterIsNotOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(newMember.getEmail());

            when(userRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.of(newMember));
            when(todoListRepository.findById(list2.getId())).thenReturn(Optional.of(list2));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> todoListService.updateTitle(list2.getId(), "New Title"));

            assertEquals("Only the owner can update the title", exception.getMessage());
        }
    }

    @Test
    void updateTitle_shouldThrowTodoListNotFoundException_whenListDoesNotExist() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.empty());

            assertThrows(TodoListNotFoundException.class,
                () -> todoListService.updateTitle(list1.getId(), "New Title"));
        }
    }

    @Test
    void deleteTodoList_shouldDelete_whenRequesterIsOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));

            todoListService.deleteTodoList(list1.getId());

            verify(todoListRepository, times(1)).delete(list1);
        }
    }

    @Test
    void deleteTodoList_shouldThrowAccessDeniedException_whenRequesterIsNotOwner() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(notOwner.getEmail());

            when(userRepository.findByEmail(notOwner.getEmail())).thenReturn(Optional.of(notOwner));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.of(list1));

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> todoListService.deleteTodoList(list1.getId()));

            assertEquals("Only the owner can delete the list", exception.getMessage());

            verify(todoListRepository, never()).delete(any());
        }
    }

    @Test
    void deleteTodoList_shouldThrowTodoListNotFoundException_whenListDoesNotExist() {
        try (MockedStatic<SecurityUtil> utilities = Mockito.mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getAuthenticatedEmail).thenReturn(user.getEmail());

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(todoListRepository.findById(list1.getId())).thenReturn(Optional.empty());

            assertThrows(TodoListNotFoundException.class,
                () -> todoListService.deleteTodoList(list1.getId()));

            verify(todoListRepository, never()).delete(any());
        }
    }
}
