package services;

import entities.PlanningCms;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanningCmsService {

    private final Connection connection;

    public PlanningCmsService() {
        this.connection = MyDataBase.getInstance().getConnection();
        createTableIfNeeded();
    }

    private void createTableIfNeeded() {
        String sql = """
            CREATE TABLE IF NOT EXISTS planning_cms (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cms VARCHAR(10) NOT NULL,
                semaine INT NOT NULL,
                annee INT NOT NULL,
                article VARCHAR(100),
                ordre VARCHAR(50),
                st_util VARCHAR(50),
                ind VARCHAR(10),
                qte DOUBLE DEFAULT 0,
                cad DOUBLE DEFAULT 0,
                nbre_h DOUBLE DEFAULT 0,
                jalonnement VARCHAR(20),
                lundi DOUBLE DEFAULT 0,
                mardi DOUBLE DEFAULT 0,
                mercredi DOUBLE DEFAULT 0,
                jeudi DOUBLE DEFAULT 0,
                vendredi DOUBLE DEFAULT 0,
                samedi DOUBLE DEFAULT 0,
                commentaire VARCHAR(255),
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PlanningCms> getByCmsAndSemaine(String cms, int semaine, int annee) {
        List<PlanningCms> list = new ArrayList<>();
        String sql = "SELECT * FROM planning_cms WHERE cms=? AND semaine=? AND annee=? ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cms); ps.setInt(2, semaine); ps.setInt(3, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void create(PlanningCms p, String cms, int semaine, int annee) throws SQLException {
        String sql = "INSERT INTO planning_cms (cms,semaine,annee,article,ordre,st_util,ind,qte,cad,nbre_h," +
                "jalonnement,lundi,mardi,mercredi,jeudi,vendredi,samedi,commentaire) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, cms); ps.setInt(2, semaine); ps.setInt(3, annee);
        ps.setString(4, p.getArticle()); ps.setString(5, p.getOrdre());
        ps.setString(6, p.getStUtil()); ps.setString(7, p.getInd());
        ps.setDouble(8, p.getQte()); ps.setDouble(9, p.getCad()); ps.setDouble(10, p.getNbreH());
        ps.setString(11, p.getJalonnement());
        ps.setDouble(12, p.getLundi()); ps.setDouble(13, p.getMardi());
        ps.setDouble(14, p.getMercredi()); ps.setDouble(15, p.getJeudi());
        ps.setDouble(16, p.getVendredi()); ps.setDouble(17, p.getSamedi());
        ps.setString(18, p.getCommentaire());
        ps.executeUpdate();
    }

    public void update(PlanningCms p) throws SQLException {
        String sql = "UPDATE planning_cms SET article=?,ordre=?,st_util=?,ind=?,qte=?,cad=?,nbre_h=?," +
                "jalonnement=?,lundi=?,mardi=?,mercredi=?,jeudi=?,vendredi=?,samedi=?,commentaire=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getArticle()); ps.setString(2, p.getOrdre());
        ps.setString(3, p.getStUtil()); ps.setString(4, p.getInd());
        ps.setDouble(5, p.getQte()); ps.setDouble(6, p.getCad()); ps.setDouble(7, p.getNbreH());
        ps.setString(8, p.getJalonnement());
        ps.setDouble(9, p.getLundi()); ps.setDouble(10, p.getMardi());
        ps.setDouble(11, p.getMercredi()); ps.setDouble(12, p.getJeudi());
        ps.setDouble(13, p.getVendredi()); ps.setDouble(14, p.getSamedi());
        ps.setString(15, p.getCommentaire());
        ps.setInt(16, p.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM planning_cms WHERE id=?");
        ps.setInt(1, id); ps.executeUpdate();
    }

    private PlanningCms map(ResultSet rs) throws SQLException {
        PlanningCms p = new PlanningCms();
        p.setId(rs.getInt("id"));
        p.setArticle(rs.getString("article")); p.setOrdre(rs.getString("ordre"));
        p.setStUtil(rs.getString("st_util")); p.setInd(rs.getString("ind"));
        p.setQte(rs.getDouble("qte")); p.setCad(rs.getDouble("cad")); p.setNbreH(rs.getDouble("nbre_h"));
        p.setJalonnement(rs.getString("jalonnement"));
        p.setLundi(rs.getDouble("lundi")); p.setMardi(rs.getDouble("mardi"));
        p.setMercredi(rs.getDouble("mercredi")); p.setJeudi(rs.getDouble("jeudi"));
        p.setVendredi(rs.getDouble("vendredi")); p.setSamedi(rs.getDouble("samedi"));
        p.setCommentaire(rs.getString("commentaire"));
        return p;
    }
}