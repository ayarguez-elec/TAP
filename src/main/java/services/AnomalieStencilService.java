package services;

import entities.AnomalieStencil;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnomalieStencilService {

    private Connection connection;

    public AnomalieStencilService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS anomalie_stencil (
                id INT AUTO_INCREMENT PRIMARY KEY,
                stencil VARCHAR(100) NOT NULL,
                ligne VARCHAR(50),
                type_probleme VARCHAR(150),
                degre ENUM('CRITIQUE','MAJEUR','MINEUR') DEFAULT 'MINEUR',
                remarque TEXT,
                resolu BOOLEAN DEFAULT FALSE,
                date_detection TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AnomalieStencil> readAll() {
        List<AnomalieStencil> list = new ArrayList<>();
        String sql = "SELECT * FROM anomalie_stencil ORDER BY date_detection DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AnomalieStencil> search(String keyword, String degre) {
        List<AnomalieStencil> list = new ArrayList<>();
        String sql = "SELECT * FROM anomalie_stencil WHERE 1=1";
        if (keyword != null && !keyword.isBlank()) sql += " AND (stencil LIKE ? OR ligne LIKE ? OR type_probleme LIKE ?)";
        if (degre != null && !degre.isBlank()) sql += " AND degre = ?";
        sql += " ORDER BY date_detection DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            if (keyword != null && !keyword.isBlank()) {
                ps.setString(i++, "%" + keyword + "%");
                ps.setString(i++, "%" + keyword + "%");
                ps.setString(i++, "%" + keyword + "%");
            }
            if (degre != null && !degre.isBlank()) ps.setString(i, degre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void create(AnomalieStencil a) throws SQLException {
        String sql = "INSERT INTO anomalie_stencil (stencil, ligne, type_probleme, degre, remarque, resolu) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, a.getStencil()); ps.setString(2, a.getLigne());
        ps.setString(3, a.getTypeProbleme()); ps.setString(4, a.getDegre());
        ps.setString(5, a.getRemarque()); ps.setBoolean(6, a.isResolu());
        ps.executeUpdate();
    }

    public void update(AnomalieStencil a) throws SQLException {
        String sql = "UPDATE anomalie_stencil SET stencil=?, ligne=?, type_probleme=?, degre=?, remarque=?, resolu=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, a.getStencil()); ps.setString(2, a.getLigne());
        ps.setString(3, a.getTypeProbleme()); ps.setString(4, a.getDegre());
        ps.setString(5, a.getRemarque()); ps.setBoolean(6, a.isResolu());
        ps.setInt(7, a.getId()); ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM anomalie_stencil WHERE id=?");
        ps.setInt(1, id); ps.executeUpdate();
    }

    private AnomalieStencil map(ResultSet rs) throws SQLException {
        AnomalieStencil a = new AnomalieStencil();
        a.setId(rs.getInt("id")); a.setStencil(rs.getString("stencil"));
        a.setLigne(rs.getString("ligne")); a.setTypeProbleme(rs.getString("type_probleme"));
        a.setDegre(rs.getString("degre")); a.setRemarque(rs.getString("remarque"));
        a.setResolu(rs.getBoolean("resolu")); a.setDateDetection(rs.getTimestamp("date_detection"));
        return a;
    }
}