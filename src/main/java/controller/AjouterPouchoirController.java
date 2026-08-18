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

public class AjouterPouchoirController {

    private static final int TOTAL_EMPLACEMENTS = 300;

    @FXML
    private TextField refField;
    @FXML
    private TextField programmeField;
    @FXML
    private ComboBox<String> faceCombo;  // ✅ AJOUTÉ
    @FXML
    private ComboBox<String> clientCombo;
    @FXML
    private ComboBox<String> produitCombo;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField nbFlanField;
    @FXML
    private ComboBox<String> racleCombo;

    private PouchoirService pouchoirService = new PouchoirService();
    private ProduitService produitService = new ProduitService();
    private ClientService clientService = new ClientService();

    private List<Client> clients;
    private List<Produit> produits;
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Racle
        racleCombo.getItems().addAll("300", "350", "400", "460");
        racleCombo.setValue("300");

        // ✅ Face (1 ou 2)
        faceCombo.getItems().addAll("1", "2");
        faceCombo.setValue("1");

        // Charger les clients
        try {
            clients = clientService.readAll();
            for (Client c : clients) {
                clientCombo.getItems().add(c.getNom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Charger les produits
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

    // NOUVEAU CLIENT - Redirige vers Gestion Clients
    @FXML
    private void handleNewClient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-client-view.fxml"));
            Pane gestionClientView = loader.load();

            Node node = refField;
            while (node != null && !(node instanceof StackPane)) {
                node = node.getParent();
            }

            if (node != null) {
                contentArea = (StackPane) node;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(gestionClientView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des clients", Alert.AlertType.ERROR);
        }
    }

    // NOUVEAU PRODUIT - Redirige vers Détails Client (avec le client sélectionné)
    @FXML
    private void handleNewProduit() {
        String selectedClient = clientCombo.getValue();

        if (selectedClient == null || selectedClient.isEmpty()) {
            showAlert("Erreur", "Veuillez d'abord sélectionner un client", Alert.AlertType.ERROR);
            return;
        }

        Client client = clients.stream()
                .filter(c -> c.getNom().equals(selectedClient))
                .findFirst()
                .orElse(null);

        if (client == null) {
            showAlert("Erreur", "Client non trouvé", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-client-view.fxml"));
            Pane detailsView = loader.load();

            DetailsClientController controller = loader.getController();
            controller.setClient(client);

            Node node = refField;
            while (node != null && !(node instanceof StackPane)) {
                node = node.getParent();
            }

            if (node != null) {
                contentArea = (StackPane) node;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(detailsView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails du client", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleValider() {
        try {
            String ref = refField.getText().trim().toUpperCase();
            String programme = programmeField.getText().trim();
            String client = clientCombo.getValue();
            String produit = produitCombo.getValue();
            String quantiteStr = quantiteField.getText().trim();
            String nbFlanStr = nbFlanField.getText().trim();
            String racle = racleCombo.getValue();
            int face = Integer.parseInt(faceCombo.getValue());  // ✅ Récupérer la face

            // Vérifications
            if (ref.isEmpty()) {
                showAlert("Erreur", "La référence est obligatoire", Alert.AlertType.ERROR);
                return;
            }
            if (programme.isEmpty()) {
                showAlert("Erreur", "Le programme est obligatoire", Alert.AlertType.ERROR);
                return;
            }
            if (client == null || client.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un client", Alert.AlertType.ERROR);
                return;
            }
            if (produit == null || produit.isEmpty() || produit.equals("Aucun produit pour ce client")) {
                showAlert("Erreur", "Veuillez sélectionner un produit", Alert.AlertType.ERROR);
                return;
            }
            if (quantiteStr.isEmpty()) {
                showAlert("Erreur", "La quantité est obligatoire", Alert.AlertType.ERROR);
                return;
            }
            if (nbFlanStr.isEmpty()) {
                showAlert("Erreur", "Le nombre de cartes dans flan est obligatoire", Alert.AlertType.ERROR);
                return;
            }

            int quantiteTotale;
            int nbCartesParFlan;
            try {
                quantiteTotale = Integer.parseInt(quantiteStr);
                nbCartesParFlan = Integer.parseInt(nbFlanStr);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "La quantité et le nombre de cartes par flan doivent être des nombres", Alert.AlertType.ERROR);
                return;
            }

            if (quantiteTotale <= 0 || nbCartesParFlan <= 0) {
                showAlert("Erreur", "Les valeurs doivent être supérieures à 0", Alert.AlertType.ERROR);
                return;
            }

            Pouchoir existing = pouchoirService.readByReference(ref);
            if (existing != null) {
                showAlert("Erreur", "Cette référence existe déjà !", Alert.AlertType.ERROR);
                return;
            }

            int emplacementLibre = trouverEmplacementLibre();

            if (emplacementLibre == -1) {
                showAlert("Erreur", "Aucun emplacement libre disponible !", Alert.AlertType.ERROR);
                return;
            }

            int x = quantiteTotale / nbCartesParFlan;
            int stockInitial = quantiteTotale - x;

            // ✅ Créer le pouchoir avec la face
            Pouchoir pouchoir = new Pouchoir(
                    ref, programme, racle, emplacementLibre, "disponible", face,
                    quantiteTotale, nbCartesParFlan, stockInitial
            );

            pouchoirService.create(pouchoir);

            showAlert("Succès",
                    "Pochoir " + ref + " ajouté avec succès !\n" +
                            "Affecté à l'emplacement " + emplacementLibre + "\n" +
                            "Programme: " + programme + "\n" +
                            "Racle: " + racle + " mm\n" +
                            "Face: " + face + "\n" +
                            "Quantité totale: " + quantiteTotale + " cartes\n" +
                            "Par flan: " + nbCartesParFlan + " cartes\n" +
                            "Stock initial: " + stockInitial + " cartes",
                    Alert.AlertType.INFORMATION);

            handleRetour();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private int trouverEmplacementLibre() throws SQLException {
        List<Pouchoir> pouchoirs = pouchoirService.readAll();

        List<Integer> emplacementsOccupes = pouchoirs.stream()
                .filter(p -> "disponible".equals(p.getStatut()))
                .map(Pouchoir::getEmplacement)
                .collect(Collectors.toList());

        for (int i = 1; i <= TOTAL_EMPLACEMENTS; i++) {
            if (!emplacementsOccupes.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    @FXML
    private void handleAnnuler() {
        refField.clear();
        programmeField.clear();
        clientCombo.setValue(null);
        produitCombo.setValue(null);
        quantiteField.clear();
        nbFlanField.clear();
        racleCombo.setValue("300");
        faceCombo.setValue("1");  // ✅ Réinitialiser face
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