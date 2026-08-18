package entities;

import java.sql.Timestamp;

public class AuditLog {
    private int id;
    private int utilisateurId;
    private String utilisateurNom;
    private String action;
    private String tableAffectee;
    private String enregistrementId;
    private String details;
    private Timestamp dateAction;
    
    // Constructeurs
    public AuditLog() {
    }
    
    public AuditLog(int id, int utilisateurId, String utilisateurNom, String action, 
                   String tableAffectee, String enregistrementId, String details, Timestamp dateAction) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.utilisateurNom = utilisateurNom;
        this.action = action;
        this.tableAffectee = tableAffectee;
        this.enregistrementId = enregistrementId;
        this.details = details;
        this.dateAction = dateAction;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUtilisateurId() {
        return utilisateurId;
    }
    
    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
    
    public String getUtilisateurNom() {
        return utilisateurNom;
    }
    
    public void setUtilisateurNom(String utilisateurNom) {
        this.utilisateurNom = utilisateurNom;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getTableAffectee() {
        return tableAffectee;
    }
    
    public void setTableAffectee(String tableAffectee) {
        this.tableAffectee = tableAffectee;
    }
    
    public String getEnregistrementId() {
        return enregistrementId;
    }
    
    public void setEnregistrementId(String enregistrementId) {
        this.enregistrementId = enregistrementId;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public Timestamp getDateAction() {
        return dateAction;
    }
    
    public void setDateAction(Timestamp dateAction) {
        this.dateAction = dateAction;
    }
}
