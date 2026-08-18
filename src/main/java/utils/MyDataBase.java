package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://gondola.proxy.rlwy.net:15054/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000";
    private final String USERNAME = "root";
    private final String PASSWORD = "TzsnXzYWdsxKUfxEtOAmNZizoUaOKxnS";

    private MyDataBase() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion etablie avec succes!");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouve!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                System.out.println("Reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Erreur verification connexion: " + e.getMessage());
            connect();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermee");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}