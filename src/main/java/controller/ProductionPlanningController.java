package controller;

import entities.PlanningCms;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import services.PlanningCmsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.List;

public class ProductionPlanningController {

    @FXML private TabPane cmsTabPane;
    @FXML private Label semaineLabel;
    @FXML private Spinner<Integer> semaineSpinner;

    // Tables par CMS
    @FXML private TableView<PlanningCms> tableCMS01, tableCMS02, tableCMS03, tableCMS04, tableCMS05;

    // Stats par CMS
    @FXML private Label chargeCMS01Label, deltaCMS01Label, ouvertureCMS01Label;
    @FXML private Label chargeCMS02Label, deltaCMS02Label, ouvertureCMS02Label;
    @FXML private Label chargeCMS03Label, deltaCMS03Label, ouvertureCMS03Label;
    @FXML private Label chargeCMS04Label, deltaCMS04Label, ouvertureCMS04Label;
    @FXML private Label chargeCMS05Label, deltaCMS05Label, ouvertureCMS05Label;

    private final PlanningCmsService service = new PlanningCmsService();
    private int currentSemaine;
    private int currentAnnee;
    private boolean readonly = false;

    private final String[] CMS_LIST = {"CMS01", "CMS02", "CMS03", "CMS04", "CMS05"};
    private final double OUVERTURE = 22.0;

