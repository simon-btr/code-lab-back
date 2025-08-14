Feature: Task Management
  As a user
  I want to manage tasks inside a todo list
  So that I can organize my work

  Background:
    Given a logged in user with the username "owner"
    And they create a todo list with title "Work"
    And another user exists with the username "member"
    And they add user "member@example.com" to the todo list

  Scenario: Add a new task
    When they add a task with title "Prepare report" and description "Due Friday"
    Then the task list should contain a task titled "Prepare report"

  Scenario: Get tasks for a list
    Given they add a task with title "Prepare report" and description "Due Friday"
    When they retrieve all tasks for the todo list
    Then they should see a task titled "Prepare report"

  Scenario: Update a task
    Given they add a task with title "Prepare report" and description "Due Friday"
    When they update the task title to "Prepare final report", description to "Due Monday", and mark it completed
    Then the task should have title "Prepare final report" and be completed

  Scenario: Delete a task
    Given they add a task with title "Prepare report" and description "Due Friday"
    When they delete the task
    Then the task list should not contain "Prepare report"

