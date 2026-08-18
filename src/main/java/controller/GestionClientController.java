package controller;

import entities.Client;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import security.PermissionGuard;
import services.AuditService;
import services.ClientService;
import services.ProduitService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class GestionClientController {

    @FXML
    private TextField searchField;
    @FXML
    private TilePane cardContainer;
    @FXML
    private Label totalLabel;

    private ClientService clientService = new ClientService();
    private ProduitService produitService = new ProduitService();  // ✅ Non statique
    private AuditService auditService = new AuditService();
    private List<Client> clients;

    @FXML
    public void initialize() {
        if (cardContainer != null) {
            cardContainer.setPrefColumns(4);
            cardContainer.setPrefTileWidth(220);
            cardContainer.setPrefTileHeight(200);
            cardContainer.setHgap(15);
            cardContainer.setVgap(15);
            cardContainer.setPadding(Insets.EMPTY);
        }

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCards(newValue);
            });
        }

        loadData();
    }

    private void loadData() {
        try {
            clients = clientService.readAll();
            displayCards(clients);
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données", Alert.AlertType.ERROR);
        }
    }

    private void filterCards(String searchText) {
        if (clients == null) return;

        if (searchText == null || searchText.trim().isEmpty()) {
            displayCards(clients);
            return;
        }

        String search = searchText.toLowerCase().trim();
        List<Client> filtered = clients.stream()
                .filter(c -> c.getNom().toLowerCase().contains(search))
                .collect(Collectors.toList());

        displayCards(filtered);
    }

    private void displayCards(List<Client> clients) {
        if (cardContainer == null) return;

        cardContainer.getChildren().clear();

        if (clients == null || clients.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 0, 50, 0));

            Label emptyLabel = new Label("📭");
            emptyLabel.setStyle("-fx-font-size: 48px;");
            Label emptyText = new Label("Aucun client trouvé");
            emptyText.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");

            emptyBox.getChildren().addAll(emptyLabel, emptyText);
            cardContainer.getChildren().add(emptyBox);
            return;
        }

        for (Client c : clients) {
            VBox card = createCard(c);
            cardContainer.getChildren().add(card);
        }

        updateStats();
    }

    private VBox createCard(Client client) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 15, 15, 15));
        card.setPrefWidth(220);
        card.setPrefHeight(200);
        card.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);" +
                        "-fx-border-color: #e8eaee;" +
                        "-fx-border-width: 1;"
        );
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);" +
                            "-fx-border-color: #3A98A5;" +
                            "-fx-border-width: 2;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);" +
                            "-fx-border-color: #e8eaee;" +
                            "-fx-border-width: 1;"
            );
        });

        // ✅ Logo ou avatar par défaut
        Node avatarNode;
        if (client.getLogo() != null && !client.getLogo().isEmpty()) {
            try {
                // ✅ Charger l'image depuis le dossier resources
                String imagePath = client.getLogo();
                // Si le chemin commence par /, on le garde, sinon on l'ajoute
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }

                Image logoImage = new Image(getClass().getResourceAsStream(imagePath));
                if (logoImage != null && !logoImage.isError()) {
                    ImageView logoView = new ImageView(logoImage);
                    logoView.setFitHeight(50);
                    logoView.setFitWidth(50);
                    logoView.setPreserveRatio(true);
                    logoView.setStyle("-fx-border-radius: 25; -fx-background-radius: 25;");
                    avatarNode = logoView;
                } else {
                    // Fallback si l'image ne se charge pas
                    Label fallbackIcon = new Label("👤");
                    fallbackIcon.setStyle("-fx-font-size: 32px;");
                    avatarNode = fallbackIcon;
                }
            } catch (Exception e) {
                // Fallback en cas d'erreur
                Label fallbackIcon = new Label("👤");
                fallbackIcon.setStyle("-fx-font-size: 32px;");
                avatarNode = fallbackIcon;
                System.err.println("❌ Erreur chargement logo pour " + client.getNom() + ": " + e.getMessage());
            }
        } else {
            Label avatarLabel = new Label("👤");
            avatarLabel.setStyle("-fx-font-size: 32px;");
            avatarNode = avatarLabel;
        }

        // Nom
        Label nomLabel = new Label(client.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #21262A;");
        nomLabel.setAlignment(Pos.CENTER);
        nomLabel.setWrapText(true);

        // Code
        Label codeLabel = new Label((client.getCode() != null ? client.getCode() : "N/A"));
        codeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        // ✅ Nombre de produits
        int nbProduits = getNombreProduits(client.getId());
        Label produitsLabel = new Label(nbProduits + " produit" + (nbProduits > 1 ? "s" : ""));
        produitsLabel.setStyle("-fx-text-fill: #3A98A5; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Boutons d'action
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        // Bouton Voir (👁️)
        Button viewButton = new Button("👁");
        viewButton.setStyle(
                "-fx-background-color: #346771;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 6 10;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        viewButton.setOnAction(e -> handleView(client));

        // Bouton Modifier (✏️)
        Button editButton = new Button("📝");
        editButton.setStyle(
                "-fx-background-color: #346771;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 6 10;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        editButton.setOnAction(e -> handleEdit(client));

        // Bouton Supprimer (🗑️)
        Button deleteButton = new Button("❌");
        deleteButton.setStyle(
                "-fx-background-color: #D9691D;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 6 10;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(e -> handleDelete(client));

        actionBox.getChildren().addAll(viewButton, editButton, deleteButton);

        card.getChildren().addAll(
                avatarNode,
                nomLabel,
                codeLabel,
                produitsLabel,
                actionBox
        );

        return card;
    }
    // ✅ Méthode non statique
    private int getNombreProduits(int clientId) {
        try {
            List<Produit> produits = produitService.readAll();
            return (int) produits.stream()
                    .filter(p -> p.getClientId() == clientId)
                    .count();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateStats() {
        if (clients == null) return;
        if (totalLabel != null) {
            totalLabel.setText("Total: " + clients.size());
        }
    }

    @FXML
    private void handleAdd() {
        // Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_CLIENT)) {
            return;
        }
        
        // Logger l'action
        auditService.logCurrentUser("CREATE", "client", null, "Ouverture formulaire ajout client");
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-client-view.fxml"));
            Pane addView = loader.load();

            addView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Node node = cardContainer;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = cardContainer;
                while (node != null) {
                    if (node instanceof StackPane) {
                        contentArea = (StackPane) node;
                        break;
                    }
                    node = node.getParent();
                }
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(addView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        showAlert("Rafraîchissement", "Liste actualisée", Alert.AlertType.INFORMATION);
    }

    private void handleView(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-client-view.fxml"));
            Pane detailsView = loader.load();

            DetailsClientController controller = loader.getController();
            controller.setClient(client);

            Node node = cardContainer;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = cardContainer;
                while (node != null) {
                    if (node instanceof StackPane) {
                        contentArea = (StackPane) node;
                        break;
                    }
                    node = node.getParent();
                }
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(detailsView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails", Alert.AlertType.ERROR);
        }
    }

    private void handleEdit(Client client) {
        // Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_CLIENT)) {
            return;
        }
        
        // Logger l'action
        auditService.logCurrentUser("UPDATE", "client", String.valueOf(client.getId()), 
            "Ouverture formulaire modification client: " + client.getNom());
        
        try {
            System.out.println("🔄 Ouverture du formulaire de modification client...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier-client-view.fxml"));
            Pane editView = loader.load();

            // Passer le client au contrôleur
            ModifierClientController controller = loader.getController();
            controller.setClient(client);

            // ✅ Forcer la taille
            editView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // ✅ Chercher le contentArea (StackPane principal)
            Node node = this.cardContainer;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = this.cardContainer;
                while (node != null) {
                    if (node instanceof StackPane) {
                        contentArea = (StackPane) node;
                        break;
                    }
                    node = node.getParent();
                }
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(editView);
                System.out.println("Vue de modification client chargée !");
            } else {
                System.err.println("contentArea non trouvé !");
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(Client client) {
        // Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_CLIENT)) {
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le client " + client.getNom() + " ?");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    clientService.delete(client.getId());
                    
                    // Logger la suppression
                    auditService.logCurrentUser("DELETE", "client", String.valueOf(client.getId()), 
                        "Suppression client: " + client.getNom());
                    
                    loadData();
                    showAlert("Succès", "Client " + client.getNom() + " supprimé", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
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