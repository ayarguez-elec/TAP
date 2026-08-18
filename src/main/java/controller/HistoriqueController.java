package controller;

import entities.AuditLog;
import entities.HistoriquePochoir;
import entities.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import security.SessionManager;
import services.AuditService;
import services.HistoriquePochoirService;
import services.UtilisateurService;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HistoriqueController implements Initializable {
    
    // Header
    @FXML private Label userLabel;
    
    // Historique Pochoir
    @FXML private TextField pouchoirFilterField;
    @FXML private ComboBox<String> actionFilterCombo;
    @FXML private TableView<HistoriquePochoir> historiquePochoirTable;
    @FXML private TableColumn<HistoriquePochoir, String> colPouchoirRef;
    @FXML private TableColumn<HistoriquePochoir, String> colAction;
    @FXML private TableColumn<HistoriquePochoir, String> colDateSortie;
    @FXML private TableColumn<HistoriquePochoir, String> colDateRetour;
    @FXML private TableColumn<HistoriquePochoir, String> colOperateur;
    @FXML private TableColumn<HistoriquePochoir, String> colLocalisation;
    @FXML private TableColumn<HistoriquePochoir, String> colRaison;
    @FXML private TableColumn<HistoriquePochoir, String> colEtatRetour;
    @FXML private TextArea remarquesArea;
    @FXML private Label pouchoirCountLabel;
    
    // Audit Log
    @FXML private ComboBox<String> utilisateurFilterCombo;
    @FXML private ComboBox<String> actionAuditFilterCombo;
    @FXML private TextField tableFilterField;
    @FXML private TableView<AuditLog> auditLogTable;
    @FXML private TableColumn<AuditLog, String> colAuditDate;
    @FXML private TableColumn<AuditLog, String> colAuditUtilisateur;
    @FXML private TableColumn<AuditLog, String> colAuditAction;
    @FXML private TableColumn<AuditLog, String> colAuditTable;
    @FXML private TableColumn<AuditLog, String> colAuditRecordId;
    @FXML private TableColumn<AuditLog, String> colAuditDetails;
    @FXML private Label auditCountLabel;
    
    private HistoriquePochoirService historiquePochoirService;
    private AuditService auditService;
    private UtilisateurService utilisateurService;
    
    private ObservableList<HistoriquePochoir> allHistoriquePochoir;
    private ObservableList<AuditLog> allAuditLogs;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les services
        historiquePochoirService = new HistoriquePochoirService();
        auditService = new AuditService();
        utilisateurService = new UtilisateurService();
        
        // Afficher l'utilisateur connecté
        updateUserLabel();
        
        // Configurer les tables
        setupHistoriquePochoirTable();
        setupAuditLogTable();
        
        // Configurer les filtres
        setupFilters();
        
        // Charger les données
        loadData();
    }
    
    private void updateUserLabel() {
        if (SessionManager.getInstance().isAuthenticated()) {
            Utilisateur user = SessionManager.getInstance().getUtilisateur();
            userLabel.setText(user.getNom() + " (" + user.getRole() + ")");
        } else {
            userLabel.setText("Aucun utilisateur connecté");
        }
    }
    
    private void setupHistoriquePochoirTable() {
        colPouchoirRef.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPouchoirRef()));
        
        colAction.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAction().toString()));
        
        colDateSortie.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateSortie() != null ? 
                dateFormat.format(data.getValue().getDateSortie()) : ""));
        
        colDateRetour.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateRetour() != null ? 
                dateFormat.format(data.getValue().getDateRetour()) : "-"));
        
        colOperateur.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getOperateurNom()));
        
        colLocalisation.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getLocalisation() != null ? 
                data.getValue().getLocalisation() : "-"));
        
        colRaison.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getRaison() != null ? 
                data.getValue().getRaison() : "-"));
        
        colEtatRetour.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEtatRetour() != null ? 
                data.getValue().getEtatRetour() : "-"));
        
        // Listener pour afficher les remarques
        historiquePochoirTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    remarquesArea.setText(newSelection.getRemarques() != null ? 
                        newSelection.getRemarques() : "Aucune remarque");
                } else {
                    remarquesArea.clear();
                }
            });
    }
    
    private void setupAuditLogTable() {
        colAuditDate.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateAction() != null ? 
                dateFormat.format(data.getValue().getDateAction()) : ""));
        
        colAuditUtilisateur.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUtilisateurNom()));
        
        colAuditAction.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAction()));
        
        colAuditTable.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTableAffectee() != null ? 
                data.getValue().getTableAffectee() : "-"));
        
        colAuditRecordId.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEnregistrementId() != null ? 
                data.getValue().getEnregistrementId() : "-"));
        
        colAuditDetails.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDetails() != null ? 
                data.getValue().getDetails() : ""));
    }
    
    private void setupFilters() {
        // Action filter pour pochoirs
        actionFilterCombo.setItems(FXCollections.observableArrayList("Tous", "SORTIE", "RETOUR"));
        actionFilterCombo.setValue("Tous");
        
        // Actions filter pour audit
        actionAuditFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "CREATE", "UPDATE", "DELETE", "READ", "LOGIN", "ACCESS_DENIED"));
        actionAuditFilterCombo.setValue("Tous");
    }
    
    private void loadData() {
        // Charger historique pochoirs
        List<HistoriquePochoir> historiques = historiquePochoirService.getAllHistorique();
        allHistoriquePochoir = FXCollections.observableArrayList(historiques);
        historiquePochoirTable.setItems(allHistoriquePochoir);
        pouchoirCountLabel.setText(allHistoriquePochoir.size() + " enregistrements");
        
        // Charger audit logs
        List<AuditLog> audits = auditService.getAllAudits();
        allAuditLogs = FXCollections.observableArrayList(audits);
        auditLogTable.setItems(allAuditLogs);
        auditCountLabel.setText(allAuditLogs.size() + " enregistrements");
        
        // Charger la liste des utilisateurs pour le filtre
        List<Utilisateur> users = utilisateurService.readAll();
        ObservableList<String> userNames = FXCollections.observableArrayList("Tous");
        userNames.addAll(users.stream().map(Utilisateur::getNom).collect(Collectors.toList()));
        utilisateurFilterCombo.setItems(userNames);
        utilisateurFilterCombo.setValue("Tous");
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
        updateUserLabel();
    }
    
    @FXML
    private void handleFilterPochoir() {
        String refFilter = pouchoirFilterField.getText().toLowerCase();
        String actionFilter = actionFilterCombo.getValue();
        
        ObservableList<HistoriquePochoir> filtered = allHistoriquePochoir.filtered(h -> {
            boolean matchRef = refFilter.isEmpty() || 
                h.getPouchoirRef().toLowerCase().contains(refFilter);
            boolean matchAction = actionFilter.equals("Tous") || 
                h.getAction().toString().equals(actionFilter);
            return matchRef && matchAction;
        });
        
        historiquePochoirTable.setItems(filtered);
        pouchoirCountLabel.setText(filtered.size() + " enregistrements");
    }
    
    @FXML
    private void handleResetFilterPochoir() {
        pouchoirFilterField.clear();
        actionFilterCombo.setValue("Tous");
        historiquePochoirTable.setItems(allHistoriquePochoir);
        pouchoirCountLabel.setText(allHistoriquePochoir.size() + " enregistrements");
    }
    
    @FXML
    private void handleFilterAudit() {
        String userFilter = utilisateurFilterCombo.getValue();
        String actionFilter = actionAuditFilterCombo.getValue();
        String tableFilter = tableFilterField.getText().toLowerCase();
        
        ObservableList<AuditLog> filtered = allAuditLogs.filtered(a -> {
            boolean matchUser = userFilter.equals("Tous") || 
                a.getUtilisateurNom().equals(userFilter);
            boolean matchAction = actionFilter.equals("Tous") || 
                a.getAction().equals(actionFilter);
            boolean matchTable = tableFilter.isEmpty() || 
                (a.getTableAffectee() != null && 
                 a.getTableAffectee().toLowerCase().contains(tableFilter));
            return matchUser && matchAction && matchTable;
        });
        
        auditLogTable.setItems(filtered);
        auditCountLabel.setText(filtered.size() + " enregistrements");
    }
    
    @FXML
    private void handleResetFilterAudit() {
        utilisateurFilterCombo.setValue("Tous");
        actionAuditFilterCombo.setValue("Tous");
        tableFilterField.clear();
        auditLogTable.setItems(allAuditLogs);
        auditCountLabel.setText(allAuditLogs.size() + " enregistrements");
    }
    
    @FXML
    private void handleExport() {
        // TODO: Implémenter l'export CSV
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setContentText("Fonctionnalité d'export à implémenter");
        alert.showAndWait();
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) userLabel.getScene().getWindow();
        stage.close();
    }
}
