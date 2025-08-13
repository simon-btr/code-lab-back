Feature: Todo List Management
As a user
  I want to manage my todo lists
  So that I can collaborate with other users

  Background:
    Given a logged in user with email "owner@example.com"

  Scenario: Create a todo list
    When they create a todo list with the title "Groceries"
    Then the todo list "Groceries" is created for this user

  Scenario: Add a member to a todo list
    And another user exists with email "member@example.com"
    When they create a todo list with the title "Team Tasks"
    And they add the user "member@example.com" to the todo list
    Then the todo list "Team Tasks" should include "member@example.com" as a member

  Scenario: Owner removes a member from a todo list
    And they create a todo list with the title "Shopping"
    And another user exists with email "friend@example.com"
    And they add the user "friend@example.com" to the todo list
    When they remove member with email "friend@example.com" from the todo list
    Then the todo list should not include "friend@example.com" as a member

  Scenario: Owner updates the title of a todo list
    And they create a todo list with the title "Old Title"
    When they update the todo list title to "New Title"
    Then the todo list title should be "New Title"

  Scenario: Owner deletes a todo list
    And they create a todo list with the title "Temporary List"
    When they delete the todo list
    Then the todo list should no longer exist

  Scenario: Non-owner cannot delete a todo list
    And they create a todo list with the title "Secret List"
    And another user exists with email "intruder@example.com"
    And the current user is switched to "intruder@example.com"
    When they try to delete the todo list
    Then an access denied error should be thrown
