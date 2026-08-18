# Interface d'Administration des Utilisateurs - Documentation d'Implémentation

## Vue d'ensemble

Interface sécurisée de gestion des utilisateurs (CRUD) réservée aux ingénieurs, sans interface d'inscription publique.

## Fichiers Créés

### 1. FXML View
**Fichier:** `src/main/resources/admin-utilisateurs-view.fxml`

**Composants:**
- Header avec titre "Gestion des Utilisateurs" et compteur total
- Barre de recherche avec filtres (Rôle, Statut)
- Boutons d'action: "Nouvel Utilisateur", "Réinitialiser"
- TableView avec 7 colonnes:
  - ID (50px)
  - Nom (200px)
  - Username (150px)
  - Rôle (120px) - avec badges colorés
  - Actif (80px) - avec indicateurs visuels ✅/❌
  - Date création (150px)
  - Actions (250px) - boutons: Modifier, Changer MDP, Activer/Désactiver, Supprimer

### 2. Controller
**Fichier:** `src/main/java/controller/AdminUtilisateursController.java`

**Fonctionnalités principales:**

#### A. Sécurité
- Vérification du rôle INGENIEUR à l'initialisation
- Redirection si accès non autorisé

#### B. CRUD Utilisateurs

**Créer (handleAdd)**
- Dialog avec tous les champs requis
- Validation du nom (non vide)
- Validation username (non vide + unicité)
- Validation mot de passe (min. 8 caractères + confirmation)
- Hash automatique BCrypt du mot de passe
- Audit log de la création

**Modifier (handleEdit)**
- Dialog de modification (nom, rôle, statut actif)
- Username non modifiable
- Mot de passe non modifiable (voir "Changer MDP")
- Audit log de la modification

**Changer mot de passe (handleChangePassword)**
- Dialog spécifique pour le changement de mot de passe
- Validation min. 8 caractères
- Confirmation du nouveau mot de passe
- Hash automatique BCrypt
- Audit log du changement

**Activer/Désactiver (handleToggleActif)**
- Toggle du statut `actif`
- Protection: impossible de se désactiver soi-même
- Confirmation avant désactivation
- Audit log de l'action

**Supprimer (handleDelete)**
- Implémentation: désactivation plutôt que suppression physique
- Protection: vérification qu'il reste au moins un INGENIEUR actif
- Confirmation forte avec avertissement
- Audit log de la "suppression"

#### C. Recherche et Filtres
- **Recherche textuelle:** par nom ou username (temps réel)
- **Filtre par rôle:** Tous, OPERATEUR, TECHNICIEN, INGENIEUR
- **Filtre par statut:** Tous, Actifs, Inactifs
- Filtres combinables

