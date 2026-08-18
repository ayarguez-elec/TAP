package controller;

import entities.Pouchoir;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import services.PouchoirService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodificationController {

    @FXML
    private TextField searchField;
    @FXML
    private TilePane cardContainer;
    @FXML
    private Label totalLabel;
    @FXML
    private Label codesLabel;
    @FXML
    private Label enAttenteLabel;
    @FXML
    private Label nonCodesLabel;
    @FXML
    private Label selectedCountLabel;
    @FXML
    private Button toggleSelectButton;

    @FXML
    private TextArea detailEmailArea;
    @FXML
    private TextField codeField;
    @FXML
    private Button btnCoder;

    // Nouveaux champs pour la modification du code
    @FXML
    private Label codeDisplayLabel;
    @FXML
    private Button btnModifierCode;
    @FXML
    private HBox modifierCodePanel;
    @FXML
    private TextField codeModifierField;

    private PouchoirService service = new PouchoirService();
    private List<Pouchoir> pouchoirs;
    private List<Pouchoir> selectedPouchoirs = new ArrayList<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private boolean allSelected = false;

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, old, newVal) -> filterCards(newVal));
        loadData();
        modifierCodePanel.setVisible(false);
        modifierCodePanel.setManaged(false);
    }

    private void loadData() {
        try {
            pouchoirs = service.readAll();
            displayCards(pouchoirs);
            updateStats();
            updateSelectedCount();
            updateEmailArea();
            updateCodeDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données", Alert.AlertType.ERROR);
        }
    }

    private void filterCards(String search) {
        if (pouchoirs == null) return;
        if (search == null || search.trim().isEmpty()) {
            displayCards(pouchoirs);
            return;
        }
        String s = search.toLowerCase().trim();
        List<Pouchoir> filtered = pouchoirs.stream()
                .filter(p -> p.getRefPouchoir().toLowerCase().contains(s))
                .collect(Collectors.toList());
        displayCards(filtered);
    }

    private void displayCards(List<Pouchoir> list) {
        cardContainer.getChildren().clear();
        checkBoxes.clear();
        for (Pouchoir p : list) {
            VBox card = createCard(p);
            cardContainer.getChildren().add(card);
        }
        updateStats();
    }

    private VBox createCard(Pouchoir p) {
        boolean isCode = p.isCodeLacroix();
        String status = p.getStatusWorkflow();

        String bgColor = isCode ? "#f0faf0" : status.equals("EN_ATTENTE") ? "#fff8e1" : "#faf0f0";
        String borderColor = isCode ? "#2E7D32" : status.equals("EN_ATTENTE") ? "#F0A500" : "#D9691D";

        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setPrefWidth(200);
        card.setPrefHeight(160);
        card.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-border-radius: 12; -fx-background-radius: 12;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 2; -fx-border-radius: 12; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: " + bgColor + ";" +
                        "-fx-border-radius: 12; -fx-background-radius: 12;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 2.5; -fx-border-radius: 12; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: " + bgColor + ";" +
                        "-fx-border-radius: 12; -fx-background-radius: 12;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 2; -fx-border-radius: 12; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);")
        );

        card.setOnMouseClicked(e -> toggleSelection(p));

        // Checkbox
        CheckBox checkBox = new CheckBox();
        checkBox.setStyle("-fx-cursor: hand; -fx-font-size: 12px;");
        checkBox.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                if (!selectedPouchoirs.contains(p)) {
                    selectedPouchoirs.add(p);
                }
            } else {
                selectedPouchoirs.remove(p);
            }
            updateSelectedCount();
            updateEmailArea();
            updateToggleButtonState();
        });

        if (selectedPouchoirs.contains(p)) {
            checkBox.setSelected(true);
        }
        checkBoxes.add(checkBox);

        // Status indicator
        Circle circle = new Circle(6, Color.web(borderColor));

        Label ref = new Label(p.getRefPouchoir());
        ref.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #21262A;");

        String statusText = isCode ? "CODÉ" : status.equals("EN_ATTENTE") ? "EN ATTENTE" : "NON CODÉ";
        Label badge = new Label(statusText);
        badge.setStyle("-fx-background-color: " + borderColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 2 10;" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Ligne supérieure : checkbox + ref + badge
        HBox topBox = new HBox(8, checkBox, ref, badge);
        topBox.setAlignment(Pos.CENTER_LEFT);

        // ===== INFORMATIONS EN VERTICAL (une en dessous de l'autre) =====
        VBox infoBox = new VBox(4);
        infoBox.setPadding(new Insets(6, 0, 0, 28)); // Décalage aligné avec la checkbox

        // Programme
        Label progLabel = new Label("📋 " + p.getProgramme());
        progLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 11px; -fx-font-weight: 500;");

        // Emplacement
        Label empLabel = new Label("📍 Emplacement: " + p.getEmplacement());
        empLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        // Face
        Label faceLabel = new Label("🔢 Face: " + p.getFace());
        faceLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");


        // Ajout des informations à la VBox (une en dessous de l'autre)
        infoBox.getChildren().addAll(progLabel, empLabel, faceLabel);

        // Code (si présent)
        Label codeLabel = new Label("");
        String codeValue = p.getCodeRecu();
        if (isCode && codeValue != null && !codeValue.isEmpty()) {
            codeLabel.setText(codeValue);
            codeLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold;");
            codeLabel.setPadding(new Insets(2, 0, 0, 28));
        }

        // Construction de la carte
        card.getChildren().addAll(topBox, infoBox);
        if (!codeLabel.getText().isEmpty()) {
            card.getChildren().add(codeLabel);
        }

        return card;
    }

    private void toggleSelection(Pouchoir p) {
        if (selectedPouchoirs.contains(p)) {
            selectedPouchoirs.remove(p);
        } else {
            if (!p.isCodeLacroix()) {
                selectedPouchoirs.add(p);
            }
        }
        updateSelectedCount();
        updateEmailArea();
        updateToggleButtonState();
        displayCards(pouchoirs);
    }

    private void updateToggleButtonState() {
        if (checkBoxes.isEmpty()) return;

        boolean allChecked = checkBoxes.stream()
                .filter(cb -> !cb.isDisabled())
                .allMatch(CheckBox::isSelected);
        boolean anySelected = checkBoxes.stream().anyMatch(CheckBox::isSelected);

        if (allChecked && anySelected) {
            toggleSelectButton.setText("Désélectionner tous");
            toggleSelectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-padding: 5 16; -fx-border-radius: 4; -fx-background-radius: 4;" +
                    "-fx-cursor: hand; -fx-font-size: 12px;");
            allSelected = true;
        } else {
            toggleSelectButton.setText("Sélectionner tous");
            toggleSelectButton.setStyle("-fx-background-color: #3A98A5; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-padding: 5 16; -fx-border-radius: 4; -fx-background-radius: 4;" +
                    "-fx-cursor: hand; -fx-font-size: 12px;");
            allSelected = false;
        }
    }

    @FXML
    private void handleToggleSelect() {
        if (checkBoxes.isEmpty()) return;

        if (allSelected) {
            for (CheckBox cb : checkBoxes) {
                cb.setSelected(false);
            }
            selectedPouchoirs.clear();
            allSelected = false;
            toggleSelectButton.setText("Sélectionner tous");
            toggleSelectButton.setStyle("-fx-background-color: #3A98A5; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-padding: 5 16; -fx-border-radius: 4; -fx-background-radius: 4;" +
                    "-fx-cursor: hand; -fx-font-size: 12px;");
        } else {
            selectedPouchoirs.clear();
            for (int i = 0; i < checkBoxes.size(); i++) {
                CheckBox cb = checkBoxes.get(i);
                if (i < pouchoirs.size() && !pouchoirs.get(i).isCodeLacroix()) {
                    cb.setSelected(true);
                    selectedPouchoirs.add(pouchoirs.get(i));
                }
            }
            allSelected = true;
            toggleSelectButton.setText("☐ Désélectionner tous");
            toggleSelectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-padding: 5 16; -fx-border-radius: 4; -fx-background-radius: 4;" +
                    "-fx-cursor: hand; -fx-font-size: 12px;");
        }
        updateSelectedCount();
        updateEmailArea();
    }

    private void updateSelectedCount() {
        selectedCountLabel.setText("Sélectionnés: " + selectedPouchoirs.size());
        boolean hasEnAttente = selectedPouchoirs.stream()
                .anyMatch(p -> p.getStatusWorkflow().equals("EN_ATTENTE"));
        btnCoder.setDisable(!hasEnAttente);
    }

    private void updateEmailArea() {
        if (selectedPouchoirs.isEmpty()) {
            detailEmailArea.setText("Sélectionnez des pouchoirs");
            updateCodeDisplay();
            return;
        }

        // Mettre à jour l'affichage du code
        updateCodeDisplay();

        List<Pouchoir> nonCodes = selectedPouchoirs.stream()
                .filter(p -> !p.isCodeLacroix())
                .collect(Collectors.toList());

        if (nonCodes.isEmpty()) {
            detailEmailArea.setText("Les pochoirs sélectionnés sont déjà codés");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hello,\n\n");
        sb.append("Can you create the new code for\n\n");

        Map<String, List<Pouchoir>> grouped = nonCodes.stream()
                .collect(Collectors.groupingBy(Pouchoir::getProgramme));

        for (Map.Entry<String, List<Pouchoir>> entry : grouped.entrySet()) {
            String programme = entry.getKey();
            List<Pouchoir> list = entry.getValue();
            for (Pouchoir p : list) {
                sb.append("Stencil_").append(p.getProgramme())
                        .append("_OF:").append(p.getRefPouchoir()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Thanks in advance\n\n");
        sb.append("Best regards");

        detailEmailArea.setText(sb.toString());
    }

    private void updateCodeDisplay() {
        if (selectedPouchoirs == null || selectedPouchoirs.isEmpty()) {
            codeDisplayLabel.setText("-");
            codeDisplayLabel.setStyle("-fx-text-fill: #999;");
            return;
        }

        // Prendre le premier pouchoir sélectionné pour afficher son code
        Pouchoir p = selectedPouchoirs.get(0);
        String code = p.getCodeRecu();

        if (code != null && !code.isEmpty()) {
            codeDisplayLabel.setText(code);
            codeDisplayLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        } else {
            codeDisplayLabel.setText("(aucun code)");
            codeDisplayLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
        }
    }

    private void updateStats() {
        if (pouchoirs == null) return;
        long total = pouchoirs.size();
        long codes = pouchoirs.stream().filter(Pouchoir::isCodeLacroix).count();
        long enAttente = pouchoirs.stream()
                .filter(p -> !p.isCodeLacroix() && p.getDateEnvoi() != null && !p.getDateEnvoi().isEmpty())
                .count();
        long nonCodes = total - codes - enAttente;

        totalLabel.setText("Total: " + total);
        codesLabel.setText("Codés: " + codes);
        enAttenteLabel.setText("En attente: " + enAttente);
        nonCodesLabel.setText("Non codés: " + nonCodes);
    }

    // ===== GESTION DE L'EMAIL =====

    @FXML
    private void handleGenererTousEmails() {
        List<Pouchoir> sansCode = pouchoirs.stream()
                .filter(p -> p.getStatusWorkflow().equals("SANS_CODE"))
                .collect(Collectors.toList());

        if (sansCode.isEmpty()) {
            showAlert("Info", "Aucun pochoir sans code !", Alert.AlertType.ERROR);
            detailEmailArea.setText("Aucun pochoir sans code !");
            return;
        }

        selectedPouchoirs.clear();
        selectedPouchoirs.addAll(sansCode);
        displayCards(pouchoirs);
        updateSelectedCount();
        updateToggleButtonState();
        updateEmailArea();
        showAlert("Succès", sansCode.size() + " emails générés !", Alert.AlertType.ERROR);
    }

    @FXML
    private void handleCopyEmail() {
        String email = detailEmailArea.getText();
        if (email == null || email.isEmpty()) {
            showAlert("Info", "Aucun email à copier", Alert.AlertType.ERROR);
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(email);
        Clipboard.getSystemClipboard().setContent(content);
        showAlert("Succès", "Email copié !", Alert.AlertType.ERROR);
    }

    @FXML
    private void handleMarquerEnvoye() {
        if (selectedPouchoirs.isEmpty()) {
            showAlert("Info", "Sélectionnez des pochoirs", Alert.AlertType.ERROR);
            return;
        }

        List<Pouchoir> nonCodes = selectedPouchoirs.stream()
                .filter(p -> !p.isCodeLacroix() && p.getStatusWorkflow().equals("SANS_CODE"))
                .collect(Collectors.toList());

        if (nonCodes.isEmpty()) {
            showAlert("Info", "Aucun pochoir sélectionné à envoyer", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Marquer comme envoyé");
        confirm.setContentText("Confirmer pour " + nonCodes.size() + " pochoir(s) ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    for (Pouchoir p : nonCodes) {
                        p.setDateEnvoi(date);
                        service.update(p);
                    }
                    loadData();
                    selectedPouchoirs.clear();
                    selectedPouchoirs.addAll(nonCodes);
                    updateSelectedCount();
                    updateToggleButtonState();
                    updateEmailArea();
                    showAlert("Succès", nonCodes.size() + " pochoirs marqués comme envoyés !", Alert.AlertType.ERROR);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la mise à jour", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleCoder() {
        List<Pouchoir> enAttente = selectedPouchoirs.stream()
                .filter(p -> p.getStatusWorkflow().equals("EN_ATTENTE"))
                .collect(Collectors.toList());

        if (enAttente.isEmpty()) {
            showAlert("Info", "Sélectionnez des pochoirs en attente", Alert.AlertType.ERROR);
            return;
        }

        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            showAlert("Info", "Entrez le code reçu", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Marquer comme codé");
        confirm.setContentText("Code: " + code + " pour " + enAttente.size() + " pochoir(s) ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    for (Pouchoir p : enAttente) {
                        p.setCodeRecu(code);
                        p.setDateReception(date);
                        p.setCodeLacroix(true);
                        service.update(p);
                    }
                    codeField.clear();
                    loadData();
                    selectedPouchoirs.clear();
                    updateSelectedCount();
                    updateToggleButtonState();
                    updateEmailArea();
                    showAlert("Succès", enAttente.size() + " pochoirs codés !", Alert.AlertType.ERROR);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la mise à jour", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ===== GESTION DE LA MODIFICATION DU CODE =====

    @FXML
    private void handleModifierCode() {
        if (selectedPouchoirs == null || selectedPouchoirs.isEmpty()) {
            showAlert("Info", "Sélectionnez un pochoir", Alert.AlertType.ERROR);
            return;
        }

        Pouchoir p = selectedPouchoirs.get(0);
        String codeActuel = p.getCodeRecu();
        codeModifierField.setText(codeActuel != null ? codeActuel : "");
        modifierCodePanel.setVisible(true);
        modifierCodePanel.setManaged(true);
        codeModifierField.requestFocus();
    }

    @FXML
    private void handleSauvegarderCode() {
        if (selectedPouchoirs == null || selectedPouchoirs.isEmpty()) {
            showAlert("Info", "Sélectionnez un pochoir", Alert.AlertType.ERROR);
            return;
        }

        Pouchoir p = selectedPouchoirs.get(0);
        String nouveauCode = codeModifierField.getText().trim();

        if (nouveauCode.isEmpty()) {
            showAlert("Info", "Entrez un code valide", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Si le pouchoir n'est pas encore codé, on le code
            if (!p.isCodeLacroix()) {
                p.setCodeLacroix(true);
                p.setDateReception(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            p.setCodeRecu(nouveauCode);
            service.update(p);

            // Cacher le panel de modification
            modifierCodePanel.setVisible(false);
            modifierCodePanel.setManaged(false);

            // Recharger les données
            loadData();

            // Réafficher les détails
            if (selectedPouchoirs != null && !selectedPouchoirs.isEmpty()) {
                updateEmailArea();
                updateCodeDisplay();
            }

            showAlert("Succès", "Code modifié avec succès !", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAnnulerModifCode() {
        modifierCodePanel.setVisible(false);
        modifierCodePanel.setManaged(false);
        codeModifierField.clear();
    }

    // ===== AUTRES MÉTHODES =====

    @FXML
    private void handleRefresh() {
        loadData();
        updateToggleButtonState();
        modifierCodePanel.setVisible(false);
        modifierCodePanel.setManaged(false);
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/suivi-pouchoir-view.fxml"));
            Pane suiviView = loader.load();  // ← Utilise Pane au lieu de Parent

            suiviView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            suiviView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Node node = cardContainer;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane) {
                    String id = ((StackPane) node).getId();
                    if (id != null && id.equals("contentArea")) {
                        contentArea = (StackPane) node;
                        break;
                    }
                }
                node = node.getParent();
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(suiviView);
            } else {
                Scene scene = cardContainer.getScene();
                if (scene != null) {
                    scene.setRoot(suiviView);
                } else {
                    showAlert("Erreur", "Impossible de trouver la zone de contenu", Alert.AlertType.ERROR);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la vue précédente", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType error) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}