package entities;

import java.sql.Timestamp;

public class AnomalieStencil {
    private int id;
    private String stencil;
    private String ligne;
    private String typeProbleme;
    private String degre; // CRITIQUE, MAJEUR, MINEUR
    private String remarque;
    private boolean resolu;
    private Timestamp dateDetection;

    public AnomalieStencil() {}

    public AnomalieStencil(int id, String stencil, String ligne, String typeProbleme,
                            String degre, String remarque, boolean resolu, Timestamp dateDetection) {
        this.id = id; this.stencil = stencil; this.ligne = ligne;
        this.typeProbleme = typeProbleme; this.degre = degre;
        this.remarque = remarque; this.resolu = resolu; this.dateDetection = dateDetection;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStencil() { return stencil; }
    public void setStencil(String stencil) { this.stencil = stencil; }
    public String getLigne() { return ligne; }
    public void setLigne(String ligne) { this.ligne = ligne; }
    public String getTypeProbleme() { return typeProbleme; }
    public void setTypeProbleme(String typeProbleme) { this.typeProbleme = typeProbleme; }
    public String getDegre() { return degre; }
    public void setDegre(String degre) { this.degre = degre; }
    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }
    public boolean isResolu() { return resolu; }
    public void setResolu(boolean resolu) { this.resolu = resolu; }
    public Timestamp getDateDetection() { return dateDetection; }
    public void setDateDetection(Timestamp dateDetection) { this.dateDetection = dateDetection; }
}