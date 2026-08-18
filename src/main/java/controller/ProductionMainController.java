package controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import security.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductionMainController {

    @FXML private StackPane contentArea;
    @FXML private Label clockLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userInitialsLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnPlanning;
    @FXML private Button btnAnomalie;
    @FXML private Button btnDeconnexion;

    private Button lastSelected;

    @FXML
    public void initialize() {
        startClock();
        updateUserInfo();
        // Afficher dashboard par defaut
        Platform.runLater(this::showDashboard);
    }

    private void startClock() {
        if (clockLabel == null) return;
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy  HH:mm:ss")));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void updateUserInfo() {
        if (!SessionManager.getInstance().isAuthenticated()) return;
        entities.Utilisateur u = SessionManager.getInstance().getUtilisateur();
        if (userNameLabel != null) userNameLabel.setText(u.getNom());
        if (userInitialsLabel != null) {
            String[] parts = u.getNom().trim().split("\\s+");
            String initials = parts.length >= 2
                    ? (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase()
                    : u.getNom().substring(0, Math.min(2, u.getNom().length())).toUpperCase();
            userInitialsLabel.setText(initials);
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/production-dashboard-view.fxml", btnDashboard);
    }

    @FXML
    public void showPlanning() {
        loadView("/production-planning-view.fxml", btnPlanning);
    }

    @FXML
    public void showAnomalie() {
        loadView("/production-anomalie-view.fxml", btnAnomalie);
    }

    private void loadView(String fxml, Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            if (view instanceof Region r) {
                r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            contentArea.getChildren().setAll(view);
            selectButton(btn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectButton(Button btn) {
        if (lastSelected != null) lastSelected.getStyleClass().remove("menu-button-category-selected");
        if (btn != null) { btn.getStyleClass().add("menu-button-category-selected"); lastSelected = btn; }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().stopTimeoutTimer();
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.close();
            Stage loginStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            loginStage.setScene(scene);
            loginStage.setTitle("Connexion - Lacroix Electronics");
            loginStage.setMinWidth(500); loginStage.setMinHeight(400);
            loginStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}