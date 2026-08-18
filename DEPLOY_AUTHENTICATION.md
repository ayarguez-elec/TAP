# Déploiement du Système d'Authentification

## 📋 Checklist de Déploiement

### ✅ Phase 1 : Préparation Base de Données

1. **Ouvrir MySQL**
   ```bash
   mysql -u root -p
   ```

2. **Exécuter le script d'initialisation**
   ```sql
   USE lacroix;
   SOURCE database_auth_schema.sql;
   ```

3. **Vérifier la création des tables**
   ```sql
   SHOW TABLES;
   -- Doit afficher : utilisateur, historique_pochoir, audit_log
   
   SELECT username, role FROM utilisateur;
   -- Doit afficher les 3 utilisateurs par défaut
   ```

### ✅ Phase 2 : Compilation Maven

1. **Installer les dépendances**
   ```bash
   mvn clean install
   ```

2. **Vérifier la dépendance BCrypt**
   ```bash
   mvn dependency:tree | grep jbcrypt
   ```
   Devrait afficher : `org.mindrot:jbcrypt:jar:0.4`

### ✅ Phase 3 : Tester le Système

1. **Générer un hash de test**
   ```bash
   java -cp target/classes utils.PasswordHashGenerator
   ```
   Cela affichera un hash BCrypt pour "admin123"

2. **Lancer l'application**
   ```bash
   mvn javafx:run
   ```

3. **Tester l'authentification**
   - Ouvrir l'application
   - Cliquer sur "Gestion Clients"
   - Un dialog d'authentification devrait apparaître
   - Tester avec : `admin` / `admin123`

### ✅ Phase 4 : Vérification Fonctionnelle

#### Test 1 : Authentification avec Ingénieur
```
Username: admin
Password: admin123
Rôle: INGENIEUR
→ Doit avoir accès à TOUTES les fonctionnalités
```

#### Test 2 : Authentification avec Technicien
```
Username: tech1
Password: admin123
Rôle: TECHNICIEN
→ Doit avoir accès à tout SAUF...
→ (actuellement tous les rôles sauf OPERATEUR ont les mêmes permissions)
```

#### Test 3 : Authentification avec Opérateur
```
Username: op1
Password: admin123
Rôle: OPERATEUR
→ Ne doit PAS avoir accès à Gestion Clients/Pochoirs
→ Doit avoir accès à Suivi et Consultation
```

#### Test 4 : Logs d'Audit
1. Effectuer une action (ex: ajouter un client)
2. Ouvrir "Historique" dans le menu
3. Aller dans l'onglet "Journal d'Audit"
4. Vérifier que l'action est enregistrée

#### Test 5 : Historique Pochoir
1. Tracer une sortie de pochoir
2. Ouvrir "Historique"
3. Vérifier dans l'onglet "Historique Pochoirs"

## 🔐 Sécurité Post-Déploiement

### 1. Changer les mots de passe par défaut

**Important** : Les 3 utilisateurs ont le mot de passe `admin123`

```sql
-- Se connecter à MySQL
mysql -u root -p lacroix

-- Option 1 : Générer un nouveau hash avec Java
-- Exécuter: java -cp target/classes utils.PasswordHashGenerator
-- Copier le hash généré

-- Option 2 : Utiliser le hash BCrypt d'un nouveau mot de passe
UPDATE utilisateur 
SET password_hash = '$2a$10$VOTRE_NOUVEAU_HASH_ICI' 
WHERE username = 'admin';

UPDATE utilisateur 
SET password_hash = '$2a$10$VOTRE_NOUVEAU_HASH_ICI' 
WHERE username = 'tech1';

UPDATE utilisateur 
SET password_hash = '$2a$10$VOTRE_NOUVEAU_HASH_ICI' 
WHERE username = 'op1';
```

### 2. Créer des utilisateurs réels

```java
// Dans un controller ou main
UtilisateurService userService = new UtilisateurService();

Utilisateur newUser = new Utilisateur();
newUser.setNom("Jean Dupont");
newUser.setUsername("jdupont");
newUser.setPasswordHash("MotDePasseSecurise123!");
newUser.setRole(Utilisateur.Role.TECHNICIEN);
newUser.setActif(true);

userService.create(newUser);
```

### 3. Désactiver les utilisateurs de test (optionnel)

```sql
UPDATE utilisateur SET actif = FALSE WHERE username IN ('admin', 'tech1', 'op1');
```

## 📊 Monitoring

### Consulter les logs d'audit

```sql
-- Dernières 20 actions
SELECT a.date_action, u.nom, a.action, a.table_affectee, a.details
FROM audit_log a
LEFT JOIN utilisateur u ON a.utilisateur_id = u.id
ORDER BY a.date_action DESC
LIMIT 20;
```

### Consulter l'historique pochoirs

