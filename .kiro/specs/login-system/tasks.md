# Implementation Plan: Login System

## Overview

This implementation plan transforms the login system design into a series of incremental coding tasks. The approach follows this sequence: (1) create the login FXML view and CSS styling, (2) implement the LoginController with authentication logic, (3) modify the Main application entry point to load the login screen, (4) integrate logout functionality into the existing MainController and main view, and (5) add property-based tests and integration tests. Each task builds on previous work to create a complete, tested authentication flow.

## Tasks

- [x] 1. Create login FXML view with professional styling
  - Create `src/main/resources/login-view.fxml` file with centered VBox layout, username TextField, password PasswordField, "Se connecter" Button, error Label
  - Apply Lacroix Electronics branding with title Label and subtitle
  - Reference `css/style.css` stylesheet
  - Set minimum scene dimensions and styling following the design specification
  - Use fx:id attributes: usernameField, passwordField, btnLogin, errorLabel
  - Configure button action: onAction="#handleLogin"
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 7.1, 7.2, 7.3, 7.4_

- [x] 2. Add CSS styles for login view components
  - Open `src/main/resources/css/style.css`
  - Add `.login-container` style with centered layout, white background, rounded corners (14px), drop shadow
  - Add `.login-title` style with 22px bold font, color #21262A
  - Add `.login-subtitle` style with 13px font, color #6F8D94
  - Add `.login-input` style with white background, #d0d5dd border, 8px border-radius, padding 10px 14px
  - Add `.login-input:focused` style with #346771 border color and subtle shadow
  - Add `.login-button` style with linear gradient background (#346771 to #3A98A5), white text, bold 14px, padding 12px 24px, 8px border-radius
  - Add `.login-button:hover` style with lighter gradient and increased shadow
  - Add `.login-error` style with color #D9691D, 12px font, padding-top 8px
  - _Requirements: 7.1, 7.2, 7.3, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10_

- [x] 3. Implement LoginController with authentication logic
  - [x] 3.1 Create `src/main/java/controller/LoginController.java`
    - Add package declaration: `package controller;`
    - Import required JavaFX classes (FXML, TextField, PasswordField, Button, Label, FXMLLoader, Scene, Stage)
    - Import services (UtilisateurService), security (SessionManager), entities (Utilisateur)
    - Add @FXML fields: usernameField, passwordField, btnLogin, errorLabel
    - Create UtilisateurService instance field
    - Add @FXML initialize() method to set focus on usernameField and hide errorLabel
    - _Requirements: 2.1, 9.1_
  
  - [x] 3.2 Implement handleLogin() method with validation and authentication
    - Add @FXML handleLogin() method
    - Get username from usernameField.getText().trim()
    - Get password from passwordField.getText()
    - Validate both fields are non-empty; if empty, call showError("Veuillez remplir tous les champs") and return
    - Call utilisateurService.authenticate(username, password)
    - If authentication returns null, call showError("Nom d'utilisateur ou mot de passe incorrect") and passwordField.clear()
    - Check if returned Utilisateur has actif=true; if false, call showError("Compte désactivé. Contactez un administrateur.")
    - If authentication succeeds, call SessionManager.getInstance().setUtilisateur(user) then navigateToDashboard()
    - Wrap in try-catch for SQLException, call showError("Erreur de connexion. Veuillez réessayer.")
    - _Requirements: 2.1, 2.3, 2.4, 2.5, 2.6, 2.8, 4.1, 4.2, 4.5, 4.8_
  
  - [x] 3.3 Implement helper methods for error display and navigation
    - Add private showError(String message) method: set errorLabel.setText(message), errorLabel.setVisible(true)
    - Add private navigateToDashboard() method: load tableau-de-board-view.fxml using FXMLLoader, create new Scene, get current Stage from usernameField.getScene().getWindow(), set new scene on stage, set title "Lacroix Electronics - Tableau de bord"
    - _Requirements: 3.2, 3.3, 4.6, 4.7_
  
  - [x] 3.4 Add Enter key event handlers for keyboard navigation
    - In initialize() method, add setOnAction listener to usernameField: when Enter pressed, requestFocus on passwordField
    - In initialize() method, add setOnAction listener to passwordField: when Enter pressed, call handleLogin()
    - _Requirements: 2.2, 9.2, 9.4, 9.5_

- [~] 4. Checkpoint - Test LoginController manually
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Modify Main application to load login screen at startup
  - [x] 5.1 Update `src/main/java/org/example/Main.java` start() method
    - Comment out or remove existing main-view.fxml loading code
    - Add FXMLLoader to load /login-view.fxml
    - Create Scene with loaded login view
    - Set scene on primaryStage
    - Set window title to "Connexion - Lacroix Electronics"
    - Set minimum width to 500px and minimum height to 400px
    - Call primaryStage.show()
    - _Requirements: 1.1, 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [x] 5.2 Verify application launches with login screen
    - Run Main.main() and verify login-view.fxml displays
    - Verify window title is correct
    - Verify minimum dimensions are enforced
    - _Requirements: 1.1, 8.1, 8.2_

- [x] 6. Add logout button to main view
  - [x] 6.1 Modify `src/main/resources/main-view.fxml` to add Déconnexion button
    - Locate the header HBox in main-view.fxml
    - Add Button with fx:id="btnDeconnexion", text="🔓 Déconnexion", styleClass="header-btn", onAction="#handleLogout"
    - Position button in top-right area of header using HBox spacing or Region spacer
    - _Requirements: 5.1, 5.2, 12.1, 12.2, 12.5, 12.6_
  
  - [x] 6.2 Implement handleLogout() method in MainController
    - Open `src/main/java/controller/MainController.java`
    - Add @FXML field: private Button btnDeconnexion;
    - Add @FXML handleLogout() method
    - In handleLogout(): call SessionManager.getInstance().logout()
    - Get current Stage from btnDeconnexion.getScene().getWindow()
    - Close current stage
    - Create new Stage, load /login-view.fxml with FXMLLoader, create Scene, set scene on new stage
    - Set title "Connexion - Lacroix Electronics", set minimum dimensions 500x400
    - Call newStage.show()
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 12.3, 12.4_

- [~] 7. Checkpoint - Test complete authentication flow
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 8. Write property tests for authentication behavior
  - [ ]* 8.1 Write property test for authentication service invocation
    - **Property 1: Authentication Service Invocation**
    - **Validates: Requirements 2.1, 2.2**
    - Create test class `LoginControllerTest` in `src/test/java/controller/`
    - Use property-based testing library (e.g., jqwik for Java)
    - Generate arbitrary username and password strings
    - Mock UtilisateurService and verify authenticate() is called with exact credentials
    - Run minimum 100 iterations
  
  - [ ]* 8.2 Write property test for empty field validation
    - **Property 2: Empty Field Validation**
    - **Validates: Requirements 2.3, 2.4**
    - Generate arbitrary strings for username and password where at least one is empty
    - Verify error message "Veuillez remplir tous les champs" is displayed
    - Verify UtilisateurService.authenticate() is NOT called
    - Run minimum 100 iterations
  
  - [ ]* 8.3 Write property test for valid credential authentication
    - **Property 3: Valid Credential Authentication**
    - **Validates: Requirements 2.5, 2.8**
    - Create test users in test database with actif=true
    - Generate valid credentials for these users
    - Verify authenticate() returns non-null Utilisateur with matching username and role
    - Run minimum 100 iterations
  
  - [ ]* 8.4 Write property test for invalid credential rejection
    - **Property 4: Invalid Credential Rejection**
    - **Validates: Requirements 2.6**
    - Generate random username/password pairs that don't match any test user
    - Verify authenticate() returns null
    - Run minimum 100 iterations
  
  - [ ]* 8.5 Write property test for session storage on success
    - **Property 6: Session Storage on Success**
    - **Validates: Requirements 3.1**
    - For any successful authentication, verify SessionManager.getUtilisateur() returns the same Utilisateur object
    - Run minimum 100 iterations
  
  - [ ]* 8.6 Write property test for password field clearing on failure
    - **Property 10: Password Field Clearing on Failure**
    - **Validates: Requirements 4.2, 4.4**
    - For any failed authentication, verify passwordField.getText() is empty
    - Verify usernameField.getText() still contains original value
    - Run minimum 100 iterations
  
  - [ ]* 8.7 Write property test for logout session clearing
    - **Property 12: Logout Session Clearing**
    - **Validates: Requirements 5.3, 5.4**
    - For any authenticated user, invoke logout and verify SessionManager.getUtilisateur() returns null
    - Run minimum 100 iterations
  
  - [ ]* 8.8 Write property test for credential exclusion from session
    - **Property 14: Credential Exclusion from Session**
    - **Validates: Requirements 6.1, 10.5**
    - For any authenticated session, verify SessionManager contains Utilisateur but no password or passwordHash fields are exposed
    - Run minimum 100 iterations

- [ ]* 9. Write integration tests for end-to-end authentication flow
  - [ ]* 9.1 Test complete login to dashboard flow
    - Start application, verify login screen loads
    - Enter valid credentials (admin/admin123), click login
    - Verify dashboard view loads and displays correctly
    - Verify SessionManager contains authenticated user
    - _Requirements: 1.1, 2.1, 3.1, 3.2, 3.3_
  
  - [ ]* 9.2 Test failed authentication with retry
    - Start application, enter invalid credentials, click login
    - Verify error message displays
    - Verify password field is cleared
    - Enter valid credentials, click login
    - Verify dashboard loads successfully
    - _Requirements: 4.1, 4.2, 4.3_
  
  - [ ]* 9.3 Test logout and re-authentication flow
    - Login with valid credentials
    - Navigate to main view
    - Click "Déconnexion" button
    - Verify login screen reappears
    - Verify SessionManager.isAuthenticated() returns false
    - Login again with valid credentials
    - Verify dashboard loads
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 5.7_
  
  - [ ]* 9.4 Test inactive user authentication rejection
    - Create test user with actif=false
    - Attempt login with that user's credentials
    - Verify error message "Compte désactivé. Contactez un administrateur." displays
    - Verify login screen remains visible
    - _Requirements: 2.8, 4.5_

- [~] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties across many generated inputs
- Integration tests validate end-to-end flows with example scenarios
- The design uses existing authentication infrastructure (UtilisateurService, SessionManager, BCrypt) - no new authentication logic needed
- Focus on UI, navigation flow, and proper integration with existing services
- CSS styling reuses the existing Lacroix color palette for visual consistency

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1", "2"] },
    { "id": 1, "tasks": ["3.1"] },
    { "id": 2, "tasks": ["3.2", "3.3", "3.4"] },
    { "id": 3, "tasks": ["5.1", "6.1"] },
    { "id": 4, "tasks": ["5.2", "6.2"] },
    { "id": 5, "tasks": ["8.1", "8.2", "8.3", "8.4", "8.5", "8.6", "8.7", "8.8"] },
    { "id": 6, "tasks": ["9.1", "9.2", "9.3", "9.4"] }
  ]
}
```
