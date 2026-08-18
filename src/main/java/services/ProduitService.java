package services;

import entities.Pouchoir;
import entities.Produit;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements ICrud<Produit> {

    private Connection connection;

    public ProduitService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void create(Produit produit) throws SQLException {
        String sql = "INSERT INTO produit (nomProduit, client_id, ecran, code_produit, description, creme_a_braser, programme) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produit.getNomProduit());
            stmt.setInt(2, produit.getClientId());
            stmt.setString(3, produit.getEcran());
            stmt.setString(4, produit.getCodeProduit());
            stmt.setString(5, produit.getDescription());
            stmt.setString(6, produit.getCremeABraser());
            stmt.setString(7, produit.getProgramme());  // ← AJOUTÉ
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                produit.setId(rs.getInt(1));
            }
            System.out.println("✅ Produit créé avec succès ! ID: " + produit.getId());
        }
    }

    @Override
    public Produit read(int id) throws SQLException {
        String sql = "SELECT * FROM produit WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id"),
                        rs.getString("nomProduit"),
                        rs.getInt("client_id"),
                        rs.getString("ecran"),
                        rs.getString("code_produit"),
                        rs.getString("description"),
                        rs.getString("creme_a_braser"),
                        rs.getString("programme"),  // ← AJOUTÉ
                        rs.getTimestamp("dateCreation")
                );
            }
        }
        return null;
    }

    @Override
    public List<Produit> readAll() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit ORDER BY id";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                produits.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nomProduit"),
                        rs.getInt("client_id"),
                        rs.getString("ecran"),
                        rs.getString("code_produit"),
                        rs.getString("description"),
                        rs.getString("creme_a_braser"),
                        rs.getString("programme"),  // ← AJOUTÉ
                        rs.getTimestamp("dateCreation")
                ));
            }
        }
        return produits;
    }

    @Override
    public void update(Produit produit) throws SQLException {
        String sql = "UPDATE produit SET nomProduit = ?, client_id = ?, ecran = ?, code_produit = ?, description = ?, creme_a_braser = ?, programme = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produit.getNomProduit());
            stmt.setInt(2, produit.getClientId());
            stmt.setString(3, produit.getEcran());
            stmt.setString(4, produit.getCodeProduit());
            stmt.setString(5, produit.getDescription());
            stmt.setString(6, produit.getCremeABraser());
            stmt.setString(7, produit.getProgramme());  // ← AJOUTÉ
            stmt.setInt(8, produit.getId());
            stmt.executeUpdate();
            System.out.println("✅ Produit mis à jour !");
        }
    }
    public Produit findByPouchoirProgramme(String programme) throws SQLException {
        String sql = "SELECT p.* FROM produit p " +
                "INNER JOIN produit_pouchoir pp ON p.id = pp.produit_id " +
                "INNER JOIN pouchoir po ON pp.pouchoir_reference = po.refPouchoir " +
                "WHERE po.programme = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, programme);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id"),
                        rs.getString("nomProduit"),
                        rs.getInt("client_id"),
                        rs.getString("ecran"),
                        rs.getString("code_produit"),
                        rs.getString("description"),
                        rs.getString("creme_a_braser"),
                        rs.getString("programme"),
                        rs.getTimestamp("dateCreation")
                );
            }
        }
        return null;
    }
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produit WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✅ Produit supprimé !");
        }
    }

    public Produit findByNom(String nom) throws SQLException {
        String sql = "SELECT * FROM produit WHERE nomProduit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id"),
                        rs.getString("nomProduit"),
                        rs.getInt("client_id"),
                        rs.getString("ecran"),
                        rs.getString("code_produit"),
                        rs.getString("description"),
                        rs.getString("creme_a_braser"),
                        rs.getString("programme"),  // ← AJOUTÉ
                        rs.getTimestamp("dateCreation")
                );
            }
        }
        return null;
    }

    // ✅ NOUVELLE MÉTHODE : Chercher un produit par programme
    public Produit findByProgramme(String programme) throws SQLException {
        String sql = "SELECT * FROM produit WHERE programme = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, programme);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id"),
                        rs.getString("nomProduit"),
                        rs.getInt("client_id"),
                        rs.getString("ecran"),
                        rs.getString("code_produit"),
                        rs.getString("description"),
                        rs.getString("creme_a_braser"),
                        rs.getString("programme"),
                        rs.getTimestamp("dateCreation")
                );
            }
        }
        return null;
    }

    public List<String> getEcransDisponibles() throws SQLException {
        PouchoirService pouchoirService = new PouchoirService();
        List<Pouchoir> pouchoirs = pouchoirService.readAll();
        return pouchoirs.stream()
                .filter(p -> "disponible".equals(p.getStatut()))
                .map(Pouchoir::getRefPouchoir)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
}