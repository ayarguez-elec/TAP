package services;

import entities.Utilisateur;
import security.BCryptUtil;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {
    
    private Connection connection;
    
    public UtilisateurService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }
    
    /**
     * Créer un nouvel utilisateur avec mot de passe hashé
     */
    public void create(Utilisateur utilisateur) {
        String query = "INSERT INTO utilisateur (nom, username, password_hash, role, actif) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getUsername());
            ps.setString(3, BCryptUtil.hashPassword(utilisateur.getPasswordHash())); // Hash the password
            ps.setString(4, utilisateur.getRole().name());
            ps.setBoolean(5, utilisateur.isActif());
            
            ps.executeUpdate();
            System.out.println("Utilisateur créé avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lire tous les utilisateurs
     */
    public List<Utilisateur> readAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur ORDER BY nom";
        
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            while (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
                user.setActif(rs.getBoolean("actif"));
                user.setDateCreation(rs.getTimestamp("date_creation"));
                
                utilisateurs.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return utilisateurs;
    }
    
    /**
     * Trouver un utilisateur par username
     */
    public Utilisateur findByUsername(String username) {
        String query = "SELECT * FROM utilisateur WHERE username = ?";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
                user.setActif(rs.getBoolean("actif"));
                user.setDateCreation(rs.getTimestamp("date_creation"));
                
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Authentifier un utilisateur
     * @return L'utilisateur si authentification réussie, null sinon
     */
    public Utilisateur authenticate(String username, String password) {
        System.out.println("[AUTH] Recherche user: " + username);

        Utilisateur user = findByUsername(username);

        if (user == null) {
            System.out.println("[AUTH] User NOT FOUND");
            return null;
        }

        System.out.println("[AUTH] User trouvé!");
        System.out.println("[AUTH] Hash DB length: " + user.getPasswordHash().length());
        System.out.println("[AUTH] Hash: " + user.getPasswordHash());

        boolean match = BCryptUtil.checkPassword(password, user.getPasswordHash());
        System.out.println("[AUTH] BCrypt match: " + match);

        if (match) {
            return user;
        }

        return null;
    }


    /**
     * Vérifier si un utilisateur a une permission
     */
    public boolean hasPermission(Utilisateur user, String action) {
        // Cette méthode peut être étendue pour des permissions plus granulaires
        // Pour l'instant, elle se base sur le rôle
        return security.PermissionGuard.hasPermission(user, action);
    }
    
    /**
     * Mettre à jour un utilisateur
     */
    public void update(Utilisateur utilisateur) {
        String query = "UPDATE utilisateur SET nom = ?, username = ?, role = ?, actif = ? WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getUsername());
            ps.setString(3, utilisateur.getRole().name());
            ps.setBoolean(4, utilisateur.isActif());
            ps.setInt(5, utilisateur.getId());
            
            ps.executeUpdate();
            System.out.println("Utilisateur mis à jour avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Changer le mot de passe d'un utilisateur
     */
    public void changePassword(int userId, String newPassword) {
        String query = "UPDATE utilisateur SET password_hash = ? WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, BCryptUtil.hashPassword(newPassword));
            ps.setInt(2, userId);
            
            ps.executeUpdate();
            System.out.println("Mot de passe changé avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors du changement de mot de passe: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
