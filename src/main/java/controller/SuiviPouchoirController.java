package controller;

import java.sql.*;

import entities.Client;
import entities.Pouchoir;
import entities.Produit;
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
import services.ClientService;
import services.PouchoirService;
import services.ProduitService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import utils.MyDataBase;

import javax.management.openmbean.TabularData;

public class SuiviPouchoirController {
    private Map<String, String> produitParPouchoir = new HashMap<>();
    private Connection connection;

    public SuiviPouchoirController() {
        connection = MyDataBase.getInstance().getConnection();
    }
    private static final int TOTAL_EMPLACEMENTS = 400;

    @FXML
    private TilePane armoireGrid;
    @FXML
    private ComboBox<String> filterRacle;
    @FXML
    private ComboBox<String> filterStatut;
    @FXML
    private TextField searchField;
    @FXML
    private Label selectedEmplacement;
    @FXML
    private Label infoDisponibles;
    @FXML
    private Label infoSortis;
    @FXML
    private Label infoVides;
    @FXML
    private Label infoTotal;
    @FXML
    private StackPane detailContent;
    @FXML
    private VBox detailPanel;
    private ProduitService produitService = new ProduitService();
    private ClientService clientService = new ClientService();
    private PouchoirService service = new PouchoirService();


    private List<Pouchoir> pouchoirs;
    private Map<Integer, Pouchoir> pouchoirMap;
    private List<Pouchoir> filteredPouchoirs;

    @FXML
    public void initialize() {
        filterRacle.getItems().addAll("Tous", "300", "350", "400", "460");
        filterRacle.setValue("Tous");

        filterStatut.getItems().addAll("Tous", "Libre", "Occupé");
        filterStatut.setValue("Tous");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        loadData();
    }

    private void loadData() {
        try {
            pouchoirs = service.readAll();
            loadProduitMap(); // ← DOIT ÊTRE ICI
            applyFilters();
            showDefaultPanel();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données", Alert.AlertType.ERROR);
        }
    }

