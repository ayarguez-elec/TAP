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
import services.PouchoirService;
import services.ProduitService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ModifierPouchoirController {

    @FXML
    private TextField refField;
    @FXML
    private TextField programmeField;
    @FXML
    private ComboBox<String> clientCombo;
    @FXML
    private ComboBox<String> produitCombo;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField nbFlanField;
    @FXML
    private TextField stockField;  // ✅ Modifiable
    @FXML
    private ComboBox<String> racleCombo;
    @FXML
    private Label referenceLabel;

    private PouchoirService pouchoirService = new PouchoirService();
    private ProduitService produitService = new ProduitService();
    private ClientService clientService = new ClientService();

    private Pouchoir pouchoir;
    private List<Client> clients;
    private List<Produit> produits;

    public void setPouchoir(Pouchoir pouchoir) {
        this.pouchoir = pouchoir;
        remplirChamps();
    }

    @FXML
    public void initialize() {
        racleCombo.getItems().addAll("300", "350", "400", "460");

        try {
            clients = clientService.readAll();
            for (Client c : clients) {
                clientCombo.getItems().add(c.getNom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            produits = produitService.readAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        clientCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrerProduitsParClient(newValue);
        });
    }

    private void filtrerProduitsParClient(String nomClient) {
        produitCombo.getItems().clear();

        if (nomClient == null || nomClient.isEmpty()) {
            for (Produit p : produits) {
                produitCombo.getItems().add(p.getNomProduit());
            }
            return;
        }

        Client client = clients.stream()
                .filter(c -> c.getNom().equals(nomClient))
                .findFirst()
                .orElse(null);

        if (client != null) {
            List<Produit> produitsFiltres = produits.stream()
                    .filter(p -> p.getClientId() == client.getId())
                    .collect(Collectors.toList());

            for (Produit p : produitsFiltres) {
                produitCombo.getItems().add(p.getNomProduit());
            }

            if (produitsFiltres.isEmpty()) {
                produitCombo.getItems().add("Aucun produit pour ce client");
            }
        }
    }

    private void remplirChamps() {
        if (pouchoir == null) return;

        refField.setText(pouchoir.getRefPouchoir());
        refField.setEditable(false);
        referenceLabel.setText("Référence : " + pouchoir.getRefPouchoir());
        programmeField.setText(pouchoir.getProgramme());
        racleCombo.setValue(pouchoir.getRacle());

        quantiteField.setText(String.valueOf(pouchoir.getQuantiteTotale()));
        nbFlanField.setText(String.valueOf(pouchoir.getNbCartesParFlan()));
        stockField.setText(String.valueOf(pouchoir.getStockActuel()));
        // ✅ stockField est maintenant modifiable
    }

    @FXML
    private void handleValider() {
        try {
            String programme = programmeField.getText().trim();
            String racle = racleCombo.getValue();
            String quantiteStr = quantiteField.getText().trim();
            String nbFlanStr = nbFlanField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (programme.isEmpty()) {
                showAlert("Erreur", "Le programme est obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (quantiteStr.isEmpty() || nbFlanStr.isEmpty() || stockStr.isEmpty()) {
                showAlert("Erreur", "Tous les champs sont obligatoires", Alert.AlertType.ERROR);
                return;
            }

            int quantiteTotale;
            int nbCartesParFlan;
            int stockActuel;
            try {
                quantiteTotale = Integer.parseInt(quantiteStr);
                nbCartesParFlan = Integer.parseInt(nbFlanStr);
                stockActuel = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Les valeurs doivent être des nombres", Alert.AlertType.ERROR);
                return;
            }

            if (quantiteTotale <= 0 || nbCartesParFlan <= 0 || stockActuel < 0) {
                showAlert("Erreur", "Les valeurs doivent être positives", Alert.AlertType.ERROR);
                return;
            }

            // ✅ Vérifier que le stock ne dépasse pas la quantité totale
            if (stockActuel > quantiteTotale) {
                showAlert("Erreur", "Le stock ne peut pas dépasser la quantité totale (" + quantiteTotale + ")", Alert.AlertType.ERROR);
                return;
            }

            // ✅ Mettre à jour le pouchoir
            pouchoir.setProgramme(programme);
            pouchoir.setRacle(racle);
            pouchoir.setQuantiteTotale(quantiteTotale);
            pouchoir.setNbCartesParFlan(nbCartesParFlan);
            pouchoir.setStockActuel(stockActuel);

            pouchoirService.update(pouchoir);

            showAlert("Succès",
                    "Pochoir " + pouchoir.getRefPouchoir() + " modifié avec succès !\n" +
                            "Nouveau stock: " + stockActuel + " cartes",
                    Alert.AlertType.INFORMATION);
            handleRetour();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-pouchoir-view.fxml"));
            Pane view = loader.load();

            Node node = refField;
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