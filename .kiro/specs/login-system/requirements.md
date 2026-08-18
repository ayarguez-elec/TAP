# Requirements Document: Login System

## Introduction

This document specifies the requirements for implementing a professional startup login system for the Lacroix Electronics industrial JavaFX application. The system provides secure authentication before main application access, integrating with existing authentication infrastructure while delivering an attractive, modern user interface consistent with the industrial design standards.

## Glossary

- **Login_Screen**: The initial FXML view (login-view.fxml) displayed on application startup for credential entry
- **Authentication_Service**: The existing UtilisateurService component that validates credentials using BCrypt
- **Session_Manager**: The singleton SessionManager that stores the authenticated user state
- **Dashboard_View**: The tableau-de-board-view.fxml screen shown after successful authentication
- **Main_Controller**: The MainController managing the main application menu and navigation
- **Credentials**: A username and password pair provided by the user for authentication
- **Active_User**: A user account with the actif flag set to true in the database
- **BCrypt**: The cryptographic hashing algorithm used for secure password storage and verification

## Requirements

### Requirement 1: Login Screen Display

**User Story:** As a user, I want to see a professional login screen when I start the application, so that I can securely authenticate before accessing the system.

#### Acceptance Criteria

1. WHEN the application starts, THE System SHALL display the login-view.fxml as the initial screen
2. THE Login_Screen SHALL include a username input field with prompt text "Nom d'utilisateur"
3. THE Login_Screen SHALL include a password input field with masked characters
4. THE Login_Screen SHALL include a "Se connecter" button styled with the Lacroix teal color palette
5. THE Login_Screen SHALL display the Lacroix Electronics branding and title
6. THE Login_Screen SHALL use the existing CSS style.css for visual consistency
7. THE Login_Screen SHALL be centered in a window with minimum dimensions 500px width by 400px height
8. THE Login_Screen SHALL have rounded corners and subtle drop shadows following the industrial design system

### Requirement 2: Credential Authentication

**User Story:** As a user, I want my credentials to be validated securely, so that only authorized users can access the application.

#### Acceptance Criteria

1. WHEN a user enters credentials and clicks "Se connecter", THE System SHALL invoke UtilisateurService.authenticate() with the provided username and password
2. WHEN a user presses Enter key in any input field, THE System SHALL trigger the login action
3. IF the username field is empty, THEN THE System SHALL display error message "Veuillez remplir tous les champs"
4. IF the password field is empty, THEN THE System SHALL display error message "Veuillez remplir tous les champs"
5. WHEN authentication succeeds, THE System SHALL retrieve the Utilisateur object from Authentication_Service
6. WHEN authentication fails, THE System SHALL return null from Authentication_Service
7. THE System SHALL use BCrypt for password verification through the existing UtilisateurService
8. THE System SHALL validate that the user account has actif flag set to true

### Requirement 3: Successful Authentication Flow

**User Story:** As an authenticated user, I want to be redirected to the dashboard after successful login, so that I can begin using the application.

#### Acceptance Criteria

1. WHEN authentication succeeds, THE System SHALL store the Utilisateur object in Session_Manager using setUtilisateur()
2. WHEN the user is stored in Session_Manager, THE System SHALL load the Dashboard_View (tableau-de-board-view.fxml)
3. WHEN the Dashboard_View loads, THE System SHALL display it as the active content area
4. WHEN the Dashboard_View is displayed, THE System SHALL show the DashboardController content
5. THE System SHALL maintain the authenticated session throughout application usage
6. THE System SHALL allow navigation to all authorized views after successful authentication

### Requirement 4: Failed Authentication Handling

**User Story:** As a user with incorrect credentials, I want to see clear error messages, so that I understand why authentication failed and can retry.

#### Acceptance Criteria

