package controller;

import controller.AdminUtilisateursController;
import controller.FicheSerigraphieController;
import entities.Pouchoir;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import security.SessionManager;
import services.PouchoirService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Locale;

public class MainController {

    // ✅ MOT DE PASSE (tu peux le changer ici)
    private static final String PASSWORD = "admin123";

    @FXML
    private StackPane contentArea;
    @FXML
    private Label totalPouchoirs;
    @FXML
    private Label disponibles;
    @FXML
    private Label sortis;
    @FXML
    private Label totalProduits;
    @FXML
    private Label notificationBadge;
    @FXML
    private StackPane notificationBadgeContainer;
    @FXML
    private Label userInitialsLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnNotifications;
    @FXML
    private Button btnNettoyage;
    @FXML
    private Button btnBoard;
    @FXML
    private Button btnSuivi;
    @FXML
    private Button btnHistorique;
    @FXML
    private Button btnGestionClients;
    @FXML
    private Button btnGestionPouchoirs;
    @FXML
    private Button btnStatistiques;
    @FXML
    private Button btnRapports;
    @FXML
    private Button btnGestionUsers;
    @FXML
    private Button btnPermissions;
    @FXML
    private Button btnPlanning;
    @FXML
    private Button btnAnomalies;
    @FXML
    private Button btnDeconnexion;
    @FXML private ComboBox<String> comboLanguage;
    @FXML private Button btnThemeToggle;
    @FXML private Label themeIconLabel;

