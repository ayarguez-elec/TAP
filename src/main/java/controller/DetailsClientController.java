package controller;

import entities.Client;
import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import services.ClientService;
import services.ProduitService;
import utils.MyDataBase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DetailsClientController {

    @FXML
    private Label clientNomLabel;
    @FXML
    private StackPane logoContainer;
    @FXML
    private Label nomValue;
    @FXML
    private Label codeValue;
    @FXML
    private Label telephoneValue;
    @FXML
    private Label emailValue;
    @FXML
    private Label adresseValue;
    @FXML
    private Label dateValue;
    @FXML
    private Label produitsCount;
    @FXML
    private TableView<Produit> produitsTable;
    @FXML
    private TableColumn<Produit, String> colNom;
    @FXML
    private TableColumn<Produit, String> colCode;
    @FXML
    private TableColumn<Produit, String> colEcran;
    @FXML
    private TableColumn<Produit, Integer> colPochoirs;
    @FXML
    private TableColumn<Produit, String> colDate;
    @FXML
    private TableColumn<Produit, Void> colActions;
    @FXML
    private TextField searchProduitField;

    private Client client;
    private ClientService clientService = new ClientService();
    private ProduitService produitService = new ProduitService();
    private ObservableList<Produit> produits;
    private List<Produit> allProduits;
    private Connection connection;

    // =============================================
    // CLASSE INTERNE POUR LES INFOS POUCHOIR
    // =============================================
    private static class PouchoirInfo {
        String refPouchoir;
        String programme;
        String racle;

        PouchoirInfo(String refPouchoir, String programme, String racle) {
            this.refPouchoir = refPouchoir;
            this.programme = programme;
            this.racle = racle;
        }
    }

    public DetailsClientController() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @FXML
    public void initialize() {
        searchProduitField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerProduits(newValue);
        });
        
        setupTableView();
    }

    public void setClient(Client client) {
        this.client = client;
        afficherDetails();
        chargerProduits();
    }
    
    private void setupTableView() {
        // Initialize columns
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeProduit"));
        colEcran.setCellValueFactory(new PropertyValueFactory<>("ecran"));
        
        // Custom cell factory for nombre de pochoirs
        colPochoirs.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                getNombrePouchoirs(cellData.getValue().getId())).asObject());
        
        // Custom cell factory for date formatting
        colDate.setCellFactory(col -> new TableCell<Produit, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Produit produit = getTableRow().getItem();
                    if (produit.getDateCreation() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        setText(produit.getDateCreation().toLocalDateTime().format(formatter));
                    } else {
                        setText("N/A");
                    }
                }
            }
        });
        
        // Setup action buttons column
        setupActionButtons();
    }
    
    private void setupActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("📝");
            private final Button deleteButton = new Button("❌");
            private final Button ficheButton = new Button("📋");
            private final HBox actionBox = new HBox(8, editButton, deleteButton, ficheButton);

            {
                actionBox.setAlignment(Pos.CENTER);
                
                editButton.setStyle(
                    "-fx-background-color: #346771; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 4 8; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
                editButton.setTooltip(new Tooltip("Modifier le produit"));
                
                deleteButton.setStyle(
                    "-fx-background-color: #D9691D; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 4 8; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
                deleteButton.setTooltip(new Tooltip("Supprimer le produit"));
                
                ficheButton.setStyle(
                    "-fx-background-color: #6F8D94; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 4 8; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
                ficheButton.setTooltip(new Tooltip("Fiche sérigraphie"));

                editButton.setOnAction(e -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    handleEditProduit(produit);
                });

                deleteButton.setOnAction(e -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    handleDeleteProduit(produit);
                });
                
                ficheButton.setOnAction(e -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    handleFicheSerigraphie(produit);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }
    
    private int getNombrePouchoirs(int produitId) {
        return getPouchoirInfosForProduit(produitId).size();
    }

    private void afficherDetails() {
        if (client == null) return;

        clientNomLabel.setText("👤 " + client.getNom());
        nomValue.setText(client.getNom());
        codeValue.setText("📋 " + (client.getCode() != null ? client.getCode() : "N/A"));
        telephoneValue.setText("📞 " + (client.getTelephone() != null ? client.getTelephone() : "N/A"));
        emailValue.setText("✉ " + (client.getEmail() != null ? client.getEmail() : "N/A"));
        adresseValue.setText("📍 " + (client.getAdresse() != null ? client.getAdresse() : "N/A"));

        if (client.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dateValue.setText("📅 " + client.getCreatedAt().toLocalDateTime().format(formatter));
        } else {
            dateValue.setText("📅 N/A");
        }

        // Logo
        if (client.getLogo() != null && !client.getLogo().isEmpty()) {
            try {
                File file = new File(client.getLogo());
                if (file.exists()) {
                    Image logoImage = new Image(file.toURI().toString());
                    ImageView logoView = new ImageView(logoImage);
                    logoView.setFitHeight(50);
                    logoView.setFitWidth(50);
                    logoView.setPreserveRatio(true);
                    logoView.setStyle("-fx-border-radius: 25; -fx-background-radius: 25;");
                    logoContainer.getChildren().clear();
                    logoContainer.getChildren().add(logoView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void chargerProduits() {
        try {
            allProduits = produitService.readAll().stream()
                    .filter(p -> p.getClientId() == client.getId())
                    .collect(Collectors.toList());
            produits = FXCollections.observableArrayList(allProduits);
            produitsTable.setItems(produits);
            produitsCount.setText(produits.size() + " produit" + (produits.size() > 1 ? "s" : ""));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les produits", Alert.AlertType.ERROR);
        }
    }

    private void filtrerProduits(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            produits = FXCollections.observableArrayList(allProduits);
        } else {
            String search = searchText.toLowerCase().trim();
            List<Produit> filtered = allProduits.stream()
                    .filter(p -> p.getNomProduit().toLowerCase().contains(search) ||
                            (p.getCodeProduit() != null && p.getCodeProduit().toLowerCase().contains(search)) ||
                            (p.getEcran() != null && p.getEcran().toLowerCase().contains(search)))
                    .collect(Collectors.toList());
            produits = FXCollections.observableArrayList(filtered);
        }
        produitsTable.setItems(produits);
        produitsCount.setText(produits.size() + " produit" + (produits.size() > 1 ? "s" : ""));
    }

    private List<PouchoirInfo> getPouchoirInfosForProduit(int produitId) {
        List<PouchoirInfo> infos = new ArrayList<>();
        try {
            String sql = "SELECT p.refPouchoir, p.programme, p.racle " +
                    "FROM pouchoir p " +
                    "INNER JOIN produit_pouchoir pp ON p.refPouchoir = pp.pouchoir_reference " +
                    "WHERE pp.produit_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, produitId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    infos.add(new PouchoirInfo(
                            rs.getString("refPouchoir"),
                            rs.getString("programme"),
                            rs.getString("racle")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return infos;
    }

    private void handleEditProduit(Produit produit) {
        try {
            System.out.println("Ouverture du formulaire de modification produit...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier-produit-view.fxml"));
            Pane editView = loader.load();

            ModifierProduitController controller = loader.getController();
            controller.setProduit(produit);

            editView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Node node = this.produitsTable;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = this.produitsTable;
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
                System.out.println("Vue de modification produit chargée !");
            } else {
                System.err.println("contentArea non trouvé !");
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteProduit(Produit produit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le produit " + produit.getNomProduit() + " ?");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    produitService.delete(produit.getId());
                    chargerProduits();
                    showAlert("Succès", "Produit supprimé", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleFicheSerigraphie(Produit produit) {
        try {
            System.out.println("Ouverture de la fiche sérigraphie pour: " + produit.getNomProduit());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fiche-serigraphie-view.fxml"));
            Pane ficheView = loader.load();

            FicheSerigraphieController controller = loader.getController();
            controller.setProduit(produit);

            ficheView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Node node = this.produitsTable;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = this.produitsTable;
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
                contentArea.getChildren().add(ficheView);
                System.out.println("Fiche sérigraphie chargée !");
            } else {
                System.err.println("contentArea non trouvé !");
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fiche sérigraphie", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-client-view.fxml"));
            Pane view = loader.load();

            Node node = nomValue;
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

    @FXML
    private void handleAddProduit() {
        try {
            System.out.println("Ouverture du formulaire d'ajout produit...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-produit-view.fxml"));
            Pane addView = loader.load();

            AjouterProduitController controller = loader.getController();
            controller.setClient(client);
            controller.setParentController(this);

            addView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Node node = this.produitsTable;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = this.produitsTable;
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
                System.out.println("Vue d'ajout produit chargée !");
            } else {
                System.err.println("contentArea non trouvé !");
                showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefreshProduits() {
        searchProduitField.clear();
        chargerProduits();
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