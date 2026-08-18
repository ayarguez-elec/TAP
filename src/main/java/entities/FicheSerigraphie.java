package entities;

/**
 * Représente une fiche sérigraphie telle que stockée dans le fichier Excel
 * (onglet COMPIL_FICHES). Chaque fiche peut avoir jusqu'à 4 passages (A, B, C, D).
 *
 * Structure des colonnes Excel (0-indexé) :
 * 0  = INDEX (CLIENT_PRODUIT_FACE_N°PROGRAMME)
 * 1  = Client
 * 2  = Produit
 * 3  = Face
 * 4  = N° Programme
 * 5  = N° PCB
 * 6  = Fournisseur crème
 * 7  = Désignation crème
 * 8  = Référence Lacroix crème
 * 9  = CB (code-barres)
 * 10 = Matière écran
 * 11 = N° écran
 * 12 = Épaisseur
 * 13 = A_Date
 * 14 = A_Machine
 * 15 = A_Pression avant (kg)
 * 16 = A_Pression arrière (kg)
 * 17 = A_Support
 * 18 = A_Nombre
 * 19 = A_Racle (mm)
 * 20 = A_Informations spécifiques
 * 21 = (vide)
 * 22 = A_Visa
 * 23 = B_Date
 * 24 = B_Machine
 * 25 = B_Pression avant
 * 26 = B_Pression arrière
 * 27 = B_Support
 * 28 = B_Nombre
 * 29 = B_Racle
 * 30 = B_Informations spécifiques
 * 31 = B_Nature évolution
 * 32 = B_Visa
 * 33 = C_Date
 * 34 = C_Machine
 * 35 = C_Pression avant
 * 36 = C_Pression arrière
 * 37 = C_Support
 * 38 = C_Nombre
 * 39 = C_Racle
 * 40 = C_Informations spécifiques
 * 41 = C_Nature évolution
 * 42 = C_Visa
 * 43 = D_Date
 * 44 = D_Machine
 * 45 = D_Pression avant
 * 46 = D_Pression arrière
 * 47 = D_Support
 * 48 = D_Nombre
 * 49 = D_Racle
 * 50 = D_Informations spécifiques
 * 51 = D_Nature évolution
 * 52 = D_Visa
 * 53 = N° Fiche
 */
public class FicheSerigraphie {

    // ===== IDENTIFIANT BASE DE DONNÉES =====
    private int id;             // clé primaire auto-increment

    // ===== IDENTIFIANT MÉTIER =====
    private String index;       // ex: ATLANTIC_AT10043L_1_34540011
    private int numeroFiche;

    // ===== INFORMATIONS GÉNÉRALES =====
    private String client;
    private String produit;
    private String face;
    private String numeroProgramme;
    private String numeroPcb;

    // ===== CRÈME À BRASER =====
    private String fournisseurCreme;
    private String designationCreme;
    private String refLacroixCreme;
    private String codeBarre;

    // ===== ÉCRAN =====
    private String matiereEcran;
    private String numeroEcran;
    private String epaisseur;

    // ===== PASSAGE A (première utilisation) =====
    private String aDate;
    private String aMachine;
    private String aPressionAvant;
    private String aPressionArriere;
    private String aSupport;
    private String aNombre;
    private String aRacle;
    private String aInfoTechniques;
    private String aVisa;

    // ===== PASSAGE B (deuxième utilisation) =====
    private String bDate;
    private String bMachine;
    private String bPressionAvant;
    private String bPressionArriere;
    private String bSupport;
    private String bNombre;
    private String bRacle;
    private String bInfoTechniques;
    private String bNatureEvolution;
    private String bVisa;

    // ===== PASSAGE C (troisième utilisation) =====
    private String cDate;
    private String cMachine;
    private String cPressionAvant;
    private String cPressionArriere;
    private String cSupport;
    private String cNombre;
    private String cRacle;
    private String cInfoTechniques;
    private String cNatureEvolution;
    private String cVisa;

    // ===== PASSAGE D (quatrième utilisation) =====
    private String dDate;
    private String dMachine;
    private String dPressionAvant;
    private String dPressionArriere;
    private String dSupport;
    private String dNombre;
    private String dRacle;
    private String dInfoTechniques;
    private String dNatureEvolution;
    private String dVisa;

    // ===== CONSTRUCTEURS =====

