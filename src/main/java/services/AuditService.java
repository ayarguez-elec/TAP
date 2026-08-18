package services;

import entities.AuditLog;
import security.SessionManager;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditService {
    
    private Connection connection;
    
    public AuditService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }
    
    /**
     * Enregistrer une action dans l'audit log
     */
    public void log(int utilisateurId, String action, String table, String recordId, String details) {
        String query = "INSERT INTO audit_log (utilisateur_id, action, table_affectee, enregistrement_id, details) VALUES (?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, utilisateurId);
            ps.setString(2, action);
            ps.setString(3, table);
            ps.setString(4, recordId);
            ps.setString(5, details);
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Logger une action de l'utilisateur courant
     */
    public void logCurrentUser(String action, String table, String recordId, String details) {
        if (SessionManager.getInstance().isAuthenticated()) {
            int userId = SessionManager.getInstance().getUtilisateur().getId();
            log(userId, action, table, recordId, details);
        }
    }
    
    /**
     * Obtenir l'historique d'audit par utilisateur
     */
    public List<AuditLog> getAuditByUser(int userId) {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT a.*, u.nom as utilisateur_nom FROM audit_log a " +
                      "LEFT JOIN utilisateur u ON a.utilisateur_id = u.id " +
                      "WHERE a.utilisateur_id = ? ORDER BY a.date_action DESC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUtilisateurId(rs.getInt("utilisateur_id"));
                log.setUtilisateurNom(rs.getString("utilisateur_nom"));
                log.setAction(rs.getString("action"));
                log.setTableAffectee(rs.getString("table_affectee"));
                log.setEnregistrementId(rs.getString("enregistrement_id"));
                log.setDetails(rs.getString("details"));
                log.setDateAction(rs.getTimestamp("date_action"));
                
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des audits: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Obtenir l'historique d'audit par table
     */
    public List<AuditLog> getAuditByTable(String table) {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT a.*, u.nom as utilisateur_nom FROM audit_log a " +
                      "LEFT JOIN utilisateur u ON a.utilisateur_id = u.id " +
                      "WHERE a.table_affectee = ? ORDER BY a.date_action DESC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, table);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUtilisateurId(rs.getInt("utilisateur_id"));
                log.setUtilisateurNom(rs.getString("utilisateur_nom"));
                log.setAction(rs.getString("action"));
                log.setTableAffectee(rs.getString("table_affectee"));
                log.setEnregistrementId(rs.getString("enregistrement_id"));
                log.setDetails(rs.getString("details"));
                log.setDateAction(rs.getTimestamp("date_action"));
                
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des audits: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Obtenir l'historique d'audit par période
     */
    public List<AuditLog> getAuditByPeriode(LocalDate debut, LocalDate fin) {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT a.*, u.nom as utilisateur_nom FROM audit_log a " +
                      "LEFT JOIN utilisateur u ON a.utilisateur_id = u.id " +
                      "WHERE DATE(a.date_action) BETWEEN ? AND ? ORDER BY a.date_action DESC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUtilisateurId(rs.getInt("utilisateur_id"));
                log.setUtilisateurNom(rs.getString("utilisateur_nom"));
                log.setAction(rs.getString("action"));
                log.setTableAffectee(rs.getString("table_affectee"));
                log.setEnregistrementId(rs.getString("enregistrement_id"));
                log.setDetails(rs.getString("details"));
                log.setDateAction(rs.getTimestamp("date_action"));
                
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des audits: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Obtenir tous les logs d'audit
     */
    public List<AuditLog> getAllAudits() {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT a.*, u.nom as utilisateur_nom FROM audit_log a " +
                      "LEFT JOIN utilisateur u ON a.utilisateur_id = u.id " +
                      "ORDER BY a.date_action DESC LIMIT 1000";
        
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUtilisateurId(rs.getInt("utilisateur_id"));
                log.setUtilisateurNom(rs.getString("utilisateur_nom"));
                log.setAction(rs.getString("action"));
                log.setTableAffectee(rs.getString("table_affectee"));
                log.setEnregistrementId(rs.getString("enregistrement_id"));
                log.setDetails(rs.getString("details"));
                log.setDateAction(rs.getTimestamp("date_action"));
                
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des audits: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
}
