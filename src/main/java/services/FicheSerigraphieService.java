package services;

import entities.FicheSerigraphie;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table fiche_serigraphie.
 *
 * SQL de création de la table (à exécuter une fois dans MySQL) :
 * CREATE TABLE IF NOT EXISTS fiche_serigraphie (
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   idx VARCHAR(120) NOT NULL,
 *   numero_fiche INT DEFAULT 0,
 *   client VARCHAR(100),
 *   produit VARCHAR(100),
 *   face VARCHAR(10),
 *   numero_programme VARCHAR(60),
 *   numero_pcb VARCHAR(60),
 *   fournisseur_creme VARCHAR(80),
 *   designation_creme VARCHAR(80),
 *   ref_lacroix_creme VARCHAR(60),
 *   code_barre VARCHAR(60),
 *   matiere_ecran VARCHAR(80),
 *   numero_ecran VARCHAR(60),
 *   epaisseur VARCHAR(20),
 *   a_date VARCHAR(20), a_machine VARCHAR(60), a_pression_avant VARCHAR(10),
 *   a_pression_arriere VARCHAR(10), a_support VARCHAR(60), a_nombre VARCHAR(10),
 *   a_racle VARCHAR(20), a_info TEXT, a_visa VARCHAR(20),
 *   b_date VARCHAR(20), b_machine VARCHAR(60), b_pression_avant VARCHAR(10),
 *   b_pression_arriere VARCHAR(10), b_support VARCHAR(60), b_nombre VARCHAR(10),
 *   b_racle VARCHAR(20), b_info TEXT, b_nature TEXT, b_visa VARCHAR(20),
 *   c_date VARCHAR(20), c_machine VARCHAR(60), c_pression_avant VARCHAR(10),
 *   c_pression_arriere VARCHAR(10), c_support VARCHAR(60), c_nombre VARCHAR(10),
 *   c_racle VARCHAR(20), c_info TEXT, c_nature TEXT, c_visa VARCHAR(20),
 *   d_date VARCHAR(20), d_machine VARCHAR(60), d_pression_avant VARCHAR(10),
 *   d_pression_arriere VARCHAR(10), d_support VARCHAR(60), d_nombre VARCHAR(10),
 *   d_racle VARCHAR(20), d_info TEXT, d_nature TEXT, d_visa VARCHAR(20),
 *   date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
 * );
 */
public class FicheSerigraphieService {

    private Connection connection;

    public FicheSerigraphieService() {
        connection = MyDataBase.getInstance().getConnection();
        creerTableSiAbsente();
    }

