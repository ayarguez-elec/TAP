package controller;

import entities.Pouchoir;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import security.PermissionGuard;
import services.AuditService;
import services.PouchoirService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class GestionPouchoirController {

    @FXML
    private TextField searchField;
    @FXML
    private TilePane cardContainer;
    @FXML
    private Label totalLabel;
    @FXML
    private Label disponiblesLabel;
    @FXML
    private Label sortisLabel;

    private PouchoirService service = new PouchoirService();
    private AuditService auditService = new AuditService();
    private List<Pouchoir> pouchoirs;

    @FXML
    public void initialize() {
        if (cardContainer != null) {
            cardContainer.setPrefColumns(4);
            cardContainer.setPrefTileWidth(180);
            cardContainer.setPrefTileHeight(190);
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
            pouchoirs = service.readAll();
            displayCards(pouchoirs);
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données", Alert.AlertType.ERROR);
        }
    }

    private void filterCards(String searchText) {
        if (pouchoirs == null) return;

        if (searchText == null || searchText.trim().isEmpty()) {
            displayCards(pouchoirs);
            return;
        }

        String search = searchText.toLowerCase().trim();
        List<Pouchoir> filtered = pouchoirs.stream()
                .filter(p -> p.getRefPouchoir().toLowerCase().contains(search))
                .collect(Collectors.toList());

        displayCards(filtered);
    }

    private void displayCards(List<Pouchoir> pouchoirs) {
        if (cardContainer == null) return;

        cardContainer.getChildren().clear();

        if (pouchoirs == null || pouchoirs.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 0, 50, 0));

            Label emptyLabel = new Label("📭");
            emptyLabel.setStyle("-fx-font-size: 48px;");
            Label emptyText = new Label("Aucun pouchoir trouvé");
            emptyText.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");

            emptyBox.getChildren().addAll(emptyLabel, emptyText);
            cardContainer.getChildren().add(emptyBox);
            return;
        }

        for (Pouchoir p : pouchoirs) {
            VBox card = createCard(p);
            cardContainer.getChildren().add(card);
        }

        updateStats();
    }

    private VBox createCard(Pouchoir pouchoir) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 15, 15, 15));
        card.setPrefWidth(180);
        card.setPrefHeight(190);
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
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);" +
                            "-fx-border-color: #3A98A5;" +
                            "-fx-border-width: 2;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);" +
                            "-fx-border-color: #e8eaee;" +
                            "-fx-border-width: 1;"
            );
        });

        boolean isDisponible = "disponible".equals(pouchoir.getStatut());
        String statutColor = isDisponible ? "#46BE62" : "#D9691D";
        String statutBg = isDisponible ? "#e8f5e9" : "#fbe9e7";
        String statutText = isDisponible ? "Disponible" : "Sorti";

        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(3, 10, 3, 10));
        statusBox.setStyle("-fx-background-color: " + statutBg + "; -fx-border-radius: 10; -fx-background-radius: 10;");

        Circle statusDot = new Circle(4);
        statusDot.setFill(Color.web(statutColor));

        Label statusLabel = new Label(statutText);
        statusLabel.setStyle("-fx-text-fill: " + statutColor + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        statusBox.getChildren().addAll(statusDot, statusLabel);

        Label refLabel = new Label(pouchoir.getRefPouchoir());
        refLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #21262A;");
        refLabel.setAlignment(Pos.CENTER);

        Label programmeLabel = new Label(pouchoir.getProgramme());
        programmeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        Label racleLabel = new Label(pouchoir.getRacle() + " mm");
        racleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        Label emplacementLabel = new Label("Emp: " + pouchoir.getEmplacement());
        emplacementLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(6, 0, 0, 0));

        Button editButton = new Button("📝");
        editButton.setStyle(
                "-fx-background-color: #346771;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 3 8;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;"
        );
        editButton.setOnAction(e -> handleEdit(pouchoir));

        Button deleteButton = new Button("❌");
        deleteButton.setStyle(
                "-fx-background-color: #D9691D;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 3 8;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(e -> handleDelete(pouchoir));

        actionBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(
                statusBox,
                refLabel,
                programmeLabel,
                racleLabel,
                emplacementLabel,
                actionBox
        );

        return card;
    }

    private void updateStats() {
        if (pouchoirs == null) return;

        long total = pouchoirs.size();
        long disponibles = pouchoirs.stream()
                .filter(p -> "disponible".equals(p.getStatut()))
                .count();
        long sortis = pouchoirs.stream()
                .filter(p -> "sorti".equals(p.getStatut()))
                .count();

        if (totalLabel != null) {
            totalLabel.setText("Total: " + total);
        }
        if (disponiblesLabel != null) {
            disponiblesLabel.setText("🟢 Disponibles: " + disponibles);
        }
        if (sortisLabel != null) {
            sortisLabel.setText("🔴 Sortis: " + sortis);
        }
    }

    @FXML
    private void handleAdd() {
        // Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_POCHOIR)) {
            return;
        }
        
        // Logger l'action
        auditService.logCurrentUser("CREATE", "pouchoir", null, "Ouverture formulaire ajout pochoir");
        
        try {
            System.out.println("🔄 Ouverture du formulaire d'ajout...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-pouchoir-view.fxml"));
            Pane addView = loader.load();

            // ✅ Forcer la taille
            addView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // ✅ Chercher le contentArea (StackPane) dans MainController
            Node node = cardContainer;
            StackPane contentArea = null;

            // Remonter jusqu'au BorderPane principal
            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            // Si pas trouvé par l'ID, chercher le premier StackPane
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
                System.out.println("Vue d'ajout chargée !");
            } else {
                System.err.println("contentArea non trouvé !");
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        showAlert("Rafraîchissement", "Liste actualisée", Alert.AlertType.INFORMATION);
    }

    private void handleEdit(Pouchoir pouchoir) {
        // Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_POCHOIR)) {
            return;
        }
        
        // Logger l'action
        auditService.logCurrentUser("UPDATE", "pouchoir", pouchoir.getRefPouchoir(), 
            "Ouverture formulaire modification pochoir: " + pouchoir.getRefPouchoir());
        
        try {
            System.out.println("🔄 Ouverture du formulaire de modification...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier-pouchoir-view.fxml"));
            Pane editView = loader.load();

            // Passer le pouchoir au contrôleur
            ModifierPouchoirController controller = loader.getController();
            controller.setPouchoir(pouchoir);

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
                System.out.println("Vue de modification chargée !");
            } else {
                System.err.println("contentArea non trouvé !");
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(Pouchoir pouchoir) {
        // Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_POCHOIR)) {
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le pouchoir " + pouchoir.getRefPouchoir() + " ?");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.deleteByReference(pouchoir.getRefPouchoir());
                    
                    // Logger la suppression
                    auditService.logCurrentUser("DELETE", "pouchoir", pouchoir.getRefPouchoir(), 
                        "Suppression pochoir: " + pouchoir.getRefPouchoir());
                    
                    loadData();
                    showAlert("Succès", "Pouchoir " + pouchoir.getRefPouchoir() + " supprimé", Alert.AlertType.INFORMATION);
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

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/suivi-pouchoir-view.fxml"));
            Pane suiviView = loader.load();

            suiviView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            suiviView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Node node = cardContainer;
            StackPane mainContentArea = null;

            while (node != null) {
                if (node instanceof StackPane && node.getId() != null && node.getId().equals("contentArea")) {
                    mainContentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (mainContentArea == null) {
                node = cardContainer;
                while (node != null) {
                    if (node instanceof StackPane) {
                        mainContentArea = (StackPane) node;
                        break;
                    }
                    node = node.getParent();
                }
            }

            if (mainContentArea != null) {
                mainContentArea.getChildren().clear();
                mainContentArea.getChildren().add(suiviView);
            } else {
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la vue précédente", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onRetourButtonHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #5a7a80; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 4 12; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-size: 11px;");
    }

    @FXML
    private void onRetourButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #6F8D94; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 4 12; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-size: 11px;");
    }
}