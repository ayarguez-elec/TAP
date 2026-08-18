package security;

import entities.Utilisateur;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import services.UtilisateurService;

/**
 * Dialog d'authentification JavaFX
 */
public class AuthDialog extends Dialog<Utilisateur> {
    
    private TextField usernameField;
    private PasswordField passwordField;
    private UtilisateurService utilisateurService;
    
    public AuthDialog(String actionDescription) {
        this.utilisateurService = new UtilisateurService();
        
        setTitle("Authentification requise");
        setHeaderText(actionDescription);
        
        // Icône
        Alert.AlertType type = Alert.AlertType.CONFIRMATION;
        setGraphic(null);
        
        // Boutons
        ButtonType loginButtonType = new ButtonType("Se connecter", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        // Créer le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        
        grid.add(new Label("Utilisateur:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Mot de passe:"), 0, 1);
        grid.add(passwordField, 1, 1);
        
        getDialogPane().setContent(grid);
        
        // Focus sur le champ username
        javafx.application.Platform.runLater(() -> usernameField.requestFocus());
        
        // Convertir le résultat en Utilisateur
        setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return authenticate();
            }
            return null;
        });
    }
    
    private Utilisateur authenticate() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return null;
        }
        
        Utilisateur user = utilisateurService.authenticate(username, password);
        
        if (user == null) {
            showError("Nom d'utilisateur ou mot de passe incorrect");
            return null;
        }
        
        if (!user.isActif()) {
            showError("Ce compte est désactivé");
            return null;
        }
        
        return user;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur d'authentification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
