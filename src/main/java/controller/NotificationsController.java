package controller;

import entities.Pouchoir;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import services.PouchoirService;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class NotificationsController {

    @FXML
    private VBox alertesContainer;
    @FXML
    private Label totalAlertes;
    @FXML
    private Label nonLues;
    @FXML
    private Label lues;

    private List<Notification> notifications = new ArrayList<>();
    private static final String NOTIF_FILE = "notifications.ser";

    @FXML
    public void initialize() {
        chargerNotifications();
        afficherNotifications();
        updateStats();
    }

    private void chargerNotifications() {
        List<Notification> savedNotifications = chargerNotificationsSauvegardees();
        List<Notification> newNotifications = genererNotificationsReelles();
        notifications = fusionnerNotifications(savedNotifications, newNotifications);
        sauvegarderNotifications(notifications);
    }

    // ✅ Générer des notifications réelles ciblées (alertes critiques et pochoirs sortis)
    private List<Notification> genererNotificationsReelles() {
        List<Notification> newNotifs = new ArrayList<>();
        try {
            PouchoirService service = new PouchoirService();
            List<Pouchoir> pouchoirs = service.readAll();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime today1 = now.minusMinutes(5);
            LocalDateTime today2 = now.minusHours(1);
            LocalDateTime yesterday = now.minusDays(1).minusHours(2);

            int count = 0;
            for (Pouchoir p : pouchoirs) {
                if ("sorti".equalsIgnoreCase(p.getStatut())) {
                    newNotifs.add(new Notification(
                            "🔴",
                            "Pochoir " + p.getRefPouchoir() + " est actuellement sorti (en production)",
                            today1.minusMinutes(count * 3),
                            false
                    ));
                    count++;
                } else if (p.getStockActuel() <= 0 && p.getQuantiteTotale() > 0) {
                    newNotifs.add(new Notification(
                            "🟠",
                            "Stock épuisé pour le pochoir " + p.getRefPouchoir() + " (0 cartes)",
                            today2.minusMinutes(count * 5),
                            false
                    ));
                    count++;
                }
            }

            if (newNotifs.isEmpty()) {
                newNotifs.add(new Notification(
                        "✅",
                        "Tous les pochoirs sont disponibles et en stock",
                        yesterday,
                        true
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newNotifs;
    }

    @SuppressWarnings("unchecked")
    private List<Notification> chargerNotificationsSauvegardees() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NOTIF_FILE))) {
            return (List<Notification>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void sauvegarderNotifications(List<Notification> notifications) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOTIF_FILE))) {
            oos.writeObject(notifications);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Notification> fusionnerNotifications(List<Notification> anciennes, List<Notification> nouvelles) {
        if (anciennes == null || anciennes.isEmpty()) {
            return nouvelles;
        }

        Map<String, Notification> anciennesMap = anciennes.stream()
                .collect(Collectors.toMap(
                        Notification::getMessage,
                        n -> n,
                        (a, b) -> a
                ));

        for (Notification notif : nouvelles) {
            if (anciennesMap.containsKey(notif.getMessage())) {
                Notification anc = anciennesMap.get(notif.getMessage());
                notif.setLu(anc.isLu());
                if (anc.getDate() != null) {
                    notif.setDate(anc.getDate());
                }
            }
        }

        return nouvelles;
    }

    private void afficherNotifications() {
        alertesContainer.getChildren().clear();

        if (notifications.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 0, 50, 0));

            Label emptyLabel = new Label("🎉");
            emptyLabel.setStyle("-fx-font-size: 48px;");
            Label emptyText = new Label("Aucune alerte");
            emptyText.setStyle("-fx-text-fill: #999; -fx-font-size: 16px;");
            Label emptySubText = new Label("Tout est en ordre !");
            emptySubText.setStyle("-fx-text-fill: #bbb; -fx-font-size: 12px;");

            emptyBox.getChildren().addAll(emptyLabel, emptyText, emptySubText);
            alertesContainer.getChildren().add(emptyBox);
            return;
        }

        // Trier les notifications par date décroissante
        notifications.sort((n1, n2) -> n2.getDate().compareTo(n1.getDate()));

        Map<LocalDate, List<Notification>> groupedByDate = notifications.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getDate().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<LocalDate> sortedDates = new ArrayList<>(groupedByDate.keySet());
        sortedDates.sort((d1, d2) -> d2.compareTo(d1));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        for (LocalDate date : sortedDates) {
            List<Notification> notifs = groupedByDate.get(date);
            notifs.sort((n1, n2) -> n2.getDate().compareTo(n1.getDate()));

            String titre;
            if (date.equals(today)) {
                titre = "📅 Aujourd'hui";
            } else if (date.equals(yesterday)) {
                titre = "📅 Hier";
            } else {
                titre = "📅 " + date.format(dateFormatter);
            }

            Label categoryLabel = new Label(titre);
            categoryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #21262A; -fx-padding: 15 0 5 0;");
            alertesContainer.getChildren().add(categoryLabel);

            for (Notification notif : notifs) {
                VBox card = createNotificationCard(notif);
                alertesContainer.getChildren().add(card);
            }
        }
    }

    private VBox createNotificationCard(Notification notif) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 18, 12, 18));
        card.setStyle(
                "-fx-background-color: " + (notif.isLu() ? "#f8f9fa" : "#ffffff") + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);" +
                        "-fx-border-color: #e8eaee;" +
                        "-fx-border-width: 1;"
        );

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(5, 0, 5, 0));

        Circle statusDot = new Circle(6);
        String color = "#46BE62";
        if (notif.getType().contains("🔴")) {
            color = "#D9691D";
        } else if (notif.getType().contains("🟠") || notif.getType().contains("🟡")) {
            color = "#F4A460";
        } else if (notif.getType().contains("✅")) {
            color = "#46BE62";
        }
        statusDot.setFill(Color.web(color));

        Label iconLabel = new Label(notif.getType());
        iconLabel.setStyle("-fx-font-size: 22px;");

        VBox textBox = new VBox(3);
        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle(
                "-fx-text-fill: " + (notif.isLu() ? "#666" : "#21262A") + ";" +
                        "-fx-font-size: 14px;" +
                        (notif.isLu() ? "" : "-fx-font-weight: bold;")
        );
        messageLabel.setWrapText(true);

        // ✅ Afficher l'heure et le temps relatif ("14:35 • il y a 10 min")
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = notif.getDate() != null ? notif.getDate().format(timeFormatter) : "";
        String relativeTime = getRelativeTimeString(notif.getDate());
        Label timeLabel = new Label(formattedTime + (relativeTime.isEmpty() ? "" : " • " + relativeTime));
        timeLabel.setStyle("-fx-text-fill: #6F8D94; -fx-font-size: 11px; -fx-font-weight: 500;");

        textBox.getChildren().addAll(messageLabel, timeLabel);

        Button markButton = new Button(notif.isLu() ? "✅ Lu" : "Marquer lu");
        markButton.setStyle(
                "-fx-background-color: " + (notif.isLu() ? "#e8eaee" : "#346771") + ";" +
                        "-fx-text-fill: " + (notif.isLu() ? "#666" : "#ffffff") + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 4 14;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );
        if (!notif.isLu()) {
            markButton.setOnAction(e -> {
                notif.setLu(true);
                sauvegarderNotifications(notifications);
                afficherNotifications();
                updateStats();
                if (MainController.getInstance() != null) {
                    MainController.getInstance().updateNotificationBadge();
                }
            });
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        content.getChildren().addAll(statusDot, iconLabel, textBox, spacer, markButton);
        card.getChildren().add(content);

        return card;
    }

    private String getRelativeTimeString(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        if (minutes < 0) return "À l'instant";
        if (minutes < 1) return "À l'instant";
        if (minutes < 60) return "il y a " + minutes + " min";
        long hours = minutes / 60;
        if (hours < 24) return "il y a " + hours + " h";
        long days = hours / 24;
        if (days == 1) return "hier";
        return "il y a " + days + " jours";
    }

    private void updateStats() {
        if (notifications.isEmpty()) {
            totalAlertes.setText("Total: 0");
            nonLues.setText("🔴 Non lues: 0");
            lues.setText("🟢 Lues: 0");
            return;
        }

        long total = notifications.size();
        long nonLuesCount = notifications.stream().filter(n -> !n.isLu()).count();
        long luesCount = notifications.stream().filter(Notification::isLu).count();

        totalAlertes.setText("Total: " + total);
        nonLues.setText("🔴 Non lues: " + nonLuesCount);
        lues.setText("🟢 Lues: " + luesCount);
    }

    @FXML
    private void handleMarkAllRead() {
        notifications.forEach(n -> n.setLu(true));
        sauvegarderNotifications(notifications);
        afficherNotifications();
        updateStats();
        if (MainController.getInstance() != null) {
            MainController.getInstance().updateNotificationBadge();
        }
    }

    @FXML
    private void handleRetour() {
        try {
            // ✅ Charger la vue Suivi Pouchoir
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/suivi-pouchoir-view.fxml"));
            Pane suiviView = loader.load();

            // Forcer la taille
            suiviView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            suiviView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Trouver le contentArea (StackPane principal)
            Node node = alertesContainer;
            StackPane contentArea = null;

            while (node != null) {
                if (node instanceof StackPane && "contentArea".equals(node.getId())) {
                    contentArea = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentArea == null) {
                node = alertesContainer;
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
                contentArea.getChildren().add(suiviView);
                System.out.println("Retour vers Suivi Pouchoir effectué !");
            } else {
                System.err.println("contentArea non trouvé !");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de revenir au Suivi Pochoir");
            alert.showAndWait();
        }
    }

    private static class Notification implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private String message;
        private LocalDateTime date;
        private boolean lu;

        public Notification(String type, String message, LocalDateTime date, boolean lu) {
            this.type = type;
            this.message = message;
            this.date = date;
            this.lu = lu;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        public boolean isLu() { return lu; }
        public void setLu(boolean lu) { this.lu = lu; }
    }
}