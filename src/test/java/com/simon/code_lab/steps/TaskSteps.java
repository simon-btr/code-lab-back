package com.simon.code_lab.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.simon.code_lab.dto.TaskDto;
import com.simon.code_lab.dto.TodoListDto;
import com.simon.code_lab.model.User;
import com.simon.code_lab.repository.TaskRepository;
import com.simon.code_lab.repository.TodoListRepository;
import com.simon.code_lab.repository.UserRepository;
import com.simon.code_lab.service.TaskService;
import com.simon.code_lab.service.TodoListService;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TaskSteps {

    @Autowired
    private TodoListService todoListService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TodoListDto createdList;
    private TaskDto createdTask;
    private List<TaskDto> taskList;

    @Given("a logged in user with the email {string}")
    public void a_logged_in_user_with_the_email(String email) {
        userRepository.save(new User("username_" + email, email, "password"));
        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(() -> "ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Given("another user exists with the email {string}")
    public void another_user_exists_with_the_email(String email) {
        userRepository.save(new User("username_" + email, email, "password"));
    }

    @Given("they create a todo list with title {string}")
    public void they_create_a_todo_list_with_title(String title) {
        createdList = todoListService.createTodoList(title);
    }

    @Given("they add user {string} to the todo list")
    public void they_add_user_to_the_todo_list(String email) {
        createdList = todoListService.addMember(createdList.getId(), email);
    }

    @When("they add a task with title {string} and description {string}")
    public void they_add_a_task_with_title_and_description(String title, String description) {
        createdTask = taskService.addTask(createdList.getId(), title, description);
    }

    @When("they retrieve all tasks for the todo list")
    public void they_retrieve_all_tasks_for_the_todo_list() {
        taskList = taskService.getTasksForList(createdList.getId());
    }

    @When("they update the task title to {string}, description to {string}, and mark it completed")
    public void they_update_the_task(String newTitle, String newDescription) {
        createdTask = taskService.updateTask(createdTask.getId(), newTitle, newDescription, true);
    }

    @When("they delete the task")
    public void they_delete_the_task() {
        taskService.deleteTask(createdTask.getId());
    }

    @Given("current user is switched to {string}")
    public void current_user_is_switched_to(String email) {
        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(() -> "ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        userRepository.save(new User("username_" + email, email, "password"));
    }

    @Then("the task list should contain a task titled {string}")
    public void the_task_list_should_contain_a_task_titled(String title) {
        boolean exists = taskRepository.findAll().stream()
                .anyMatch(t -> t.getTitle().equals(title));
        assertTrue(exists);
    }

    @Then("they should see a task titled {string}")
    public void they_should_see_a_task_titled(String title) {
        assertTrue(taskList.stream()
                .anyMatch(t -> t.getTitle().equals(title)));
    }

    @Then("the task should have title {string} and be completed")
    public void the_task_should_have_title_and_be_completed(String title) {
        assertEquals(title, createdTask.getTitle());
        assertTrue(createdTask.isCompleted());
    }

    @Then("the task list should not contain {string}")
    public void the_task_list_should_not_contain(String title) {
        boolean exists = taskRepository.findAll().stream()
                .anyMatch(t -> t.getTitle().equals(title));
        assertFalse(exists);
    }

    @After
    public void cleanDatabase() {
        taskRepository.deleteAll();
        todoListRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }
}