    public FicheSerigraphie() {}

    // ===== MÉTHODES UTILITAIRES =====

    /** Retourne true si le passage A contient des données réelles */
    public boolean hasPassageA() {
        return isValidValue(aDate) || isValidValue(aMachine);
    }

    /** Retourne true si le passage B contient des données réelles */
    public boolean hasPassageB() {
        return isValidValue(bDate) || isValidValue(bMachine);
    }

    /** Retourne true si le passage C contient des données réelles */
    public boolean hasPassageC() {
        return isValidValue(cDate) || isValidValue(cMachine);
    }

    /** Retourne true si le passage D contient des données réelles */
    public boolean hasPassageD() {
        return isValidValue(dDate) || isValidValue(dMachine);
    }

    /** Retourne le nombre de passages renseignés */
    public int getNombrePassages() {
        int count = 0;
        if (hasPassageA()) count++;
        if (hasPassageB()) count++;
        if (hasPassageC()) count++;
        if (hasPassageD()) count++;
        return count;
    }

    private boolean isValidValue(String val) {
        if (val == null) return false;
        String v = val.trim();
        return !v.isEmpty() && !v.equals("0") && !v.equals("12/31/99")
                && !v.equals("12:00:00 AM") && !v.equals("N/A");
    }

    @Override
    public String toString() {
        return index != null ? index : "FicheSerigraphie()";
    }

