package entities;

import java.sql.Timestamp;
import java.util.Objects;

public class Produit {
    private int id;
    private String nomProduit;
    private int clientId;
    private String ecran;
    private String codeProduit;
    private String description;
    private String cremeABraser;
    private String programme;  // ← NOUVEAU CHAMP
    private Timestamp dateCreation;

    // Constructeur vide
    public Produit() {}

    // Constructeur complet avec programme
    public Produit(int id, String nomProduit, int clientId, String ecran,
                   String codeProduit, String description, String cremeABraser,
                   String programme, Timestamp dateCreation) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.clientId = clientId;
        this.ecran = ecran;
        this.codeProduit = codeProduit;
        this.description = description;
        this.cremeABraser = cremeABraser;
        this.programme = programme;
        this.dateCreation = dateCreation;
    }

    // Constructeur sans programme (pour compatibilité)
    public Produit(int id, String nomProduit, int clientId, String ecran,
                   String codeProduit, String description, String cremeABraser,
                   Timestamp dateCreation) {
        this(id, nomProduit, clientId, ecran, codeProduit, description, cremeABraser, null, dateCreation);
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public String getEcran() { return ecran; }
    public void setEcran(String ecran) { this.ecran = ecran; }

    public String getCodeProduit() { return codeProduit; }
    public void setCodeProduit(String codeProduit) { this.codeProduit = codeProduit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCremeABraser() { return cremeABraser; }
    public void setCremeABraser(String cremeABraser) { this.cremeABraser = cremeABraser; }

    public String getProgramme() { return programme; }  // ← NOUVEAU GETTER
    public void setProgramme(String programme) { this.programme = programme; }  // ← NOUVEAU SETTER

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return nomProduit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return id == produit.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}