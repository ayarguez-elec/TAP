package controller;

import entities.AnomalieStencil;
import entities.Pouchoir;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.AnomalieStencilService;
import services.PouchoirService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ProductionAnomalieController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private VBox pochoirListContainer;
    @FXML private Label totalPochoirsLabel;
    @FXML private Label statDispoLabel, statSortisLabel, statAutresLabel;

    // Detail panel
    @FXML private VBox emptyState, detailContent;
    @FXML private Label detailRefLabel, detailProgLabel, detailStatutBadge;
    @FXML private Label infoRef, infoProg, infoRacle, infoEmplacement;
    @FXML private Label infoStatut, infoFace, infoEpaisseur, infoSupport;
    @FXML private Label infoStock, infoQte, infoNbFlan;
    @FXML private VBox anomaliesListContainer;
    @FXML private Label anomaliesCountLabel;

    private final PouchoirService pouchoirService = new PouchoirService();
    private final AnomalieStencilService anomalieService = new AnomalieStencilService();
    private List<Pouchoir> allPochoirs;
    private Pouchoir selectedPochoir;

    @FXML
    public void initialize() {
        anomalieService.createTable();
        filterStatut.getItems().addAll("Tous", "disponible", "sorti");
        filterStatut.setValue("Tous");
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        filterStatut.valueProperty().addListener((obs, old, val) -> applyFilter());
        loadPochoirs();
    }

    private void loadPochoirs() {
        try {
            allPochoirs = pouchoirService.readAll();
            applyFilter();
            updateStats();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void applyFilter() {
        if (allPochoirs == null) return;
        String keyword = searchField.getText().toLowerCase();
        String statut = filterStatut.getValue();
        List<Pouchoir> filtered = allPochoirs.stream()
                .filter(p -> keyword.isBlank()
                        || p.getRefPouchoir().toLowerCase().contains(keyword)
                        || (p.getProgramme() != null && p.getProgramme().toLowerCase().contains(keyword))
                        || (p.getRacle() != null && p.getRacle().toLowerCase().contains(keyword)))
                .filter(p -> "Tous".equals(statut) || statut.equals(p.getStatut()))
                .collect(Collectors.toList());
        totalPochoirsLabel.setText(filtered.size() + " pochoir" + (filtered.size() > 1 ? "s" : ""));
        buildCards(filtered);
    }

    private void updateStats() {
        if (allPochoirs == null) return;
        long dispo = allPochoirs.stream().filter(p -> "disponible".equals(p.getStatut())).count();
        long sorti = allPochoirs.stream().filter(p -> "sorti".equals(p.getStatut())).count();
        long autres = allPochoirs.size() - dispo - sorti;
        statDispoLabel.setText(String.valueOf(dispo));
        statSortisLabel.setText(String.valueOf(sorti));
        statAutresLabel.setText(String.valueOf(autres));
    }

    private void buildCards(List<Pouchoir> pochoirs) {
        pochoirListContainer.getChildren().clear();
        if (pochoirs.isEmpty()) {
            Label l = new Label("Aucun pochoir trouve");
            l.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 13px; -fx-padding: 20;");
            pochoirListContainer.getChildren().add(l);
            return;
        }
        for (Pouchoir p : pochoirs) pochoirListContainer.getChildren().add(buildCard(p));
    }

    private VBox buildCard(Pouchoir p) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10, 12, 10, 12));
        // Verifier si ce pochoir a des anomalies actives
        List<AnomalieStencil> anomalies = anomalieService.search(p.getRefPouchoir(), null);
        long actives = anomalies.stream().filter(a -> !a.isResolu()).count();
        String borderColor = actives > 0 ? "#E53E3E" : "disponible".equals(p.getStatut()) ? "#46BE62" : "sorti".equals(p.getStatut()) ? "#D9691D" : "#6F8D94";
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 1 1 1 3; " +
                "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);");

        HBox row1 = new HBox(8); row1.setAlignment(Pos.CENTER_LEFT);
        Label ref = new Label(p.getRefPouchoir());
        ref.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #21262A;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Badge anomalie si actives
        if (actives > 0) {
            Label anomBadge = new Label("⚠ " + actives + " anomalie" + (actives > 1 ? "s" : ""));
            anomBadge.setStyle("-fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: #E53E3E; -fx-text-fill: white;");
            row1.getChildren().addAll(ref, sp, anomBadge);
        } else {
            String statutColor = "disponible".equals(p.getStatut()) ? "#46BE62" : "sorti".equals(p.getStatut()) ? "#D9691D" : "#6F8D94";
            Label badge = new Label(p.getStatut() != null ? p.getStatut() : "—");
            badge.setStyle("-fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: " + statutColor + "; -fx-text-fill: white;");
            row1.getChildren().addAll(ref, sp, badge);
        }

        HBox row2 = new HBox(12); row2.setAlignment(Pos.CENTER_LEFT);
        Label prog = new Label("Prog: " + (p.getProgramme() != null ? p.getProgramme() : "—"));
        prog.setStyle("-fx-font-size: 11px; -fx-text-fill: #6F8D94;");
        Label racle = new Label("Racle: " + (p.getRacle() != null ? p.getRacle() : "—"));
        racle.setStyle("-fx-font-size: 11px; -fx-text-fill: #6F8D94;");
        row2.getChildren().addAll(prog, racle);

        card.getChildren().addAll(row1, row2);
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("-fx-background-color: #ffffff", "-fx-background-color: #F0F9FF")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-background-color: #F0F9FF", "-fx-background-color: #ffffff")));
        card.setOnMouseClicked(e -> showDetail(p));
        return card;
    }

    private void set(javafx.scene.control.Label label, String value) {
        if (label != null) label.setText(value != null ? value : "—");
    }

    private void showDetail(Pouchoir p) {
        selectedPochoir = p;
        String statutColor = "disponible".equals(p.getStatut()) ? "#46BE62" : "sorti".equals(p.getStatut()) ? "#D9691D" : "#6F8D94";
        set(detailRefLabel, p.getRefPouchoir());
        set(detailProgLabel, p.getProgramme());
        if (detailStatutBadge != null) {
            detailStatutBadge.setText(p.getStatut() != null ? p.getStatut() : "—");
            detailStatutBadge.setStyle("-fx-padding: 5 14; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: " + statutColor + "; -fx-text-fill: white;");
        }
        set(infoRef,         p.getRefPouchoir());
        set(infoProg,        p.getProgramme());
        set(infoRacle,       p.getRacle());
        set(infoEmplacement, String.valueOf(p.getEmplacement()));
        set(infoStatut,      p.getStatut());
        set(infoFace,        String.valueOf(p.getFace()));
        set(infoEpaisseur,   p.getEpaisseur());
        set(infoSupport,     p.getSupport());
        set(infoStock,       p.getStockActuel() + " / " + p.getQuantiteTotale());
        set(infoQte,         String.valueOf(p.getQuantiteTotale()));
        set(infoNbFlan,      String.valueOf(p.getNbCartesParFlan()));
        loadAnomaliesForPochoir(p);
        if (emptyState != null)  { emptyState.setVisible(false);  emptyState.setManaged(false); }
        if (detailContent != null) { detailContent.setVisible(true); detailContent.setManaged(true); }
    }

    private void loadAnomaliesForPochoir(Pouchoir p) {
        if (anomaliesListContainer == null) return;
        anomaliesListContainer.getChildren().clear();
        List<AnomalieStencil> list = anomalieService.search(p.getRefPouchoir(), null);
        if (anomaliesCountLabel != null)
            anomaliesCountLabel.setText(list.size() + " anomalie" + (list.size() > 1 ? "s" : ""));
        if (list.isEmpty()) {
            Label l = new Label("Aucune anomalie signalee");
            l.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 12px;");
            anomaliesListContainer.getChildren().add(l);
            return;
        }
        for (AnomalieStencil a : list) {
            HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(6, 8, 6, 8));
            String color = "CRITIQUE".equals(a.getDegre()) ? "#E53E3E" : "MAJEUR".equals(a.getDegre()) ? "#D9691D" : "#46BE62";
            row.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + color + "40; -fx-border-width: 1;");
            VBox info = new VBox(2);
            Label type = new Label(a.getTypeProbleme() != null ? a.getTypeProbleme() : "—");
            type.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #21262A;");
            Label rem = new Label(a.getRemarque() != null && !a.getRemarque().isBlank() ? a.getRemarque() : "Pas de remarque");
            rem.setStyle("-fx-font-size: 11px; -fx-text-fill: #6F8D94;"); rem.setWrapText(true);
            info.getChildren().addAll(type, rem);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            VBox badges = new VBox(4);
            Label degreBadge = new Label(a.getDegre());
            degreBadge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
            if (a.isResolu()) {
                Label resoluBadge = new Label("Resolu");
                resoluBadge.setStyle("-fx-background-color: #46BE62; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
                badges.getChildren().addAll(degreBadge, resoluBadge);
            } else {
                // Bouton Resoudre
                Button btnRes = new Button("Resoudre");
                btnRes.setStyle("-fx-background-color: #46BE62; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 10px;");
                btnRes.setOnAction(ev -> { a.setResolu(true); try { anomalieService.update(a); loadAnomaliesForPochoir(selectedPochoir); applyFilter(); } catch (Exception ex) { ex.printStackTrace(); } });
                badges.getChildren().addAll(degreBadge, btnRes);
            }
            row.getChildren().addAll(info, sp, badges);
            anomaliesListContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleSignalerAnomalie() {
        if (selectedPochoir == null) return;
        String fieldStyle = "-fx-padding: 6 10; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-color: #d0d5dd; -fx-border-width: 1; -fx-font-size: 12px; -fx-background-color: #fafbfc;";
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Signaler une anomalie — " + selectedPochoir.getRefPouchoir());
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().setPrefWidth(440);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(16));
        // Stencil pre-rempli
        TextField fStencil = new TextField(selectedPochoir.getRefPouchoir()); fStencil.setStyle(fieldStyle); fStencil.setEditable(false);
        fStencil.setStyle(fieldStyle + "-fx-opacity: 0.7;");
        TextField fLigne = new TextField(selectedPochoir.getProgramme() != null ? selectedPochoir.getProgramme() : ""); fLigne.setStyle(fieldStyle); fLigne.setPromptText("Ex: CMS01");
        TextField fType = new TextField(); fType.setPromptText("Ex: Bourrage creme, ecran defectueux..."); fType.setStyle(fieldStyle); fType.setPrefWidth(260);
        ComboBox<String> fDegre = new ComboBox<>(); fDegre.getItems().addAll("CRITIQUE","MAJEUR","MINEUR"); fDegre.setValue("MINEUR"); fDegre.setStyle(fieldStyle);
        TextArea fRem = new TextArea(); fRem.setPromptText("Remarques, observations..."); fRem.setPrefRowCount(3); fRem.setStyle(fieldStyle);
        g.add(lbl("Stencil"), 0, 0); g.add(fStencil, 1, 0);
        g.add(lbl("Ligne CMS"), 0, 1); g.add(fLigne, 1, 1);
        g.add(lbl("Type de probleme *"), 0, 2); g.add(fType, 1, 2);
        g.add(lbl("Degre d'importance *"), 0, 3); g.add(fDegre, 1, 3);
        g.add(lbl("Remarque"), 0, 4); g.add(fRem, 1, 4);
        d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK && !fType.getText().isBlank()) {
                AnomalieStencil a = new AnomalieStencil();
                a.setStencil(selectedPochoir.getRefPouchoir());
                a.setLigne(fLigne.getText().trim());
                a.setTypeProbleme(fType.getText().trim());
                a.setDegre(fDegre.getValue());
                a.setRemarque(fRem.getText().trim());
                a.setResolu(false);
                try {
                    anomalieService.create(a);
                    loadAnomaliesForPochoir(selectedPochoir);
                    applyFilter(); // refresh cartes
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 12px;"); l.setMinWidth(130); return l;
    }
}