#### D. Indicateurs Visuels
**Colonne Actif:**
- ✅ Vert (#46BE62) pour actif
- ❌ Rouge (#D9691D) pour inactif

**Colonne Rôle (badges colorés):**
- OPERATEUR: bleu (#3B82F6)
- TECHNICIEN: vert (#10B981)
- INGENIEUR: violet (#8B5CF6)

**Boutons d'action:**
- Modifier: ✏️ (teal #346771)
- Changer MDP: 🔑 (gris #6F8D94)
- Activer/Désactiver: ✅/🚫 (vert/rouge)
- Supprimer: 🗑️ (rouge #D9691D)

### 3. Dialog Réutilisable
**Fichier:** `src/main/java/dialogs/UtilisateurDialog.java`

**Modes:**
- **Création:** Tous les champs + mot de passe
- **Modification:** Nom, rôle, statut (username et password exclus)

**Validations intégrées:**
- Nom non vide
- Username unique (vérifié en base)
- Mot de passe min. 8 caractères
- Correspondance mot de passe/confirmation

## Fichiers Modifiés

### 1. PermissionGuard
**Fichier:** `src/main/java/security/PermissionGuard.java`

**Ajouts:**
```java
public static final String ADMIN_USERS = "admin_users";

// Dans static block:
PERMISSIONS.put(ADMIN_USERS, EnumSet.of(Utilisateur.Role.INGENIEUR));
```

### 2. MainController
**Fichier:** `src/main/java/controller/MainController.java`

**Ajouts:**
- Champ FXML: `@FXML private Button btnGestionUsers;`
- Méthode: `showGestionUtilisateurs()`
  - Vérification du rôle INGENIEUR
  - Chargement de la vue admin-utilisateurs-view.fxml

### 3. Main View FXML
**Fichier:** `src/main/resources/main-view.fxml`

**Ajouts:**
- Nouvelle section "ADMINISTRATION" dans le sidebar
- Bouton "👥 Utilisateurs" avec action `showGestionUtilisateurs`

## Règles de Sécurité Implémentées

### Protection des Actions
1. ✅ **Accès restreint:** Seulement les INGENIEUR peuvent accéder à l'interface
2. ✅ **Auto-protection:** Impossible de se désactiver soi-même
3. ✅ **Protection du dernier INGENIEUR:** Impossible de supprimer/désactiver le dernier INGENIEUR actif
4. ✅ **Confirmation forte:** Dialogs de confirmation pour actions critiques (désactivation, suppression)

### Audit Complet
Toutes les actions sont loggées dans `audit_log`:
- `CREATE_USER` - Création d'utilisateur
- `UPDATE_USER` - Modification d'utilisateur
- `CHANGE_PASSWORD` - Changement de mot de passe
- `ACTIVATE_USER` - Activation d'utilisateur
- `DEACTIVATE_USER` - Désactivation d'utilisateur
- `DELETE_USER` - Suppression (désactivation) d'utilisateur

## Validation des Données

### Au niveau Dialog
- Nom: non vide
- Username: non vide + unicité (vérification en base)
- Mot de passe: minimum 8 caractères
- Confirmation: identique au mot de passe

### Au niveau Controller
- Rôle: obligatoire (défaut: OPERATEUR)
- Statut actif: boolean (défaut: true)

## Styles Utilisés

L'interface utilise les styles existants de l'application:
- `suivi-header-card` - En-tête
- `suivi-filter-card` - Barre de filtres
- `suivi-search-box` - Zone de recherche
- `btn-primary` - Bouton principal
- `btn-reset-modern` - Bouton réinitialiser
- `table-view` - TableView
- Styles inline pour les badges de rôle et indicateurs

## Base de Données

### Table Utilisée
`utilisateur` avec colonnes:
- `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
- `nom` (VARCHAR(100), NOT NULL)
- `username` (VARCHAR(50), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL)
- `role` (ENUM: 'OPERATEUR', 'TECHNICIEN', 'INGENIEUR')
- `actif` (BOOLEAN, DEFAULT TRUE)
- `date_creation` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

### Utilisateurs Par Défaut
Disponibles dans `database_auth_schema.sql`:
- `admin` / admin123 (INGENIEUR)
- `tech1` / admin123 (TECHNICIEN)
- `op1` / admin123 (OPERATEUR)

## Points Clés d'Implémentation

### 1. Sécurité du Mot de Passe
- Hash BCrypt automatique lors de la création/modification
- Pas de stockage en clair
- Minimum 8 caractères requis

### 2. Soft Delete
La suppression n'est pas physique, on désactive l'utilisateur:
```java
user.setActif(false);
utilisateurService.update(user);
```

### 3. Navigation
Depuis le MainController:
```
Menu Sidebar → Administration → Utilisateurs → admin-utilisateurs-view.fxml
```

### 4. Filtrage Temps Réel
Les filtres (recherche, rôle, statut) se combinent et s'appliquent immédiatement:
```java
searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
roleFilterCombo.setOnAction(e -> applyFilters());
statutFilterCombo.setOnAction(e -> applyFilters());
```

## Tests Recommandés

### Tests Fonctionnels
1. ✅ Accès réservé INGENIEUR
2. ✅ Création d'utilisateur avec validation
3. ✅ Modification (nom, rôle, statut)
4. ✅ Changement de mot de passe
5. ✅ Activation/Désactivation
6. ✅ Protection auto-désactivation
7. ✅ Protection dernier INGENIEUR
8. ✅ Recherche et filtres
9. ✅ Audit logs

### Tests de Sécurité
1. Tenter d'accéder avec un OPERATEUR → Refusé
2. Tenter d'accéder avec un TECHNICIEN → Refusé
3. Tenter de se désactiver soi-même → Bloqué
4. Tenter de désactiver le dernier INGENIEUR → Bloqué
5. Vérifier que les mots de passe sont hashés en base

## Notes de Déploiement

### Prérequis
1. Base de données `lacroix` configurée
2. Table `utilisateur` créée (via `database_auth_schema.sql`)
3. Au moins un utilisateur INGENIEUR actif en base

### Configuration
Aucune configuration supplémentaire requise. Le système utilise:
- `UtilisateurService` pour les opérations CRUD
- `AuditService` pour la traçabilité
- `PermissionGuard` pour la sécurité
- `SessionManager` pour la session courante
- `BCryptUtil` pour le hashing

## Conclusion

L'interface d'administration des utilisateurs est maintenant complètement opérationnelle avec:
- ✅ CRUD complet et sécurisé
- ✅ Validation robuste des données
- ✅ Audit complet des actions
- ✅ Protection contre les actions dangereuses
- ✅ Interface cohérente avec le reste de l'application
- ✅ Recherche et filtres performants
- ✅ Indicateurs visuels clairs

Le système est prêt pour une utilisation en production avec les utilisateurs par défaut. Il est recommandé de changer les mots de passe par défaut lors du premier déploiement.