    /** Appeler depuis MainController pour masquer les boutons d ajout/suppression */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
        // Cacher les boutons Ajouter dans chaque onglet
        hideAddButtons();
    }

    private void hideAddButtons() {
        // Parcourir tous les TabPanes et cacher les boutons "Ajouter ligne"
        if (cmsTabPane == null) return;
        cmsTabPane.getTabs().forEach(tab -> {
            if (tab.getContent() instanceof javafx.scene.layout.VBox vbox) {
                hideButtonsInNode(vbox);
            }
        });
    }

    private void hideButtonsInNode(javafx.scene.Parent parent) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof javafx.scene.control.Button btn) {
                String text = btn.getText();
                if (text != null && (text.contains("Ajouter") || text.contains("+"))) {
                    btn.setVisible(false);
                    btn.setManaged(false);
                }
            } else if (node instanceof javafx.scene.layout.Pane pane) {
                hideButtonsInNode(pane);
            }
        }
    }

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        currentSemaine = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        currentAnnee = today.getYear();

        if (semaineLabel != null)
            semaineLabel.setText("Semaine " + currentSemaine + " — " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (semaineSpinner != null) {
            SpinnerValueFactory.IntegerSpinnerValueFactory svf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, currentSemaine);
            semaineSpinner.setValueFactory(svf);
            semaineSpinner.valueProperty().addListener((obs, old, val) -> {
                currentSemaine = val;
                loadAllTables();
            });
        }

        setupAllTables();
        loadAllTables();
    }

    // ===== SETUP COLONNES =====
    private void setupTable(TableView<PlanningCms> table, String cms) {
        if (table == null) return;
        table.getColumns().clear();
        table.setEditable(false);

        String[] colNames = {"Article","Ordre","StUtil","Ind","Qté","Cad","Nbre h","Jalon.","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Commentaire"};
        String[] propNames = {"article","ordre","stUtil","ind","qte","cad","nbreH","jalonnement","lundi","mardi","mercredi","jeudi","vendredi","samedi","commentaire"};
        int[] widths = {110,95,65,40,65,65,60,80,60,60,75,60,75,65,150};

        for (int i = 0; i < colNames.length; i++) {
            TableColumn<PlanningCms, Object> col = new TableColumn<>(colNames[i]);
            col.setPrefWidth(widths[i]);
            final String prop = propNames[i];
            col.setCellValueFactory(new PropertyValueFactory<>(prop));

            // Formatter doubles
            if (i >= 8 && i <= 13) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(Object v, boolean empty) {
                        super.updateItem(v, empty);
                        if (empty || v == null) { setText(""); setStyle(""); }
                        else {
                            double d = ((Number)v).doubleValue();
                            if (d == 0) { setText(""); setStyle(""); }
                            else {
                                setText(String.format("%.2f", d));
                                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #346771;");
                            }
                        }
                    }
                });
            }
            table.getColumns().add(col);
        }

        // Colonne actions — masquee en mode lecture seule
        if (readonly) return;
        TableColumn<PlanningCms, Void> colAction = new TableColumn<>("Actions");
        colAction.setPrefWidth(85);
        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button btnDel = new Button("🗑");
            { btnDel.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 6; -fx-cursor: hand;");
              btnDel.setOnAction(e -> {
                PlanningCms item = getTableView().getItems().get(getIndex());
                try { service.delete(item.getId()); loadTable(getTableView(), cms); }
                catch (Exception ex) { ex.printStackTrace(); }
              });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : btnDel);
            }
        });
        table.getColumns().add(colAction);
    }

    private void setupAllTables() {
        setupTable(tableCMS01, "CMS01"); setupTable(tableCMS02, "CMS02");
        setupTable(tableCMS03, "CMS03"); setupTable(tableCMS04, "CMS04");
        setupTable(tableCMS05, "CMS05");
    }

    // ===== CHARGEMENT =====
    private void loadTable(TableView<PlanningCms> table, String cms) {
        if (table == null) return;
        List<PlanningCms> data = service.getByCmsAndSemaine(cms, currentSemaine, currentAnnee);
        table.setItems(FXCollections.observableArrayList(data));
        updateStats(cms, data);
    }

    private void loadAllTables() {
        loadTable(tableCMS01, "CMS01"); loadTable(tableCMS02, "CMS02");
        loadTable(tableCMS03, "CMS03"); loadTable(tableCMS04, "CMS04");
        loadTable(tableCMS05, "CMS05");
    }

    private void updateStats(String cms, List<PlanningCms> data) {
        double charge = data.stream().mapToDouble(PlanningCms::getNbreH).sum();
        double delta = OUVERTURE - charge;

        Label chargeL = getChargeLabel(cms);
        Label deltaL  = getDeltaLabel(cms);
        Label ouvrL   = getOuvertureLabel(cms);

        if (chargeL != null) chargeL.setText(String.format("%.1fh", charge));
        if (deltaL  != null) {
            deltaL.setText(String.format("%.1fh", delta));
            deltaL.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (delta >= 0 ? "#46BE62" : "#E53E3E") + ";");
        }
        if (ouvrL != null) ouvrL.setText(String.format("%.0fh", OUVERTURE));
    }

    private Label getChargeLabel(String cms) {
        return switch (cms) {
            case "CMS01" -> chargeCMS01Label; case "CMS02" -> chargeCMS02Label;
            case "CMS03" -> chargeCMS03Label; case "CMS04" -> chargeCMS04Label;
            case "CMS05" -> chargeCMS05Label; default -> null;
        };
    }
    private Label getDeltaLabel(String cms) {
        return switch (cms) {
            case "CMS01" -> deltaCMS01Label; case "CMS02" -> deltaCMS02Label;
            case "CMS03" -> deltaCMS03Label; case "CMS04" -> deltaCMS04Label;
            case "CMS05" -> deltaCMS05Label; default -> null;
        };
    }
    private Label getOuvertureLabel(String cms) {
        return switch (cms) {
            case "CMS01" -> ouvertureCMS01Label; case "CMS02" -> ouvertureCMS02Label;
            case "CMS03" -> ouvertureCMS03Label; case "CMS04" -> ouvertureCMS04Label;
            case "CMS05" -> ouvertureCMS05Label; default -> null;
        };
    }

    // ===== AJOUT LIGNE =====
    @FXML private void handleAddCMS01() { showAddDialog("CMS01"); }
    @FXML private void handleAddCMS02() { showAddDialog("CMS02"); }
    @FXML private void handleAddCMS03() { showAddDialog("CMS03"); }
    @FXML private void handleAddCMS04() { showAddDialog("CMS04"); }
    @FXML private void handleAddCMS05() { showAddDialog("CMS05"); }

    private void showAddDialog(String cms) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Ajouter une ligne — " + cms + " (S" + currentSemaine + ")");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().setPrefWidth(520);

        String fieldStyle = "-fx-padding: 6 10; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-color: #d0d5dd; -fx-border-width: 1; -fx-font-size: 12px; -fx-background-color: #fafbfc;";

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16));

        TextField fArticle = tf("Ex: DT10004M", fieldStyle);
        TextField fOrdre   = tf("Ex: 10170860", fieldStyle);
        TextField fStUtil  = tf("Ex: SER", fieldStyle);
        TextField fInd     = tf("Ex: 09", fieldStyle);
        TextField fQte     = tf("Ex: 1002", fieldStyle);
        TextField fCad     = tf("Ex: 144.0", fieldStyle);
        TextField fNbreH   = tf("Ex: 7.0", fieldStyle);
        ComboBox<String> fJalon = new ComboBox<>();
        fJalon.getItems().addAll("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi");
        fJalon.setStyle(fieldStyle); fJalon.setPrefWidth(200);
        TextField fLundi   = tf("0.0", fieldStyle); TextField fMardi  = tf("0.0", fieldStyle);
        TextField fMerc    = tf("0.0", fieldStyle); TextField fJeudi  = tf("0.0", fieldStyle);
        TextField fVend    = tf("0.0", fieldStyle); TextField fSam    = tf("0.0", fieldStyle);
        TextField fComment = tf("Commentaire...", fieldStyle);

        // Auto-remplir heures selon jalonnement
        fNbreH.textProperty().addListener((obs, old, val) -> autoFillHours(val, fJalon.getValue(), fLundi, fMardi, fMerc, fJeudi, fVend, fSam));
        fJalon.valueProperty().addListener((obs, old, val) -> autoFillHours(fNbreH.getText(), val, fLundi, fMardi, fMerc, fJeudi, fVend, fSam));

        String[] labels = {"Article *","Ordre","St.Util.","Ind","Qté","Cad","Nbre h *","Jalonnement"};
        TextField[] fields = {fArticle, fOrdre, fStUtil, fInd, fQte, fCad, fNbreH};
        for (int i = 0; i < fields.length; i++) {
            g.add(lbl(labels[i]), 0, i); g.add(fields[i], 1, i);
        }
        g.add(lbl("Jalonnement"), 0, 7); g.add(fJalon, 1, 7);
        g.add(lbl("Heures par jour"), 0, 8);
        HBox hdays = new HBox(6);
        hdays.getChildren().addAll(
            dayBox("L", fLundi), dayBox("Ma", fMardi), dayBox("Me", fMerc),
            dayBox("J", fJeudi), dayBox("V", fVend), dayBox("S", fSam));
        g.add(hdays, 1, 8);
        g.add(lbl("Commentaire"), 0, 9); g.add(fComment, 1, 9);

        d.getDialogPane().setContent(g);

        d.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && !fArticle.getText().isBlank()) {
                PlanningCms p = new PlanningCms();
                p.setArticle(fArticle.getText().trim()); p.setOrdre(fOrdre.getText().trim());
                p.setStUtil(fStUtil.getText().trim()); p.setInd(fInd.getText().trim());
                p.setQte(parseDouble(fQte)); p.setCad(parseDouble(fCad)); p.setNbreH(parseDouble(fNbreH));
                p.setJalonnement(fJalon.getValue());
                p.setLundi(parseDouble(fLundi)); p.setMardi(parseDouble(fMardi));
                p.setMercredi(parseDouble(fMerc)); p.setJeudi(parseDouble(fJeudi));
                p.setVendredi(parseDouble(fVend)); p.setSamedi(parseDouble(fSam));
                p.setCommentaire(fComment.getText().trim());
                try {
                    service.create(p, cms, currentSemaine, currentAnnee);
                    loadTable(getTable(cms), cms);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private void autoFillHours(String nbreHStr, String jalon, TextField l, TextField ma, TextField me, TextField j, TextField v, TextField s) {
        if (jalon == null || nbreHStr == null || nbreHStr.isBlank()) return;
        try {
            double h = Double.parseDouble(nbreHStr.replace(",","."));
            l.setText("0.0"); ma.setText("0.0"); me.setText("0.0");
            j.setText("0.0"); v.setText("0.0"); s.setText("0.0");
            switch (jalon) {
                case "Lundi"    -> l.setText(String.valueOf(h));
                case "Mardi"    -> ma.setText(String.valueOf(h));
                case "Mercredi" -> me.setText(String.valueOf(h));
                case "Jeudi"    -> j.setText(String.valueOf(h));
                case "Vendredi" -> v.setText(String.valueOf(h));
                case "Samedi"   -> s.setText(String.valueOf(h));
            }
        } catch (NumberFormatException ignored) {}
    }

    private HBox dayBox(String label, TextField field) {
        field.setPrefWidth(55);
        VBox vb = new VBox(2, new Label(label), field);
        vb.setAlignment(Pos.CENTER);
        HBox hb = new HBox(vb); return hb;
    }
    private TextField tf(String prompt, String style) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setStyle(style); tf.setPrefWidth(200); return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 12px;"); return l;
    }
    private double parseDouble(TextField tf) {
        try { return Double.parseDouble(tf.getText().replace(",",".")); } catch (Exception e) { return 0; }
    }
    private TableView<PlanningCms> getTable(String cms) {
        return switch (cms) {
            case "CMS01" -> tableCMS01; case "CMS02" -> tableCMS02;
            case "CMS03" -> tableCMS03; case "CMS04" -> tableCMS04;
            case "CMS05" -> tableCMS05; default -> null;
        };
    }

    @FXML private void refresh() { loadAllTables(); }
}