package controller;

import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import security.PermissionGuard;
import services.AuditService;
import services.UtilisateurService;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionsController {

    @FXML private TabPane tabPane;
    @FXML private GridPane rolesMatrixGrid;
    @FXML private TextField userSearchField;
    @FXML private ListView<Utilisateur> usersListView;
    @FXML private Label selectedUserLabel;
    @FXML private VBox userOverridesContainer;

    private UtilisateurService utilisateurService;
    private AuditService auditService;
    private List<Utilisateur> allUsers;
    private Utilisateur selectedUser;

    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        auditService = new AuditService();

        // 1. Initialiser la matrice des permissions par rôle
        buildRolesMatrix();

        // 2. Charger les utilisateurs pour les surcharges individuelles
        loadUsers();

        // 3. Écouteurs de sélection de liste et de recherche
        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                selectedUserLabel.setText(newVal.getNom() + " (" + newVal.getRole() + ")");
                buildUserOverrides();
            }
        });

        userSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));
    }

    private void buildRolesMatrix() {
        rolesMatrixGrid.getChildren().clear();

        // Titres de colonnes
        Label actionHeader = new Label("Action Système");
        actionHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #346771; -fx-font-size: 13px;");
        rolesMatrixGrid.add(actionHeader, 0, 0);

        int col = 1;
        for (Utilisateur.Role role : Utilisateur.Role.values()) {
            Label roleHeader = new Label(role.toString());
            roleHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #346771; -fx-font-size: 13px;");
            rolesMatrixGrid.add(roleHeader, col++, 0);
        }

        // Séparateur horizontal
        Separator sep = new Separator();
        rolesMatrixGrid.add(sep, 0, 1, 6, 1);
        GridPane.setMargin(sep, new Insets(5, 0, 5, 0));

        // Remplir les lignes
        int row = 2;
        Map<String, Set<Utilisateur.Role>> rolePerms = PermissionGuard.getRolePermissions();

        for (Map.Entry<String, String> entry : PermissionGuard.ACTION_NAMES.entrySet()) {
            String action = entry.getKey();
            String displayName = entry.getValue();

            // Label de l'action
            Label lblAction = new Label(displayName);
            lblAction.setStyle("-fx-font-size: 12px; -fx-text-fill: #21262A;");
            lblAction.setWrapText(true);
            lblAction.setMaxWidth(280);
            rolesMatrixGrid.add(lblAction, 0, row);

            col = 1;
            for (Utilisateur.Role role : Utilisateur.Role.values()) {
                CheckBox cb = new CheckBox();
                cb.setSelected(rolePerms.get(action).contains(role));
                
                // Mettre à jour la matrice lors du changement
                cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        rolePerms.get(action).add(role);
                    } else {
                        rolePerms.get(action).remove(role);
                    }
                    PermissionGuard.savePermissions();
                });
                GridPane.setHalignment(cb, javafx.geometry.HPos.CENTER);
                rolesMatrixGrid.add(cb, col++, row);
            }
            row++;
        }
    }

    private void loadUsers() {
        allUsers = utilisateurService.readAll();
        // Filtrer uniquement les utilisateurs actifs
        List<Utilisateur> activeUsers = allUsers.stream()
                .filter(Utilisateur::isActif)
                .collect(Collectors.toList());
        usersListView.setItems(FXCollections.observableArrayList(activeUsers));
    }

    private void filterUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadUsers();
            return;
        }
        String lowerQuery = query.toLowerCase();
        List<Utilisateur> filtered = allUsers.stream()
                .filter(u -> u.isActif() && (u.getNom().toLowerCase().contains(lowerQuery) || u.getUsername().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
        usersListView.setItems(FXCollections.observableArrayList(filtered));
    }

    private void buildUserOverrides() {
        userOverridesContainer.getChildren().clear();
        if (selectedUser == null) return;

        Map<String, Set<Integer>> overrides = PermissionGuard.getUserOverrides();

        for (Map.Entry<String, String> entry : PermissionGuard.ACTION_NAMES.entrySet()) {
            String action = entry.getKey();
            String displayName = entry.getValue();

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

            VBox textContainer = new VBox(2);
            Label lblAction = new Label(displayName);
            lblAction.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #21262A;");

            // Calcul du droit par défaut via son rôle
            boolean defaultAccess = PermissionGuard.getRolePermissions().get(action).contains(selectedUser.getRole());
            Label lblDefault = new Label(defaultAccess ? "Autorisé par défaut (Rôle: " + selectedUser.getRole() + ")" : "Non autorisé par défaut (Rôle: " + selectedUser.getRole() + ")");
            lblDefault.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (defaultAccess ? "#46BE62" : "#6F8D94") + ";");

            textContainer.getChildren().addAll(lblAction, lblDefault);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            // Checkbox pour la surcharge individuelle
            CheckBox cbOverride = new CheckBox("Accorder accès individuel");
            cbOverride.setStyle("-fx-font-size: 12px; -fx-text-fill: #21262A;");
            
            boolean isOverridden = overrides.get(action).contains(selectedUser.getId());
            cbOverride.setSelected(isOverridden);

            cbOverride.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    overrides.get(action).add(selectedUser.getId());
                } else {
                    overrides.get(action).remove(selectedUser.getId());
                }
            });

            row.getChildren().addAll(textContainer, cbOverride);
            userOverridesContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Sauvegarder les permissions
            PermissionGuard.savePermissions();

            // Journaliser l'audit
            Utilisateur currentAdmin = security.SessionManager.getInstance().getUtilisateur();
            if (currentAdmin != null) {
                auditService.log(currentAdmin.getId(), "UPDATE_PERMISSIONS", "PermissionGuard", null,
                        "Permissions et habilitations mises à jour par l'administrateur.");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sauvegarde réussie");
            alert.setHeaderText(null);
            alert.setContentText("Les habilitations et permissions des rôles et individus ont été enregistrées avec succès !");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'enregistrer les permissions : " + e.getMessage());
            alert.showAndWait();
        }
    }
}
