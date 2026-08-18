package utils;

import security.BCryptUtil;

/**
 * Utilitaire pour générer des hashes BCrypt pour les mots de passe
 * Utilisez cette classe pour générer les hashes à insérer dans la base de données
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        String password = "admin123";
        String hash = BCryptUtil.hashPassword(password);
        
        System.out.println("===========================================");
        System.out.println("Générateur de Hash BCrypt");
        System.out.println("===========================================");
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash BCrypt:  " + hash);
        System.out.println("===========================================");
        System.out.println("\nInstructions SQL:");
        System.out.println("INSERT INTO utilisateur (nom, username, password_hash, role, actif)");
        System.out.println("VALUES ('Nom Utilisateur', 'username', '" + hash + "', 'TECHNICIEN', TRUE);");
        System.out.println("===========================================");
        
        // Test de vérification
        System.out.println("\nTest de vérification:");
        boolean isValid = BCryptUtil.checkPassword(password, hash);
        System.out.println("Vérification: " + (isValid ? "✅ SUCCÈS" : "❌ ÉCHEC"));
    }
}
