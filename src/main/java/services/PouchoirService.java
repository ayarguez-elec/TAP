package services;

import entities.Pouchoir;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PouchoirService implements ICrud<Pouchoir> {

    private Connection connection;

    public PouchoirService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void create(Pouchoir pouchoir) throws SQLException {
        String sql = "INSERT INTO pouchoir (refPouchoir, programme, racle, emplacement, statut, face, " +
                "quantite_totale, nb_cartes_par_flan, stock_actuel, epaisseur, support, code_lacroix, " +
                "code_recu, date_envoi, date_reception) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pouchoir.getRefPouchoir());
            stmt.setString(2, pouchoir.getProgramme());
            stmt.setString(3, pouchoir.getRacle());
            stmt.setInt(4, pouchoir.getEmplacement());
            stmt.setString(5, pouchoir.getStatut());
            stmt.setInt(6, pouchoir.getFace());
            stmt.setInt(7, pouchoir.getQuantiteTotale());
            stmt.setInt(8, pouchoir.getNbCartesParFlan());
            stmt.setInt(9, pouchoir.getStockActuel());
            stmt.setString(10, pouchoir.getEpaisseur());
            stmt.setString(11, pouchoir.getSupport());
            stmt.setBoolean(12, pouchoir.isCodeLacroix());
            stmt.setString(13, pouchoir.getCodeRecu());
            stmt.setString(14, pouchoir.getDateEnvoi());
            stmt.setString(15, pouchoir.getDateReception());
            stmt.executeUpdate();
            System.out.println("✅ Pouchoir créé avec succès ! Réf: " + pouchoir.getRefPouchoir());
        }
    }

    @Override
    public Pouchoir read(int id) throws SQLException {
        throw new UnsupportedOperationException("Utilisez readByReference(String)");
    }

    public List<Pouchoir> readAll() throws SQLException {
        List<Pouchoir> pouchoirs = new ArrayList<>();
        String sql = "SELECT * FROM pouchoir ORDER BY refPouchoir";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                pouchoirs.add(new Pouchoir(
                        rs.getString("refPouchoir"),
                        rs.getString("programme"),
                        rs.getString("racle"),
                        rs.getInt("emplacement"),
                        rs.getString("statut"),
                        rs.getInt("face"),
                        rs.getInt("quantite_totale"),
                        rs.getInt("nb_cartes_par_flan"),
                        rs.getInt("stock_actuel"),
                        rs.getString("epaisseur"),
                        rs.getString("support"),
                        rs.getBoolean("code_lacroix"),
                        rs.getString("code_recu"),
                        rs.getString("date_envoi"),
                        rs.getString("date_reception"),
                        rs.getTimestamp("date_creation"),
                        rs.getTimestamp("date_modification")
                ));
            }
        }
        return pouchoirs;
    }

    public Pouchoir readByReference(String ref) throws SQLException {
        String sql = "SELECT * FROM pouchoir WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ref);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Pouchoir(
                        rs.getString("refPouchoir"),
                        rs.getString("programme"),
                        rs.getString("racle"),
                        rs.getInt("emplacement"),
                        rs.getString("statut"),
                        rs.getInt("face"),
                        rs.getInt("quantite_totale"),
                        rs.getInt("nb_cartes_par_flan"),
                        rs.getInt("stock_actuel"),
                        rs.getString("epaisseur"),
                        rs.getString("support"),
                        rs.getBoolean("code_lacroix"),
                        rs.getString("code_recu"),
                        rs.getString("date_envoi"),
                        rs.getString("date_reception"),
                        rs.getTimestamp("date_creation"),
                        rs.getTimestamp("date_modification")
                );
            }
        }
        return null;
    }

    public List<Pouchoir> findByStatut(String statut) throws SQLException {
        List<Pouchoir> pouchoirs = new ArrayList<>();
        String sql = "SELECT * FROM pouchoir WHERE statut = ? ORDER BY refPouchoir";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pouchoirs.add(new Pouchoir(
                        rs.getString("refPouchoir"),
                        rs.getString("programme"),
                        rs.getString("racle"),
                        rs.getInt("emplacement"),
                        rs.getString("statut"),
                        rs.getInt("face"),
                        rs.getInt("quantite_totale"),
                        rs.getInt("nb_cartes_par_flan"),
                        rs.getInt("stock_actuel"),
                        rs.getString("epaisseur"),
                        rs.getString("support"),
                        rs.getBoolean("code_lacroix"),
                        rs.getString("code_recu"),
                        rs.getString("date_envoi"),
                        rs.getString("date_reception"),
                        rs.getTimestamp("date_creation"),
                        rs.getTimestamp("date_modification")
                ));
            }
        }
        return pouchoirs;
    }

    public List<Pouchoir> findByEmplacement(int emplacement) throws SQLException {
        List<Pouchoir> pouchoirs = new ArrayList<>();
        String sql = "SELECT * FROM pouchoir WHERE emplacement = ? ORDER BY refPouchoir";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, emplacement);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pouchoirs.add(new Pouchoir(
                        rs.getString("refPouchoir"),
                        rs.getString("programme"),
                        rs.getString("racle"),
                        rs.getInt("emplacement"),
                        rs.getString("statut"),
                        rs.getInt("face"),
                        rs.getInt("quantite_totale"),
                        rs.getInt("nb_cartes_par_flan"),
                        rs.getInt("stock_actuel"),
                        rs.getString("epaisseur"),
                        rs.getString("support"),
                        rs.getBoolean("code_lacroix"),
                        rs.getString("code_recu"),
                        rs.getString("date_envoi"),
                        rs.getString("date_reception"),
                        rs.getTimestamp("date_creation"),
                        rs.getTimestamp("date_modification")
                ));
            }
        }
        return pouchoirs;
    }

    public List<Pouchoir> findLowStock() throws SQLException {
        List<Pouchoir> lowStock = new ArrayList<>();
        String sql = "SELECT * FROM pouchoir WHERE stock_actuel < 100 AND statut = 'disponible' ORDER BY stock_actuel";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                lowStock.add(new Pouchoir(
                        rs.getString("refPouchoir"),
                        rs.getString("programme"),
                        rs.getString("racle"),
                        rs.getInt("emplacement"),
                        rs.getString("statut"),
                        rs.getInt("face"),
                        rs.getInt("quantite_totale"),
                        rs.getInt("nb_cartes_par_flan"),
                        rs.getInt("stock_actuel"),
                        rs.getString("epaisseur"),
                        rs.getString("support"),
                        rs.getBoolean("code_lacroix"),
                        rs.getString("code_recu"),
                        rs.getString("date_envoi"),
                        rs.getString("date_reception"),
                        rs.getTimestamp("date_creation"),
                        rs.getTimestamp("date_modification")
                ));
            }
        }
        return lowStock;
    }

    @Override
    public void update(Pouchoir pouchoir) throws SQLException {
        String sql = "UPDATE pouchoir SET programme = ?, racle = ?, emplacement = ?, statut = ?, face = ?, " +
                "quantite_totale = ?, nb_cartes_par_flan = ?, stock_actuel = ?, epaisseur = ?, support = ?, " +
                "code_lacroix = ?, code_recu = ?, date_envoi = ?, date_reception = ?, date_modification = CURRENT_TIMESTAMP " +
                "WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pouchoir.getProgramme());
            stmt.setString(2, pouchoir.getRacle());
            stmt.setInt(3, pouchoir.getEmplacement());
            stmt.setString(4, pouchoir.getStatut());
            stmt.setInt(5, pouchoir.getFace());
            stmt.setInt(6, pouchoir.getQuantiteTotale());
            stmt.setInt(7, pouchoir.getNbCartesParFlan());
            stmt.setInt(8, pouchoir.getStockActuel());
            stmt.setString(9, pouchoir.getEpaisseur());
            stmt.setString(10, pouchoir.getSupport());
            stmt.setBoolean(11, pouchoir.isCodeLacroix());
            stmt.setString(12, pouchoir.getCodeRecu());
            stmt.setString(13, pouchoir.getDateEnvoi());
            stmt.setString(14, pouchoir.getDateReception());
            stmt.setString(15, pouchoir.getRefPouchoir());
            stmt.executeUpdate();
            System.out.println("✅ Pouchoir mis à jour !");
        }
    }

    // ===== NOUVELLES MÉTHODES POUR LE WORKFLOW =====

    /**
     * Marque les pochoirs comme "En attente" (email envoyé)
     */
    public void marquerEnAttente(List<Pouchoir> pouchoirs, String dateEnvoi) throws SQLException {
        String sql = "UPDATE pouchoir SET date_envoi = ?, code_lacroix = false, date_modification = CURRENT_TIMESTAMP WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Pouchoir p : pouchoirs) {
                stmt.setString(1, dateEnvoi);
                stmt.setString(2, p.getRefPouchoir());
                stmt.addBatch();
            }
            stmt.executeBatch();
            System.out.println("✅ " + pouchoirs.size() + " pochoir(s) marqués comme 'En attente'");
        }
    }

    /**
     * Marque les pochoirs comme "Codés" avec le code reçu
     */
    public void marquerCode(List<Pouchoir> pouchoirs, String code, String dateReception) throws SQLException {
        String sql = "UPDATE pouchoir SET code_lacroix = true, code_recu = ?, date_reception = ?, date_modification = CURRENT_TIMESTAMP WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Pouchoir p : pouchoirs) {
                stmt.setString(1, code);
                stmt.setString(2, dateReception);
                stmt.setString(3, p.getRefPouchoir());
                stmt.addBatch();
            }
            stmt.executeBatch();
            System.out.println("✅ " + pouchoirs.size() + " pochoir(s) marqués comme 'Codés'");
        }
    }

    /**
     * Récupère les pochoirs par statut de workflow
     */
    public List<Pouchoir> getPouchoirsByWorkflowStatus(String status) throws SQLException {
        List<Pouchoir> all = readAll();
        return all.stream()
                .filter(p -> {
                    if ("SANS_CODE".equals(status)) {
                        return !p.isCodeLacroix() && (p.getDateEnvoi() == null || p.getDateEnvoi().isEmpty());
                    } else if ("EN_ATTENTE".equals(status)) {
                        return !p.isCodeLacroix() && p.getDateEnvoi() != null && !p.getDateEnvoi().isEmpty();
                    } else if ("CODE".equals(status)) {
                        return p.isCodeLacroix();
                    }
                    return false;
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Récupère tous les pochoirs non codés (SANS_CODE + EN_ATTENTE)
     */
    public List<Pouchoir> getPouchoirsNonCodes() throws SQLException {
        List<Pouchoir> all = readAll();
        return all.stream()
                .filter(p -> !p.isCodeLacroix())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Récupère les pochoirs en attente de code
     */
    public List<Pouchoir> getPouchoirsEnAttente() throws SQLException {
        List<Pouchoir> all = readAll();
        return all.stream()
                .filter(p -> !p.isCodeLacroix() && p.getDateEnvoi() != null && !p.getDateEnvoi().isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Récupère les pochoirs codés
     */
    public List<Pouchoir> getPouchoirsCodes() throws SQLException {
        List<Pouchoir> all = readAll();
        return all.stream()
                .filter(Pouchoir::isCodeLacroix)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public void delete(int id) throws SQLException {
        throw new UnsupportedOperationException("Utilisez deleteByReference(String)");
    }

    public void deleteByReference(String ref) throws SQLException {
        String sql = "DELETE FROM pouchoir WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ref);
            stmt.executeUpdate();
            System.out.println("✅ Pouchoir supprimé !");
        }
    }

    public void diminuerStock(String refPouchoir) throws SQLException {
        String sql = "UPDATE pouchoir SET stock_actuel = ? WHERE refPouchoir = ?";
        Pouchoir p = readByReference(refPouchoir);
        if (p != null && p.hasEnoughStock()) {
            p.diminuerStock();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, p.getStockActuel());
                stmt.setString(2, refPouchoir);
                stmt.executeUpdate();
                System.out.println("✅ Stock diminué pour " + refPouchoir + " - Nouveau stock: " + p.getStockActuel());
            }
        } else {
            System.out.println("⚠️ Stock insuffisant pour " + refPouchoir);
        }
    }

    public void reinitialiserStock(String refPouchoir) throws SQLException {
        String sql = "UPDATE pouchoir SET stock_actuel = quantite_totale - (quantite_totale / nb_cartes_par_flan) WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, refPouchoir);
            stmt.executeUpdate();
            System.out.println("✅ Stock réinitialisé pour " + refPouchoir);
        }
    }

    public void augmenterStock(String refPouchoir, int quantiteAjoutee) throws SQLException {
        String sql = "UPDATE pouchoir SET quantite_totale = quantite_totale + ?, stock_actuel = stock_actuel + ? WHERE refPouchoir = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantiteAjoutee);
            stmt.setInt(2, quantiteAjoutee);
            stmt.setString(3, refPouchoir);
            stmt.executeUpdate();
            System.out.println("✅ Stock augmenté pour " + refPouchoir + " + " + quantiteAjoutee + " cartes");
        }
    }

    public boolean verifierStock(String refPouchoir) throws SQLException {
        Pouchoir p = readByReference(refPouchoir);
        return p != null && p.hasEnoughStock();
    }

    public int getStockActuel(String refPouchoir) throws SQLException {
        Pouchoir p = readByReference(refPouchoir);
        return p != null ? p.getStockActuel() : 0;
    }
}