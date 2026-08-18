package security;

import entities.Utilisateur;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton pour gerer la session utilisateur courante.
 * TECHNICIEN et INGENIEUR : deconnexion automatique apres 5 min d'inactivite.
 * OPERATEUR : pas de timeout.
 */
public class SessionManager {

    private static final long TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    private static SessionManager instance;
    private Utilisateur utilisateurCourant;

    private Timer timeoutTimer;
    private long lastActivityTime;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUtilisateur(Utilisateur user) {
        this.utilisateurCourant = user;
        resetActivity();
    }

    public Utilisateur getUtilisateur() {
        return utilisateurCourant;
    }

    public boolean isAuthenticated() {
        return utilisateurCourant != null;
    }

    public boolean hasRole(Utilisateur.Role... roles) {
        if (!isAuthenticated()) return false;
        for (Utilisateur.Role role : roles) {
            if (utilisateurCourant.getRole() == role) return true;
        }
        return false;
    }

    public void logout() {
        stopTimeoutTimer();
        this.utilisateurCourant = null;
    }

    public void resetActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    public void startTimeoutTimer(Runnable onTimeout) {
        if (!isAuthenticated()) return;
        if (utilisateurCourant.getRole() == Utilisateur.Role.OPERATEUR ||
            utilisateurCourant.getRole() == Utilisateur.Role.PRODUCTION) return;

        stopTimeoutTimer();
        lastActivityTime = System.currentTimeMillis();

        System.out.println("[TIMER] Timer demarre. Timeout dans 5 minutes.");

        timeoutTimer = new Timer("SessionTimeoutTimer", true);
        timeoutTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long inactif = System.currentTimeMillis() - lastActivityTime;
                long restant = (TIMEOUT_MS - inactif) / 1000;
                System.out.println("[TIMER] Inactivite: " + (inactif/1000) + "s - Restant: " + restant + "s");
                if (inactif >= TIMEOUT_MS) {
                    System.out.println("[TIMER] TIMEOUT! Deconnexion en cours...");
                    stopTimeoutTimer();
                    Platform.runLater(onTimeout);
                }
            }
        }, 10_000, 10_000);
    }

    public void stopTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    public long getRemainingSeconds() {
        long inactif = System.currentTimeMillis() - lastActivityTime;
        long remaining = (TIMEOUT_MS - inactif) / 1000;
        return Math.max(0, remaining);
    }
}