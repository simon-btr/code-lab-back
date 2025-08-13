package com.simon.code_lab.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.simon.code_lab.dto.TodoListDto;
import com.simon.code_lab.exception.AccessDeniedException;
import com.simon.code_lab.model.User;
import com.simon.code_lab.repository.TodoListRepository;
import com.simon.code_lab.repository.UserRepository;
import com.simon.code_lab.service.TodoListService;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TodoListSteps {

    @Autowired
    private TodoListService todoListService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    private TodoListDto createdList;
    private TodoListDto updatedList;
    private String currentUserEmail;
    private Exception caughtException;

    @Given("a logged in user with email {string}")
    public void a_logged_in_user_with_email(String email) {
        currentUserEmail = email;

        userRepository.save(new User("username", email, "password"));

        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(() -> "ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Given("another user exists with email {string}")
    public void another_user_exists_with_email(String email) {
        userRepository.save(new User("username2", email, "test"));
    }

    @Given("the current user is switched to {string}")
    public void the_current_user_is_switched_to(String email) {
        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(() -> "ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        currentUserEmail = email;
    }

    @When("they create a todo list with the title {string}")
    public void they_create_a_todo_list_with_the_title(String title) {
        createdList = todoListService.createTodoList(title);
    }

    @When("they add the user {string} to the todo list")
    public void they_add_the_user_to_the_todo_list(String email) {
        updatedList = todoListService.addMember(createdList.getId(), email);
    }

    @When("they remove member with email {string} from the todo list")
    public void remove_member_from_todo_list(String memberEmail) {
        createdList = todoListService.removeMember(createdList.getId(), memberEmail);
    }

    @When("they update the todo list title to {string}")
    public void they_update_the_todo_list_title_to(String newTitle) {
        createdList = todoListService.updateTitle(createdList.getId(), newTitle);
    }

    @When("they delete the todo list")
    public void they_delete_the_todo_list() {
        todoListService.deleteTodoList(createdList.getId());
        createdList = null;
    }

    @When("they try to delete the todo list")
    public void they_try_to_delete_the_todo_list() {
        caughtException = assertThrows(
                AccessDeniedException.class,
                () -> todoListService.deleteTodoList(createdList.getId()));
    }

    @Then("the todo list {string} is created for this user")
    public void the_todo_list_is_created_for_this_user(String title) {
        assertNotNull(createdList);
        assertEquals(title, createdList.getTitle());
        assertTrue(createdList.getMembers().stream()
                .anyMatch(u -> u.getEmail().equals(currentUserEmail)));
    }

    @Then("the todo list {string} should include {string} as a member")
    public void todo_list_should_include_member(String title, String email) {
        assertEquals(title, updatedList.getTitle());
        assertTrue(updatedList.getMembers().stream()
                .anyMatch(u -> u.getEmail().equals(email)));
    }

    @Then("the todo list should not include {string} as a member")
    public void todo_list_should_not_include_member(String memberEmail) {
        assertTrue(createdList.getMembers().stream()
                .noneMatch(u -> u.getEmail().equals(memberEmail)));
    }

    @Then("the todo list title should be {string}")
    public void the_todo_list_title_should_be(String expectedTitle) {
        assertEquals(expectedTitle, createdList.getTitle());
    }

    @Then("the todo list should no longer exist")
    public void the_todo_list_should_no_longer_exist() {
        assertTrue(todoListRepository.findById(createdList != null ? createdList.getId() : -1L).isEmpty());
    }

    @Then("an access denied error should be thrown")
    public void an_access_denied_error_should_be_thrown() {
        assertNotNull(caughtException);
        assertTrue(caughtException.getMessage().contains("Only the owner"));
    }

    @After
    public void cleanDatabase() {
        todoListRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }
}