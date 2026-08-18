package entities;

import java.sql.Timestamp;
import java.util.Objects;

public class Client {
    private int id;
    private String nom;
    private String code;
    private String adresse;
    private String telephone;
    private String email;
    private String logo;  // ✅ Seulement logo, pas logoName
    private Timestamp createdAt;

    // Constructeur vide
    public Client() {}

    // ✅ Constructeur avec tous les champs (sans logoName)
    public Client(int id, String nom, String code, String adresse,
                  String telephone, String email, String logo, Timestamp createdAt) {
        this.id = id;
        this.nom = nom;
        this.code = code;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.logo = logo;
        this.createdAt = createdAt;
    }

    // Constructeur simplifié
    public Client(String nom) {
        this.nom = nom;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}