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

public class ModifierUtilisateurController {

    @FXML private TextField nomField;
    @FXML private TextField usernameField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox actifCheck;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private AuditService auditService = new AuditService();
    private StackPane contentArea;
    private Utilisateur utilisateur;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        nomField.setText(u.getNom());
        usernameField.setText(u.getUsername());
        roleCombo.setValue(u.getRole().name());
        actifCheck.setSelected(u.isActif());
    }

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("OPERATEUR", "TECHNICIEN", "INGENIEUR", "ADMIN", "PRODUCTION");
    }

    @FXML
    private void handleRetour() { navigateBack(); }

    @FXML
    private void handleAnnuler() { navigateBack(); }

    @FXML
    private void handleEnregistrer() {
        String nom = nomField.getText().trim();
        String username = usernameField.getText().trim();

        if (nom.isEmpty() || username.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        try {
            utilisateur.setNom(nom);
            utilisateur.setUsername(username);
            utilisateur.setRole(Utilisateur.Role.valueOf(roleCombo.getValue()));
            utilisateur.setActif(actifCheck.isSelected());

            utilisateurService.update(utilisateur);
            auditService.logCurrentUser("UPDATE_USER", "utilisateur",
                    String.valueOf(utilisateur.getId()),
                    "Modification: " + username + " - Role: " + roleCombo.getValue());

            showAlert("Succ\u00e8s", "Utilisateur modifi\u00e9 avec succ\u00e8s.", Alert.AlertType.INFORMATION);
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
