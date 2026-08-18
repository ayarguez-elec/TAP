package security;

import entities.Utilisateur;
import javafx.scene.control.Alert;
import services.AuditService;

import java.io.*;
import java.util.*;

/**
 * Gère les permissions selon les rôles utilisateurs et des personnes individuelles
 */
public class PermissionGuard {
    
    // Constantes pour les actions
    public static final String GESTION_CLIENT = "gestion_client";
    public static final String GESTION_POCHOIR = "gestion_pochoir";
    public static final String FICHE_SERIGRAPHIE_CRUD = "fiche_crud";
    public static final String CODIFICATION = "codification";
    public static final String SUIVI_POCHOIR = "suivi_pochoir";
    public static final String CONSULTATION_FICHE = "consultation_fiche";
    public static final String ADMIN_USERS = "admin_users";

    // CRUD Client
    public static final String CREATE_CLIENT = "CREATE_CLIENT";
    public static final String READ_CLIENT   = "READ_CLIENT";
    public static final String UPDATE_CLIENT = "UPDATE_CLIENT";
    public static final String DELETE_CLIENT = "DELETE_CLIENT";

    // CRUD Pochoir
    public static final String CREATE_POCHOIR = "CREATE_POCHOIR";
    public static final String READ_POCHOIR   = "READ_POCHOIR";
    public static final String UPDATE_POCHOIR = "UPDATE_POCHOIR";
    public static final String DELETE_POCHOIR = "DELETE_POCHOIR";

    // CRUD Fiche Sérigraphie
    public static final String CREATE_FICHE = "CREATE_FICHE";
    public static final String READ_FICHE   = "READ_FICHE";
    public static final String UPDATE_FICHE = "UPDATE_FICHE";
    public static final String DELETE_FICHE = "DELETE_FICHE";

    // CRUD Utilisateur
    public static final String CREATE_USER = "CREATE_USER";
    public static final String READ_USER   = "READ_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";

    // Production
    public static final String VIEW_PLANNING    = "VIEW_PLANNING";
    public static final String VIEW_ANOMALIES   = "VIEW_ANOMALIES";
    public static final String REPORT_ANOMALIE  = "REPORT_ANOMALIE";

    // Noms lisibles pour les actions
    public static final Map<String, String> ACTION_NAMES = new LinkedHashMap<>();
    static {
        ACTION_NAMES.put(SUIVI_POCHOIR, "Visualiser l'armoire (Suivi)");
        ACTION_NAMES.put(CONSULTATION_FICHE, "Consulter les fiches");
        ACTION_NAMES.put(GESTION_CLIENT, "Gérer les clients (Ajouter/Modifier)");
        ACTION_NAMES.put(GESTION_POCHOIR, "Gérer les pochoirs (Ajouter/Modifier)");
        ACTION_NAMES.put(FICHE_SERIGRAPHIE_CRUD, "Gérer les fiches de sérigraphie (CRUD)");
        ACTION_NAMES.put(CODIFICATION, "Codification des pochoirs");
        ACTION_NAMES.put(ADMIN_USERS, "Administration des utilisateurs & permissions");
        // CRUD Client
        ACTION_NAMES.put(CREATE_CLIENT, "Créer Client");
        ACTION_NAMES.put(READ_CLIENT,   "Consulter Client");
        ACTION_NAMES.put(UPDATE_CLIENT, "Modifier Client");
        ACTION_NAMES.put(DELETE_CLIENT, "Supprimer Client");
        // CRUD Pochoir
        ACTION_NAMES.put(CREATE_POCHOIR, "Créer Pochoir");
        ACTION_NAMES.put(READ_POCHOIR,   "Consulter Pochoir");
        ACTION_NAMES.put(UPDATE_POCHOIR, "Modifier Pochoir");
        ACTION_NAMES.put(DELETE_POCHOIR, "Supprimer Pochoir");
        // CRUD Fiche Sérigraphie
        ACTION_NAMES.put(CREATE_FICHE, "Créer Fiche Sérigraphie");
        ACTION_NAMES.put(READ_FICHE,   "Consulter Fiche Sérigraphie");
        ACTION_NAMES.put(UPDATE_FICHE, "Modifier Fiche Sérigraphie");
        ACTION_NAMES.put(DELETE_FICHE, "Supprimer Fiche Sérigraphie");
        // CRUD Utilisateur
        ACTION_NAMES.put(CREATE_USER, "Créer Utilisateur");
        ACTION_NAMES.put(READ_USER,   "Consulter Utilisateur");
        ACTION_NAMES.put(UPDATE_USER, "Modifier Utilisateur");
        ACTION_NAMES.put(DELETE_USER, "Supprimer Utilisateur");
        // Production
        ACTION_NAMES.put(VIEW_PLANNING,   "Voir Planning CMS");
        ACTION_NAMES.put(VIEW_ANOMALIES,  "Voir Anomalies Production");
        ACTION_NAMES.put(REPORT_ANOMALIE, "Signaler Anomalie");
    }

