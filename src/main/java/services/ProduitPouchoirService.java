package services;

import entities.ProduitPouchoir;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitPouchoirService {

    private Connection connection;

    public ProduitPouchoirService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // Créer une association
    public void create(ProduitPouchoir association) throws SQLException {
        String sql = "INSERT INTO produit_pouchoir (produit_id, pouchoir_reference, face) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, association.getProduitId());
            stmt.setString(2, association.getPouchoirReference());
            stmt.setInt(3, association.getFace());
            stmt.executeUpdate();
            System.out.println("Association créée !");
        }
    }

    // Lire toutes les associations d'un produit
    public List<ProduitPouchoir> findByProduitId(int produitId) throws SQLException {
        List<ProduitPouchoir> associations = new ArrayList<>();
        String sql = "SELECT * FROM produit_pouchoir WHERE produit_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, produitId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                associations.add(new ProduitPouchoir(
                        rs.getInt("produit_id"),
                        rs.getString("pouchoir_reference"),
                        rs.getInt("face"),
                        rs.getTimestamp("date_association")
                ));
            }
        }
        return associations;
    }

    // Lire toutes les associations d'un pouchoir
    public List<ProduitPouchoir> findByPouchoirReference(String reference) throws SQLException {
        List<ProduitPouchoir> associations = new ArrayList<>();
        String sql = "SELECT * FROM produit_pouchoir WHERE pouchoir_reference = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reference);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                associations.add(new ProduitPouchoir(
                        rs.getInt("produit_id"),
                        rs.getString("pouchoir_reference"),
                        rs.getInt("face"),
                        rs.getTimestamp("date_association")
                ));
            }
        }
        return associations;
    }

    // Supprimer une association spécifique
    public void delete(int produitId, String pouchoirReference, int face) throws SQLException {
        String sql = "DELETE FROM produit_pouchoir WHERE produit_id = ? AND pouchoir_reference = ? AND face = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, produitId);
            stmt.setString(2, pouchoirReference);
            stmt.setInt(3, face);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Association supprimée !");
            } else {
                System.out.println("Aucune association trouvée");
            }
        }
    }

    // Supprimer toutes les associations d'un produit
    public void deleteByProduitId(int produitId) throws SQLException {
        String sql = "DELETE FROM produit_pouchoir WHERE produit_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, produitId);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " associations supprimées pour le produit " + produitId);
        }
    }

    // Lire toutes les associations
    public List<ProduitPouchoir> readAll() throws SQLException {
        List<ProduitPouchoir> associations = new ArrayList<>();
        String sql = "SELECT * FROM produit_pouchoir ORDER BY produit_id, pouchoir_reference";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                associations.add(new ProduitPouchoir(
                        rs.getInt("produit_id"),
                        rs.getString("pouchoir_reference"),
                        rs.getInt("face"),  // ✅ face est un INT
                        rs.getTimestamp("date_association")
                ));
            }
        }
        return associations;
    }
}