```sql
-- Pochoirs actuellement sortis
SELECT h.pouchoir_ref, u.nom as operateur, h.date_sortie, h.localisation
FROM historique_pochoir h
LEFT JOIN utilisateur u ON h.operateur_id = u.id
WHERE h.action = 'SORTIE' AND h.date_retour IS NULL;
```

## 🔧 Intégration dans d'Autres Controllers

### Template d'Intégration

```java
package controller;

import security.PermissionGuard;
import services.AuditService;
// ... autres imports

public class MonController {
    
    private AuditService auditService = new AuditService();
    
    @FXML
    private void handleCreate() {
        // 1. Vérifier permission
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_XXX)) {
            return;
        }
        
        // 2. Logger l'ouverture
        auditService.logCurrentUser("CREATE", "table_name", null, 
            "Description de l'action");
        
        // 3. Logique métier
        // ...
        
        // 4. Logger le succès
        auditService.logCurrentUser("CREATE", "table_name", 
            String.valueOf(newId), "Création réussie: " + details);
    }
    
    @FXML
    private void handleUpdate() {
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_XXX)) {
            return;
        }
        
        auditService.logCurrentUser("UPDATE", "table_name", 
            String.valueOf(recordId), "Modification: " + details);
        
        // Logique métier...
    }
    
    @FXML
    private void handleDelete() {
        if (!PermissionGuard.checkPermission(PermissionGuard.GESTION_XXX)) {
            return;
        }
        
        // Confirmation dialog...
        
        auditService.logCurrentUser("DELETE", "table_name", 
            String.valueOf(recordId), "Suppression: " + details);
        
        // Logique métier...
    }
}
```

### Controllers à Intégrer

#### Priorité Haute (Gestion CRUD)
- ✅ **GestionClientController** - Fait
- ✅ **AjouterClientController** - Fait
- ⏳ **ModifierClientController** - À faire
- ⏳ **GestionPouchoirController** - À faire
- ⏳ **AjouterPouchoirController** - À faire
- ⏳ **ModifierPouchoirController** - À faire

#### Priorité Moyenne (Fonctionnalités métier)
- ⏳ **FicheSerigraphieController** - À faire
- ⏳ **CodificationController** - À faire

#### Priorité Basse (Consultation)
- ⏳ **SuiviPouchoirController** - Pas de restriction, logs optionnels
- ⏳ **DashboardController** - Pas de restriction

## 📝 Checklist Actions Suivantes

### Immédiat
- [ ] Changer les mots de passe par défaut
- [ ] Créer les utilisateurs réels de votre équipe
- [ ] Tester chaque rôle (OPERATEUR, TECHNICIEN, INGENIEUR)

### Court terme
- [ ] Intégrer les permissions dans ModifierClientController
- [ ] Intégrer les permissions dans GestionPouchoirController
- [ ] Intégrer les permissions dans AjouterPouchoirController
- [ ] Intégrer les permissions dans ModifierPouchoirController

### Moyen terme
- [ ] Intégrer dans FicheSerigraphieController
- [ ] Intégrer dans CodificationController
- [ ] Ajouter des logs dans SuiviPouchoirController
- [ ] Créer une interface de gestion des utilisateurs (CRUD)

### Long terme
- [ ] Implémenter l'export CSV de l'historique
- [ ] Ajouter des rapports d'audit mensuels
- [ ] Ajouter des permissions plus granulaires si nécessaire
- [ ] Implémenter une expiration de session (optionnel)

## 🐛 Troubleshooting

### Problème : "Table 'utilisateur' doesn't exist"
**Solution** : 
```bash
mysql -u root -p lacroix < database_auth_schema.sql
```

### Problème : "ClassNotFoundException: org.mindrot.jbcrypt.BCrypt"
**Solution** : 
```bash
mvn clean install
```

### Problème : Dialog d'authentification n'apparaît pas
**Solution** : Vérifier les imports
```java
import security.PermissionGuard;
```

### Problème : Mot de passe refusé
**Solution** : Vérifier que vous utilisez bien `admin123` et que le hash est correct dans la DB

### Problème : NullPointerException sur SessionManager
**Solution** : L'utilisateur n'est pas authentifié. Vérifier que `checkPermission()` est appelé avant `logCurrentUser()`

### Problème : Audit log vide
**Solution** : Vérifier que l'utilisateur est authentifié
```java
if (SessionManager.getInstance().isAuthenticated()) {
    auditService.logCurrentUser(...);
}
```

## 📞 Support

Pour toute question, consulter :
1. `AUTHENTICATION_SYSTEM_GUIDE.md` - Guide complet du système
2. Code source dans `src/main/java/security/`
3. Exemples d'intégration dans `GestionClientController` et `AjouterClientController`

---

**Version du système : 1.0**  
**Date de déploiement : _À compléter_**  
**Déployé par : _À compléter_**
