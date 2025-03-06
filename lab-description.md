# Lab 08 Instructions

1. Follow the slides on [CI/CD and Mocking](./slides/lab8-ci-cd-mocking.pdf)

2. Follow the instructions on [lab-instructions](./lab-instructions.md) along with the TA.

3. Complete Lab Exercise.  

# Lab 08 Participation Exercise

Proper completion of this exercise is considered as part of course participation.

1. Implement a new feature for the app to ensure that movies have unique titles. You will need to modify the `MovieProvider.java` to check with the database a movie with that name does not already exist. You also must give feedback to the user with an error message **before closing the dialogue window** for adding/editing a movie. You **must** make an issue and a branch on your respository to implement this feature for.

2. Add one (or more) JUnit tests with a mock to test this new functionality.

3. Implement a UI test in the `MainActivityTest.java` file to try adding a duplicate movie and check that the error message is displayed to the user.

4. Make a PR to main from your branch and ensure that the CI/CD pipeline passes before merging your branch into main.
