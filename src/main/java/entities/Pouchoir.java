package entities;

import java.sql.Timestamp;
import java.util.Objects;

public class Pouchoir {
    private String refPouchoir;
    private String programme;
    private String racle;
    private int emplacement;
    private String statut;
    private int face;
    private int quantiteTotale;
    private int nbCartesParFlan;
    private int stockActuel;
    private String epaisseur;
    private String support;
    private boolean codeLacroix;
    private String codeRecu;
    private String dateEnvoi;
    private String dateReception;
    private Timestamp dateCreation;
    private Timestamp dateModification;

    // ===== CONSTRUCTEURS =====

    // 1. Constructeur complet avec tous les champs (17 paramètres)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan, int stockActuel,
                    String epaisseur, String support, boolean codeLacroix,
                    String codeRecu, String dateEnvoi, String dateReception,
                    Timestamp dateCreation, Timestamp dateModification) {
        this.refPouchoir = refPouchoir;
        this.programme = programme;
        this.racle = racle;
        this.emplacement = emplacement;
        this.statut = statut;
        this.face = face;
        this.quantiteTotale = quantiteTotale;
        this.nbCartesParFlan = nbCartesParFlan;
        this.stockActuel = stockActuel;
        this.epaisseur = epaisseur;
        this.support = support;
        this.codeLacroix = codeLacroix;
        this.codeRecu = codeRecu;
        this.dateEnvoi = dateEnvoi;
        this.dateReception = dateReception;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    // 2. Constructeur avec les nouveaux champs (sans Timestamp - 15 paramètres)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan, int stockActuel,
                    String epaisseur, String support, boolean codeLacroix,
                    String codeRecu, String dateEnvoi, String dateReception) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                quantiteTotale, nbCartesParFlan, stockActuel,
                epaisseur, support, codeLacroix,
                codeRecu, dateEnvoi, dateReception,
                null, null);
    }

    // 3. Constructeur principal avec codeLacroix et Timestamp (14 paramètres)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan, int stockActuel,
                    String epaisseur, String support, boolean codeLacroix,
                    Timestamp dateCreation, Timestamp dateModification) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                quantiteTotale, nbCartesParFlan, stockActuel,
                epaisseur, support, codeLacroix,
                null, null, null,
                dateCreation, dateModification);
    }

    // 4. Constructeur avec codeLacroix (sans Timestamp - 12 paramètres)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan, int stockActuel,
                    String epaisseur, String support, boolean codeLacroix) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                quantiteTotale, nbCartesParFlan, stockActuel,
                epaisseur, support, codeLacroix,
                null, null, null,
                null, null);
    }

    // 5. Constructeur sans codeLacroix (11 paramètres)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan, int stockActuel,
                    String epaisseur, String support) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                quantiteTotale, nbCartesParFlan, stockActuel,
                epaisseur, support, false,
                null, null, null,
                null, null);
    }

    // 6. Constructeur avec Timestamp sans codeLacroix (11 paramètres - pour compatibilité)
    // ⚠️ ATTENTION : Ce constructeur a le même nombre de paramètres que le #5
    // Il est donc SUPPRIMÉ car il crée une ambiguïté

    // 7. Constructeur avec 9 paramètres (sans epaisseur et support)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan, int stockActuel) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                quantiteTotale, nbCartesParFlan, stockActuel,
                null, null, false,
                null, null, null,
                null, null);
    }

    // 8. Constructeur avec 8 paramètres (sans stockActuel)
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face,
                    int quantiteTotale, int nbCartesParFlan) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                quantiteTotale, nbCartesParFlan,
                quantiteTotale - (quantiteTotale / nbCartesParFlan),
                null, null, false,
                null, null, null,
                null, null);
    }

    // 9. Constructeur avec 6 paramètres
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut, int face) {
        this(refPouchoir, programme, racle, emplacement, statut, face,
                0, 10, 0,
                null, null, false,
                null, null, null,
                null, null);
    }

    // 10. Constructeur avec 5 paramètres
    public Pouchoir(String refPouchoir, String programme, String racle,
                    int emplacement, String statut) {
        this(refPouchoir, programme, racle, emplacement, statut, 1,
                0, 10, 0,
                null, null, false,
                null, null, null,
                null, null);
    }

    // ===== GETTERS ET SETTERS =====

    public String getRefPouchoir() { return refPouchoir; }
    public void setRefPouchoir(String refPouchoir) { this.refPouchoir = refPouchoir; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getRacle() { return racle; }
    public void setRacle(String racle) { this.racle = racle; }

    public int getEmplacement() { return emplacement; }
    public void setEmplacement(int emplacement) { this.emplacement = emplacement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getFace() { return face; }
    public void setFace(int face) {
        if (face < 1 || face > 2) {
            throw new IllegalArgumentException("La face doit être 1 ou 2");
        }
        this.face = face;
    }

    public int getQuantiteTotale() { return quantiteTotale; }
    public void setQuantiteTotale(int quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
        calculerStockActuel();
    }

    public int getNbCartesParFlan() { return nbCartesParFlan; }
    public void setNbCartesParFlan(int nbCartesParFlan) {
        this.nbCartesParFlan = nbCartesParFlan;
        calculerStockActuel();
    }

    public int getStockActuel() { return stockActuel; }
    public void setStockActuel(int stockActuel) { this.stockActuel = stockActuel; }

    public String getEpaisseur() { return epaisseur; }
    public void setEpaisseur(String epaisseur) { this.epaisseur = epaisseur; }

    public String getSupport() { return support; }
    public void setSupport(String support) { this.support = support; }

    public boolean isCodeLacroix() { return codeLacroix; }
    public void setCodeLacroix(boolean codeLacroix) { this.codeLacroix = codeLacroix; }

    public String getCodeRecu() { return codeRecu; }
    public void setCodeRecu(String codeRecu) { this.codeRecu = codeRecu; }

    public String getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(String dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getDateReception() { return dateReception; }
    public void setDateReception(String dateReception) { this.dateReception = dateReception; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    // ===== MÉTHODES UTILITAIRES POUR LE WORKFLOW =====

    public String getStatusWorkflow() {
        if (codeLacroix) {
            return "CODE";
        } else if (dateEnvoi != null && !dateEnvoi.isEmpty()) {
            return "EN_ATTENTE";
        } else {
            return "SANS_CODE";
        }
    }

    public String getStatusLabel() {
        switch (getStatusWorkflow()) {
            case "SANS_CODE": return "🔴 Sans code";
            case "EN_ATTENTE": return "🟡 En attente";
            case "CODE": return "🟢 Codé";
            default: return "❓ Inconnu";
        }
    }

    public String getStatusColor() {
        switch (getStatusWorkflow()) {
            case "SANS_CODE": return "#D9691D";
            case "EN_ATTENTE": return "#F0A500";
            case "CODE": return "#2E7D32";
            default: return "#999999";
        }
    }

    public boolean isSelectable() {
        return !codeLacroix && (dateEnvoi == null || dateEnvoi.isEmpty());
    }

    // ===== MÉTHODES EXISTANTES =====

    private void calculerStockActuel() {
        if (nbCartesParFlan > 0) {
            int x = quantiteTotale / nbCartesParFlan;
            this.stockActuel = quantiteTotale - x;
        } else {
            this.stockActuel = quantiteTotale;
        }
    }

    public void diminuerStock() {
        if (nbCartesParFlan > 0) {
            int x = quantiteTotale / nbCartesParFlan;
            this.stockActuel = this.stockActuel - x;
            if (this.stockActuel < 0) {
                this.stockActuel = 0;
            }
        }
    }

    public boolean hasEnoughStock() {
        return stockActuel > 0;
    }

    public boolean isDisponible() {
        return "disponible".equals(statut);
    }

    public boolean isSorti() {
        return "sorti".equals(statut);
    }

    @Override
    public String toString() {
        return String.format("Pouchoir{ref='%s', programme='%s', racle='%s', " +
                        "emplacement=%d, statut='%s', face=%d, stock=%d/%d, nbFlan=%d, " +
                        "epaisseur='%s', support='%s', codeLacroix=%s, codeRecu='%s'}",
                refPouchoir, programme, racle, emplacement, statut, face,
                stockActuel, quantiteTotale, nbCartesParFlan,
                epaisseur != null ? epaisseur : "N/A",
                support != null ? support : "N/A",
                codeLacroix,
                codeRecu != null ? codeRecu : "N/A");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pouchoir pouchoir = (Pouchoir) o;
        return Objects.equals(refPouchoir, pouchoir.refPouchoir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refPouchoir);
    }
}