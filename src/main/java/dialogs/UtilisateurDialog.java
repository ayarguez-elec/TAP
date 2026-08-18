package dialogs;

import entities.Utilisateur;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import services.UtilisateurService;

public class UtilisateurDialog extends Dialog<Utilisateur> {
    
    private boolean isEditMode;
    private Utilisateur utilisateur;
    private UtilisateurService utilisateurService;
    
    private TextField nomField;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<Utilisateur.Role> roleComboBox;
    private CheckBox actifCheckBox;
    
    public UtilisateurDialog(Utilisateur user) {
        this.isEditMode = (user != null);
        this.utilisateur = user != null ? user : new Utilisateur();
        this.utilisateurService = new UtilisateurService();
        
        setTitle(isEditMode ? "Modifier Utilisateur" : "Nouvel Utilisateur");
        setHeaderText(isEditMode ? "Modifier les informations de l'utilisateur" : "Créer un nouvel utilisateur");
        
        ButtonType saveButtonType = new ButtonType(isEditMode ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Nom
        nomField = new TextField();
        nomField.setPromptText("Nom complet");
        if (isEditMode) {
            nomField.setText(utilisateur.getNom());
        }
        
        // Username
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        if (isEditMode) {
            usernameField.setText(utilisateur.getUsername());
            usernameField.setDisable(true); // Ne pas permettre de modifier le username
        }
        
        // Role
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Utilisateur.Role.values());
        if (isEditMode) {
            roleComboBox.setValue(utilisateur.getRole());
        } else {
            roleComboBox.setValue(Utilisateur.Role.OPERATEUR);
        }
        
        // Actif
        actifCheckBox = new CheckBox();
        actifCheckBox.setSelected(isEditMode ? utilisateur.isActif() : true);
        
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        
        grid.add(new Label("Rôle:"), 0, 2);
        grid.add(roleComboBox, 1, 2);
        
        grid.add(new Label("Actif:"), 0, 3);
        grid.add(actifCheckBox, 1, 3);
        
        // Si création, ajouter les champs mot de passe
        if (!isEditMode) {
            passwordField = new PasswordField();
            passwordField.setPromptText("Mot de passe (min. 8 caractères)");
            
            confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirmer le mot de passe");
            
            grid.add(new Label("Mot de passe:"), 0, 4);
            grid.add(passwordField, 1, 4);
            
            grid.add(new Label("Confirmation:"), 0, 5);
            grid.add(confirmPasswordField, 1, 5);
        }
        
        getDialogPane().setContent(grid);
        
        // Style
        getDialogPane().setStyle("-fx-padding: 15;");
        
        // Validation et conversion
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return validateAndBuildUser();
            }
            return null;
        });
    }
    
    private Utilisateur validateAndBuildUser() {
        // Validation du nom
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showError("Le nom ne peut pas être vide");
            return null;
        }
        
        // Validation du username (seulement en création)
        if (!isEditMode) {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                showError("Le username ne peut pas être vide");
                return null;
            }
            
            // Vérifier l'unicité du username
            if (utilisateurService.findByUsername(username) != null) {
                showError("Ce username est déjà utilisé");
                return null;
            }
            
            // Validation du mot de passe
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (password.length() < 8) {
                showError("Le mot de passe doit contenir au moins 8 caractères");
                return null;
            }
            
            if (!password.equals(confirmPassword)) {
                showError("Les mots de passe ne correspondent pas");
                return null;
            }
            
            utilisateur.setUsername(username);
            utilisateur.setPasswordHash(password); // Sera hashé par le service
        }
        
        utilisateur.setNom(nom);
        utilisateur.setRole(roleComboBox.getValue());
        utilisateur.setActif(actifCheckBox.isSelected());
        
        return utilisateur;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-padding: 15;");
        alert.showAndWait();
    }
}
