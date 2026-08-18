package security;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {
    
    /**
     * Hash un mot de passe en utilisant BCrypt
     * @param plainPassword Le mot de passe en clair
     * @return Le hash BCrypt du mot de passe
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }
    
    /**
     * Vérifie si un mot de passe correspond à un hash
     * @param plainPassword Le mot de passe en clair à vérifier
     * @param hashedPassword Le hash stocké
     * @return true si le mot de passe correspond, false sinon
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
