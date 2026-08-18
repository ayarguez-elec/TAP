package controller;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import services.AuditService;
import services.UtilisateurService;
import java.io.IOException;

public class AjouterUtilisateurController {

    @FXML private TextField nomField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox actifCheck;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private AuditService auditService = new AuditService();

    // Set by the calling controller to know where to go back
    private StackPane contentArea;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("OPERATEUR", "TECHNICIEN", "INGENIEUR", "ADMIN", "PRODUCTION");
        roleCombo.setValue("OPERATEUR");
        actifCheck.setSelected(true);
    }

    @FXML
    private void handleRetour() {
        navigateBack();
    }

    @FXML
    private void handleAnnuler() {
        navigateBack();
    }

    @FXML
    private void handleEnregistrer() {
        String nom = nomField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validation
        if (nom.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
            return;
        }
        if (password.length() < 8) {
            showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caract\u00e8res.", Alert.AlertType.ERROR);
            return;
        }
        if (!password.equals(confirm)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setUsername(username);
            user.setPasswordHash(password); // Service will hash it
            user.setRole(Utilisateur.Role.valueOf(roleCombo.getValue()));
            user.setActif(actifCheck.isSelected());

            utilisateurService.create(user);
            auditService.logCurrentUser("CREATE_USER", "utilisateur", null,
                    "Creation utilisateur: " + username + " - Role: " + roleCombo.getValue());

            showAlert("Succ\u00e8s", "Utilisateur cr\u00e9\u00e9 avec succ\u00e8s.", Alert.AlertType.INFORMATION);
            navigateBack();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la cr\u00e9ation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private StackPane resolveContentArea() {
        if (contentArea != null) return contentArea;
        if (MainController.getInstance() != null) return MainController.getInstance().getContentArea();
        return null;
    }

    private void navigateBack() {
        StackPane target = resolveContentArea();
        if (target == null) return;
        contentArea = target;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-utilisateurs-view.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