    // Matrice de permissions : Action -> Rôles autorisés
    private static final Map<String, Set<Utilisateur.Role>> ROLE_PERMISSIONS = new HashMap<>();
    // Surcharges individuelles : Action -> Liste d'utilisateurs autorisés par leur ID
    private static final Map<String, Set<Integer>> USER_OVERRIDES = new HashMap<>();
    
    private static final String PERMISSIONS_FILE = "role_permissions.ser";
    private static final String OVERRIDES_FILE = "user_permissions_overrides.ser";

    static {
        loadPermissions();
    }

    private static void initDefaults() {
        ROLE_PERMISSIONS.clear();

        // ── Legacy / broad permissions ───────────────────────────────────────
        ROLE_PERMISSIONS.put(SUIVI_POCHOIR,          EnumSet.allOf(Utilisateur.Role.class));
        ROLE_PERMISSIONS.put(CONSULTATION_FICHE,     EnumSet.allOf(Utilisateur.Role.class));
        ROLE_PERMISSIONS.put(GESTION_CLIENT,         EnumSet.of(Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR));
        ROLE_PERMISSIONS.put(GESTION_POCHOIR,        EnumSet.of(Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR));
        ROLE_PERMISSIONS.put(FICHE_SERIGRAPHIE_CRUD, EnumSet.of(Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR));
        ROLE_PERMISSIONS.put(CODIFICATION,           EnumSet.of(Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR));
        ROLE_PERMISSIONS.put(ADMIN_USERS,            EnumSet.of(Utilisateur.Role.INGENIEUR));

        // ── CRUD Client ──────────────────────────────────────────────────────
        // READ: tout le monde sauf PRODUCTION
        ROLE_PERMISSIONS.put(READ_CLIENT, EnumSet.of(
                Utilisateur.Role.OPERATEUR, Utilisateur.Role.TECHNICIEN,
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        // CREATE / UPDATE: TECHNICIEN, INGENIEUR, ADMIN
        ROLE_PERMISSIONS.put(CREATE_CLIENT, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(UPDATE_CLIENT, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        // DELETE: INGENIEUR, ADMIN
        ROLE_PERMISSIONS.put(DELETE_CLIENT, EnumSet.of(
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));

        // ── CRUD Pochoir ─────────────────────────────────────────────────────
        ROLE_PERMISSIONS.put(READ_POCHOIR, EnumSet.of(
                Utilisateur.Role.OPERATEUR, Utilisateur.Role.TECHNICIEN,
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN,
                Utilisateur.Role.PRODUCTION));
        ROLE_PERMISSIONS.put(CREATE_POCHOIR, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(UPDATE_POCHOIR, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(DELETE_POCHOIR, EnumSet.of(
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));

        // ── CRUD Fiche Sérigraphie ────────────────────────────────────────────
        ROLE_PERMISSIONS.put(READ_FICHE, EnumSet.of(
                Utilisateur.Role.OPERATEUR, Utilisateur.Role.TECHNICIEN,
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(CREATE_FICHE, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(UPDATE_FICHE, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(DELETE_FICHE, EnumSet.of(
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));

        // ── CRUD Utilisateur ─────────────────────────────────────────────────
        ROLE_PERMISSIONS.put(READ_USER, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(CREATE_USER, EnumSet.of(
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(UPDATE_USER, EnumSet.of(
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
        ROLE_PERMISSIONS.put(DELETE_USER, EnumSet.of(
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));

        // ── Production ────────────────────────────────────────────────────────
        ROLE_PERMISSIONS.put(VIEW_PLANNING, EnumSet.of(
                Utilisateur.Role.OPERATEUR, Utilisateur.Role.TECHNICIEN,
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN,
                Utilisateur.Role.PRODUCTION));
        ROLE_PERMISSIONS.put(VIEW_ANOMALIES, EnumSet.of(
                Utilisateur.Role.OPERATEUR, Utilisateur.Role.TECHNICIEN,
                Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN,
                Utilisateur.Role.PRODUCTION));
        ROLE_PERMISSIONS.put(REPORT_ANOMALIE, EnumSet.of(
                Utilisateur.Role.TECHNICIEN, Utilisateur.Role.INGENIEUR,
                Utilisateur.Role.ADMIN, Utilisateur.Role.PRODUCTION));

        USER_OVERRIDES.clear();
        for (String action : ACTION_NAMES.keySet()) {
            USER_OVERRIDES.put(action, new HashSet<>());
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadPermissions() {
        initDefaults();
        
        File roleFile = new File(PERMISSIONS_FILE);
        if (roleFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(roleFile))) {
                Map<String, Set<Utilisateur.Role>> loaded = (Map<String, Set<Utilisateur.Role>>) ois.readObject();
                ROLE_PERMISSIONS.putAll(loaded);
            } catch (Exception e) {
                System.err.println("Erreur chargement permissions roles: " + e.getMessage());
            }
        }

        File overrideFile = new File(OVERRIDES_FILE);
        if (overrideFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(overrideFile))) {
                Map<String, Set<Integer>> loaded = (Map<String, Set<Integer>>) ois.readObject();
                USER_OVERRIDES.putAll(loaded);
            } catch (Exception e) {
                System.err.println("Erreur chargement surcharges utilisateurs: " + e.getMessage());
            }
        }
        // Always enforce INGENIEUR and ADMIN can manage users/permissions
        ROLE_PERMISSIONS.computeIfAbsent(ADMIN_USERS, k -> EnumSet.noneOf(Utilisateur.Role.class))
                .addAll(EnumSet.of(Utilisateur.Role.INGENIEUR, Utilisateur.Role.ADMIN));
    }

    public static void savePermissions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PERMISSIONS_FILE))) {
            oos.writeObject(ROLE_PERMISSIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OVERRIDES_FILE))) {
            oos.writeObject(USER_OVERRIDES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Set<Utilisateur.Role>> getRolePermissions() {
        return ROLE_PERMISSIONS;
    }

    public static Map<String, Set<Integer>> getUserOverrides() {
        return USER_OVERRIDES;
    }

    /**
     * Vérifie si l'utilisateur courant a la permission pour une action
     * @param action L'action à vérifier
     * @return true si autorisé, false sinon
     */
    public static boolean checkPermission(String action) {
        SessionManager session = SessionManager.getInstance();
        
        // Si pas d'utilisateur authentifié, demander l'authentification
        if (!session.isAuthenticated()) {
            AuthDialog dialog = new AuthDialog("Cette action nécessite une authentification");
            Utilisateur user = dialog.showAndWait().orElse(null);
            
            if (user == null) {
                return false; // Authentification annulée
            }
            
            session.setUtilisateur(user);
        }
        
        Utilisateur user = session.getUtilisateur();
        
        if (hasPermission(user, action)) {
            return true;
        }
        
        // Permission refusée
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Accès refusé");
        alert.setHeaderText("Permission insuffisante");
        alert.setContentText("Votre rôle ou votre compte ne dispose pas des droits requis pour cette action.");
        alert.showAndWait();
        
        // Logger le refus
        AuditService auditService = new AuditService();
        auditService.log(user.getId(), "ACCESS_DENIED", action, null, 
                       "Tentative d'accès refusée pour: " + action);
        
        return false;
    }
    
    /**
     * Vérifie si un utilisateur a une permission (sans afficher de dialog)
     * @param user L'utilisateur à vérifier
     * @param action L'action à vérifier
     * @return true si autorisé, false sinon
     */
    public static boolean hasPermission(Utilisateur user, String action) {
        if (user == null) {
            return false;
        }
        
        // 1. Vérifier les surcharges individuelles (droits accordés nominativement)
        Set<Integer> overriddenUsers = USER_OVERRIDES.get(action);
        if (overriddenUsers != null && overriddenUsers.contains(user.getId())) {
            return true;
        }
        
        // 2. Vérifier si le rôle a la permission
        Set<Utilisateur.Role> allowedRoles = ROLE_PERMISSIONS.get(action);
        return allowedRoles != null && allowedRoles.contains(user.getRole());
    }
}
