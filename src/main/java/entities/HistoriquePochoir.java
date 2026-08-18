package entities;

import java.sql.Timestamp;

public class HistoriquePochoir {
    private int id;
    private String pouchoirRef;
    private ActionPochoir action;
    private Timestamp dateSortie;
    private Timestamp dateRetour;
    private int operateurId;
    private String operateurNom;
    private String localisation;
    private String raison;
    private String etatRetour;
    private String remarques;
    
    public enum ActionPochoir {
        SORTIE, RETOUR
    }
    
    // Constructeurs
    public HistoriquePochoir() {
    }
    
    public HistoriquePochoir(int id, String pouchoirRef, ActionPochoir action, Timestamp dateSortie, 
                            Timestamp dateRetour, int operateurId, String operateurNom, String localisation, 
                            String raison, String etatRetour, String remarques) {
        this.id = id;
        this.pouchoirRef = pouchoirRef;
        this.action = action;
        this.dateSortie = dateSortie;
        this.dateRetour = dateRetour;
        this.operateurId = operateurId;
        this.operateurNom = operateurNom;
        this.localisation = localisation;
        this.raison = raison;
        this.etatRetour = etatRetour;
        this.remarques = remarques;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getPouchoirRef() {
        return pouchoirRef;
    }
    
    public void setPouchoirRef(String pouchoirRef) {
        this.pouchoirRef = pouchoirRef;
    }
    
    public ActionPochoir getAction() {
        return action;
    }
    
    public void setAction(ActionPochoir action) {
        this.action = action;
    }
    
    public Timestamp getDateSortie() {
        return dateSortie;
    }
    
    public void setDateSortie(Timestamp dateSortie) {
        this.dateSortie = dateSortie;
    }
    
    public Timestamp getDateRetour() {
        return dateRetour;
    }
    
    public void setDateRetour(Timestamp dateRetour) {
        this.dateRetour = dateRetour;
    }
    
    public int getOperateurId() {
        return operateurId;
    }
    
    public void setOperateurId(int operateurId) {
        this.operateurId = operateurId;
    }
    
    public String getOperateurNom() {
        return operateurNom;
    }
    
    public void setOperateurNom(String operateurNom) {
        this.operateurNom = operateurNom;
    }
    
    public String getLocalisation() {
        return localisation;
    }
    
    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
    
    public String getRaison() {
        return raison;
    }
    
    public void setRaison(String raison) {
        this.raison = raison;
    }
    
    public String getEtatRetour() {
        return etatRetour;
    }
    
    public void setEtatRetour(String etatRetour) {
        this.etatRetour = etatRetour;
    }
    
    public String getRemarques() {
        return remarques;
    }
    
    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
}
