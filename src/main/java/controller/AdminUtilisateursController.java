package controller;

import controller.AjouterUtilisateurController;
import controller.ModifierUtilisateurController;
import controller.ChangerMdpController;
import dialogs.UtilisateurDialog;
import entities.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import security.PermissionGuard;
import security.SessionManager;
import services.AuditService;
import services.UtilisateurService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminUtilisateursController {

    private StackPane contentArea;

    public void setContentArea(StackPane ca) { this.contentArea = ca; }

    private StackPane getEffectiveContentArea() {
        if (contentArea != null) return contentArea;
        if (MainController.getInstance() != null) return MainController.getInstance().getContentArea();
        return null;
    }

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilterCombo;
    @FXML
    private ComboBox<String> statutFilterCombo;
    @FXML
    private TableView<Utilisateur> tableView;
    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colUsername;
    @FXML
    private TableColumn<Utilisateur, Utilisateur.Role> colRole;
    @FXML
    private TableColumn<Utilisateur, Boolean> colActif;
    @FXML
    private TableColumn<Utilisateur, String> colDateCreation;
    @FXML
    private TableColumn<Utilisateur, Void> colActions;
    @FXML
    private Label totalLabel;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private AuditService auditService = new AuditService();
    private ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> filteredUtilisateurs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Verifier que l'utilisateur est INGENIEUR ou ADMIN
        entities.Utilisateur currentUser = security.SessionManager.getInstance().getUtilisateur();
        boolean canManageUsers = currentUser != null && (
            currentUser.getRole() == entities.Utilisateur.Role.INGENIEUR ||
            currentUser.getRole() == entities.Utilisateur.Role.ADMIN
        );
        if (!canManageUsers) {
            showAlert("Acces refuse", "Seuls les ingenieurs et admins peuvent gerer les utilisateurs", Alert.AlertType.ERROR);
            return;
        }

        setupTableView();
        setupFilters();
        loadData();
    }

    private void setupTableView() {
        // Colonne Actif avec indicateur visuel
        colActif.setCellFactory(col -> new TableCell<Utilisateur, Boolean>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(actif ? "✅" : "❌");
                    label.setStyle(actif ? "-fx-text-fill: #46BE62; -fx-font-size: 16px;" : 
                                           "-fx-text-fill: #D9691D; -fx-font-size: 16px;");
                    setGraphic(label);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Role avec badge coloré
        colRole.setCellFactory(col -> new TableCell<Utilisateur, Utilisateur.Role>() {
            @Override
            protected void updateItem(Utilisateur.Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(role.name());
                    String style = switch (role) {
                        case OPERATEUR   -> "-fx-background-color: #3B82F6; -fx-text-fill: white;";
                        case TECHNICIEN  -> "-fx-background-color: #10B981; -fx-text-fill: white;";
                        case INGENIEUR   -> "-fx-background-color: #8B5CF6; -fx-text-fill: white;";
                        case ADMIN       -> "-fx-background-color: #E53E3E; -fx-text-fill: white;";
                        case PRODUCTION  -> "-fx-background-color: #D9691D; -fx-text-fill: white;";
                        default          -> "-fx-background-color: #6F8D94; -fx-text-fill: white;";
                    };
                    badge.setStyle(style + " -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Date Création
        colDateCreation.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateCreation() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(sdf.format(cellData.getValue().getDateCreation()));
            }
            return new SimpleStringProperty("N/A");
        });

        // Colonne Actions
        colActions.setCellFactory(col -> new TableCell<Utilisateur, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    HBox actionBox = new HBox(8);
                    actionBox.setAlignment(Pos.CENTER);

                    // Bouton Modifier
                    Button btnEdit = new Button("📝");
                    btnEdit.setStyle("-fx-background-color: #346771; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnEdit.setOnAction(e -> handleEdit(user));

                    // Bouton Changer MDP
                    Button btnPassword = new Button("🔑");
                    btnPassword.setStyle("-fx-background-color: #6F8D94; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnPassword.setOnAction(e -> handleChangePassword(user));

                    // Bouton Activer/Désactiver
                    Button btnToggle = new Button(user.isActif() ? "🚫" : "✅");
                    btnToggle.setStyle((user.isActif() ? "-fx-background-color: #E53E3E;" : "-fx-background-color: #46BE62;") +
                            " -fx-text-fill: white; -fx-padding: 4 10; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnToggle.setOnAction(e -> handleToggleActif(user));

                    // Bouton Supprimer
                    Button btnDelete = new Button("❌");
                    btnDelete.setStyle("-fx-background-color: #D9691D; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnDelete.setOnAction(e -> handleDelete(user));

                    actionBox.getChildren().addAll(btnEdit, btnPassword, btnToggle, btnDelete);
                    setGraphic(actionBox);
                }
            }
        });

        tableView.setItems(filteredUtilisateurs);
    }

    private void setupFilters() {
        // Filtre par rôle
        roleFilterCombo.getItems().addAll("Tous", "OPERATEUR", "TECHNICIEN", "INGENIEUR");
        roleFilterCombo.setValue("Tous");
        roleFilterCombo.setOnAction(e -> applyFilters());

        // Filtre par statut
        statutFilterCombo.getItems().addAll("Tous", "Actifs", "Inactifs");
        statutFilterCombo.setValue("Tous");
        statutFilterCombo.setOnAction(e -> applyFilters());

        // Recherche
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void loadData() {
        List<Utilisateur> users = utilisateurService.readAll();
        utilisateurs.clear();
        utilisateurs.addAll(users);
        applyFilters();
        updateStats();
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase().trim();
        String roleFilter = roleFilterCombo.getValue();
        String statutFilter = statutFilterCombo.getValue();

        List<Utilisateur> filtered = utilisateurs.stream()
                .filter(u -> {
                    // Filtre de recherche
                    boolean matchSearch = search.isEmpty() ||
                            u.getNom().toLowerCase().contains(search) ||
                            u.getUsername().toLowerCase().contains(search);

                    // Filtre de rôle
                    boolean matchRole = roleFilter.equals("Tous") ||
                            u.getRole().name().equals(roleFilter);

                    // Filtre de statut
                    boolean matchStatut = statutFilter.equals("Tous") ||
                            (statutFilter.equals("Actifs") && u.isActif()) ||
                            (statutFilter.equals("Inactifs") && !u.isActif());

                    return matchSearch && matchRole && matchStatut;
                })
                .collect(Collectors.toList());

        filteredUtilisateurs.clear();
        filteredUtilisateurs.addAll(filtered);
        updateStats();
    }

    private void updateStats() {
        if (totalLabel != null) {
            totalLabel.setText("Total: " + filteredUtilisateurs.size() + " utilisateur(s)");
        }
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-utilisateur-view.fxml"));
            Parent view = loader.load();
            AjouterUtilisateurController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            if (view instanceof Region) {
                ((Region) view).setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                ((Region) view).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            getEffectiveContentArea().getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout.", Alert.AlertType.ERROR);
        }
    }

    private void handleEdit(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier-utilisateur-view.fxml"));
            Parent view = loader.load();
            ModifierUtilisateurController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            ctrl.setUtilisateur(user);
            if (view instanceof Region) {
                ((Region) view).setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                ((Region) view).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            getEffectiveContentArea().getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification.", Alert.AlertType.ERROR);
        }
    }

    private void handleChangePassword(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/changer-mdp-view.fxml"));
            Parent view = loader.load();
            ChangerMdpController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            ctrl.setUtilisateur(user);
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            getEffectiveContentArea().getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire.", Alert.AlertType.ERROR);
        }
    }

    private void handleToggleActif(Utilisateur user) {
        // Empêcher l'utilisateur de se désactiver lui-même
        Utilisateur currentUser = SessionManager.getInstance().getUtilisateur();
        if (currentUser.getId() == user.getId() && user.isActif()) {
            showAlert("Erreur", "Vous ne pouvez pas vous désactiver vous-même", Alert.AlertType.ERROR);
            return;
        }

        String action = user.isActif() ? "désactiver" : "activer";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous " + action + " l'utilisateur " + user.getNom() + " ?");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    user.setActif(!user.isActif());
                    utilisateurService.update(user);
                    auditService.logCurrentUser(user.isActif() ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                            "utilisateur", String.valueOf(user.getId()),
                            (user.isActif() ? "Activation" : "Désactivation") + " utilisateur: " + user.getUsername());
                    loadData();
                    showAlert("Succès", "Utilisateur " + action + " avec succès", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleDelete(Utilisateur user) {
        // Vérifier qu'il ne reste pas qu'un seul INGENIEUR
        long ingenieurCount = utilisateurs.stream()
                .filter(u -> u.getRole() == Utilisateur.Role.INGENIEUR && u.isActif())
                .count();

        if (user.getRole() == Utilisateur.Role.INGENIEUR && ingenieurCount <= 1) {
            showAlert("Erreur", "Impossible de supprimer le dernier ingénieur actif", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("⚠️ Attention : Action irréversible");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + user.getNom() +
                " ?\n\nCette action ne peut pas être annulée.");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // En réalité, on désactive plutôt que de supprimer
                    user.setActif(false);
                    utilisateurService.update(user);
                    auditService.logCurrentUser("DELETE_USER", "utilisateur", String.valueOf(user.getId()),
                            "Suppression (désactivation) utilisateur: " + user.getUsername());
                    loadData();
                    showAlert("Succès", "Utilisateur supprimé avec succès", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        roleFilterCombo.setValue("Tous");
        statutFilterCombo.setValue("Tous");
        loadData();
        showAlert("Succès", "Liste actualisée", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-padding: 15;");
        alert.showAndWait();
    }
}
