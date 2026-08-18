package entities;

import java.sql.Timestamp;

public class Utilisateur {
    private int id;
    private String nom;
    private String username;
    private String passwordHash;
    private Role role;
    private boolean actif;
    private Timestamp dateCreation;
    
    public enum Role {
        OPERATEUR, TECHNICIEN, INGENIEUR, ADMIN, PRODUCTION
    }
    
    // Constructeurs
    public Utilisateur() {
    }
    
    public Utilisateur(int id, String nom, String username, String passwordHash, Role role, boolean actif, Timestamp dateCreation) {
        this.id = id;
        this.nom = nom;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.actif = actif;
        this.dateCreation = dateCreation;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    public Timestamp getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    @Override
    public String toString() {
        return nom + " (" + role + ")";
    }
}
