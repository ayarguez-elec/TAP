package controller;

import entities.Client;
import entities.Pouchoir;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import services.ClientService;
import services.ProduitService;
import services.PouchoirService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ModifierProduitController {

    @FXML
    private TextField idField;
    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<String> ecranCombo;
    @FXML
    private TextField programmeField;
    @FXML
    private TextField racleField;
    @FXML
    private TextField clientField;
    @FXML
    private Label produitInfoLabel;

    private ProduitService produitService = new ProduitService();
    private ClientService clientService = new ClientService();
    private PouchoirService pouchoirService = new PouchoirService();

    private Produit produit;
    private Client client;
    private List<Pouchoir> pouchoirs;
    private DetailsClientController parentController;

    public void setProduit(Produit produit) {
        this.produit = produit;
        remplirChamps();
    }

    public void setParentController(DetailsClientController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        chargerPouchoirs();
    }

    private void chargerPouchoirs() {
        try {
            pouchoirs = pouchoirService.readAll();

            List<String> references = pouchoirs.stream()
                    .filter(p -> "disponible".equals(p.getStatut()))
                    .map(Pouchoir::getRefPouchoir)
                    .collect(Collectors.toList());

            ecranCombo.getItems().addAll(references);

            if (references.isEmpty()) {
                ecranCombo.getItems().add("Aucun pouchoir disponible");
            }

            System.out.println("✅ " + references.size() + " pouchoirs disponibles chargés");
        } catch (SQLException e) {
            e.printStackTrace();
            ecranCombo.getItems().add("Erreur de chargement");
        }
    }

    private void remplirChamps() {
        if (produit == null) return;

        idField.setText(String.valueOf(produit.getId()));
        nomField.setText(produit.getNomProduit());
        produitInfoLabel.setText("Produit : " + produit.getNomProduit());

        // Charger le client
        try {
            client = clientService.read(produit.getClientId());
            if (client != null) {
                clientField.setText(client.getNom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sélectionner le pouchoir actuel
        if (produit.getEcran() != null) {
            ecranCombo.setValue(produit.getEcran());
            // Remplir programme et racle
            handleEcranSelected();
        }
    }

    // ✅ Quand un pouchoir est sélectionné → remplir programme et racle
    @FXML
    private void handleEcranSelected() {
        String selectedRef = ecranCombo.getValue();

        if (selectedRef == null || selectedRef.isEmpty() ||
                selectedRef.equals("Aucun pouchoir disponible") ||
                selectedRef.equals("Erreur de chargement")) {
            programmeField.clear();
            racleField.clear();
            return;
        }

        Pouchoir pouchoir = pouchoirs.stream()
                .filter(p -> selectedRef.equals(p.getRefPouchoir()))
                .findFirst()
                .orElse(null);

        if (pouchoir != null) {
            programmeField.setText(pouchoir.getProgramme());
            racleField.setText(pouchoir.getRacle() + " mm");
            System.out.println("✅ Programme et racle chargés pour " + selectedRef);
        } else {
            programmeField.clear();
            racleField.clear();
        }
    }

    // ✅ Nouveau pouchoir → rediriger vers Gestion des Pouchoirs
    @FXML
    private void handleNewEcran() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-pouchoir-view.fxml"));
            Pane gestionView = loader.load();

            Node node = nomField;
            while (node != null && !(node instanceof StackPane)) {
                node = node.getParent();
            }

            if (node != null) {
                ((StackPane) node).getChildren().clear();
                ((StackPane) node).getChildren().add(gestionView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des pouchoirs", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleValider() {
        try {
            String nom = nomField.getText().trim();
            String refPouchoir = ecranCombo.getValue();

            if (nom.isEmpty()) {
                showAlert("Erreur", "Le nom du produit est obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (refPouchoir == null || refPouchoir.isEmpty() ||
                    refPouchoir.equals("Aucun pouchoir disponible") ||
                    refPouchoir.equals("Erreur de chargement")) {
                showAlert("Erreur", "Veuillez sélectionner un pouchoir", Alert.AlertType.ERROR);
                return;
            }

            if (client == null) {
                showAlert("Erreur", "Aucun client sélectionné", Alert.AlertType.ERROR);
                return;
            }

            // ✅ Mettre à jour le produit
            produit.setNomProduit(nom);
            produit.setEcran(refPouchoir);

            produitService.update(produit);

            showAlert("Succès", "✅ Produit " + nom + " modifié avec succès !", Alert.AlertType.INFORMATION);
            handleRetour();

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler() {
        remplirChamps();
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-client-view.fxml"));
            Pane detailsView = loader.load();

            DetailsClientController controller = loader.getController();
            controller.setClient(client);

            Node node = nomField;
            while (node != null && !(node instanceof StackPane)) {
                node = node.getParent();
            }
            if (node != null) {
                ((StackPane) node).getChildren().clear();
                ((StackPane) node).getChildren().add(detailsView);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir", Alert.AlertType.ERROR);
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