package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.UpdateChecker;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Check for updates in background (non-blocking)
        UpdateChecker.checkForUpdates();

        // Load login view as the initial screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
        Scene scene = new Scene(loader.load());

        // Apply CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configure window properties
        primaryStage.setTitle("Connexion - Lacroix Electronics");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}