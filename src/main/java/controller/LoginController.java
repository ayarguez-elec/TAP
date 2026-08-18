package controller;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import security.SessionManager;
import services.UtilisateurService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button btnLogin;

    @FXML
    private Label errorLabel;

    private UtilisateurService utilisateurService;

    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        errorLabel.setVisible(false);
        usernameField.requestFocus();
        
        // Add Enter key event handlers for keyboard navigation
        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> handleLogin());
    }

    /**
     * Displays an error message in the error label
     * @param message The error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigates to the dashboard view after successful authentication
     */
    private void navigateToDashboard() {
        try {
            Utilisateur user = SessionManager.getInstance().getUtilisateur();
            String fxml = (user != null && user.getRole() == Utilisateur.Role.PRODUCTION)
                    ? "/production-main-view.fxml"
                    : "/main-view.fxml";
            String title = (user != null && user.getRole() == Utilisateur.Role.PRODUCTION)
                    ? "Lacroix Electronics - Production"
                    : "Lacroix Electronics";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setResources(controller.MainController.bundle);
            Scene scene = new Scene(loader.load(), 1200, 800);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Erreur de chargement de l application");
            e.printStackTrace();
        }
    }


    /**
     * Handles the login action when the login button is clicked or Enter is pressed in password field
     * Validates credentials, authenticates the user, checks account status, and navigates to dashboard
     */
    @FXML
    private void handleLogin() {
        // Get credentials from input fields
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // DEBUG LOG
        System.out.println("=== DEBUG LOGIN ===");
        System.out.println("Username: '" + username + "'");
        System.out.println("Password: '" + password + "'");

        // Validate both fields are non-empty
        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Authenticate with the service
        Utilisateur user = utilisateurService.authenticate(username, password);

        // DEBUG LOG
        System.out.println("Utilisateur: " + (user != null ? user.getUsername() : "NULL"));
        System.out.println("===================");

        // Check if authentication failed
        if (user == null) {
            showError("Nom d'utilisateur ou mot de passe incorrect");
            passwordField.clear();
            return;
        }

        // Check if user account is active
        if (!user.isActif()) {
            showError("Compte désactivé. Contactez un administrateur.");
            passwordField.clear();
            return;
        }

        // Authentication successful
        SessionManager.getInstance().setUtilisateur(user);
        navigateToDashboard();
    }

}
