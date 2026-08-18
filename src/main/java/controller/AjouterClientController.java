package controller;

import entities.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import services.AuditService;
import services.ClientService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterClientController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField codeField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField adresseField;
    @FXML
    private ImageView logoPreview;

    private ClientService service = new ClientService();
    private AuditService auditService = new AuditService();
    private String logoPath = null;

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                logoPreview.setImage(image);
                logoPath = selectedFile.getAbsolutePath();
                System.out.println("Logo sélectionné : " + logoPath);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleValider() {
        try {
            String nom = nomField.getText().trim();
            String code = codeField.getText().trim();
            String telephone = telephoneField.getText().trim();
            String email = emailField.getText().trim();
            String adresse = adresseField.getText().trim();

            if (nom.isEmpty()) {
                showAlert("Erreur", "Le nom est obligatoire", Alert.AlertType.ERROR);
                return;
            }

            Client client = new Client();
            client.setNom(nom);
            client.setCode(code.isEmpty() ? null : code);
            client.setTelephone(telephone.isEmpty() ? null : telephone);
            client.setEmail(email.isEmpty() ? null : email);
            client.setAdresse(adresse.isEmpty() ? null : adresse);
            client.setLogo(logoPath);  // ✅ Sauvegarder le chemin

            service.create(client);
            
            // Logger la création
            auditService.logCurrentUser("CREATE", "client", null, 
                "Création client: " + nom);
            
            showAlert("Succès", "Client " + nom + " ajouté avec succès !", Alert.AlertType.INFORMATION);
            handleRetour();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler() {
        nomField.clear();
        codeField.clear();
        telephoneField.clear();
        emailField.clear();
        adresseField.clear();
        logoPreview.setImage(null);
        logoPath = null;
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-client-view.fxml"));
            Pane view = loader.load();

            Node node = nomField;
            while (node != null && !(node instanceof StackPane)) {
                node = node.getParent();
            }
            if (node != null) {
                ((StackPane) node).getChildren().clear();
                ((StackPane) node).getChildren().add(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setStyle("-fx-padding: 15;");
        alert.showAndWait();
    }
}