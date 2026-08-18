package controller;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import controller.AjouterFicheController;
import controller.ModifierFicheController;
import entities.FicheSerigraphie;
import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import services.ExcelFicheService;
import services.FicheSerigraphieService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FicheSerigraphieController {

    private StackPane contentArea;
    public void setContentArea(StackPane ca) { this.contentArea = ca; }

    private StackPane getEffectiveContentArea() {
        if (contentArea != null) return contentArea;
        if (MainController.getInstance() != null) return MainController.getInstance().getContentArea();
        return null;
    }

    @FXML private TextField searchField;
    @FXML private TableView<FicheSerigraphie> fichesTable;
    @FXML private TableColumn<FicheSerigraphie, Integer> colNumero;
    @FXML private TableColumn<FicheSerigraphie, String> colClient;
    @FXML private TableColumn<FicheSerigraphie, String> colProduit;
    @FXML private TableColumn<FicheSerigraphie, String> colFace;
    @FXML private TableColumn<FicheSerigraphie, String> colEcran;
    @FXML private TableColumn<FicheSerigraphie, String> colEpaisseur;
    @FXML private TableColumn<FicheSerigraphie, String> colProgramme;
    @FXML private TableColumn<FicheSerigraphie, String> colCreme;
    @FXML private TableColumn<FicheSerigraphie, Integer> colPassages;
    @FXML private TableColumn<FicheSerigraphie, Void> colActions;
    @FXML private Label statsLabel;

    private FicheSerigraphieService service;
    private ObservableList<FicheSerigraphie> fichesList;

    @FXML
    public void initialize() {
        service = new FicheSerigraphieService();
        fichesList = FXCollections.observableArrayList();

        // Initialize TableView columns
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroFiche"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        colProduit.setCellValueFactory(new PropertyValueFactory<>("produit"));
        colFace.setCellValueFactory(new PropertyValueFactory<>("face"));
        colEcran.setCellValueFactory(new PropertyValueFactory<>("numeroEcran"));
        colEpaisseur.setCellValueFactory(new PropertyValueFactory<>("epaisseur"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("numeroProgramme"));
        colCreme.setCellValueFactory(new PropertyValueFactory<>("designationCreme"));
        
        // Custom cell factory for passages count
        colPassages.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getNombrePassages()).asObject());

        // Setup action buttons column
        setupActionButtons();
        
        // Load data from database
        loadData();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("📝");
            private final Button deleteButton = new Button("❌");
            private final HBox actionBox = new HBox(8, editButton, deleteButton);

            {
                actionBox.setAlignment(Pos.CENTER);
                
                editButton.setStyle(
                    "-fx-background-color: #346771; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 4 8; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
                
                deleteButton.setStyle(
                    "-fx-background-color: #D9691D; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 4 8; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");

                editButton.setOnAction(e -> {
                    FicheSerigraphie fiche = getTableView().getItems().get(getIndex());
                    handleEdit(fiche);
                });

                deleteButton.setOnAction(e -> {
                    FicheSerigraphie fiche = getTableView().getItems().get(getIndex());
                    handleDelete(fiche);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void loadData() {
        try {
            List<FicheSerigraphie> fiches = service.readAll();
            fichesList.clear();
            fichesList.addAll(fiches);
            fichesTable.setItems(fichesList);
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les fiches", Alert.AlertType.ERROR);
        }
    }

    private void updateStats() {
        int count = fichesList.size();
        statsLabel.setText(count + " fiche" + (count > 1 ? "s" : ""));
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter-fiche-view.fxml"));
            Parent view = loader.load();
            AjouterFicheController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            getEffectiveContentArea().getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleActualiser() {
        loadData();
        showAlert("Actualisation", "Liste des fiches actualisée", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleImporterExcel() {
        // Open file chooser for Excel file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier Excel");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers Excel", "*.xls", "*.xlsx", "*.xlsm"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(fichesTable.getScene().getWindow());
        if (selectedFile == null) {
            return; // User cancelled
        }

        try {
            // Read fiches from Excel
            ExcelFicheService excelService = new ExcelFicheService(selectedFile);
            List<FicheSerigraphie> excelFiches = excelService.lireFichesListe();
            
            if (excelFiches.isEmpty()) {
                showAlert("Information", "Aucune fiche trouvée dans le fichier Excel", Alert.AlertType.WARNING);
                return;
            }

            // Get existing fiches from database
            List<FicheSerigraphie> existingFiches = service.readAll();
            
            // Build a map of existing fiches by index for quick lookup
            Map<String, FicheSerigraphie> existingMap = new java.util.HashMap<>();
            for (FicheSerigraphie existing : existingFiches) {
                if (existing.getIndex() != null && !existing.getIndex().isEmpty()) {
                    existingMap.put(existing.getIndex(), existing);
                }
            }

            // Counters for summary
            int countCreated = 0;
            int countUpdated = 0;
            int countIgnored = 0;
            StringBuilder errors = new StringBuilder();

            // Process each Excel fiche
            for (FicheSerigraphie excelFiche : excelFiches) {
                try {
                    String index = excelFiche.getIndex();
                    if (index == null || index.isEmpty()) {
                        countIgnored++;
                        continue;
                    }

                    FicheSerigraphie existingFiche = existingMap.get(index);
                    
                    if (existingFiche != null) {
                        // Fiche exists - update it
                        excelFiche.setId(existingFiche.getId()); // Keep the database ID
                        
                        // Preserve the existing numeroFiche when updating
                        if (excelFiche.getNumeroFiche() == 0) {
                            excelFiche.setNumeroFiche(existingFiche.getNumeroFiche());
                        }
                        
                        service.update(excelFiche);
                        countUpdated++;
                    } else {
                        // Fiche doesn't exist - create new
                        service.create(excelFiche);
                        
                        // Auto-generate numeroFiche if missing or 0
                        if (excelFiche.getNumeroFiche() == 0) {
                            // Use the auto-generated database ID as the fiche number
                            excelFiche.setNumeroFiche(excelFiche.getId());
                            service.update(excelFiche);
                        }
                        
                        countCreated++;
                    }
                } catch (Exception e) {
                    errors.append("Erreur pour ").append(excelFiche.getIndex())
                          .append(": ").append(e.getMessage()).append("\n");
                    countIgnored++;
                }
            }

            // Refresh table
            loadData();

            // Show summary
            StringBuilder summary = new StringBuilder();
            summary.append("Import terminé :\n\n");
            summary.append("✓ ").append(countCreated).append(" fiche(s) créée(s)\n");
            summary.append("✓ ").append(countUpdated).append(" fiche(s) mise(s) à jour\n");
            if (countIgnored > 0) {
                summary.append("⚠ ").append(countIgnored).append(" fiche(s) ignorée(s)\n");
            }
            
            if (errors.length() > 0) {
                summary.append("\nErreurs :\n").append(errors.toString());
            }

            showAlert("Import réussi", summary.toString(), 
                     errors.length() > 0 ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la lecture du fichier Excel: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'accès à la base de données: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRechercher() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadData();
            return;
        }

        try {
            List<FicheSerigraphie> results = service.search(query);
            fichesList.clear();
            fichesList.addAll(results);
            fichesTable.setItems(fichesList);
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        }
    }

    private void handleEdit(FicheSerigraphie fiche) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier-fiche-view.fxml"));
            Parent view = loader.load();
            ModifierFicheController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            ctrl.setFiche(fiche);
            if (view instanceof Region r) { r.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); }
            getEffectiveContentArea().getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire.", Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(FicheSerigraphie fiche) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la fiche de " + fiche.getClient() + 
                              " / " + fiche.getProduit() + " ?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(fiche.getId());
                loadData();
                showAlert("Succès", "Fiche supprimée", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
            }
        }
    }

    private void showFicheDialog(FicheSerigraphie fiche) {
        Dialog<FicheSerigraphie> dialog = new Dialog<>();
        dialog.setTitle(fiche == null ? "Nouvelle fiche sérigraphie" : "Modifier la fiche");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // General fields
        TextField clientField = new TextField();
        TextField produitField = new TextField();
        TextField faceField = new TextField();
        TextField programmeField = new TextField();
        TextField pcbField = new TextField();
        
        // Screen & cream fields
        TextField matiereEcranField = new TextField();
        TextField numeroEcranField = new TextField();
        TextField epaisseurField = new TextField();
        TextField fournisseurCremeField = new TextField();
        TextField designationCremeField = new TextField();
        TextField refLacroixField = new TextField();
        TextField codeBarreField = new TextField();

        // Passage A fields
        TextField aDateField = new TextField();
        TextField aMachineField = new TextField();
        TextField aPressionAvField = new TextField();
        TextField aPressionArField = new TextField();
        TextField aSupportField = new TextField();
        TextField aNombreField = new TextField();
        TextField aRacleField = new TextField();
        TextArea aInfoField = new TextArea();
        aInfoField.setPrefRowCount(2);
        TextField aVisaField = new TextField();

        // Passage B fields
        TextField bDateField = new TextField();
        TextField bMachineField = new TextField();
        TextField bPressionAvField = new TextField();
        TextField bPressionArField = new TextField();
        TextField bSupportField = new TextField();
        TextField bNombreField = new TextField();
        TextField bRacleField = new TextField();
        TextArea bInfoField = new TextArea();
        bInfoField.setPrefRowCount(2);
        TextArea bNatureField = new TextArea();
        bNatureField.setPrefRowCount(2);
        TextField bVisaField = new TextField();

        // Passage C fields
        TextField cDateField = new TextField();
        TextField cMachineField = new TextField();
        TextField cPressionAvField = new TextField();
        TextField cPressionArField = new TextField();
        TextField cSupportField = new TextField();
        TextField cNombreField = new TextField();
        TextField cRacleField = new TextField();
        TextArea cInfoField = new TextArea();
        cInfoField.setPrefRowCount(2);
        TextArea cNatureField = new TextArea();
        cNatureField.setPrefRowCount(2);
        TextField cVisaField = new TextField();

        // Passage D fields
        TextField dDateField = new TextField();
        TextField dMachineField = new TextField();
        TextField dPressionAvField = new TextField();
        TextField dPressionArField = new TextField();
        TextField dSupportField = new TextField();
        TextField dNombreField = new TextField();
        TextField dRacleField = new TextField();
        TextArea dInfoField = new TextArea();
        dInfoField.setPrefRowCount(2);
        TextArea dNatureField = new TextArea();
        dNatureField.setPrefRowCount(2);
        TextField dVisaField = new TextField();

        // If editing, populate fields
        if (fiche != null) {
            clientField.setText(fiche.getClient());
            produitField.setText(fiche.getProduit());
            faceField.setText(fiche.getFace());
            programmeField.setText(fiche.getNumeroProgramme());
            pcbField.setText(fiche.getNumeroPcb());
            
            matiereEcranField.setText(fiche.getMatiereEcran());
            numeroEcranField.setText(fiche.getNumeroEcran());
            epaisseurField.setText(fiche.getEpaisseur());
            fournisseurCremeField.setText(fiche.getFournisseurCreme());
            designationCremeField.setText(fiche.getDesignationCreme());
            refLacroixField.setText(fiche.getRefLacroixCreme());
            codeBarreField.setText(fiche.getCodeBarre());

            aDateField.setText(fiche.getADate());
            aMachineField.setText(fiche.getAMachine());
            aPressionAvField.setText(fiche.getAPressionAvant());
            aPressionArField.setText(fiche.getAPressionArriere());
            aSupportField.setText(fiche.getASupport());
            aNombreField.setText(fiche.getANombre());
            aRacleField.setText(fiche.getARacle());
            aInfoField.setText(fiche.getAInfoTechniques());
            aVisaField.setText(fiche.getAVisa());

            bDateField.setText(fiche.getBDate());
            bMachineField.setText(fiche.getBMachine());
            bPressionAvField.setText(fiche.getBPressionAvant());
            bPressionArField.setText(fiche.getBPressionArriere());
            bSupportField.setText(fiche.getBSupport());
            bNombreField.setText(fiche.getBNombre());
            bRacleField.setText(fiche.getBRacle());
            bInfoField.setText(fiche.getBInfoTechniques());
            bNatureField.setText(fiche.getBNatureEvolution());
            bVisaField.setText(fiche.getBVisa());

            cDateField.setText(fiche.getCDate());
            cMachineField.setText(fiche.getCMachine());
            cPressionAvField.setText(fiche.getCPressionAvant());
            cPressionArField.setText(fiche.getCPressionArriere());
            cSupportField.setText(fiche.getCSupport());
            cNombreField.setText(fiche.getCNombre());
            cRacleField.setText(fiche.getCRacle());
            cInfoField.setText(fiche.getCInfoTechniques());
            cNatureField.setText(fiche.getCNatureEvolution());
            cVisaField.setText(fiche.getCVisa());

            dDateField.setText(fiche.getDDate());
            dMachineField.setText(fiche.getDMachine());
            dPressionAvField.setText(fiche.getDPressionAvant());
            dPressionArField.setText(fiche.getDPressionArriere());
            dSupportField.setText(fiche.getDSupport());
            dNombreField.setText(fiche.getDNombre());
            dRacleField.setText(fiche.getDRacle());
            dInfoField.setText(fiche.getDInfoTechniques());
            dNatureField.setText(fiche.getDNatureEvolution());
            dVisaField.setText(fiche.getDVisa());
        }

        // Layout form with ScrollPane for long form
        ScrollPane scrollPane = new ScrollPane();
        VBox formContent = new VBox(15);
        formContent.setPadding(new Insets(10));

        // Section: General
        Label generalLabel = new Label("INFORMATIONS GÉNÉRALES");
        generalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane generalGrid = new GridPane();
        generalGrid.setHgap(10);
        generalGrid.setVgap(8);
        generalGrid.add(new Label("Client:"), 0, 0);
        generalGrid.add(clientField, 1, 0);
        generalGrid.add(new Label("Produit:"), 0, 1);
        generalGrid.add(produitField, 1, 1);
        generalGrid.add(new Label("Face:"), 0, 2);
        generalGrid.add(faceField, 1, 2);
        generalGrid.add(new Label("N° Programme:"), 0, 3);
        generalGrid.add(programmeField, 1, 3);
        generalGrid.add(new Label("N° PCB:"), 0, 4);
        generalGrid.add(pcbField, 1, 4);

        // Section: Screen & Cream
        Label ecranCremeLabel = new Label("ÉCRAN & CRÈME");
        ecranCremeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane ecranCremeGrid = new GridPane();
        ecranCremeGrid.setHgap(10);
        ecranCremeGrid.setVgap(8);
        ecranCremeGrid.add(new Label("Matière écran:"), 0, 0);
        ecranCremeGrid.add(matiereEcranField, 1, 0);
        ecranCremeGrid.add(new Label("N° Écran:"), 0, 1);
        ecranCremeGrid.add(numeroEcranField, 1, 1);
        ecranCremeGrid.add(new Label("Épaisseur:"), 0, 2);
        ecranCremeGrid.add(epaisseurField, 1, 2);
        ecranCremeGrid.add(new Label("Fournisseur crème:"), 0, 3);
        ecranCremeGrid.add(fournisseurCremeField, 1, 3);
        ecranCremeGrid.add(new Label("Désignation crème:"), 0, 4);
        ecranCremeGrid.add(designationCremeField, 1, 4);
        ecranCremeGrid.add(new Label("Réf. Lacroix:"), 0, 5);
        ecranCremeGrid.add(refLacroixField, 1, 5);
        ecranCremeGrid.add(new Label("Code-barres:"), 0, 6);
        ecranCremeGrid.add(codeBarreField, 1, 6);

        // Section: Passage A
        Label passageALabel = new Label("PASSAGE A");
        passageALabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane passageAGrid = new GridPane();
        passageAGrid.setHgap(10);
        passageAGrid.setVgap(8);
        passageAGrid.add(new Label("Date:"), 0, 0);
        passageAGrid.add(aDateField, 1, 0);
        passageAGrid.add(new Label("Machine:"), 0, 1);
        passageAGrid.add(aMachineField, 1, 1);
        passageAGrid.add(new Label("Pression avant:"), 0, 2);
        passageAGrid.add(aPressionAvField, 1, 2);
        passageAGrid.add(new Label("Pression arrière:"), 0, 3);
        passageAGrid.add(aPressionArField, 1, 3);
        passageAGrid.add(new Label("Support:"), 0, 4);
        passageAGrid.add(aSupportField, 1, 4);
        passageAGrid.add(new Label("Nombre:"), 0, 5);
        passageAGrid.add(aNombreField, 1, 5);
        passageAGrid.add(new Label("Racle (mm):"), 0, 6);
        passageAGrid.add(aRacleField, 1, 6);
        passageAGrid.add(new Label("Info techniques:"), 0, 7);
        passageAGrid.add(aInfoField, 1, 7);
        passageAGrid.add(new Label("Visa:"), 0, 8);
        passageAGrid.add(aVisaField, 1, 8);

        // Section: Passage B
        Label passageBLabel = new Label("PASSAGE B");
        passageBLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane passageBGrid = new GridPane();
        passageBGrid.setHgap(10);
        passageBGrid.setVgap(8);
        passageBGrid.add(new Label("Date:"), 0, 0);
        passageBGrid.add(bDateField, 1, 0);
        passageBGrid.add(new Label("Machine:"), 0, 1);
        passageBGrid.add(bMachineField, 1, 1);
        passageBGrid.add(new Label("Pression avant:"), 0, 2);
        passageBGrid.add(bPressionAvField, 1, 2);
        passageBGrid.add(new Label("Pression arrière:"), 0, 3);
        passageBGrid.add(bPressionArField, 1, 3);
        passageBGrid.add(new Label("Support:"), 0, 4);
        passageBGrid.add(bSupportField, 1, 4);
        passageBGrid.add(new Label("Nombre:"), 0, 5);
        passageBGrid.add(bNombreField, 1, 5);
        passageBGrid.add(new Label("Racle (mm):"), 0, 6);
        passageBGrid.add(bRacleField, 1, 6);
        passageBGrid.add(new Label("Info techniques:"), 0, 7);
        passageBGrid.add(bInfoField, 1, 7);
        passageBGrid.add(new Label("Nature évolution:"), 0, 8);
        passageBGrid.add(bNatureField, 1, 8);
        passageBGrid.add(new Label("Visa:"), 0, 9);
        passageBGrid.add(bVisaField, 1, 9);

        // Section: Passage C
        Label passageCLabel = new Label("PASSAGE C");
        passageCLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane passageCGrid = new GridPane();
        passageCGrid.setHgap(10);
        passageCGrid.setVgap(8);
        passageCGrid.add(new Label("Date:"), 0, 0);
        passageCGrid.add(cDateField, 1, 0);
        passageCGrid.add(new Label("Machine:"), 0, 1);
        passageCGrid.add(cMachineField, 1, 1);
        passageCGrid.add(new Label("Pression avant:"), 0, 2);
        passageCGrid.add(cPressionAvField, 1, 2);
        passageCGrid.add(new Label("Pression arrière:"), 0, 3);
        passageCGrid.add(cPressionArField, 1, 3);
        passageCGrid.add(new Label("Support:"), 0, 4);
        passageCGrid.add(cSupportField, 1, 4);
        passageCGrid.add(new Label("Nombre:"), 0, 5);
        passageCGrid.add(cNombreField, 1, 5);
        passageCGrid.add(new Label("Racle (mm):"), 0, 6);
        passageCGrid.add(cRacleField, 1, 6);
        passageCGrid.add(new Label("Info techniques:"), 0, 7);
        passageCGrid.add(cInfoField, 1, 7);
        passageCGrid.add(new Label("Nature évolution:"), 0, 8);
        passageCGrid.add(cNatureField, 1, 8);
        passageCGrid.add(new Label("Visa:"), 0, 9);
        passageCGrid.add(cVisaField, 1, 9);

        // Section: Passage D
        Label passageDLabel = new Label("PASSAGE D");
        passageDLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane passageDGrid = new GridPane();
        passageDGrid.setHgap(10);
        passageDGrid.setVgap(8);
        passageDGrid.add(new Label("Date:"), 0, 0);
        passageDGrid.add(dDateField, 1, 0);
        passageDGrid.add(new Label("Machine:"), 0, 1);
        passageDGrid.add(dMachineField, 1, 1);
        passageDGrid.add(new Label("Pression avant:"), 0, 2);
        passageDGrid.add(dPressionAvField, 1, 2);
        passageDGrid.add(new Label("Pression arrière:"), 0, 3);
        passageDGrid.add(dPressionArField, 1, 3);
        passageDGrid.add(new Label("Support:"), 0, 4);
        passageDGrid.add(dSupportField, 1, 4);
        passageDGrid.add(new Label("Nombre:"), 0, 5);
        passageDGrid.add(dNombreField, 1, 5);
        passageDGrid.add(new Label("Racle (mm):"), 0, 6);
        passageDGrid.add(dRacleField, 1, 6);
        passageDGrid.add(new Label("Info techniques:"), 0, 7);
        passageDGrid.add(dInfoField, 1, 7);
        passageDGrid.add(new Label("Nature évolution:"), 0, 8);
        passageDGrid.add(dNatureField, 1, 8);
        passageDGrid.add(new Label("Visa:"), 0, 9);
        passageDGrid.add(dVisaField, 1, 9);

        // Add all sections to form
        formContent.getChildren().addAll(
            generalLabel, generalGrid,
            new Separator(),
            ecranCremeLabel, ecranCremeGrid,
            new Separator(),
            passageALabel, passageAGrid,
            new Separator(),
            passageBLabel, passageBGrid,
            new Separator(),
            passageCLabel, passageCGrid,
            new Separator(),
            passageDLabel, passageDGrid
        );

        scrollPane.setContent(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);
        scrollPane.setPrefViewportWidth(550);

        dialog.getDialogPane().setContent(scrollPane);

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                FicheSerigraphie result = fiche != null ? fiche : new FicheSerigraphie();
                
                // Set general fields
                result.setClient(clientField.getText());
                result.setProduit(produitField.getText());
                result.setFace(faceField.getText());
                result.setNumeroProgramme(programmeField.getText());
                result.setNumeroPcb(pcbField.getText());
                
                // Generate index
                String index = result.getClient() + "_" + result.getProduit() + "_" + 
                              result.getFace() + "_" + result.getNumeroProgramme();
                result.setIndex(index);
                
                // Set screen & cream fields
                result.setMatiereEcran(matiereEcranField.getText());
                result.setNumeroEcran(numeroEcranField.getText());
                result.setEpaisseur(epaisseurField.getText());
                result.setFournisseurCreme(fournisseurCremeField.getText());
                result.setDesignationCreme(designationCremeField.getText());
                result.setRefLacroixCreme(refLacroixField.getText());
                result.setCodeBarre(codeBarreField.getText());

                // Set passage A fields
                result.setADate(aDateField.getText());
                result.setAMachine(aMachineField.getText());
                result.setAPressionAvant(aPressionAvField.getText());
                result.setAPressionArriere(aPressionArField.getText());
                result.setASupport(aSupportField.getText());
                result.setANombre(aNombreField.getText());
                result.setARacle(aRacleField.getText());
                result.setAInfoTechniques(aInfoField.getText());
                result.setAVisa(aVisaField.getText());

                // Set passage B fields
                result.setBDate(bDateField.getText());
                result.setBMachine(bMachineField.getText());
                result.setBPressionAvant(bPressionAvField.getText());
                result.setBPressionArriere(bPressionArField.getText());
                result.setBSupport(bSupportField.getText());
                result.setBNombre(bNombreField.getText());
                result.setBRacle(bRacleField.getText());
                result.setBInfoTechniques(bInfoField.getText());
                result.setBNatureEvolution(bNatureField.getText());
                result.setBVisa(bVisaField.getText());

                // Set passage C fields
                result.setCDate(cDateField.getText());
                result.setCMachine(cMachineField.getText());
                result.setCPressionAvant(cPressionAvField.getText());
                result.setCPressionArriere(cPressionArField.getText());
                result.setCSupport(cSupportField.getText());
                result.setCNombre(cNombreField.getText());
                result.setCRacle(cRacleField.getText());
                result.setCInfoTechniques(cInfoField.getText());
                result.setCNatureEvolution(cNatureField.getText());
                result.setCVisa(cVisaField.getText());

                // Set passage D fields
                result.setDDate(dDateField.getText());
                result.setDMachine(dMachineField.getText());
                result.setDPressionAvant(dPressionAvField.getText());
                result.setDPressionArriere(dPressionArField.getText());
                result.setDSupport(dSupportField.getText());
                result.setDNombre(dNombreField.getText());
                result.setDRacle(dRacleField.getText());
                result.setDInfoTechniques(dInfoField.getText());
                result.setDNatureEvolution(dNatureField.getText());
                result.setDVisa(dVisaField.getText());

                return result;
            }
            return null;
        });

        Optional<FicheSerigraphie> result = dialog.showAndWait();
        result.ifPresent(ficheResult -> {
            try {
                if (fiche == null) {
                    // Create new
                    service.create(ficheResult);
                    showAlert("Succès", "Fiche créée avec succès", Alert.AlertType.INFORMATION);
                } else {
                    // Update existing
                    service.update(ficheResult);
                    showAlert("Succès", "Fiche modifiée avec succès", Alert.AlertType.INFORMATION);
                }
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), 
                         Alert.AlertType.ERROR);
            }
        });
    }

    // =========================================================
    // PDF EXPORT - Keep existing functionality
    // =========================================================

    @FXML
    private void handleGenererPDF() {
        FicheSerigraphie selectedFiche = fichesTable.getSelectionModel().getSelectedItem();
        if (selectedFiche == null) {
            showAlert("Information", "Sélectionnez une fiche dans le tableau", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer la fiche en PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("Fiche_" + selectedFiche.getClient() + "_" + 
                             selectedFiche.getProduit() + ".pdf");
        File file = fc.showSaveDialog(null);
        if (file == null) return;

        try {
            exporterPDF(file, selectedFiche);
            showAlert("Succès", "PDF généré avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération du PDF: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
        }
    }

    private void exporterPDF(File file, FicheSerigraphie f) throws Exception {
        DeviceRgb tealDark = new DeviceRgb(0x34, 0x67, 0x71);
        DeviceRgb tealLight = new DeviceRgb(0xEB, 0xF8, 0xF9);
        DeviceRgb headerGray = new DeviceRgb(0x21, 0x26, 0x2A);
        DeviceRgb rowAlt = new DeviceRgb(0xF8, 0xFA, 0xFC);
        DeviceRgb yellow = new DeviceRgb(0xFF, 0xF8, 0xE1);
        DeviceRgb borderGray = new DeviceRgb(0xE2, 0xE8, 0xF0);

        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.setMargins(30, 30, 30, 30);

            // Title
            Table titleTable = new Table(UnitValue.createPercentArray(new float[]{80, 20}))
                    .setWidth(UnitValue.createPercentValue(100));
            Cell titleCell = new Cell()
                    .add(new Paragraph("FICHE SÉRIGRAPHIE").setBold().setFontSize(16)
                            .setFontColor(ColorConstants.WHITE))
                    .add(new Paragraph(safe(f.getIndex())).setFontSize(8)
                            .setFontColor(new DeviceRgb(0xBF, 0xDB, 0xDF)))
                    .setBackgroundColor(tealDark)
                    .setPadding(12).setBorder(null);
            String numStr = f.getNumeroFiche() > 0 ? "N° " + f.getNumeroFiche() : "";
            Cell numCell = new Cell()
                    .add(new Paragraph(numStr).setBold().setFontSize(14)
                            .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT))
                    .setBackgroundColor(tealDark)
                    .setPadding(12).setBorder(null);
            titleTable.addCell(titleCell);
            titleTable.addCell(numCell);
            doc.add(titleTable);
            doc.add(new Paragraph(" ").setFontSize(4));

            // General info
            doc.add(buildSectionHeader("INFORMATIONS GÉNÉRALES", tealLight, tealDark));
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{20, 20, 10, 25, 25}))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(infoTable, headerGray, "CLIENT", "PRODUIT", "FACE", "PROGRAMME", "PCB");
            addDataRow(infoTable, rowAlt, borderGray, true,
                    f.getClient(), f.getProduit(), f.getFace(), 
                    f.getNumeroProgramme(), f.getNumeroPcb());
            doc.add(infoTable);
            doc.add(new Paragraph(" ").setFontSize(4));

            // Screen & cream
            doc.add(buildSectionHeader("ÉCRAN & CRÈME", tealLight, tealDark));
            Table ecranTable = new Table(UnitValue.createPercentArray(new float[]{20, 15, 20, 25, 20}))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(ecranTable, headerGray, "ÉCRAN", "ÉPAISSEUR", "CRÈME", "RÉF. LACROIX", "FOURNISSEUR");
            addDataRow(ecranTable, rowAlt, borderGray, true,
                    f.getNumeroEcran(), f.getEpaisseur(), f.getDesignationCreme(),
                    f.getRefLacroixCreme(), f.getFournisseurCreme());
            doc.add(ecranTable);
            doc.add(new Paragraph(" ").setFontSize(4));

            // Passages
            doc.add(buildSectionHeader("PASSAGES", tealLight, tealDark));
            float[] passWidths = {11, 15, 9, 9, 12, 6, 8, 24, 6};
            Table passTable = new Table(UnitValue.createPercentArray(passWidths))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(passTable, headerGray,
                    "Date", "Machine", "Press. Av.", "Press. Ar.", "Support",
                    "Nbre", "Racle", "Info", "Visa");

            int row = 0;
            if (f.hasPassageA()) {
                addPassageRow(passTable, row++ % 2 == 0 ? ColorConstants.WHITE : rowAlt,
                        borderGray, "A", f.getADate(), f.getAMachine(), f.getAPressionAvant(),
                        f.getAPressionArriere(), f.getASupport(), f.getANombre(),
                        f.getARacle(), f.getAInfoTechniques(), f.getAVisa());
            }
            if (f.hasPassageB()) {
                addPassageRow(passTable, row++ % 2 == 0 ? ColorConstants.WHITE : rowAlt,
                        borderGray, "B", f.getBDate(), f.getBMachine(), f.getBPressionAvant(),
                        f.getBPressionArriere(), f.getBSupport(), f.getBNombre(),
                        f.getBRacle(), f.getBInfoTechniques(), f.getBVisa());
                addNatureRow(passTable, yellow, f.getBNatureEvolution());
            }
            if (f.hasPassageC()) {
                addPassageRow(passTable, row++ % 2 == 0 ? ColorConstants.WHITE : rowAlt,
                        borderGray, "C", f.getCDate(), f.getCMachine(), f.getCPressionAvant(),
                        f.getCPressionArriere(), f.getCSupport(), f.getCNombre(),
                        f.getCRacle(), f.getCInfoTechniques(), f.getCVisa());
                addNatureRow(passTable, yellow, f.getCNatureEvolution());
            }
            if (f.hasPassageD()) {
                addPassageRow(passTable, row++ % 2 == 0 ? ColorConstants.WHITE : rowAlt,
                        borderGray, "D", f.getDDate(), f.getDMachine(), f.getDPressionAvant(),
                        f.getDPressionArriere(), f.getDSupport(), f.getDNombre(),
                        f.getDRacle(), f.getDInfoTechniques(), f.getDVisa());
                addNatureRow(passTable, yellow, f.getDNatureEvolution());
            }

            doc.add(passTable);
            doc.add(new Paragraph(" ").setFontSize(6));
            doc.add(new Paragraph("Document LACROIX Electronics - Confidentiel")
                    .setFontSize(7).setItalic()
                    .setFontColor(new DeviceRgb(0x94, 0xA3, 0xB8)));
        }
    }


    // PDF Helper methods
    private Paragraph buildSectionHeader(String text, DeviceRgb bg, DeviceRgb fg) {
        return new Paragraph(text)
                .setBold().setFontSize(9)
                .setFontColor(fg)
                .setBackgroundColor(bg)
                .setPadding(4);
    }

    private void addHeaderRow(Table table, DeviceRgb bg, String... headers) {
        for (String h : headers) {
            table.addCell(new Cell()
                    .add(new Paragraph(h).setBold().setFontSize(8)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(bg)
                    .setPadding(5)
                    .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(
                            new DeviceRgb(0x34, 0x67, 0x71), 1)));
        }
    }

    private void addDataRow(Table table, DeviceRgb altBg, DeviceRgb borderColor,
                           boolean alternate, String... values) {
        for (String v : values) {
            table.addCell(new Cell()
                    .add(new Paragraph(safe(v)).setFontSize(9))
                    .setBackgroundColor(alternate ? altBg : ColorConstants.WHITE)
                    .setPadding(5)
                    .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(borderColor, 0.5f)));
        }
    }

    private void addPassageRow(Table table, com.itextpdf.kernel.colors.Color bg,
                              DeviceRgb borderColor, String lettre,
                              String date, String machine, String pressAv, String pressAr,
                              String support, String nombre, String racle, String info, String visa) {
        String[] vals = {
                lettre + " " + safe(date), safe(machine), safe(pressAv), safe(pressAr),
                safe(support), safe(nombre), safe(racle), safe(info), safe(visa)
        };
        for (int i = 0; i < vals.length; i++) {
            Paragraph p = new Paragraph(vals[i]).setFontSize(8);
            if (i == 0) p.setBold();
            table.addCell(new Cell()
                    .add(p)
                    .setBackgroundColor(bg)
                    .setPadding(4)
                    .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(borderColor, 0.5f)));
        }
    }

    private void addNatureRow(Table table, DeviceRgb bg, String nature) {
        if (nature == null || nature.isBlank() || nature.equals("0") || nature.equals("N/A")) return;
        table.addCell(new Cell(1, 9)
                .add(new Paragraph("↳ Nature évolution: " + nature)
                        .setFontSize(7).setItalic()
                        .setFontColor(new DeviceRgb(0x85, 0x64, 0x04)))
                .setBackgroundColor(bg)
                .setPadding(4)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(
                        new DeviceRgb(0xFF, 0xE0, 0x82), 0.5f)));
    }

    private String safe(String val) {
        if (val == null || val.isBlank() || val.equals("0")) return "—";
        return val.trim();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-padding: 15;");
        alert.showAndWait();
    }

    public void setProduit(Produit produit) {
    }
}