    private void showDetailsInPanel(Pouchoir pouchoir) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10, 0, 10, 0));
        panel.setAlignment(Pos.TOP_CENTER);

        boolean isDisponible = "disponible".equals(pouchoir.getStatut());
        String statutColor = isDisponible ? "#46BE62" : "#D9691D";
        String statutBg = isDisponible ? "#E8F5E9" : "#FBE9E7";
        String statutBorder = isDisponible ? "#C8E6C9" : "#FFCCBC";
        String statutText = isDisponible ? "● DISPONIBLE" : "● SORTI";

        // Badge de Statut
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(6, 14, 6, 14));
        statusBox.setStyle("-fx-background-color: " + statutBg + "; -fx-border-color: " + statutBorder + 
                           "; -fx-border-radius: 20; -fx-background-radius: 20; -fx-border-width: 1;");

        Label statusLabel = new Label(statutText);
        statusLabel.setStyle("-fx-text-fill: " + statutColor + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        statusBox.getChildren().add(statusLabel);

        // Référence Card Header
        VBox refCard = new VBox(4);
        refCard.setAlignment(Pos.CENTER);
        refCard.setPadding(new Insets(12, 10, 12, 10));
        refCard.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E2E8F0; -fx-border-width: 1;");

        Label refTitle = new Label("RÉFÉRENCE POCHOIR");
        refTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #6F8D94; -fx-letter-spacing: 1px;");

        Label refLabel = new Label(pouchoir.getRefPouchoir());
        refLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #21262A;");

        refCard.getChildren().addAll(refTitle, refLabel);

        // Récupération du produit et du client
        Produit produit = getProduitByProgramme(pouchoir.getProgramme());
        Client client = null;
        if (produit != null) {
            client = getClientById(produit.getClientId());
        }

        // Grille d'informations modernes
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(12, 12, 12, 12));
        grid.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E2E8F0; -fx-border-width: 1;");

        int row = 0;

        addInfoRowCompact(grid, row++, "Programme:", pouchoir.getProgramme());
        addInfoRowCompact(grid, row++, "Racle:", pouchoir.getRacle() + " mm");
        addInfoRowCompact(grid, row++, "Emplacement:", String.valueOf(pouchoir.getEmplacement()));
        addInfoRowCompact(grid, row++, "Stock cartes:", pouchoir.getStockActuel() + " / " + pouchoir.getQuantiteTotale());
        addInfoRowCompact(grid, row++, "Date d'ajout:", formatDateCompact(pouchoir.getDateCreation()));

        // Produit et Client
        addInfoRowCompact(grid, row++, "Produit:", produit != null ? produit.getNomProduit() : "Aucun produit associé");
        addInfoRowCompact(grid, row++, "Client:", client != null ? client.getNom() : "Aucun client associé");

        // Zone de Boutons d'Action
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(8, 0, 5, 0));

        if (isDisponible) {
            Button outButton = new Button("📦 Marquer SORTI (OUT)");
            outButton.setStyle(
                    "-fx-background-color: #D9691D;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 12px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(217,105,29,0.3), 8, 0, 0, 2);"
            );
            outButton.setOnAction(e -> markOut(pouchoir));
            actionBox.getChildren().add(outButton);
        }

        panel.getChildren().addAll(
                statusBox,
                refCard,
                grid,
                actionBox
        );

        detailContent.getChildren().clear();
        detailContent.getChildren().add(panel);
    }

    private void addInfoRowCompact(GridPane grid, int row, String label, String value) {
        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6F8D94;");

        Label valueField = new Label(value != null ? value : "-");
        valueField.setStyle("-fx-text-fill: #21262A; -fx-font-size: 12px; -fx-font-weight: bold;");
        valueField.setWrapText(true);

        grid.add(labelField, 0, row);
        grid.add(valueField, 1, row);
    }

    private VBox createCell(int emplacement) {
        VBox cell = new VBox();
        cell.setAlignment(Pos.CENTER);
        cell.setPrefWidth(82);
        cell.setPrefHeight(82);
        cell.getStyleClass().add("armoire-cell");

        Label label = new Label(String.valueOf(emplacement));
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Pouchoir pouchoir = filteredPouchoirs.stream()
                .filter(p -> p.getEmplacement() == emplacement && "disponible".equals(p.getStatut()))
                .findFirst()
                .orElse(null);

        if (pouchoir != null) {
            boolean stockFaible = pouchoir.getStockActuel() < 100 && pouchoir.getStockActuel() > 0;
            boolean stockEpuise = pouchoir.getStockActuel() <= 0;

            cell.getStyleClass().add("armoire-cell-disponible");
            label.setText(pouchoir.getRefPouchoir());
            label.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");

            if (stockEpuise) {
                cell.setStyle("-fx-border-color: #E53E3E; -fx-border-width: 3; -fx-border-radius: 12;");
            } else if (stockFaible) {
                cell.setStyle("-fx-border-color: #D9691D; -fx-border-width: 3; -fx-border-radius: 12;");
            }

            // Tooltip modernisé
            String nomProduit = produitParPouchoir.get(pouchoir.getRefPouchoir());
            Tooltip tooltip = new Tooltip();
            tooltip.setStyle(
                    "-fx-background-color: #21262A;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-font-size: 11px;" +
                            "-fx-padding: 10 14;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
            );
            String tooltipText = "📦 POCHOIR " + pouchoir.getRefPouchoir() + "\n" +
                    "Programme: " + pouchoir.getProgramme() + "\n" +
                    "Racle: " + pouchoir.getRacle() + " mm\n" +
                    "Emplacement: N°" + pouchoir.getEmplacement() + "\n" +
                    "Stock: " + pouchoir.getStockActuel() + " cartes / " + pouchoir.getQuantiteTotale() + "\n" +
                    "Statut: DISPONIBLE";

            if (nomProduit != null) {
                tooltipText += "\nProduit: " + nomProduit;
            }

            tooltip.setText(tooltipText);
            Tooltip.install(cell, tooltip);

            cell.setOnMouseClicked(e -> {
                selectedEmplacement.setText("Emplacement sélectionné: N°" + emplacement + " (" + pouchoir.getRefPouchoir() + ")");
                highlightCell(cell);
                showDetailsInPanel(pouchoir);
            });

        } else {
            // Emplacement libre
            cell.getStyleClass().add("armoire-cell-vide");
            label.setText(String.valueOf(emplacement));
            label.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");

            Tooltip tooltip = new Tooltip();
            tooltip.setStyle(
                    "-fx-background-color: #21262A;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-font-size: 11px;" +
                            "-fx-padding: 8 12;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;"
            );
            tooltip.setText(
                    "Emplacement N°" + emplacement + "\n" +
                            "🟢 LIBRE\n" +
                            "Cliquez pour affecter un pochoir"
            );
            Tooltip.install(cell, tooltip);

            cell.setOnMouseClicked(e -> {
                selectedEmplacement.setText("Emplacement sélectionné: N°" + emplacement + " (Libre)");
                highlightCell(cell);
                showEmptyPanel(emplacement);
            });
        }

        cell.getChildren().add(label);
        return cell;
    }

    private void applyFilters() {
        String selectedRacle = filterRacle.getValue();
        String selectedStatut = filterStatut.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        System.out.println("=== APPLY FILTERS ===");
        System.out.println("Recherche: '" + searchText + "'");
        System.out.println("Taille de la Map produit: " + produitParPouchoir.size());

        filteredPouchoirs = new ArrayList<>(pouchoirs);

        // Filtre par racle
        if (selectedRacle != null && !selectedRacle.equals("Tous")) {
            filteredPouchoirs = filteredPouchoirs.stream()
                    .filter(p -> p.getRacle().equals(selectedRacle))
                    .collect(Collectors.toList());
            System.out.println("Après filtre racle: " + filteredPouchoirs.size());
        }

        // Filtre par statut
        if (selectedStatut != null && !selectedStatut.equals("Tous")) {
            if (selectedStatut.equals("Occupé")) {
                filteredPouchoirs = filteredPouchoirs.stream()
                        .filter(p -> "disponible".equals(p.getStatut()))
                        .collect(Collectors.toList());
            } else if (selectedStatut.equals("Libre")) {
                filteredPouchoirs = filteredPouchoirs.stream()
                        .filter(p -> !"disponible".equals(p.getStatut()))
                        .collect(Collectors.toList());
            }
            System.out.println("Après filtre statut: " + filteredPouchoirs.size());
        }

        // Recherche : référence + emplacement + produit
        if (searchText != null && !searchText.isEmpty()) {
            filteredPouchoirs = filteredPouchoirs.stream()
                    .filter(p -> {
                        // 1. Recherche par référence
                        boolean matchRef = p.getRefPouchoir().toLowerCase().contains(searchText);

                        // 2. Recherche par emplacement
                        boolean matchEmplacement = false;
                        try {
                            int emplacement = Integer.parseInt(searchText);
                            matchEmplacement = p.getEmplacement() == emplacement;
                        } catch (NumberFormatException e) {}

                        // 3. Recherche par nom de produit
                        String nomProduit = produitParPouchoir.get(p.getRefPouchoir());
                        boolean matchProduit = nomProduit != null && nomProduit.toLowerCase().contains(searchText);

                        if (matchProduit) {
                            System.out.println("✅ Match produit: " + p.getRefPouchoir() + " → " + nomProduit +
                                    " (codé: " + p.isCodeLacroix() + ", statut: " + p.getStatut() + ")");
                        }

                        return matchRef || matchEmplacement || matchProduit;
                    })
                    .collect(Collectors.toList());
        }

        System.out.println("Résultats filtrés: " + filteredPouchoirs.size() + " pouchoirs");

        // Afficher les 10 premiers résultats pour debug
        if (!filteredPouchoirs.isEmpty()) {
            System.out.println("Premiers résultats:");
            for (int i = 0; i < Math.min(10, filteredPouchoirs.size()); i++) {
                Pouchoir p = filteredPouchoirs.get(i);
                String nomProduit = produitParPouchoir.get(p.getRefPouchoir());
                System.out.println("   " + (i+1) + ". " + p.getRefPouchoir() +
                        " | emplacement: " + p.getEmplacement() +
                        " | produit: " + (nomProduit != null ? nomProduit : "Aucun") +
                        " | codé: " + p.isCodeLacroix() +
                        " | statut: " + p.getStatut());
            }
        }

        // Reconstruire la map pour l'affichage
        pouchoirMap = filteredPouchoirs.stream()
                .filter(p -> "disponible".equals(p.getStatut()))
                .collect(Collectors.toMap(
                        Pouchoir::getEmplacement,
                        p -> p,
                        (p1, p2) -> p1
                ));

        System.out.println("Pouchoirs disponibles dans la map: " + pouchoirMap.size());

        updateGrid(selectedStatut, searchText);
    }

    private void updateGrid(String statutFilter, String searchText) {
        armoireGrid.getChildren().clear();

        // Filtrer par statut si demande
        String statut = (statutFilter != null && !statutFilter.equals("Tous")) ? statutFilter : null;

        // Determiner quels emplacements correspondent a la recherche/filtre
        List<Integer> emplacementsFiltres = null; // null = afficher tout
        if ((searchText != null && !searchText.isEmpty()) || statut != null) {
            List<Pouchoir> base = (searchText != null && !searchText.isEmpty()) ? filteredPouchoirs : pouchoirs;
            if (statut != null) {
                String statutCode = statut.equals("Occupe") || statut.equals("Occupé") ? "disponible" : "vide";
                base = base.stream().filter(p -> statutCode.equals(p.getStatut())).collect(Collectors.toList());
            }
            emplacementsFiltres = base.stream()
                    .map(Pouchoir::getEmplacement)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // Afficher TOUS les emplacements de 1 a TOTAL_EMPLACEMENTS
        for (int i = 1; i <= TOTAL_EMPLACEMENTS; i++) {
            if (emplacementsFiltres == null || emplacementsFiltres.contains(i)) {
                VBox cell = createCell(i);
                armoireGrid.getChildren().add(cell);
            }
        }
    }

    @FXML
    private void openCodification() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/codification-view.fxml"));
            Pane codificationView = loader.load();

            Node parent = detailPanel.getParent();
            while (parent != null && !(parent instanceof StackPane)) {
                parent = parent.getParent();
            }

            if (parent != null) {
                StackPane contentArea = (StackPane) parent;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(codificationView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la codification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCodificationButtonHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #3A98A5; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-pref-width: 250;");
    }

    @FXML
    private void onCodificationButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #346771; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-pref-width: 250;");
    }

    @FXML
    private void openGenerationEmail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/generation-email-view.fxml"));
            Pane generationView = loader.load();

            Node parent = detailPanel.getParent();
            while (parent != null && !(parent instanceof StackPane)) {
                parent = parent.getParent();
            }

            if (parent != null) {
                StackPane contentArea = (StackPane) parent;
                contentArea.getChildren().clear();
                contentArea.getChildren().add(generationView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la génération d'emails", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onEmailButtonHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #3A98A5; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-pref-width: 250;");
    }

    @FXML
    private void onEmailButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #346771; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-pref-width: 250;");
    }

    private void showDefaultPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 20, 0));
        panel.setSpacing(8);

        Label iconLabel = new Label("👆");
        iconLabel.setStyle("-fx-font-size: 28px;");

        detailContent.getChildren().clear();
        detailContent.getChildren().add(panel);
    }

    private void showEmptyPanel(int emplacement) {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 10, 20, 10));

        Label iconLabel = new Label("📭");
        iconLabel.setStyle("-fx-font-size: 40px;");

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Emplacement N°" + emplacement);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #21262A;");

        Label badgeLabel = new Label("● LIBRE ET DISPONIBLE");
        badgeLabel.setStyle("-fx-text-fill: #3A98A5; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: #E0F2F1; -fx-padding: 4 10; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #B2DFDB; -fx-border-width: 1;");

        titleBox.getChildren().addAll(titleLabel, badgeLabel);

        Label messageLabel = new Label("Cet emplacement est actuellement libre. Vous pouvez y affecter un nouveau pochoir ou réintégrer un pochoir sorti.");
        messageLabel.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 12px; -fx-text-alignment: center;");
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setWrapText(true);

        Button addButton = new Button("➕ Affecter un pochoir");
        addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3A98A5, #346771);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 24;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(58,152,165,0.3), 8, 0, 0, 2);"
        );
        addButton.setOnAction(e -> showAddFormInPanel(emplacement));

        panel.getChildren().addAll(iconLabel, titleBox, messageLabel, addButton);

        detailContent.getChildren().clear();
        detailContent.getChildren().add(panel);
    }

    private void showAddFormInPanel(int emplacement) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10, 0, 10, 0));

        Label titleLabel = new Label("Affecter un Pochoir");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #21262A;");

        Label subtitleLabel = new Label("Emplacement N°" + emplacement + " (Libre)");
        subtitleLabel.setStyle("-fx-text-fill: #3A98A5; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label infoLabel = new Label("💡 Si la référence existe déjà dans la base, le pochoir sera réintégré.");
        infoLabel.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 11px; -fx-font-style: italic;");
        infoLabel.setWrapText(true);

        Separator separator = new Separator();
        separator.getStyleClass().add("panel-separator");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(12, 12, 12, 12));
        formGrid.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E2E8F0; -fx-border-width: 1;");

        TextField refField = new TextField();
        refField.setPromptText("Ex: PCH-2026-01");
        refField.setStyle("-fx-padding: 6 10; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #CBD5E1; -fx-font-size: 12px;");

        TextField programmeField = new TextField();
        programmeField.setPromptText("Nom du programme");
        programmeField.setStyle("-fx-padding: 6 10; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #CBD5E1; -fx-font-size: 12px;");

        ComboBox<String> racleField = new ComboBox<>();
        racleField.getItems().addAll("300", "350", "400", "460");
        racleField.setValue("300");
        racleField.setStyle("-fx-padding: 4 8; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #CBD5E1; -fx-font-size: 12px;");

        formGrid.add(createFormLabel("Référence:"), 0, 0);
        formGrid.add(refField, 1, 0);
        formGrid.add(createFormLabel("Programme:"), 0, 1);
        formGrid.add(programmeField, 1, 1);
        formGrid.add(createFormLabel("Racle:"), 0, 2);
        formGrid.add(racleField, 1, 2);

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 5, 0));

        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle(
                "-fx-background-color: #46BE62;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 6 20;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 11px;"
        );
        saveButton.setOnAction(e -> {
            try {
                String ref = refField.getText().trim().toUpperCase();
                String programme = programmeField.getText().trim();
                String racle = racleField.getValue();

                if (ref.isEmpty()) {
                    showAlert("Erreur", "La référence est obligatoire", Alert.AlertType.ERROR);
                    return;
                }

                Pouchoir existingPouchoir = service.readByReference(ref);

                if (existingPouchoir != null) {
                    boolean emplacementOccupe = pouchoirs.stream()
                            .filter(p -> "disponible".equals(p.getStatut()))
                            .anyMatch(p -> p.getEmplacement() == emplacement);

                    if (emplacementOccupe) {
                        showAlert("Erreur", "L'emplacement " + emplacement + " est déjà occupé !", Alert.AlertType.ERROR);
                        return;
                    }

                    existingPouchoir.setEmplacement(emplacement);
                    existingPouchoir.setStatut("disponible");

                    service.update(existingPouchoir);
                    loadData();
                    showAlert("Succès",
                            "Pochoir " + ref + " réintégré à l'emplacement " + emplacement + "\n" +
                                    "Stock actuel : " + existingPouchoir.getStockActuel() + " cartes",
                            Alert.AlertType.INFORMATION);
                    showEmptyPanel(emplacement);

                } else {
                    if (programme.isEmpty()) {
                        showAlert("Erreur", "Le programme est obligatoire pour un nouveau pochoir", Alert.AlertType.ERROR);
                        return;
                    }

                    int quantiteInitiale = 100;
                    int nbFlan = 10;
                    int stockInitial = quantiteInitiale - (quantiteInitiale / nbFlan);

                    Pouchoir newPouchoir = new Pouchoir(ref, programme, racle, emplacement, "disponible",
                            quantiteInitiale, nbFlan, stockInitial);
                    service.create(newPouchoir);
                    loadData();
                    showAlert("Succès",
                            "Pochoir " + ref + " ajouté à l'emplacement " + emplacement + "\n" +
                                    "Stock initial : " + stockInitial + " cartes",
                            Alert.AlertType.INFORMATION);
                    showEmptyPanel(emplacement);
                }

            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur : " + ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle(
                "-fx-background-color: #6F8D94;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 6 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 11px;"
        );
        cancelButton.setOnAction(e -> showEmptyPanel(emplacement));

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        panel.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                infoLabel,
                separator,
                formGrid,
                buttonBox
        );

        detailContent.getChildren().clear();
        detailContent.getChildren().add(panel);
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #21262A; -fx-font-size: 11px;");
        return label;
    }

    private void markOut(Pouchoir pouchoir) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Marquer " + pouchoir.getRefPouchoir() + " comme SORTI ?\n\n" +
                "Stock actuel : " + pouchoir.getStockActuel() + " cartes\n" +
                "L'emplacement " + pouchoir.getEmplacement() + " deviendra libre.");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (pouchoir.hasEnoughStock()) {
                        pouchoir.diminuerStock();
                        pouchoir.setStatut("sorti");
                        service.update(pouchoir);
                        loadData();
                        showDefaultPanel();
                        showAlert("Succès",
                                "Pochoir " + pouchoir.getRefPouchoir() + " marqué comme SORTI.\n" +
                                        "Stock restant : " + pouchoir.getStockActuel() + " cartes\n" +
                                        "L'emplacement " + pouchoir.getEmplacement() + " est maintenant LIBRE.",
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Erreur",
                                "Stock insuffisant !\n" +
                                        "Stock actuel : " + pouchoir.getStockActuel() + " cartes\n" +
                                        "Impossible de marquer ce pouchoir comme SORTI.",
                                Alert.AlertType.ERROR);
                    }
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la mise à jour", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    private void highlightCell(VBox selected) {
        for (var child : armoireGrid.getChildren()) {
            child.getStyleClass().remove("armoire-cell-selected");
        }
        selected.getStyleClass().add("armoire-cell-selected");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-padding: 15;");
        alert.showAndWait();
    }

    private String formatDateCompact(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        return timestamp.toLocalDateTime().format(formatter);
    }

    @FXML
    private void handleFilter() {
        applyFilters();
    }

    @FXML
    private void handleReset() {
        filterRacle.setValue("Tous");
        filterStatut.setValue("Tous");
        searchField.clear();
        loadData();
    }

    @FXML
    private void openGestionPouchoir() {
        if (MainController.verifierMotDePasseStatic()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-pouchoir-view.fxml"));
                Pane gestionView = loader.load();

                Node parent = detailPanel.getParent();
                while (parent != null && !(parent instanceof StackPane)) {
                    parent = parent.getParent();
                }

                if (parent != null) {
                    StackPane contentArea = (StackPane) parent;
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(gestionView);
                } else {
                    detailContent.getChildren().clear();
                    detailContent.getChildren().add(new Label("Chargement..."));
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir la gestion des pochoirs", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onGestionButtonHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #3A98A5; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-pref-width: 250;");
    }

    @FXML
    private void onGestionButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #346771; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-font-size: 13px; -fx-pref-width: 250;");
    }

    @FXML
    private void reinitialiserStock(Pouchoir pouchoir) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Réinitialiser le stock de " + pouchoir.getRefPouchoir() + " ?\n\n" +
                "Stock actuel : " + pouchoir.getStockActuel() + " cartes\n" +
                "Cette opération remettra le stock à sa valeur initiale.");
        confirm.getDialogPane().setStyle("-fx-padding: 15;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.reinitialiserStock(pouchoir.getRefPouchoir());
                    loadData();
                    showAlert("Succès", "Stock réinitialisé pour " + pouchoir.getRefPouchoir(), Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la réinitialisation", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }
    private Produit getProduitByProgramme(String programme) {
        try {
            // Utiliser la connexion de MyDataBase
            Connection conn = utils.MyDataBase.getInstance().getConnection();
            String sql = "SELECT p.* FROM produit p " +
                    "INNER JOIN produit_pouchoir pp ON p.id = pp.produit_id " +
                    "INNER JOIN pouchoir po ON pp.pouchoir_reference = po.refPouchoir " +
                    "WHERE po.programme = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, programme);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new Produit(
                            rs.getInt("id"),
                            rs.getString("nomProduit"),
                            rs.getInt("client_id"),
                            rs.getString("ecran"),
                            rs.getString("code_produit"),
                            rs.getString("description"),
                            rs.getString("creme_a_braser"),
                            rs.getString("programme"),
                            rs.getTimestamp("dateCreation")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    private Client getClientById(int clientId) {
        try {
            return clientService.read(clientId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void loadProduitMap() {
        try {
            System.out.println("=== CHARGEMENT DES PRODUITS ===");
            String sql = "SELECT pp.pouchoir_reference, pr.nomProduit FROM produit_pouchoir pp " +
                    "INNER JOIN produit pr ON pp.produit_id = pr.id";
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                int count = 0;
                while (rs.next()) {
                    String ref = rs.getString("pouchoir_reference");
                    String nom = rs.getString("nomProduit");
                    produitParPouchoir.put(ref, nom);
                    count++;
                    if (count <= 10) {
                        System.out.println("   " + ref + " → " + nom);
                    }
                }
                System.out.println("✅ " + count + " associations chargées dans la Map");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}