    // ===== GETTERS / SETTERS =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public int getNumeroFiche() { return numeroFiche; }
    public void setNumeroFiche(int numeroFiche) { this.numeroFiche = numeroFiche; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getProduit() { return produit; }
    public void setProduit(String produit) { this.produit = produit; }

    public String getFace() { return face; }
    public void setFace(String face) { this.face = face; }

    public String getNumeroProgramme() { return numeroProgramme; }
    public void setNumeroProgramme(String numeroProgramme) { this.numeroProgramme = numeroProgramme; }

    public String getNumeroPcb() { return numeroPcb; }
    public void setNumeroPcb(String numeroPcb) { this.numeroPcb = numeroPcb; }

    public String getFournisseurCreme() { return fournisseurCreme; }
    public void setFournisseurCreme(String fournisseurCreme) { this.fournisseurCreme = fournisseurCreme; }

    public String getDesignationCreme() { return designationCreme; }
    public void setDesignationCreme(String designationCreme) { this.designationCreme = designationCreme; }

    public String getRefLacroixCreme() { return refLacroixCreme; }
    public void setRefLacroixCreme(String refLacroixCreme) { this.refLacroixCreme = refLacroixCreme; }

    public String getCodeBarre() { return codeBarre; }
    public void setCodeBarre(String codeBarre) { this.codeBarre = codeBarre; }

    public String getMatiereEcran() { return matiereEcran; }
    public void setMatiereEcran(String matiereEcran) { this.matiereEcran = matiereEcran; }

    public String getNumeroEcran() { return numeroEcran; }
    public void setNumeroEcran(String numeroEcran) { this.numeroEcran = numeroEcran; }

    public String getEpaisseur() { return epaisseur; }
    public void setEpaisseur(String epaisseur) { this.epaisseur = epaisseur; }

    // Passage A
    public String getADate() { return aDate; }
    public void setADate(String aDate) { this.aDate = aDate; }

    public String getAMachine() { return aMachine; }
    public void setAMachine(String aMachine) { this.aMachine = aMachine; }

    public String getAPressionAvant() { return aPressionAvant; }
    public void setAPressionAvant(String aPressionAvant) { this.aPressionAvant = aPressionAvant; }

    public String getAPressionArriere() { return aPressionArriere; }
    public void setAPressionArriere(String aPressionArriere) { this.aPressionArriere = aPressionArriere; }

    public String getASupport() { return aSupport; }
    public void setASupport(String aSupport) { this.aSupport = aSupport; }

    public String getANombre() { return aNombre; }
    public void setANombre(String aNombre) { this.aNombre = aNombre; }

    public String getARacle() { return aRacle; }
    public void setARacle(String aRacle) { this.aRacle = aRacle; }

    public String getAInfoTechniques() { return aInfoTechniques; }
    public void setAInfoTechniques(String aInfoTechniques) { this.aInfoTechniques = aInfoTechniques; }

    public String getAVisa() { return aVisa; }
    public void setAVisa(String aVisa) { this.aVisa = aVisa; }

    // Passage B
    public String getBDate() { return bDate; }
    public void setBDate(String bDate) { this.bDate = bDate; }

    public String getBMachine() { return bMachine; }
    public void setBMachine(String bMachine) { this.bMachine = bMachine; }

    public String getBPressionAvant() { return bPressionAvant; }
    public void setBPressionAvant(String bPressionAvant) { this.bPressionAvant = bPressionAvant; }

    public String getBPressionArriere() { return bPressionArriere; }
    public void setBPressionArriere(String bPressionArriere) { this.bPressionArriere = bPressionArriere; }

    public String getBSupport() { return bSupport; }
    public void setBSupport(String bSupport) { this.bSupport = bSupport; }

    public String getBNombre() { return bNombre; }
    public void setBNombre(String bNombre) { this.bNombre = bNombre; }

    public String getBRacle() { return bRacle; }
    public void setBRacle(String bRacle) { this.bRacle = bRacle; }

    public String getBInfoTechniques() { return bInfoTechniques; }
    public void setBInfoTechniques(String bInfoTechniques) { this.bInfoTechniques = bInfoTechniques; }

    public String getBNatureEvolution() { return bNatureEvolution; }
    public void setBNatureEvolution(String bNatureEvolution) { this.bNatureEvolution = bNatureEvolution; }

    public String getBVisa() { return bVisa; }
    public void setBVisa(String bVisa) { this.bVisa = bVisa; }

    // Passage C
    public String getCDate() { return cDate; }
    public void setCDate(String cDate) { this.cDate = cDate; }

    public String getCMachine() { return cMachine; }
    public void setCMachine(String cMachine) { this.cMachine = cMachine; }

    public String getCPressionAvant() { return cPressionAvant; }
    public void setCPressionAvant(String cPressionAvant) { this.cPressionAvant = cPressionAvant; }

    public String getCPressionArriere() { return cPressionArriere; }
    public void setCPressionArriere(String cPressionArriere) { this.cPressionArriere = cPressionArriere; }

    public String getCSupport() { return cSupport; }
    public void setCSupport(String cSupport) { this.cSupport = cSupport; }

    public String getCNombre() { return cNombre; }
    public void setCNombre(String cNombre) { this.cNombre = cNombre; }

    public String getCRacle() { return cRacle; }
    public void setCRacle(String cRacle) { this.cRacle = cRacle; }

    public String getCInfoTechniques() { return cInfoTechniques; }
    public void setCInfoTechniques(String cInfoTechniques) { this.cInfoTechniques = cInfoTechniques; }

    public String getCNatureEvolution() { return cNatureEvolution; }
    public void setCNatureEvolution(String cNatureEvolution) { this.cNatureEvolution = cNatureEvolution; }

    public String getCVisa() { return cVisa; }
    public void setCVisa(String cVisa) { this.cVisa = cVisa; }

    // Passage D
    public String getDDate() { return dDate; }
    public void setDDate(String dDate) { this.dDate = dDate; }

    public String getDMachine() { return dMachine; }
    public void setDMachine(String dMachine) { this.dMachine = dMachine; }

    public String getDPressionAvant() { return dPressionAvant; }
    public void setDPressionAvant(String dPressionAvant) { this.dPressionAvant = dPressionAvant; }

    public String getDPressionArriere() { return dPressionArriere; }
    public void setDPressionArriere(String dPressionArriere) { this.dPressionArriere = dPressionArriere; }

    public String getDSupport() { return dSupport; }
    public void setDSupport(String dSupport) { this.dSupport = dSupport; }

    public String getDNombre() { return dNombre; }
    public void setDNombre(String dNombre) { this.dNombre = dNombre; }

    public String getDRacle() { return dRacle; }
    public void setDRacle(String dRacle) { this.dRacle = dRacle; }

    public String getDInfoTechniques() { return dInfoTechniques; }
    public void setDInfoTechniques(String dInfoTechniques) { this.dInfoTechniques = dInfoTechniques; }

    public String getDNatureEvolution() { return dNatureEvolution; }
    public void setDNatureEvolution(String dNatureEvolution) { this.dNatureEvolution = dNatureEvolution; }

    public String getDVisa() { return dVisa; }
    public void setDVisa(String dVisa) { this.dVisa = dVisa; }
}