    private boolean isDarkTheme = false;
    public static ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("fr"));

    private Button lastSelectedButton;
    private String currentFxmlPath = "/tableau-de-board-view.fxml";
    private PouchoirService pouchoirService = new PouchoirService();
    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    @FXML
    public void initialize() {
        instance = this;
        updateUserInfo();
        updateStats();
        updateNotificationBadge();
        
        if (comboLanguage != null) {
            comboLanguage.getItems().addAll("Français", "English");
            comboLanguage.getSelectionModel().selectFirst();
        }

        selectButton(btnBoard);
        showBoard();
        // Start session timeout after scene is ready
        javafx.application.Platform.runLater(this::startSessionTimeout);
    }

    private void startSessionTimeout() {
        entities.Utilisateur user = security.SessionManager.getInstance().getUtilisateur();
        if (user == null) {
            System.out.println("[TIMEOUT] Pas d utilisateur en session - timer non demarre.");
            return;
        }
        if (user.getRole() == entities.Utilisateur.Role.OPERATEUR) {
            System.out.println("[TIMEOUT] OPERATEUR - pas de timeout.");
            return;
        }

        System.out.println("[TIMEOUT] Demarrage timer pour: " + user.getUsername() + " (" + user.getRole() + ")");

        javafx.scene.Scene scene = contentArea.getScene();
        if (scene != null) {
            registerActivityListeners(scene);
            security.SessionManager.getInstance().startTimeoutTimer(this::performAutoLogout);
        } else {
            System.out.println("[TIMEOUT] Scene null - attente sceneProperty...");
            contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    System.out.println("[TIMEOUT] Scene attachee - listeners enregistres");
                    registerActivityListeners(newScene);
                    security.SessionManager.getInstance().startTimeoutTimer(this::performAutoLogout);
                }
            });
        }
    }

    /**
     * Registers ONLY real human input events to reset inactivity timer.
     * Excludes synthetic/system-generated events to avoid false resets.
     */
    private void registerActivityListeners(javafx.scene.Scene scene) {
        // Only real mouse clicks and movements by user
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
            e -> { if (!e.isSynthesized()) { security.SessionManager.getInstance().resetActivity(); } });
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED,
            e -> { if (!e.isSynthesized()) { security.SessionManager.getInstance().resetActivity(); } });
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED,
            e -> { if (!e.isSynthesized()) { security.SessionManager.getInstance().resetActivity(); } });

        // Only real keyboard presses by user
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,
            e -> { if (!e.getCode().isFunctionKey() || !e.isShiftDown()) {
                       security.SessionManager.getInstance().resetActivity();
                   }
            });

        // Scroll wheel
        scene.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL,
            e -> security.SessionManager.getInstance().resetActivity());

        System.out.println("[TIMEOUT] Listeners enregistres sur evenements humains uniquement");
    }

    /**
     * Auto-logout after inactivity with warning message.
     */
    private void performAutoLogout() {
        security.SessionManager.getInstance().stopTimeoutTimer();
        security.SessionManager.getInstance().logout();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session expirée");
        alert.setHeaderText("Déconnexion automatique");
        alert.setContentText("Votre session a expiré après 5 minutes d'inactivité.\nVeuillez vous reconnecter.");
        alert.showAndWait();

        returnToLogin();
    }

    /**
     * Returns to login screen. Used by both manual logout and auto-logout.
     */
    public void returnToLogin() {
        try {
            Stage currentStage = (Stage) contentArea.getScene().getWindow();
            currentStage.close();

            Stage loginStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);

            loginStage.setScene(loginScene);
            loginStage.setTitle("Connexion - Lacroix Electronics");
            loginStage.setMinWidth(500);
            loginStage.setMinHeight(400);
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserInfo() {
        if (SessionManager.getInstance().isAuthenticated()) {
            entities.Utilisateur user = SessionManager.getInstance().getUtilisateur();
            if (userNameLabel != null) userNameLabel.setText(user.getNom());
            if (userRoleLabel != null) userRoleLabel.setText(user.getRole() != null ? user.getRole().toString() : "");
            if (userInitialsLabel != null) userInitialsLabel.setText(extractInitials(user.getNom()));
        } else {
            if (userNameLabel != null) userNameLabel.setText("Utilisateur");
            if (userRoleLabel != null) userRoleLabel.setText("Opérateur");
            if (userInitialsLabel != null) userInitialsLabel.setText("U");
        }
    }

    private String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        } else {
            return parts[0].substring(0, 1).toUpperCase();
        }
    }

    private void updateStats() {
        try {
            List<Pouchoir> pouchoirs = pouchoirService.readAll();
            totalPouchoirs.setText(String.valueOf(pouchoirs.size()));

            long dispo = pouchoirs.stream().filter(p -> "disponible".equals(p.getStatut())).count();
            long sorti = pouchoirs.stream().filter(p -> "sorti".equals(p.getStatut())).count();

            disponibles.setText(String.valueOf(dispo));
            sortis.setText(String.valueOf(sorti));

            totalProduits.setText("0");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateNotificationBadge() {
        long unreadCount = countUnreadNotifications();
        if (notificationBadge != null) {
            notificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        }
        if (notificationBadgeContainer != null) {
            notificationBadgeContainer.setVisible(unreadCount > 0);
        }
    }

    private long countUnreadNotifications() {
        java.io.File file = new java.io.File("notifications.ser");
        if (file.exists()) {
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                List<?> list = (List<?>) ois.readObject();
                long unread = 0;
                for (Object obj : list) {
                    try {
                        java.lang.reflect.Method isLuMethod = obj.getClass().getMethod("isLu");
                        Boolean isLu = (Boolean) isLuMethod.invoke(obj);
                        if (isLu != null && !isLu) {
                            unread++;
                        }
                    } catch (Exception ignored) {}
                }
                return unread;
            } catch (Exception ignored) {}
        }
        try {
            List<Pouchoir> pouchoirs = pouchoirService.readAll();
            return pouchoirs.stream().filter(p -> "sorti".equalsIgnoreCase(p.getStatut())).count();
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean verifierMotDePasseStatic() {
        // Créer le dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🔐 Accès restreint");
        dialog.setHeaderText("Veuillez entrer le mot de passe");

        // Contenu
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("🔒");
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label messageLabel = new Label("Cette section est protégée par un mot de passe");
        messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Entrez le mot de passe");
        passwordField.setStyle("-fx-padding: 8 12; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-color: #d0d5dd;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        content.getChildren().addAll(iconLabel, messageLabel, passwordField, errorLabel);

        ButtonType confirmButton = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(380);

        // ✅ Gérer le résultat
        Optional<ButtonType> result = dialog.showAndWait();

        // ✅ Si annulé ou fermé
        if (result.isEmpty() || result.get() == ButtonType.CANCEL || result.get() == ButtonType.CLOSE) {
            return false;
        }

        // ✅ Si validé
        if (result.get() == confirmButton) {
            String password = passwordField.getText();
            if (PASSWORD.equals(password)) {
                return true;
            } else {
                // ✅ Afficher l'erreur et réessayer
                errorLabel.setText("❌ Mot de passe incorrect !");
                errorLabel.setVisible(true);

                // ✅ Réafficher le dialog
                Optional<ButtonType> retry = dialog.showAndWait();
                if (retry.isEmpty() || retry.get() == ButtonType.CANCEL || retry.get() == ButtonType.CLOSE) {
                    return false;
                }
                if (retry.get() == confirmButton) {
                    String retryPassword = passwordField.getText();
                    return PASSWORD.equals(retryPassword);
                }
            }
        }

        return false;
    }

    // ===== ACCÈS DIRECT SANS MOT DE PASSE =====
    @FXML
    private void showGestionClients() {
        loadView("/gestion-client-view.fxml", btnGestionClients);
    }

    @FXML
    private void showGestionPouchoirs() {
        loadView("/gestion-pouchoir-view.fxml", btnGestionPouchoirs);
    }

    @FXML
    private void showNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/notifications-view.fxml"));
            Pane notificationsView = loader.load();

            notificationsView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(notificationsView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les notifications", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showNettoyage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nettoyage-view.fxml"));
            Pane nettoyageView = loader.load();

            nettoyageView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(nettoyageView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page Nettoyage Ecran", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void selectButton(Button selected) {
        if (lastSelectedButton != null) {
            lastSelectedButton.getStyleClass().remove("menu-button-category-selected");
        }
        if (selected != null) {
            selected.getStyleClass().add("menu-button-category-selected");
            lastSelectedButton = selected;
        }
    }

    private void loadView(String fxmlPath, Button selectedButton) {
        try {
            // Use the existing bundle from MainController
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setResources(bundle); // ✅ Pass the resource bundle
            Parent view = loader.load();

            if (view instanceof Region) {
                ((Region) view).setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                ((Region) view).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            selectButton(selectedButton);

            // Re-apply theme after view load
            if (isDarkTheme) {
                final javafx.scene.Parent finalView = view;
                javafx.application.Platform.runLater(() -> {
                    forceDarkNodes(finalView);
                    if (contentArea.getScene() != null) applyTheme(contentArea.getScene());
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de : " + fxmlPath);
        }
    }

    @FXML
    private void showBoard() {
        loadView("/tableau-de-board-view.fxml", btnBoard);
    }

    @FXML
    private void showSuivi() {
        loadView("/suivi-pouchoir-view.fxml", btnSuivi);
    }

    @FXML
    private void showHistorique() {
        // Tous les utilisateurs peuvent consulter l'historique
        // Pas de restriction de permission requise pour la consultation
        loadView("/historique-view.fxml", btnHistorique);
    }

    @FXML
    private void showRapports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fiche-serigraphie-view.fxml"));
            Parent view = loader.load();
            FicheSerigraphieController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            contentArea.getChildren().setAll(view);
            selectButton(btnRapports);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showGestionUtilisateurs() {
        if (!security.SessionManager.getInstance().isAuthenticated() ||
            !security.SessionManager.getInstance().hasRole(entities.Utilisateur.Role.INGENIEUR, entities.Utilisateur.Role.ADMIN)) {
            showAlert("Accès refusé", "Réservé aux ingénieurs", Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin-utilisateurs-view.fxml"));
            Parent view = loader.load();
            AdminUtilisateursController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            contentArea.getChildren().setAll(view);
            selectButton(btnGestionUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showPermissions() {
        if (!security.PermissionGuard.checkPermission(security.PermissionGuard.ADMIN_USERS)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/permissions-view.fxml"));
            Parent view = loader.load();
            if (view instanceof Region r) {
                r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            contentArea.getChildren().setAll(view);
            selectButton(btnPermissions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showPlanning() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/production-planning-view.fxml"));
            Parent view = loader.load();
            // Mode lecture seule : masquer ajout/suppression
            controller.ProductionPlanningController ctrl = loader.getController();
            ctrl.setReadonly(true);
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            contentArea.getChildren().setAll(view);
            selectButton(btnPlanning);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void showAnomalies() {
        // Vue lecture seule des anomalies (sans bouton signaler)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/anomalies-readonly-view.fxml"));
            Parent view = loader.load();
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            contentArea.getChildren().setAll(view);
            selectButton(btnAnomalies);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Walks the scene graph and replaces light inline styles with dark equivalents.
     * This handles inline style= attributes that CSS selectors cannot override.
     */
    private void forceDarkNodes(javafx.scene.Parent parent) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            String style = node.getStyle();
            if (style != null && !style.isEmpty()) {
                String s = style;

                // Replace ANY white or very light background
                s = s.replaceAll(
                    "-fx-background-color\\s*:\\s*(#(?:fff(?:fff)?|f[89a-f][f89a-f][0-9a-f]{3}|f0f2f5|f5f6fa|f8fafc|fafbfc|f0f7fa|eaf0f5|e8eaee|e2e8f0|ffffff|FFFFFF)|rgba?\\([^)]{1,30}\\))[^;]*;?",
                    "-fx-background-color: #252B42;"
                );

                // Replace dark text colors (visible on light but invisible on dark)
                s = s.replaceAll(
                    "-fx-text-fill\\s*:\\s*#(?:000|111|21262[aA]|2D383E|384B52|374151|374151|4[aA]5568|1[aA]202[aA]|21262a|2d2d|333)[^;]*;?",
                    "-fx-text-fill: #E0E6F8;"
                );
                // Also catch rgb(0,0,0) style text
                s = s.replaceAll(
                    "-fx-text-fill\\s*:\\s*rgba?\\(\\s*0\\s*,\\s*0\\s*,\\s*0[^)]*\\)[^;]*;?",
                    "-fx-text-fill: #E0E6F8;"
                );

                // Replace border colors that are very light
                s = s.replaceAll(
                    "-fx-border-color\\s*:\\s*#(?:e2e8f0|E2E8F0|d0d5dd|D0D5DD|e8eaee|E8EAEE|CBD5E1|cbd5e1)[^;]*;?",
                    "-fx-border-color: #363D60;"
                );

                if (!s.equals(style)) node.setStyle(s);
            }
            if (node instanceof javafx.scene.Parent p) forceDarkNodes(p);
        }
    }

    @FXML
    private void handleLogout() {
        security.SessionManager.getInstance().stopTimeoutTimer();
        security.SessionManager.getInstance().logout();
        returnToLogin();
    }

    @FXML
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        // Use Platform.runLater to ensure scene is ready
        javafx.application.Platform.runLater(() -> applyTheme(contentArea.getScene()));
    }

    public void applyTheme(javafx.scene.Scene scene) {
        if (scene == null) return;
        java.net.URL darkUrl = getClass().getResource("/css/dark-theme.css");
        if (darkUrl == null) {
            System.err.println("[THEME] dark-theme.css not found!");
            return;
        }
        String darkCss = darkUrl.toExternalForm();
        javafx.scene.Parent root = scene.getRoot();

        if (isDarkTheme) {
            if (!scene.getStylesheets().contains(darkCss))
                scene.getStylesheets().add(darkCss);
            if (!root.getStyleClass().contains("dark"))
                root.getStyleClass().add("dark");
            // Force dark on inline-styled nodes
            javafx.application.Platform.runLater(() -> forceDarkNodes(root));
            if (themeIconLabel != null) themeIconLabel.setText("\u2600"); // sun
        } else {
            scene.getStylesheets().remove(darkCss);
            root.getStyleClass().remove("dark");
            if (themeIconLabel != null) themeIconLabel.setText("\u263E"); // moon
        }
    }
    @FXML
    private void changeLanguage() {
        if (comboLanguage.getValue() == null) return;
        String lang = comboLanguage.getValue();
        bundle = "English".equals(lang)
                ? ResourceBundle.getBundle("messages", Locale.ENGLISH)
                : ResourceBundle.getBundle("messages", Locale.FRENCH);

        // Update sidebar button texts
        String[][] sidebarKeys = {
            {"nav.dashboard",     null},  // btnBoard - use fx:id mapping below
        };
        updateSidebarTexts();

        // Reload current view with new bundle
        if (currentFxmlPath != null) {
            loadView(currentFxmlPath, lastSelectedButton);
        }
    }

    private void updateSidebarTexts() {
        try {
            if (btnBoard != null)           btnBoard.setText(bundle.getString("nav.dashboard"));
            if (btnSuivi != null)           btnSuivi.setText(bundle.getString("nav.suivi"));
            if (btnHistorique != null)      btnHistorique.setText(bundle.getString("nav.historique"));
            if (btnPlanning != null)        btnPlanning.setText(bundle.getString("nav.planning"));
            if (btnAnomalies != null)       btnAnomalies.setText(bundle.getString("nav.anomalies"));
            if (btnGestionClients != null)  btnGestionClients.setText(bundle.getString("nav.clients"));
            if (btnGestionPouchoirs != null) btnGestionPouchoirs.setText(bundle.getString("nav.pochoirs"));
            if (btnRapports != null)        btnRapports.setText(bundle.getString("nav.fiches"));
            if (btnGestionUsers != null)    btnGestionUsers.setText(bundle.getString("nav.utilisateurs"));
            if (btnPermissions != null)     btnPermissions.setText(bundle.getString("nav.permissions"));
            if (btnDeconnexion != null)     btnDeconnexion.setText("\uD83D\uDEAA  " + bundle.getString("nav.logout"));
        } catch (Exception e) {
            System.err.println("[LANG] Key missing: " + e.getMessage());
        }
    }
}
