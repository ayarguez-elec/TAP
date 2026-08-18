package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;

/**
 * Checks for app updates on GitHub and downloads the new JAR if available.
 */
public class UpdateChecker {

    private static final String CURRENT_VERSION = "1.0.0";
    private static final String VERSION_URL     = "https://raw.githubusercontent.com/ayarguez-elec/TAP/main/version.txt";
    private static final String DOWNLOAD_URL    = "https://github.com/ayarguez-elec/TAP/releases/latest/download/LacroixElectronics.jar";

    public static void checkForUpdates() {
        Thread t = new Thread(() -> {
            try {
                String latestVersion = fetchLatestVersion();
                if (latestVersion == null) return;
                if (!latestVersion.trim().equals(CURRENT_VERSION)) {
                    Platform.runLater(() -> promptUpdate(latestVersion.trim()));
                }
            } catch (Exception e) {
                System.err.println("[UPDATE] Check failed: " + e.getMessage());
            }
        }, "UpdateCheckerThread");
        t.setDaemon(true);
        t.start();
    }

    private static String fetchLatestVersion() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(VERSION_URL).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        if (conn.getResponseCode() != 200) return null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return r.readLine();
        }
    }

    private static void promptUpdate(String newVersion) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mise à jour disponible");
        alert.setHeaderText("Version " + newVersion + " disponible");
        alert.setContentText("Une nouvelle version de Lacroix Electronics est disponible.\n\nVoulez-vous mettre à jour maintenant ?\nL'application redémarrera automatiquement.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            downloadAndRestart();
        }
    }

    private static void downloadAndRestart() {
        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("Mise à jour en cours");
        progress.setHeaderText("Téléchargement...");
        progress.setContentText("Veuillez patienter pendant le téléchargement de la mise à jour.");
        progress.getButtonTypes().clear();
        progress.show();

        Thread dl = new Thread(() -> {
            try {
                String currentJar = UpdateChecker.class
                        .getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                // Fix Windows path (leading slash)
                if (currentJar.startsWith("/") && currentJar.contains(":")) {
                    currentJar = currentJar.substring(1);
                }
                Path currentPath = Paths.get(currentJar);
                Path newPath = currentPath.getParent().resolve("LacroixElectronics-new.jar");

                // Download
                HttpURLConnection conn = (HttpURLConnection) new URL(DOWNLOAD_URL).openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(120000);
                conn.setRequestProperty("User-Agent", "LacroixElectronics-Updater/1.0");

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(newPath.toFile())) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }

                // Write update script
                Path launcher = currentPath.getParent().resolve("update.bat");
                String bat = "@echo off\r\n"
                        + "timeout /t 2 /nobreak >nul\r\n"
                        + "move /Y \"" + newPath.toAbsolutePath() + "\" \"" + currentPath.toAbsolutePath() + "\"\r\n"
                        + "start javaw --add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.web"
                        + " --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED"
                        + " --add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED"
                        + " -jar \"" + currentPath.toAbsolutePath() + "\"\r\n"
                        + "del \"%~f0\"\r\n";
                Files.writeString(launcher, bat);

                Runtime.getRuntime().exec("cmd /c start \"\" \"" + launcher.toAbsolutePath() + "\"");

                Platform.runLater(() -> {
                    progress.close();
                    System.exit(0);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progress.close();
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Erreur de mise à jour");
                    err.setContentText("Échec du téléchargement: " + e.getMessage());
                    err.show();
                });
            }
        }, "DownloadThread");
        dl.setDaemon(false);
        dl.start();
    }
}