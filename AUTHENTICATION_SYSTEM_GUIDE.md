# Guide du Système d'Authentification et Traçabilité

## Vue d'ensemble

Ce système implémente une authentification complète avec contrôle d'accès basé sur les rôles (RBAC) et traçabilité des actions pour l'application JavaFX de gestion de pochoirs.

## Architecture

### 1. Entités (entities/)
- **Utilisateur.java** : Représente un utilisateur avec son rôle (OPERATEUR, TECHNICIEN, INGENIEUR)
- **HistoriquePochoir.java** : Trace les mouvements des pochoirs (sortie/retour)
- **AuditLog.java** : Journalise toutes les actions utilisateurs

### 2. Sécurité (security/)
- **BCryptUtil.java** : Utilitaire pour hasher et vérifier les mots de passe avec BCrypt
- **SessionManager.java** : Singleton qui gère la session utilisateur courante
- **PermissionGuard.java** : Vérifie les permissions selon la matrice de rôles
- **AuthDialog.java** : Dialog JavaFX pour l'authentification

### 3. Services (services/)
- **UtilisateurService.java** : CRUD des utilisateurs + authentification
- **AuditService.java** : Enregistrement et consultation du journal d'audit
- **HistoriquePochoirService.java** : Traçabilité des mouvements pochoirs

### 4. Interface (controller/ + resources/)
- **HistoriqueController.java** : Contrôleur pour la vue historique
- **historique-view.fxml** : Interface avec 2 onglets (Historique Pochoirs + Audit Log)

## Installation

### 1. Créer les tables de base de données

Exécuter le script SQL : `database_auth_schema.sql`

```bash
mysql -u root -p lacroix < database_auth_schema.sql
```

Cela créera :
- Table `utilisateur` avec 3 utilisateurs par défaut
- Table `historique_pochoir` pour tracer les mouvements
- Table `audit_log` pour le journal d'audit

### 2. Utilisateurs par défaut

| Username | Mot de passe | Rôle       |
|----------|--------------|------------|
| admin    | admin123     | INGENIEUR  |
| tech1    | admin123     | TECHNICIEN |
| op1      | admin123     | OPERATEUR  |

⚠️ **Important** : Changez ces mots de passe en production !

### 3. Compiler le projet

```bash
mvn clean install
```

La dépendance BCrypt est déjà ajoutée au `pom.xml`.

## Matrice de Permissions

| Action                          | Opérateur | Technicien | Ingénieur |
|---------------------------------|-----------|------------|-----------|
| Suivi pochoirs (sortie/retour)  | ✅        | ✅         | ✅        |
| Consultation fiches             | ✅        | ✅         | ✅        |
| Gestion clients (CRUD)          | ❌        | ✅         | ✅        |
| Gestion pochoirs (CRUD)         | ❌        | ✅         | ✅        |
| Fiches sérigraphie (CRUD)       | ❌        | ✅         | ✅        |
| Codification                    | ❌        | ✅         | ✅        |

## Utilisation

### Dans un Controller

```java
import security.PermissionGuard;
import services.AuditService;

public class MonController {
    private AuditService auditService = new AuditService();
    
    @FXML
    private void handleActionSensible() {
        // 1. Vérifier les permissions
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_CLIENT)) {
            return; // Permission refusée
        }
        
        // 2. Logger l'action
        auditService.logCurrentUser("CREATE", "client", null, 
            "Description de l'action");
        
        // 3. Effectuer l'action
        // ... votre code ...
    }
}
```

### Permissions disponibles

```java
PermissionGuard.GESTION_CLIENT          // Gestion des clients
PermissionGuard.GESTION_POCHOIR         // Gestion des pochoirs
PermissionGuard.FICHE_SERIGRAPHIE_CRUD  // CRUD fiches sérigraphie
PermissionGuard.CODIFICATION            // Codification
PermissionGuard.SUIVI_POCHOIR           // Suivi des pochoirs
PermissionGuard.CONSULTATION_FICHE      // Consultation des fiches
```

### Tracer un mouvement de pochoir

```java
import services.HistoriquePochoirService;
import security.SessionManager;

HistoriquePochoirService histoService = new HistoriquePochoirService();

// Sortie d'un pochoir
int userId = SessionManager.getInstance().getUtilisateur().getId();
int histId = histoService.enregistrerSortie(
    "REF-POCHOIR-123", 
    userId, 
    "Atelier A", 
    "Production"
);

// Retour du pochoir
histoService.enregistrerRetour(histId, "Bon état", "RAS");
```

### Actions d'audit disponibles

- **CREATE** : Création d'un enregistrement
- **UPDATE** : Modification d'un enregistrement
- **DELETE** : Suppression d'un enregistrement
- **READ** : Consultation d'un enregistrement
- **LOGIN** : Connexion utilisateur
- **ACCESS_DENIED** : Tentative d'accès refusée

