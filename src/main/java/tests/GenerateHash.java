package tests;

import security.BCryptUtil;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "admin123";

        // Générer 3 hash différents pour être sûr
        System.out.println("Génération de hash BCrypt pour: " + password);
        System.out.println();

        for (int i = 1; i <= 3; i++) {
            String hash = BCryptUtil.hashPassword(password);
            System.out.println("Hash #" + i + ":");
            System.out.println(hash);

            // Vérifier immédiatement
            boolean match = BCryptUtil.checkPassword(password, hash);
            System.out.println("Vérification: " + (match ? "✅ OK" : "❌ FAIL"));
            System.out.println();
        }

        System.out.println("=== Script SQL ===");
        String finalHash = BCryptUtil.hashPassword(password);
        System.out.println("UPDATE utilisateur SET password_hash = '" + finalHash + "' WHERE username = 'admin';");
    }
}
