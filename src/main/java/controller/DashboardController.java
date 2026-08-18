package controller;

import entities.Client;
import entities.Pouchoir;
import entities.Produit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import services.ClientService;
import services.PouchoirService;
import services.ProduitService;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    // ===== KPI Labels =====
    @FXML private Label totalPouchoirsLabel;
    @FXML private Label disponiblesLabel;
    @FXML private Label sortisLabel;
    @FXML private Label clientsLabel;

    // ===== Dashboard Banner =====
    @FXML private Label lblBannerDate;
    @FXML private Label lblBannerGreeting;
    @FXML private Label lblBannerSubtext;

    // ===== KPI Badges =====
    @FXML private Label kpiTotalBadge;
    @FXML private Label kpiDispoBadge;
    @FXML private Label kpiSortiBadge;
    @FXML private Label kpiBadgeClients;

    // ===== BarChart Racle =====
    @FXML private BarChart<String, Number> racleBarChart;
    @FXML private Label racle300Label;
    @FXML private Label racle350Label;
    @FXML private Label racle400Label;
    @FXML private Label racle460Label;

    // ===== Top Programmes & Clients =====
    @FXML private VBox topProgrammesContainer;
    @FXML private VBox topClientsContainer;

    // ===== CALENDRIER, PLANNING & ALERTES (MOCKUP STYLE) =====
    @FXML private Label calendarMonthLabel;
    @FXML private HBox calendarDayStrip;
    @FXML private Label selectedDayLabel;
    @FXML private VBox planningContainer;
    @FXML private VBox dashboardAlertsContainer;

    private LocalDate today;
    private LocalDate selectedDate;

    // Services
    private final PouchoirService pouchoirService = new PouchoirService();
    private final ProduitService produitService = new ProduitService();
    private final ClientService clientService = new ClientService();

    @FXML
    public void initialize() {
        today = LocalDate.now();
        selectedDate = today;

        chargerStatistiques();
        chargerRacleBarChart();
        chargerTopProgrammes();
        chargerTopClients();
        chargerBanniere();

        // Charger Calendrier, Planning et Alertes au style Mockup
        renderWeeklyCalendar();
        chargerPlanning();
        chargerAlertes();
    }

    private void chargerBanniere() {
        LocalDate today = LocalDate.now();
        String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
        
        String monthName = today.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        
        lblBannerDate.setText("📅 " + dayName + " " + today.getDayOfMonth() + " " + monthName + " " + today.getYear());

        // Message de salutation
        entities.Utilisateur user = security.SessionManager.getInstance().getUtilisateur();
        String name = user != null ? user.getNom() : "Utilisateur";
        lblBannerGreeting.setText("Bonjour, " + name + " !");

        // Message du jour de la semaine
        lblBannerSubtext.setText("Passez une excellente journée de " + dayName.toLowerCase() + " !");
    }

    // =========================================================
    // CALENDRIER HEBDOMADAIRE (Bandeau horizontal)
    // =========================================================
    private void renderWeeklyCalendar() {
        calendarDayStrip.getChildren().clear();

        // En-tête : Mois actuel
        String monthName = selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        calendarMonthLabel.setText(monthName);

        // Label du jour sélectionné sous la bande, format "LUNDI, 13"
        String dayName = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE).toUpperCase();
        selectedDayLabel.setText(dayName + ", " + selectedDate.getDayOfMonth() + " " + monthName.toUpperCase());

        // Trouver le lundi de la semaine courante
        LocalDate startOfWeek = selectedDate.with(DayOfWeek.MONDAY);

        // Remplir les 7 jours de la semaine (Lundi à Dimanche)
        String[] daysShort = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            LocalDate dateOfCell = startOfWeek.plusDays(i);
            
            VBox dayBox = new VBox(4);
            dayBox.getStyleClass().add("calendar-strip-day");
            
            Label lblDayName = new Label(daysShort[i]);
            lblDayName.getStyleClass().add("calendar-strip-day-name");
            
            Label lblDayNum = new Label(String.valueOf(dateOfCell.getDayOfMonth()));
            lblDayNum.getStyleClass().add("calendar-strip-day-num");
            
            dayBox.getChildren().addAll(lblDayName, lblDayNum);

            // Mettre en surbrillance si c'est le jour sélectionné
            if (dateOfCell.equals(selectedDate)) {
                dayBox.getStyleClass().add("calendar-strip-active");
            } else {
                dayBox.getStyleClass().add("calendar-strip-normal");
            }

            // Événement clic pour changer de jour
            dayBox.setOnMouseClicked(event -> {
                selectedDate = dateOfCell;
                renderWeeklyCalendar();
                chargerPlanning();
            });

            calendarDayStrip.getChildren().add(dayBox);
        }
    }

    // =========================================================
    // PLANNING & TÂCHES (Style Mockup avec lignes pointillées & puces)
    // =========================================================
    public static class PlanningEvent implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String hour;
        private String task;
        private String color;

        public PlanningEvent(String hour, String task, String color) {
            this.hour = hour;
            this.task = task;
            this.color = color;
        }

        public String getHour() { return hour; }
        public String getTask() { return task; }
        public String getColor() { return color; }
    }

    private Map<LocalDate, List<PlanningEvent>> planningEvents = new java.util.HashMap<>();
    private static final String PLANNING_FILE = "planning_events.ser";

    @SuppressWarnings("unchecked")
    private void loadPlanningEvents() {
        java.io.File file = new java.io.File(PLANNING_FILE);
        if (file.exists()) {
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                planningEvents = (Map<LocalDate, List<PlanningEvent>>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Erreur chargement planning : " + e.getMessage());
            }
        }
    }

    private void savePlanningEvents() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(PLANNING_FILE))) {
            oos.writeObject(planningEvents);
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde planning : " + e.getMessage());
        }
    }

    private void chargerPlanning() {
        planningContainer.getChildren().clear();
        
        if (planningEvents.isEmpty()) {
            loadPlanningEvents();
        }

        // Si aucun planning pour la date sélectionnée, initialiser avec des tâches par défaut
        if (!planningEvents.containsKey(selectedDate)) {
            List<PlanningEvent> defaultEvents = new java.util.ArrayList<>();
            defaultEvents.add(new PlanningEvent("09:00", "Contrôle tension requis (SMT Ligne 1)", "#FF8B3D"));
            defaultEvents.add(new PlanningEvent("11:30", "Lavage programmé Pochoir #P-230", "#5D5FEF"));
            defaultEvents.add(new PlanningEvent("14:00", "Mise en rack Pochoirs Client Lacroix", "#46BE62"));
            planningEvents.put(selectedDate, defaultEvents);
            savePlanningEvents();
        }

        List<PlanningEvent> events = planningEvents.get(selectedDate);
        if (events == null || events.isEmpty()) {
            Label noTask = new Label("Aucune tâche de planifiée.");
            noTask.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 10 0;");
            planningContainer.getChildren().add(noTask);
            return;
        }

        for (PlanningEvent ev : events) {
            HBox row = new HBox(6);
            row.getStyleClass().add("planning-item-row");
            row.setAlignment(Pos.CENTER_LEFT);

            // Heure
            Label lblTime = new Label(ev.getHour());
            lblTime.getStyleClass().add("planning-time-label");

            // Puce
            Circle bullet = new Circle(4);
            bullet.setStyle("-fx-fill: " + ev.getColor() + ";");

            // Tâche
            Label lblTask = new Label(ev.getTask());
            lblTask.getStyleClass().add("planning-task-label");
            lblTask.setWrapText(true);
            lblTask.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblTask, Priority.ALWAYS);

            row.getChildren().addAll(lblTime, bullet, lblTask);
            planningContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleAddTask() {
        // Dialogue de création de tâche personnalisé et moderne
        javafx.scene.control.Dialog<PlanningEvent> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Ajouter une tâche");
        dialog.setHeaderText("Planifier une nouvelle tâche pour le " + selectedDate.getDayOfMonth() + " " + selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE));

        ButtonType btnValider = new ButtonType("Ajouter", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfTime = new TextField("08:00");
        tfTime.setPromptText("HH:MM");
        TextField tfDesc = new TextField();
        tfDesc.setPromptText("Description de la tâche");

        javafx.scene.control.ComboBox<String> cbColor = new javafx.scene.control.ComboBox<>();
        cbColor.getItems().addAll("Orange (Tension)", "Bleu (Lavage)", "Vert (Stockage)", "Violet (Audit)");
        cbColor.setValue("Bleu (Lavage)");

        grid.add(new Label("Heure :"), 0, 0);
        grid.add(tfTime, 1, 0);
        grid.add(new Label("Tâche :"), 0, 1);
        grid.add(tfDesc, 1, 1);
        grid.add(new Label("Catégorie :"), 0, 2);
        grid.add(cbColor, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                String colorHex = "#5D5FEF";
                String val = cbColor.getValue();
                if (val.startsWith("Orange")) colorHex = "#FF8B3D";
                else if (val.startsWith("Vert")) colorHex = "#46BE62";
                else if (val.startsWith("Violet")) colorHex = "#8E44AD";

                return new PlanningEvent(tfTime.getText().trim(), tfDesc.getText().trim(), colorHex);
            }
            return null;
        });

        java.util.Optional<PlanningEvent> result = dialog.showAndWait();
        result.ifPresent(ev -> {
            if (!ev.getTask().isEmpty()) {
                planningEvents.computeIfAbsent(selectedDate, k -> new java.util.ArrayList<>()).add(ev);
                savePlanningEvents();
                chargerPlanning();
            }
        });
    }

    // =========================================================
    // ALERTES CRITIQUES (Style moderne avec bande rouge)
    // =========================================================
    private void chargerAlertes() {
        dashboardAlertsContainer.getChildren().clear();

        try {
            List<Pouchoir> pouchoirs = pouchoirService.readAll();
            
            // Détecter les pochoirs actuellement sortis (Ligne active)
            List<Pouchoir> sortis = pouchoirs.stream()
                    .filter(p -> "sorti".equalsIgnoreCase(p.getStatut()))
                    .limit(2)
                    .collect(Collectors.toList());

            if (sortis.isEmpty()) {
                HBox info = new HBox(8);
                info.setStyle("-fx-padding: 8 10; -fx-background-color: #F8FAFC; -fx-background-radius: 8;");
                info.getChildren().add(new Label("✅ Aucun pochoir en alerte critique."));
                dashboardAlertsContainer.getChildren().add(info);
                return;
            }

            for (Pouchoir p : sortis) {
                HBox alertBox = new HBox(12);
                alertBox.getStyleClass().add("alert-card-modern");
                alertBox.setAlignment(Pos.CENTER_LEFT);

                // Icône d'alerte rouge vectorielle moderne (pas d'émoji qui se transforme en carré)
                javafx.scene.shape.SVGPath warningIcon = new javafx.scene.shape.SVGPath();
                warningIcon.setContent("M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4m0 4h.01");
                warningIcon.getStyleClass().add("alert-header-svg");

                VBox details = new VBox(2);
                Label title = new Label("Pochoir resté hors de l'armoire");
                title.getStyleClass().add("alert-card-modern-title");
                
                Label desc = new Label("Réf : " + p.getRefPouchoir() + " | Prog : " + p.getProgramme());
                desc.getStyleClass().add("alert-card-modern-desc");

                details.getChildren().addAll(title, desc);
                alertBox.getChildren().addAll(warningIcon, details);

                dashboardAlertsContainer.getChildren().add(alertBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // STATISTIQUES GLOBALES
    // =========================================================
    private void chargerStatistiques() {
        try {
            List<Pouchoir> pouchoirs = pouchoirService.readAll();
            int total = pouchoirs.size();
            totalPouchoirsLabel.setText(String.valueOf(total));
            if (kpiTotalBadge != null) kpiTotalBadge.setText("Parc : " + total + " unités");

            long disponibles = pouchoirs.stream()
                    .filter(p -> "disponible".equalsIgnoreCase(p.getStatut()))
                    .count();
            disponiblesLabel.setText(String.valueOf(disponibles));

            long sortis = pouchoirs.stream()
                    .filter(p -> "sorti".equalsIgnoreCase(p.getStatut()))
                    .count();
            sortisLabel.setText(String.valueOf(sortis));

            if (total > 0) {
                int pctDispo = (int) Math.round((disponibles * 100.0) / total);
                int pctSorti = (int) Math.round((sortis * 100.0) / total);
                if (kpiDispoBadge != null) kpiDispoBadge.setText(pctDispo + "% en stock");
                if (kpiSortiBadge != null) kpiSortiBadge.setText(pctSorti + "% en production");
            }

            List<Client> clients = clientService.readAll();
            clientsLabel.setText(String.valueOf(clients.size()));

            List<Produit> produits = produitService.readAll();
            if (kpiBadgeClients != null) kpiBadgeClients.setText(produits.size() + " produits référencés");

        } catch (SQLException e) {
            e.printStackTrace();
            totalPouchoirsLabel.setText("0");
            disponiblesLabel.setText("0");
            sortisLabel.setText("0");
            clientsLabel.setText("0");
        }
    }

    // =========================================================
    // BAR CHART — Répartition par taille de racle
    // =========================================================
    private void chargerRacleBarChart() {
        if (racleBarChart == null) return;
        racleBarChart.getData().clear();

        try {
            List<Pouchoir> pouchoirs = pouchoirService.readAll();
            Map<String, Long> racleCounts = pouchoirs.stream()
                    .filter(p -> p.getRacle() != null && !p.getRacle().isEmpty())
                    .collect(Collectors.groupingBy(Pouchoir::getRacle, Collectors.counting()));

            String[] sizes = {"300", "350", "400", "460"};
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Pochoirs");

            for (String size : sizes) {
                long count = racleCounts.getOrDefault(size, 0L);
                XYChart.Data<String, Number> data = new XYChart.Data<>(size + " mm", count);
                series.getData().add(data);
            }

            racleBarChart.getData().add(series);

            // Style bars after scene is rendered
            Platform.runLater(this::styleBarChartBars);
            racleBarChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(this::styleBarChartBars);
                }
            });

            long c300 = racleCounts.getOrDefault("300", 0L);
            long c350 = racleCounts.getOrDefault("350", 0L);
            long c400 = racleCounts.getOrDefault("400", 0L);
            long c460 = racleCounts.getOrDefault("460", 0L);

            if (racle300Label != null) racle300Label.setText(String.valueOf(c300));
            if (racle350Label != null) racle350Label.setText(String.valueOf(c350));
            if (racle400Label != null) racle400Label.setText(String.valueOf(c400));
            if (racle460Label != null) racle460Label.setText(String.valueOf(c460));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void styleBarChartBars() {
        // Vivid, distinct colors per bar
        String[] barColors = {
                "#3DD9D9",  // Cyan teal - 300mm
                "#7C6FEF",  // Indigo purple - 350mm
                "#F97316",  // Vibrant orange - 400mm
                "#22C55E"   // Bright green - 460mm
        };
        if (racleBarChart.getData().isEmpty()) return;
        XYChart.Series<String, Number> s = racleBarChart.getData().get(0);
        for (int i = 0; i < s.getData().size(); i++) {
            XYChart.Data<String, Number> d = s.getData().get(i);
            if (d.getNode() != null) {
                String color = barColors[Math.min(i, barColors.length - 1)];
                d.getNode().setStyle(
                    "-fx-bar-fill: " + color + ";" +
                    "-fx-background-radius: 6 6 0 0;"
                );
            }
        }
    }

    // =========================================================
    // TOP PROGRAMMES & CLIENTS
    // =========================================================
    private void chargerTopProgrammes() {
        if (topProgrammesContainer == null) return;
        topProgrammesContainer.getChildren().clear();

        try {
            List<Pouchoir> pouchoirs = pouchoirService.readAll();
            Map<String, Long> counts = pouchoirs.stream()
                    .filter(p -> p.getProgramme() != null && !p.getProgramme().isEmpty())
                    .collect(Collectors.groupingBy(Pouchoir::getProgramme, Collectors.counting()));

            if (counts.isEmpty()) {
                Label empty = new Label("Aucun programme trouvé");
                empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-style: italic;");
                topProgrammesContainer.getChildren().add(empty);
                return;
            }

            long maxCount = counts.values().stream().max(Long::compareTo).orElse(1L);
            String[] barColors = {"#346771", "#46BE62", "#D9691D", "#8E44AD", "#346771", "#46BE62"};

            int idx = 0;
            List<Map.Entry<String, Long>> sorted = counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(6)
                    .collect(Collectors.toList());

            for (Map.Entry<String, Long> entry : sorted) {
                double pct = (entry.getValue() * 100.0) / maxCount;
                VBox row = new VBox(4);

                HBox header = new HBox();
                header.setAlignment(Pos.CENTER_LEFT);
                Label nameLabel = new Label(entry.getKey());
                nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
                nameLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                Label pctLabel = new Label(entry.getValue() + " u");
                pctLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #21262A;");
                header.getChildren().addAll(nameLabel, pctLabel);

                StackPane barBg = new StackPane();
                barBg.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 4; -fx-pref-height: 5;");
                barBg.setMaxWidth(Double.MAX_VALUE);

                HBox barFillWrapper = new HBox();
                Region barFill = new Region();
                barFill.setStyle("-fx-background-color: " + barColors[idx] + "; -fx-background-radius: 4; -fx-pref-height: 5;");
                barFill.setPrefWidth(pct);
                HBox.setHgrow(barFill, Priority.NEVER);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                barFillWrapper.getChildren().addAll(barFill, spacer);

                final double finalPct = pct;
                barBg.widthProperty().addListener((obs, ov, nv) -> 
                    barFill.setPrefWidth(nv.doubleValue() * finalPct / 100.0)
                );

                barBg.getChildren().add(barFillWrapper);
                StackPane.setAlignment(barFillWrapper, Pos.CENTER_LEFT);

                row.getChildren().addAll(header, barBg);
                topProgrammesContainer.getChildren().add(row);

                idx = (idx + 1) % barColors.length;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerTopClients() {
        if (topClientsContainer == null) return;
        topClientsContainer.getChildren().clear();

        try {
            List<Client> clients = clientService.readAll();
            List<Produit> produits = produitService.readAll();

            if (clients.isEmpty()) {
                Label empty = new Label("Aucun client trouvé");
                empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-style: italic;");
                topClientsContainer.getChildren().add(empty);
                return;
            }

            Map<Integer, Long> produitsParClient = produits.stream()
                    .collect(Collectors.groupingBy(Produit::getClientId, Collectors.counting()));

            long maxCount = produitsParClient.values().stream().max(Long::compareTo).orElse(1L);

            String[] barColors = {"#346771", "#46BE62", "#D9691D", "#8E44AD", "#2980B9", "#E67E22"};
            String[] bgColors  = {"#E0F4F6", "#E8F5E9", "#FBE9E7", "#F3E5F5", "#E3F2FD", "#FFF3E0"};

            int idx = 0;
            List<Client> sorted = clients.stream()
                    .sorted((a, b) -> Long.compare(
                            produitsParClient.getOrDefault(b.getId(), 0L),
                            produitsParClient.getOrDefault(a.getId(), 0L)))
                    .limit(6)
                    .collect(Collectors.toList());

            for (Client client : sorted) {
                long count = produitsParClient.getOrDefault(client.getId(), 0L);
                double pct = maxCount > 0 ? (count * 100.0) / maxCount : 0;

                String barColor = barColors[idx % barColors.length];
                String bgColor  = bgColors[idx % bgColors.length];

                VBox row = new VBox(5);
                HBox header = new HBox(8);
                header.setAlignment(Pos.CENTER_LEFT);

                Label rankLabel = new Label(String.valueOf(idx + 1));
                rankLabel.setStyle(
                        "-fx-background-color: " + barColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-min-width: 20; -fx-min-height: 20;" +
                        "-fx-background-radius: 10; -fx-alignment: center;");

                Label nameLabel = new Label(client.getNom());
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
                nameLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                Label countBadge = new Label(count + " produit" + (count > 1 ? "s" : ""));
                countBadge.setStyle(
                        "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + barColor + ";" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-padding: 2 8; -fx-background-radius: 10;");

                header.getChildren().addAll(rankLabel, nameLabel, countBadge);

                StackPane barBg = new StackPane();
                barBg.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 4; -fx-pref-height: 6;");
                barBg.setMaxWidth(Double.MAX_VALUE);

                HBox barFillWrapper = new HBox();
                Region barFill = new Region();
                barFill.setStyle("-fx-background-color: " + barColor + "; -fx-background-radius: 4; -fx-pref-height: 6;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                barFillWrapper.getChildren().addAll(barFill, spacer);

                final double finalPct = pct;
                barBg.widthProperty().addListener((obs, ov, nv) ->
                        barFill.setPrefWidth(nv.doubleValue() * finalPct / 100.0));

                barBg.getChildren().add(barFillWrapper);
                StackPane.setAlignment(barFillWrapper, Pos.CENTER_LEFT);

                row.getChildren().addAll(header, barBg);
                topClientsContainer.getChildren().add(row);

                idx++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // NAVIGATION
    // =========================================================
    @FXML
    private void openSuiviArmoire() { changerVue("/suivi-pouchoir-view.fxml"); }

    @FXML
    private void openCodificationView() { changerVue("/codification-view.fxml"); }

    private void changerVue(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            javafx.scene.Node node = totalPouchoirsLabel;
            javafx.scene.Node parent = node.getParent();
            while (parent != null && !(parent instanceof StackPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                StackPane contentArea = (StackPane) parent;
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}