## Intégration Existante

### GestionClientController

Le contrôleur `GestionClientController` a été mis à jour avec :

1. **handleAdd()** : Vérifie `GESTION_CLIENT` + log "CREATE"
2. **handleEdit()** : Vérifie `GESTION_CLIENT` + log "UPDATE"
3. **handleDelete()** : Vérifie `GESTION_CLIENT` + log "DELETE"

### Exemple de log

Quand un technicien modifie un client :
```
utilisateur_id: 2
action: UPDATE
table_affectee: client
enregistrement_id: 5
details: Ouverture formulaire modification client: Société XYZ
date_action: 2024-01-15 14:30:45
```

## Consultation de l'Historique

### Interface Historique

Accessible via le menu principal → bouton "Historique"

**Onglet 1 : Historique Pochoirs**
- Affiche tous les mouvements de pochoirs (sorties/retours)
- Filtres : référence pochoir, action (SORTIE/RETOUR)
- Affiche : dates, opérateur, localisation, raison, état retour, remarques

**Onglet 2 : Journal d'Audit**
- Affiche toutes les actions utilisateurs
- Filtres : utilisateur, action, table
- Affiche : date/heure, utilisateur, action, table, détails

## Workflow sans écran de login

Ce système **n'utilise PAS d'écran de login initial**. L'authentification se fait à la demande :

1. L'utilisateur accède à l'application directement
2. Quand il tente une action sensible, un dialog d'authentification apparaît
3. Il entre ses credentials (username + password)
4. Si authentifié et autorisé → action effectuée
5. Si rôle insuffisant → message d'erreur

### Avantages
- Pas besoin de se connecter au démarrage
- Authentification uniquement pour actions sensibles
- Consultation libre pour tous

## Gestion des Utilisateurs

### Créer un utilisateur

```java
UtilisateurService userService = new UtilisateurService();

Utilisateur newUser = new Utilisateur();
newUser.setNom("Jean Dupont");
newUser.setUsername("jdupont");
newUser.setPasswordHash("motdepasse123"); // Sera hashé automatiquement
newUser.setRole(Utilisateur.Role.TECHNICIEN);
newUser.setActif(true);

userService.create(newUser);
```

### Changer un mot de passe

```java
userService.changePassword(userId, "nouveauMotDePasse");
```

### Désactiver un utilisateur

```java
Utilisateur user = userService.findByUsername("jdupont");
user.setActif(false);
userService.update(user);
```

## Sécurité

### Hashing BCrypt
- Tous les mots de passe sont hashés avec BCrypt (10 rounds)
- Jamais stockés en clair dans la base de données
- Vérification sécurisée avec `BCrypt.checkpw()`

### Session Management
- Session utilisateur stockée en mémoire (pas de cookie/token)
- Singleton thread-safe
- Logout disponible : `SessionManager.getInstance().logout()`

### Audit Trail
- Toutes les actions sensibles sont tracées
- Horodatage automatique
- Lien vers l'utilisateur qui a effectué l'action
- Détails contextuels enregistrés

## Prochaines Étapes

Pour intégrer le système dans d'autres controllers :

1. **Importer les classes nécessaires**
   ```java
   import security.PermissionGuard;
   import services.AuditService;
   ```

2. **Ajouter AuditService**
   ```java
   private AuditService auditService = new AuditService();
   ```

3. **Protéger les méthodes sensibles**
   ```java
   if (!PermissionGuard.checkPermission(PermissionGuard.XXX)) {
       return;
   }
   auditService.logCurrentUser("ACTION", "table", "id", "details");
   ```

## Controllers à intégrer

- ✅ **GestionClientController** : Fait
- ⏳ **AjouterClientController** : À faire
- ⏳ **ModifierClientController** : À faire
- ⏳ **GestionPouchoirController** : À faire
- ⏳ **AjouterPouchoirController** : À faire
- ⏳ **ModifierPouchoirController** : À faire
- ⏳ **FicheSerigraphieController** : À faire
- ⏳ **CodificationController** : À faire

## Troubleshooting

### Erreur "Table doesn't exist"
→ Vérifiez que le script SQL a été exécuté

### Dialog d'authentification ne s'affiche pas
→ Vérifiez les imports de `security.PermissionGuard`

### Mot de passe refusé
→ Les mots de passe par défaut sont : `admin123`
→ Vérifiez que BCrypt est bien dans les dépendances Maven

### Logs d'audit vides
→ Vérifiez que `auditService.logCurrentUser()` est appelé après l'authentification

## Support

Pour toute question ou problème, vérifiez :
1. Les tables de base de données sont créées
2. Les utilisateurs par défaut existent
3. La dépendance BCrypt est installée (`mvn clean install`)
4. Le SessionManager a un utilisateur authentifié avant le logging

---

**Système développé avec JavaFX 17, MySQL 8, BCrypt 0.4**
