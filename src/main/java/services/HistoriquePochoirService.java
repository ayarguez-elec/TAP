package services;

import entities.HistoriquePochoir;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoriquePochoirService {
    
    private Connection connection;
    
    public HistoriquePochoirService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }
    
    /**
     * Enregistrer une sortie de pochoir
     */
    public int enregistrerSortie(String pouchoirRef, int operateurId, String localisation, String raison) {
        String query = "INSERT INTO historique_pochoir (pouchoir_ref, action, date_sortie, operateur_id, localisation, raison) " +
                      "VALUES (?, 'SORTIE', NOW(), ?, ?, ?)";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pouchoirRef);
            ps.setInt(2, operateurId);
            ps.setString(3, localisation);
            ps.setString(4, raison);
            
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de la sortie: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Enregistrer un retour de pochoir
     */
    public void enregistrerRetour(int historiqueId, String etatRetour, String remarques) {
        String query = "UPDATE historique_pochoir SET action = 'RETOUR', date_retour = NOW(), " +
                      "etat_retour = ?, remarques = ? WHERE id = ?";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, etatRetour);
            ps.setString(2, remarques);
            ps.setInt(3, historiqueId);
            
            ps.executeUpdate();
            System.out.println("Retour enregistré avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement du retour: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtenir l'historique d'un pochoir spécifique
     */
    public List<HistoriquePochoir> getHistoriqueByPouchoir(String ref) {
        List<HistoriquePochoir> historiques = new ArrayList<>();
        String query = "SELECT h.*, u.nom as operateur_nom FROM historique_pochoir h " +
                      "LEFT JOIN utilisateur u ON h.operateur_id = u.id " +
                      "WHERE h.pouchoir_ref = ? ORDER BY h.date_sortie DESC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, ref);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                HistoriquePochoir hist = mapResultSetToHistorique(rs);
                historiques.add(hist);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historiques;
    }
    
    /**
     * Obtenir les pochoirs actuellement en cours (sortis mais pas encore retournés)
     */
    public List<HistoriquePochoir> getPochoirsEnCours() {
        List<HistoriquePochoir> historiques = new ArrayList<>();
        String query = "SELECT h.*, u.nom as operateur_nom FROM historique_pochoir h " +
                      "LEFT JOIN utilisateur u ON h.operateur_id = u.id " +
                      "WHERE h.action = 'SORTIE' AND h.date_retour IS NULL " +
                      "ORDER BY h.date_sortie DESC";
        
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            while (rs.next()) {
                HistoriquePochoir hist = mapResultSetToHistorique(rs);
                historiques.add(hist);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des pochoirs en cours: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historiques;
    }
    
    /**
     * Obtenir l'historique par période
     */
    public List<HistoriquePochoir> getHistoriqueByPeriode(LocalDate debut, LocalDate fin) {
        List<HistoriquePochoir> historiques = new ArrayList<>();
        String query = "SELECT h.*, u.nom as operateur_nom FROM historique_pochoir h " +
                      "LEFT JOIN utilisateur u ON h.operateur_id = u.id " +
                      "WHERE DATE(h.date_sortie) BETWEEN ? AND ? " +
                      "ORDER BY h.date_sortie DESC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                HistoriquePochoir hist = mapResultSetToHistorique(rs);
                historiques.add(hist);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historiques;
    }
    
    /**
     * Obtenir tout l'historique
     */
    public List<HistoriquePochoir> getAllHistorique() {
        List<HistoriquePochoir> historiques = new ArrayList<>();
        String query = "SELECT h.*, u.nom as operateur_nom FROM historique_pochoir h " +
                      "LEFT JOIN utilisateur u ON h.operateur_id = u.id " +
                      "ORDER BY h.date_sortie DESC LIMIT 1000";
        
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            while (rs.next()) {
                HistoriquePochoir hist = mapResultSetToHistorique(rs);
                historiques.add(hist);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historiques;
    }
    
    /**
     * Mapper un ResultSet vers un objet HistoriquePochoir
     */
    private HistoriquePochoir mapResultSetToHistorique(ResultSet rs) throws SQLException {
        HistoriquePochoir hist = new HistoriquePochoir();
        hist.setId(rs.getInt("id"));
        hist.setPouchoirRef(rs.getString("pouchoir_ref"));
        hist.setAction(HistoriquePochoir.ActionPochoir.valueOf(rs.getString("action")));
        hist.setDateSortie(rs.getTimestamp("date_sortie"));
        hist.setDateRetour(rs.getTimestamp("date_retour"));
        hist.setOperateurId(rs.getInt("operateur_id"));
        hist.setOperateurNom(rs.getString("operateur_nom"));
        hist.setLocalisation(rs.getString("localisation"));
        hist.setRaison(rs.getString("raison"));
        hist.setEtatRetour(rs.getString("etat_retour"));
        hist.setRemarques(rs.getString("remarques"));
        
        return hist;
    }
}
