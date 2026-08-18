package entities;

public class PlanningCms {
    private int id;
    private String cms;
    private String article;
    private String ordre;
    private String stUtil;
    private String ind;
    private double qte;
    private double cad;
    private double nbreH;
    private String jalonnement;
    private double lundi;
    private double mardi;
    private double mercredi;
    private double jeudi;
    private double vendredi;
    private double samedi;
    private String commentaire;

    public PlanningCms() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCms() { return cms; }
    public void setCms(String cms) { this.cms = cms; }
    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }
    public String getOrdre() { return ordre; }
    public void setOrdre(String ordre) { this.ordre = ordre; }
    public String getStUtil() { return stUtil; }
    public void setStUtil(String stUtil) { this.stUtil = stUtil; }
    public String getInd() { return ind; }
    public void setInd(String ind) { this.ind = ind; }
    public double getQte() { return qte; }
    public void setQte(double qte) { this.qte = qte; }
    public double getCad() { return cad; }
    public void setCad(double cad) { this.cad = cad; }
    public double getNbreH() { return nbreH; }
    public void setNbreH(double nbreH) { this.nbreH = nbreH; }
    public String getJalonnement() { return jalonnement; }
    public void setJalonnement(String jalonnement) { this.jalonnement = jalonnement; }
    public double getLundi() { return lundi; }
    public void setLundi(double lundi) { this.lundi = lundi; }
    public double getMardi() { return mardi; }
    public void setMardi(double mardi) { this.mardi = mardi; }
    public double getMercredi() { return mercredi; }
    public void setMercredi(double mercredi) { this.mercredi = mercredi; }
    public double getJeudi() { return jeudi; }
    public void setJeudi(double jeudi) { this.jeudi = jeudi; }
    public double getVendredi() { return vendredi; }
    public void setVendredi(double vendredi) { this.vendredi = vendredi; }
    public double getSamedi() { return samedi; }
    public void setSamedi(double samedi) { this.samedi = samedi; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}