1. WHEN authentication fails due to invalid credentials, THE System SHALL display error message "Nom d'utilisateur ou mot de passe incorrect"
2. WHEN authentication fails, THE System SHALL clear the password field for security
3. WHEN authentication fails, THE System SHALL keep the Login_Screen visible
4. WHEN authentication fails, THE System SHALL keep the username field populated
5. IF the user account exists but actif is false, THEN THE System SHALL display error message "Compte désactivé. Contactez un administrateur."
6. WHEN an error message is displayed, THE System SHALL style it in red color (#D9691D)
7. WHEN an error message is displayed, THE System SHALL position it below the login button
8. IF the Authentication_Service throws SQLException, THEN THE System SHALL display error message "Erreur de connexion. Veuillez réessayer."

### Requirement 5: Logout Functionality

**User Story:** As an authenticated user, I want to log out of the application, so that I can end my session securely.

#### Acceptance Criteria

1. THE Main_Controller SHALL include a "Déconnexion" button in the main-view.fxml header
2. THE "Déconnexion" button SHALL be positioned in the top-right corner of the header
3. WHEN a user clicks "Déconnexion", THE System SHALL invoke Session_Manager.logout()
4. WHEN Session_Manager.logout() is called, THE System SHALL set utilisateurCourant to null
5. WHEN the session is cleared, THE System SHALL close the current main application window
6. WHEN the main window closes after logout, THE System SHALL reopen the Login_Screen
7. THE System SHALL require re-authentication after logout to access the application again

### Requirement 6: Session Management

**User Story:** As a system administrator, I want user sessions to be managed securely, so that unauthorized access is prevented.

#### Acceptance Criteria

1. THE Session_Manager SHALL store only the authenticated Utilisateur object, not credentials
2. WHEN a user authenticates successfully, THE Session_Manager SHALL maintain session state throughout application usage
3. THE Session_Manager SHALL provide isAuthenticated() method returning true when a user is authenticated
4. THE Session_Manager SHALL provide getUtilisateur() method returning the current Utilisateur or null
5. WHEN logout is invoked, THE Session_Manager SHALL completely clear the session state
6. IF Session_Manager.getUtilisateur() returns null during application usage, THEN THE System SHALL redirect to Login_Screen with message "Session expirée. Veuillez vous reconnecter."

### Requirement 7: Visual Design and Styling

**User Story:** As a user, I want the login screen to be visually attractive and consistent with the application design, so that I have confidence in the system's professionalism.

#### Acceptance Criteria

1. THE Login_Screen SHALL use the Lacroix teal color (#346771) as the primary action color
2. THE Login_Screen SHALL apply rounded corners with radius 8-14px to all input fields and buttons
3. THE Login_Screen SHALL use subtle drop shadows (gaussian blur 10px, opacity 0.06) for depth
4. THE Login_Screen SHALL use Segoe UI font family consistently
5. THE Login_Screen SHALL style input fields with white background and light gray border (#d0d5dd)
6. WHEN an input field receives focus, THE System SHALL change border color to Lacroix teal (#346771)
7. THE "Se connecter" button SHALL use linear gradient background from #346771 to #3A98A5
8. WHEN the "Se connecter" button is hovered, THE System SHALL display lighter gradient with increased shadow
9. THE Login_Screen SHALL maintain visual consistency with existing application views
10. THE Login_Screen SHALL follow the Materially design system used throughout the application

### Requirement 8: Application Entry Point Modification

**User Story:** As a developer, I want the application to launch with the login screen, so that authentication is enforced from startup.

#### Acceptance Criteria

1. THE Main application class SHALL load login-view.fxml as the initial scene in the start() method
2. THE Main application SHALL set window title to "Connexion - Lacroix Electronics"
3. THE Main application SHALL set minimum window width to 500px
4. THE Main application SHALL set minimum window height to 400px
5. THE Main application SHALL not load main-view.fxml or dashboard-view.fxml at startup
6. WHEN authentication succeeds in LoginController, THE System SHALL transition to Dashboard_View

### Requirement 9: Input Validation and User Experience

**User Story:** As a user, I want smooth interaction with the login form, so that authentication is quick and intuitive.

#### Acceptance Criteria

1. WHEN the Login_Screen loads, THE System SHALL set focus on the username field
2. WHEN the user presses Tab in the username field, THE System SHALL move focus to the password field
3. WHEN the user presses Tab in the password field, THE System SHALL move focus to the "Se connecter" button
4. WHEN the user presses Enter in the username field, THE System SHALL move focus to the password field
5. WHEN the user presses Enter in the password field, THE System SHALL trigger the login action
6. THE System SHALL display input field prompt text in light gray when fields are empty
7. THE System SHALL clear prompt text when user begins typing
8. WHEN an error occurs, THE System SHALL maintain keyboard focus on the password field for easy retry

### Requirement 10: Security and Password Handling

**User Story:** As a security-conscious user, I want my password to be handled securely, so that my credentials are protected.

#### Acceptance Criteria

1. THE Login_Screen SHALL use PasswordField component to mask password characters
2. THE System SHALL NOT log plain-text passwords at any point
3. THE System SHALL clear the password field after failed authentication attempts
4. THE System SHALL pass credentials to Authentication_Service for BCrypt verification
5. THE System SHALL NOT store credentials in Session_Manager
6. THE System SHALL use PreparedStatement in UtilisateurService to prevent SQL injection
7. THE System SHALL display generic error messages to prevent username enumeration
8. THE Login_Screen SHALL NOT provide password visibility toggle per industrial security requirements

### Requirement 11: Integration with Existing Authentication System

**User Story:** As a system integrator, I want the login system to use existing authentication components, so that no duplicate authentication logic is created.

#### Acceptance Criteria

1. THE Login_Controller SHALL use the existing UtilisateurService for authentication
2. THE Login_Controller SHALL use the existing SessionManager singleton for session management
3. THE System SHALL authenticate against the existing utilisateur database table
4. THE System SHALL support all three existing roles: OPERATEUR, TECHNICIEN, INGENIEUR
5. THE System SHALL work with existing default users (admin/admin123, tech1/admin123, op1/admin123)
6. THE System SHALL integrate with the existing PermissionGuard for role-based access control
7. THE System SHALL NOT modify the database schema or authentication service logic

### Requirement 12: Logout Button Integration

**User Story:** As a developer, I want to add a logout button to the main menu, so that users can end their session from any screen.

#### Acceptance Criteria

1. THE main-view.fxml SHALL include a Button with fx:id="btnDeconnexion"
2. THE "Déconnexion" button SHALL be placed in the header HBox container
3. THE Main_Controller SHALL include a handleLogout() method annotated with @FXML
4. THE handleLogout() method SHALL be bound to the onAction event of btnDeconnexion
5. THE "Déconnexion" button SHALL use header-btn CSS class for consistent styling
6. THE "Déconnexion" button SHALL display logout icon and text "Déconnexion"
7. WHEN the "Déconnexion" button is hovered, THE System SHALL apply hover styling from CSS
