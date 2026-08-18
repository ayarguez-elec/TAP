package controller;

import entities.AnomalieStencil;
import entities.Pouchoir;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import security.SessionManager;
import services.AnomalieStencilService;
import services.PouchoirService;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class ProductionDashboardController {

    @FXML private Label lblBannerDate, lblBannerGreeting, lblBannerSubtext;
    @FXML private Label kpiTotal, kpiDispo, kpiSortis, kpiAnomalies;
    @FXML private Label badgeTotal, badgeDispo, badgeSortis, badgeAnomalies;
    @FXML private VBox pouchoirsSortisContainer, recentAnomaliesContainer;
    @FXML private Label calendarMonthLabel, selectedDayLabel;
    @FXML private HBox calendarDayStrip;
    @FXML private VBox planningContainer;

    private final PouchoirService pouchoirService = new PouchoirService();
    private final AnomalieStencilService anomalieService = new AnomalieStencilService();
    private LocalDate selectedDate = LocalDate.now();
    private Map<LocalDate, List<String>> planningEvents = new HashMap<>();
    private static final String PLANNING_FILE = "prod_planning.ser";

    @FXML
    public void initialize() {
        anomalieService.createTable();
        loadPlanningEvents();
        chargerBanniere();
        chargerKpis();
        chargerPochoirsSortis();
        chargerAnomaliesRecentes();
        renderCalendar();
        chargerPlanning();
    }

    private void chargerBanniere() {
        LocalDate today = LocalDate.now();
        String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        dayName = dayName.substring(0,1).toUpperCase() + dayName.substring(1);
        String monthName = today.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        lblBannerDate.setText("📅 " + dayName + " " + today.getDayOfMonth() + " " + monthName + " " + today.getYear());
        String name = SessionManager.getInstance().isAuthenticated()
                ? SessionManager.getInstance().getUtilisateur().getNom() : "Production";
        lblBannerGreeting.setText("Bonjour, " + name + " !");
        lblBannerSubtext.setText("Bienvenue sur l'interface Production — " + dayName);
    }

    private void chargerKpis() {
        try {
            List<Pouchoir> all = pouchoirService.readAll();
            long dispo = all.stream().filter(p -> "disponible".equals(p.getStatut())).count();
            long sortis = all.stream().filter(p -> "sorti".equals(p.getStatut())).count();
            int total = all.size();

            kpiTotal.setText(String.valueOf(total));
            kpiDispo.setText(String.valueOf(dispo));
            kpiSortis.setText(String.valueOf(sortis));
            badgeTotal.setText("Parc : " + total + " unités");
            badgeDispo.setText(total > 0 ? String.format("%.0f%% en stock", (dispo * 100.0 / total)) : "0%");
            badgeSortis.setText(total > 0 ? String.format("%.0f%% en utilisation", (sortis * 100.0 / total)) : "0%");
        } catch (SQLException e) { e.printStackTrace(); }

        List<AnomalieStencil> anomalies = anomalieService.readAll();
        long actives = anomalies.stream().filter(a -> !a.isResolu()).count();
        kpiAnomalies.setText(String.valueOf(actives));
        badgeAnomalies.setText(actives > 0 ? actives + " en cours" : "Aucune");
    }

    private void chargerPochoirsSortis() {
        pouchoirsSortisContainer.getChildren().clear();
        try {
            List<Pouchoir> sortis = pouchoirService.findByStatut("sorti");
            if (sortis.isEmpty()) {
                Label l = new Label("Aucun pochoir sorti");
                l.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 12px;");
                pouchoirsSortisContainer.getChildren().add(l);
                return;
            }
            sortis.stream().limit(6).forEach(p -> {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 0, 5, 0));
                Label dot = new Label("●");
                dot.setStyle("-fx-text-fill: #D9691D; -fx-font-size: 10px;");
                VBox info = new VBox(1);
                Label ref = new Label(p.getRefPouchoir());
                ref.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #21262A;");
                Label prog = new Label(p.getProgramme() != null ? p.getProgramme() : "—");
                prog.setStyle("-fx-font-size: 11px; -fx-text-fill: #6F8D94;");
                info.getChildren().addAll(ref, prog);
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label badge = new Label("Sorti");
                badge.setStyle("-fx-background-color: #D9691D; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                row.getChildren().addAll(dot, info, sp, badge);
                pouchoirsSortisContainer.getChildren().add(row);
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerAnomaliesRecentes() {
        recentAnomaliesContainer.getChildren().clear();
        List<AnomalieStencil> list = anomalieService.readAll();
        if (list.isEmpty()) {
            Label l = new Label("Aucune anomalie enregistrée");
            l.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 12px;");
            recentAnomaliesContainer.getChildren().add(l);
            return;
        }
        list.stream().limit(5).forEach(a -> {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 0, 5, 0));
            String color = "CRITIQUE".equals(a.getDegre()) ? "#E53E3E" : "MAJEUR".equals(a.getDegre()) ? "#D9691D" : "#46BE62";
            Label dot = new Label("●"); dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px;");
            VBox info = new VBox(1);
            Label s = new Label(a.getStencil()); s.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #21262A;");
            Label t = new Label(a.getTypeProbleme() != null ? a.getTypeProbleme() : "—"); t.setStyle("-fx-font-size: 11px; -fx-text-fill: #6F8D94;");
            info.getChildren().addAll(s, t);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label badge = new Label(a.getDegre()); badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
            row.getChildren().addAll(dot, info, sp, badge);
            recentAnomaliesContainer.getChildren().add(row);
        });
    }

    // ===== CALENDRIER =====
    private void renderCalendar() {
        calendarDayStrip.getChildren().clear();
        String monthName = selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        monthName = monthName.substring(0,1).toUpperCase() + monthName.substring(1);
        calendarMonthLabel.setText(monthName);
        String dayName = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE).toUpperCase();
        selectedDayLabel.setText(dayName + ", " + selectedDate.getDayOfMonth() + " " + monthName.toUpperCase());

        LocalDate startOfWeek = selectedDate.with(DayOfWeek.MONDAY);
        String[] daysShort = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            LocalDate d = startOfWeek.plusDays(i);
            VBox dayBox = new VBox(4);
            dayBox.getStyleClass().add("calendar-strip-day");
            Label n = new Label(daysShort[i]); n.getStyleClass().add("calendar-strip-day-name");
            Label num = new Label(String.valueOf(d.getDayOfMonth())); num.getStyleClass().add("calendar-strip-day-num");
            dayBox.getChildren().addAll(n, num);
            dayBox.getStyleClass().add(d.equals(selectedDate) ? "calendar-strip-active" : "calendar-strip-normal");
            final LocalDate fd = d;
            dayBox.setOnMouseClicked(e -> { selectedDate = fd; renderCalendar(); chargerPlanning(); });
            calendarDayStrip.getChildren().add(dayBox);
        }
    }

    private void chargerPlanning() {
        planningContainer.getChildren().clear();
        List<String> events = planningEvents.getOrDefault(selectedDate, new ArrayList<>());
        if (events.isEmpty()) {
            Label l = new Label("Aucune tâche planifiée");
            l.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 10 0;");
            planningContainer.getChildren().add(l);
        } else {
            for (String e : events) {
                HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(4, 0, 4, 0));
                Label dot = new Label("◆"); dot.setStyle("-fx-text-fill: #346771; -fx-font-size: 8px;");
                Label lbl = new Label(e);
            lbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
            lbl.setWrapText(true);
            lbl.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().addAll(dot, lbl);
                planningContainer.getChildren().add(row);
            }
        }
    }

    @FXML
    private void handleAddTask() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouvelle tâche");
        dialog.setHeaderText("Ajouter une tâche pour le " + selectedDate);
        dialog.setContentText("Tâche :");
        dialog.showAndWait().ifPresent(task -> {
            if (!task.isBlank()) {
                planningEvents.computeIfAbsent(selectedDate, k -> new ArrayList<>()).add(task);
                savePlanningEvents();
                chargerPlanning();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadPlanningEvents() {
        java.io.File file = new java.io.File(PLANNING_FILE);
        if (file.exists()) {
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                planningEvents = (Map<LocalDate, List<String>>) ois.readObject();
            } catch (Exception e) { System.err.println("Erreur chargement planning : " + e.getMessage()); }
        }
    }

    private void savePlanningEvents() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(PLANNING_FILE))) {
            oos.writeObject(planningEvents);
        } catch (Exception e) { System.err.println("Erreur sauvegarde planning : " + e.getMessage()); }
    }
}