package entities;

import java.sql.Timestamp;
import java.util.Objects;

public class ProduitPouchoir {
    private int produitId;
    private String pouchoirReference;
    private int face;  // ✅ Changé de String à int
    private Timestamp dateAssociation;

    // Constructeur vide
    public ProduitPouchoir() {
    }

    // Constructeur avec tous les champs
    public ProduitPouchoir(int produitId, String pouchoirReference,
                           int face, Timestamp dateAssociation) {
        this.produitId = produitId;
        this.pouchoirReference = pouchoirReference;
        this.face = face;
        this.dateAssociation = dateAssociation;
    }

    // Constructeur simplifié (pour les insertions)
    public ProduitPouchoir(int produitId, String pouchoirReference, int face) {
        this.produitId = produitId;
        this.pouchoirReference = pouchoirReference;
        this.face = face;
    }

    // Getters et Setters
    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        if (produitId <= 0) {
            throw new IllegalArgumentException("L'ID du produit doit être positif");
        }
        this.produitId = produitId;
    }

    public String getPouchoirReference() {
        return pouchoirReference;
    }

    public void setPouchoirReference(String pouchoirReference) {
        if (pouchoirReference == null || pouchoirReference.trim().isEmpty()) {
            throw new IllegalArgumentException("La référence du pouchoir ne peut pas être vide");
        }
        this.pouchoirReference = pouchoirReference;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        if (face < 1) {
            throw new IllegalArgumentException("La face doit être positive (1, 2, 3, ...)");
        }
        this.face = face;
    }

    public Timestamp getDateAssociation() {
        return dateAssociation;
    }

    public void setDateAssociation(Timestamp dateAssociation) {
        this.dateAssociation = dateAssociation;
    }

    @Override
    public String toString() {
        return String.format("ProduitPouchoir{produitId=%d, pouchoir='%s', face=%d}",
                produitId, pouchoirReference, face);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduitPouchoir that = (ProduitPouchoir) o;
        return produitId == that.produitId &&
                Objects.equals(pouchoirReference, that.pouchoirReference) &&
                face == that.face;
    }

    @Override
    public int hashCode() {
        return Objects.hash(produitId, pouchoirReference, face);
    }
}