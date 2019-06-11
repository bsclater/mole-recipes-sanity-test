Feature: Mole Recipes Service

  Scenario: Can get all recipes
    Given I post the following recipe:
      | id | name               | method     | ingredients   |
      | -2 | Marmite Sandwiches | just do it | Marmite,Bread |
    When a request is made to retrieve all recipes
    Then the following recipes should be returned:
      | id | name               | method     | ingredients   |
      | -2 | Marmite Sandwiches | just do it | Marmite,Bread |

  Scenario: Can post a recipe
    Given I post the following recipe:
      | id | name               | method     | ingredients   |
      | -2 | Marmite Sandwiches | just do it | Marmite,Bread |
    When a request is made to retrieve my recipe "Marmite Sandwiches"
    Then the following recipes should be returned:
      | id | name               | method     | ingredients   |
      | -2 | Marmite Sandwiches | just do it | Marmite,Bread |

  Scenario: Can update a recipe
    Given I post the following recipe:
      | id | name           | method     | ingredients |
      | 3  | Jam Sandwiches | just do it | Jam,Bread   |
    And I update the recipe like this:
      | id | name           | method     | ingredients   |
      | 3  | Jam Sandwiches | just do it | Jam,Cardboard |
    When a request is made to retrieve my recipe "Jam Sandwiches"
    Then the following recipes should be returned:
      | id | name           | method     | ingredients   |
      | 3  | Jam Sandwiches | just do it | Jam,Cardboard |

  Scenario: Can delete a recipe
    Given I post the following recipe:
      | id | name               | method     | ingredients   |
      | -2 | Marmite Sandwiches | just do it | Marmite,Bread |
    When a request is made to delete my recipe "Marmite Sandwiches"
    Then no recipe should be returned