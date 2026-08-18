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

public class ChangerMdpController {

    @FXML private Label userInfoLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label strengthLabel;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private AuditService auditService = new AuditService();
    private StackPane contentArea;
    private Utilisateur utilisateur;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        userInfoLabel.setText(u.getNom() + " (@" + u.getUsername() + ") - " + u.getRole());
    }

    @FXML
    public void initialize() {
        // Live password strength indicator
        newPasswordField.textProperty().addListener((obs, old, val) -> {
            if (val.length() == 0) {
                strengthLabel.setText("");
            } else if (val.length() < 8) {
                strengthLabel.setText("Trop court (min. 8 caract\u00e8res)");
                strengthLabel.setStyle("-fx-text-fill: #D9691D; -fx-font-size: 11px;");
            } else if (val.length() < 12) {
                strengthLabel.setText("Moyen");
                strengthLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 11px;");
            } else {
                strengthLabel.setText("Fort");
                strengthLabel.setStyle("-fx-text-fill: #46BE62; -fx-font-size: 11px;");
            }
        });
    }

    @FXML
    private void handleRetour() { navigateBack(); }

    @FXML
    private void handleAnnuler() { navigateBack(); }

    @FXML
    private void handleEnregistrer() {
        String newPassword = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (newPassword.length() < 8) {
            showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caract\u00e8res.", Alert.AlertType.ERROR);
            return;
        }
        if (!newPassword.equals(confirm)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas.", Alert.AlertType.ERROR);
            return;
        }

        try {
            utilisateurService.changePassword(utilisateur.getId(), newPassword);
            auditService.logCurrentUser("CHANGE_PASSWORD", "utilisateur",
                    String.valueOf(utilisateur.getId()),
                    "Changement de mot de passe pour: " + utilisateur.getUsername());

            showAlert("Succ\u00e8s", "Mot de passe modifi\u00e9 avec succ\u00e8s.", Alert.AlertType.INFORMATION);
            navigateBack();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
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
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
