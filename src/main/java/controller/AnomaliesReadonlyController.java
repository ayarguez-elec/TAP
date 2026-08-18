package controller;

import entities.AnomalieStencil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.AnomalieStencilService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AnomaliesReadonlyController {

    @FXML private TableView<AnomalieStencil> anomaliesTable;
    @FXML private TableColumn<AnomalieStencil, String> colStencil, colLigne, colType, colDegre, colRemarque, colStatut, colDate;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterDegre, filterStatut;
    @FXML private Label kpiCritiques, kpiMajeurs, kpiResolus;

    private final AnomalieStencilService service = new AnomalieStencilService();
    private ObservableList<AnomalieStencil> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        service.createTable();
        setupTable();
        filterDegre.getItems().addAll("Tous", "CRITIQUE", "MAJEUR", "MINEUR");
        filterDegre.setValue("Tous");
        filterStatut.getItems().addAll("Tous", "Actives", "Resolues");
        filterStatut.setValue("Tous");
        searchField.textProperty().addListener((obs, o, v) -> applyFilters());
        filterDegre.valueProperty().addListener((obs, o, v) -> applyFilters());
        filterStatut.valueProperty().addListener((obs, o, v) -> applyFilters());
        loadData();
    }

    private void setupTable() {
        colStencil.setCellValueFactory(new PropertyValueFactory<>("stencil"));
        colLigne.setCellValueFactory(new PropertyValueFactory<>("ligne"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeProbleme"));
        colRemarque.setCellValueFactory(new PropertyValueFactory<>("remarque"));

        // Degre avec couleur
        colDegre.setCellValueFactory(new PropertyValueFactory<>("degre"));
        colDegre.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(""); setStyle(""); return; }
                String color = switch (v) {
                    case "CRITIQUE" -> "#E53E3E";
                    case "MAJEUR"   -> "#D9691D";
                    default         -> "#46BE62";
                };
                setText(v);
                setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color +
                         "; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-background-radius: 8;");
            }
        });

        // Statut resolu
        colStatut.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isResolu() ? "Resolu" : "Actif"));
        colStatut.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(""); setStyle(""); return; }
                boolean resolu = "Resolu".equals(v);
                setText(v);
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " +
                        (resolu ? "#46BE62" : "#E53E3E") + ";");
            }
        });

        // Date
        colDate.setCellValueFactory(cd -> {
            if (cd.getValue().getDateDetection() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(
                cd.getValue().getDateDetection().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });

        anomaliesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadData() {
        allData.setAll(service.readAll());
        applyFilters();
        updateKpis();
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase();
        String degre = filterDegre.getValue();
        String statut = filterStatut.getValue();

        List<AnomalieStencil> filtered = allData.stream()
                .filter(a -> keyword.isBlank()
                        || a.getStencil().toLowerCase().contains(keyword)
                        || (a.getTypeProbleme() != null && a.getTypeProbleme().toLowerCase().contains(keyword))
                        || (a.getLigne() != null && a.getLigne().toLowerCase().contains(keyword)))
                .filter(a -> "Tous".equals(degre) || degre.equals(a.getDegre()))
                .filter(a -> {
                    if ("Actives".equals(statut)) return !a.isResolu();
                    if ("Resolues".equals(statut)) return a.isResolu();
                    return true;
                })
                .collect(Collectors.toList());

        anomaliesTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void updateKpis() {
        long critiques = allData.stream().filter(a -> "CRITIQUE".equals(a.getDegre()) && !a.isResolu()).count();
        long majeurs   = allData.stream().filter(a -> "MAJEUR".equals(a.getDegre()) && !a.isResolu()).count();
        long resolus   = allData.stream().filter(AnomalieStencil::isResolu).count();
        if (kpiCritiques != null) kpiCritiques.setText(String.valueOf(critiques));
        if (kpiMajeurs   != null) kpiMajeurs.setText(String.valueOf(majeurs));
        if (kpiResolus   != null) kpiResolus.setText(String.valueOf(resolus));
    }

    @FXML
    private void refresh() { loadData(); }
}