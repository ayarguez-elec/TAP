package services;

import entities.Client;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService implements ICrud<Client> {

    private Connection connection;

    public ClientService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void create(Client client) throws SQLException {
        // ✅ Sans logoName
        String sql = "INSERT INTO client (nom, code, adresse, telephone, email, logo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getCode());
            stmt.setString(3, client.getAdresse());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getEmail());
            stmt.setString(6, client.getLogo());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                client.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Client read(int id) throws SQLException {
        String sql = "SELECT * FROM client WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // ✅ Constructeur sans logoName
                return new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("code"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("logo"),
                        rs.getTimestamp("created_at")
                );
            }
        }
        return null;
    }

    @Override
    public List<Client> readAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // ✅ Constructeur sans logoName
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("code"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("logo"),
                        rs.getTimestamp("created_at")
                ));
            }
        }
        return clients;
    }

    @Override
    public void update(Client client) throws SQLException {
        // ✅ Sans logoName
        String sql = "UPDATE client SET nom = ?, code = ?, adresse = ?, telephone = ?, email = ?, logo = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getCode());
            stmt.setString(3, client.getAdresse());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getEmail());
            stmt.setString(6, client.getLogo());
            stmt.setInt(7, client.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM client WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Client findByNom(String nom) throws SQLException {
        String sql = "SELECT * FROM client WHERE nom = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // ✅ Constructeur sans logoName
                return new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("code"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("logo"),
                        rs.getTimestamp("created_at")
                );
            }
        }
        return null;
    }
}