    /** Crée la table si elle n'existe pas encore */
    private void creerTableSiAbsente() {
        String sql = """
            CREATE TABLE IF NOT EXISTS fiche_serigraphie (
              id INT AUTO_INCREMENT PRIMARY KEY,
              idx VARCHAR(150) NOT NULL,
              numero_fiche INT DEFAULT 0,
              client VARCHAR(100), produit VARCHAR(100), face VARCHAR(10),
              numero_programme VARCHAR(60), numero_pcb VARCHAR(60),
              fournisseur_creme VARCHAR(100), designation_creme VARCHAR(100),
              ref_lacroix_creme VARCHAR(80), code_barre VARCHAR(80),
              matiere_ecran VARCHAR(100), numero_ecran VARCHAR(80), epaisseur VARCHAR(30),
              a_date VARCHAR(20), a_machine VARCHAR(100), a_pression_avant VARCHAR(20),
              a_pression_arriere VARCHAR(20), a_support VARCHAR(100), a_nombre VARCHAR(20),
              a_racle VARCHAR(30), a_info TEXT, a_visa VARCHAR(30),
              b_date VARCHAR(20), b_machine VARCHAR(100), b_pression_avant VARCHAR(20),
              b_pression_arriere VARCHAR(20), b_support VARCHAR(100), b_nombre VARCHAR(20),
              b_racle VARCHAR(30), b_info TEXT, b_nature TEXT, b_visa VARCHAR(30),
              c_date VARCHAR(20), c_machine VARCHAR(100), c_pression_avant VARCHAR(20),
              c_pression_arriere VARCHAR(20), c_support VARCHAR(100), c_nombre VARCHAR(20),
              c_racle VARCHAR(30), c_info TEXT, c_nature TEXT, c_visa VARCHAR(30),
              d_date VARCHAR(20), d_machine VARCHAR(100), d_pression_avant VARCHAR(20),
              d_pression_arriere VARCHAR(20), d_support VARCHAR(100), d_nombre VARCHAR(20),
              d_racle VARCHAR(30), d_info TEXT, d_nature TEXT, d_visa VARCHAR(30),
              date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Impossible de créer la table fiche_serigraphie : " + e.getMessage());
        }
    }

    // =========================================================
    // CREATE
    // =========================================================

    public void create(FicheSerigraphie f) throws SQLException {
        String sql = """
            INSERT INTO fiche_serigraphie (
              idx, numero_fiche, client, produit, face, numero_programme, numero_pcb,
              fournisseur_creme, designation_creme, ref_lacroix_creme, code_barre,
              matiere_ecran, numero_ecran, epaisseur,
              a_date, a_machine, a_pression_avant, a_pression_arriere, a_support, a_nombre, a_racle, a_info, a_visa,
              b_date, b_machine, b_pression_avant, b_pression_arriere, b_support, b_nombre, b_racle, b_info, b_nature, b_visa,
              c_date, c_machine, c_pression_avant, c_pression_arriere, c_support, c_nombre, c_racle, c_info, c_nature, c_visa,
              d_date, d_machine, d_pression_avant, d_pression_arriere, d_support, d_nombre, d_racle, d_info, d_nature, d_visa
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, f);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) f.setId(rs.getInt(1));
        }
    }

    // =========================================================
    // READ ALL
    // =========================================================

    public List<FicheSerigraphie> readAll() throws SQLException {
        List<FicheSerigraphie> list = new ArrayList<>();
        String sql = "SELECT * FROM fiche_serigraphie ORDER BY date_modification DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // =========================================================
    // READ BY ID
    // =========================================================

    public FicheSerigraphie readById(int id) throws SQLException {
        String sql = "SELECT * FROM fiche_serigraphie WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(FicheSerigraphie f) throws SQLException {
        String sql = """
            UPDATE fiche_serigraphie SET
              idx=?, numero_fiche=?, client=?, produit=?, face=?, numero_programme=?, numero_pcb=?,
              fournisseur_creme=?, designation_creme=?, ref_lacroix_creme=?, code_barre=?,
              matiere_ecran=?, numero_ecran=?, epaisseur=?,
              a_date=?, a_machine=?, a_pression_avant=?, a_pression_arriere=?, a_support=?, a_nombre=?, a_racle=?, a_info=?, a_visa=?,
              b_date=?, b_machine=?, b_pression_avant=?, b_pression_arriere=?, b_support=?, b_nombre=?, b_racle=?, b_info=?, b_nature=?, b_visa=?,
              c_date=?, c_machine=?, c_pression_avant=?, c_pression_arriere=?, c_support=?, c_nombre=?, c_racle=?, c_info=?, c_nature=?, c_visa=?,
              d_date=?, d_machine=?, d_pression_avant=?, d_pression_arriere=?, d_support=?, d_nombre=?, d_racle=?, d_info=?, d_nature=?, d_visa=?
            WHERE id=?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, f);
            ps.setInt(54, f.getId());
            ps.executeUpdate();
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM fiche_serigraphie WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // GET NEXT NUMERO FICHE
    // =========================================================

    /** Retourne le prochain numéro de fiche disponible (max + 1) */
    public int getNextNumeroFiche() throws SQLException {
        String sql = "SELECT COALESCE(MAX(numero_fiche), 0) + 1 FROM fiche_serigraphie";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        }
    }

    // =========================================================
    // SEARCH
    // =========================================================

    public List<FicheSerigraphie> search(String query) throws SQLException {
        List<FicheSerigraphie> list = new ArrayList<>();
        String q = "%" + query + "%";
        String sql = """
            SELECT * FROM fiche_serigraphie
            WHERE client LIKE ? OR produit LIKE ? OR idx LIKE ? OR numero_programme LIKE ?
            ORDER BY date_modification DESC
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q);
            ps.setString(3, q); ps.setString(4, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /** Mappe un ResultSet sur un objet FicheSerigraphie */
    private FicheSerigraphie map(ResultSet rs) throws SQLException {
        FicheSerigraphie f = new FicheSerigraphie();
        f.setId(rs.getInt("id"));
        f.setIndex(rs.getString("idx"));
        f.setNumeroFiche(rs.getInt("numero_fiche"));
        f.setClient(rs.getString("client"));
        f.setProduit(rs.getString("produit"));
        f.setFace(rs.getString("face"));
        f.setNumeroProgramme(rs.getString("numero_programme"));
        f.setNumeroPcb(rs.getString("numero_pcb"));
        f.setFournisseurCreme(rs.getString("fournisseur_creme"));
        f.setDesignationCreme(rs.getString("designation_creme"));
        f.setRefLacroixCreme(rs.getString("ref_lacroix_creme"));
        f.setCodeBarre(rs.getString("code_barre"));
        f.setMatiereEcran(rs.getString("matiere_ecran"));
        f.setNumeroEcran(rs.getString("numero_ecran"));
        f.setEpaisseur(rs.getString("epaisseur"));
        f.setADate(rs.getString("a_date")); f.setAMachine(rs.getString("a_machine"));
        f.setAPressionAvant(rs.getString("a_pression_avant")); f.setAPressionArriere(rs.getString("a_pression_arriere"));
        f.setASupport(rs.getString("a_support")); f.setANombre(rs.getString("a_nombre"));
        f.setARacle(rs.getString("a_racle")); f.setAInfoTechniques(rs.getString("a_info")); f.setAVisa(rs.getString("a_visa"));
        f.setBDate(rs.getString("b_date")); f.setBMachine(rs.getString("b_machine"));
        f.setBPressionAvant(rs.getString("b_pression_avant")); f.setBPressionArriere(rs.getString("b_pression_arriere"));
        f.setBSupport(rs.getString("b_support")); f.setBNombre(rs.getString("b_nombre"));
        f.setBRacle(rs.getString("b_racle")); f.setBInfoTechniques(rs.getString("b_info"));
        f.setBNatureEvolution(rs.getString("b_nature")); f.setBVisa(rs.getString("b_visa"));
        f.setCDate(rs.getString("c_date")); f.setCMachine(rs.getString("c_machine"));
        f.setCPressionAvant(rs.getString("c_pression_avant")); f.setCPressionArriere(rs.getString("c_pression_arriere"));
        f.setCSupport(rs.getString("c_support")); f.setCNombre(rs.getString("c_nombre"));
        f.setCRacle(rs.getString("c_racle")); f.setCInfoTechniques(rs.getString("c_info"));
        f.setCNatureEvolution(rs.getString("c_nature")); f.setCVisa(rs.getString("c_visa"));
        f.setDDate(rs.getString("d_date")); f.setDMachine(rs.getString("d_machine"));
        f.setDPressionAvant(rs.getString("d_pression_avant")); f.setDPressionArriere(rs.getString("d_pression_arriere"));
        f.setDSupport(rs.getString("d_support")); f.setDNombre(rs.getString("d_nombre"));
        f.setDRacle(rs.getString("d_racle")); f.setDInfoTechniques(rs.getString("d_info"));
        f.setDNatureEvolution(rs.getString("d_nature")); f.setDVisa(rs.getString("d_visa"));
        return f;
    }

    /** Applique les 53 paramètres sur un PreparedStatement (INSERT ou UPDATE sans id) */
    private void setParams(PreparedStatement ps, FicheSerigraphie f) throws SQLException {
        ps.setString(1,  f.getIndex());
        ps.setInt(2,     f.getNumeroFiche());
        ps.setString(3,  f.getClient());
        ps.setString(4,  f.getProduit());
        ps.setString(5,  f.getFace());
        ps.setString(6,  f.getNumeroProgramme());
        ps.setString(7,  f.getNumeroPcb());
        ps.setString(8,  f.getFournisseurCreme());
        ps.setString(9,  f.getDesignationCreme());
        ps.setString(10, f.getRefLacroixCreme());
        ps.setString(11, f.getCodeBarre());
        ps.setString(12, f.getMatiereEcran());
        ps.setString(13, f.getNumeroEcran());
        ps.setString(14, f.getEpaisseur());
        ps.setString(15, f.getADate());    ps.setString(16, f.getAMachine());
        ps.setString(17, f.getAPressionAvant()); ps.setString(18, f.getAPressionArriere());
        ps.setString(19, f.getASupport()); ps.setString(20, f.getANombre());
        ps.setString(21, f.getARacle());   ps.setString(22, f.getAInfoTechniques()); ps.setString(23, f.getAVisa());
        ps.setString(24, f.getBDate());    ps.setString(25, f.getBMachine());
        ps.setString(26, f.getBPressionAvant()); ps.setString(27, f.getBPressionArriere());
        ps.setString(28, f.getBSupport()); ps.setString(29, f.getBNombre());
        ps.setString(30, f.getBRacle());   ps.setString(31, f.getBInfoTechniques());
        ps.setString(32, f.getBNatureEvolution()); ps.setString(33, f.getBVisa());
        ps.setString(34, f.getCDate());    ps.setString(35, f.getCMachine());
        ps.setString(36, f.getCPressionAvant()); ps.setString(37, f.getCPressionArriere());
        ps.setString(38, f.getCSupport()); ps.setString(39, f.getCNombre());
        ps.setString(40, f.getCRacle());   ps.setString(41, f.getCInfoTechniques());
        ps.setString(42, f.getCNatureEvolution()); ps.setString(43, f.getCVisa());
        ps.setString(44, f.getDDate());    ps.setString(45, f.getDMachine());
        ps.setString(46, f.getDPressionAvant()); ps.setString(47, f.getDPressionArriere());
        ps.setString(48, f.getDSupport()); ps.setString(49, f.getDNombre());
        ps.setString(50, f.getDRacle());   ps.setString(51, f.getDInfoTechniques());
        ps.setString(52, f.getDNatureEvolution()); ps.setString(53, f.getDVisa());
    }
}
