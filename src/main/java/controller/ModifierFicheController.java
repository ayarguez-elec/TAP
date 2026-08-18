package controller;

import entities.FicheSerigraphie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import services.FicheSerigraphieService;
import java.io.IOException;
import java.sql.SQLException;

public class ModifierFicheController {

    // ===== Informations Générales =====
    @FXML private TextField clientField;
    @FXML private TextField produitField;
    @FXML private TextField faceField;
    @FXML private TextField programmeField;
    @FXML private TextField pcbField;

    // ===== Écran & Crème =====
    @FXML private TextField matiereEcranField;
    @FXML private TextField numeroEcranField;
    @FXML private TextField epaisseurField;
    @FXML private TextField fournisseurCremeField;
    @FXML private TextField designationCremeField;
    @FXML private TextField refLacroixField;
    @FXML private TextField codeBarreField;

    // ===== Passage A =====
    @FXML private TextField aDateField;
    @FXML private TextField aMachineField;
    @FXML private TextField aPressionAvField;
    @FXML private TextField aPressionArField;
    @FXML private TextField aSupportField;
    @FXML private TextField aNombreField;
    @FXML private TextField aRacleField;
    @FXML private TextArea  aInfoField;
    @FXML private TextField aVisaField;

    // ===== Passage B =====
    @FXML private TextField bDateField;
    @FXML private TextField bMachineField;
    @FXML private TextField bPressionAvField;
    @FXML private TextField bPressionArField;
    @FXML private TextField bSupportField;
    @FXML private TextField bNombreField;
    @FXML private TextField bRacleField;
    @FXML private TextArea  bInfoField;
    @FXML private TextArea  bNatureField;
    @FXML private TextField bVisaField;

    // ===== Passage C =====
    @FXML private TextField cDateField;
    @FXML private TextField cMachineField;
    @FXML private TextField cPressionAvField;
    @FXML private TextField cPressionArField;
    @FXML private TextField cSupportField;
    @FXML private TextField cNombreField;
    @FXML private TextField cRacleField;
    @FXML private TextArea  cInfoField;
    @FXML private TextArea  cNatureField;
    @FXML private TextField cVisaField;

    // ===== Passage D =====
    @FXML private TextField dDateField;
    @FXML private TextField dMachineField;
    @FXML private TextField dPressionAvField;
    @FXML private TextField dPressionArField;
    @FXML private TextField dSupportField;
    @FXML private TextField dNombreField;
    @FXML private TextField dRacleField;
    @FXML private TextArea  dInfoField;
    @FXML private TextArea  dNatureField;
    @FXML private TextField dVisaField;

    private final FicheSerigraphieService service = new FicheSerigraphieService();
    private StackPane contentArea;
    private FicheSerigraphie fiche;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    /**
     * Injecte la fiche à modifier et pré-remplit tous les champs.
     */
    public void setFiche(FicheSerigraphie f) {
        this.fiche = f;

        // Informations générales
        setText(clientField,   f.getClient());
        setText(produitField,  f.getProduit());
        setText(faceField,     f.getFace());
        setText(programmeField, f.getNumeroProgramme());
        setText(pcbField,      f.getNumeroPcb());

        // Écran & Crème
        setText(matiereEcranField,      f.getMatiereEcran());
        setText(numeroEcranField,       f.getNumeroEcran());
        setText(epaisseurField,         f.getEpaisseur());
        setText(fournisseurCremeField,  f.getFournisseurCreme());
        setText(designationCremeField,  f.getDesignationCreme());
        setText(refLacroixField,        f.getRefLacroixCreme());
        setText(codeBarreField,         f.getCodeBarre());

        // Passage A
        setText(aDateField,       f.getADate());
        setText(aMachineField,    f.getAMachine());
        setText(aPressionAvField, f.getAPressionAvant());
        setText(aPressionArField, f.getAPressionArriere());
        setText(aSupportField,    f.getASupport());
        setText(aNombreField,     f.getANombre());
        setText(aRacleField,      f.getARacle());
        setAreaText(aInfoField,   f.getAInfoTechniques());
        setText(aVisaField,       f.getAVisa());

        // Passage B
        setText(bDateField,       f.getBDate());
        setText(bMachineField,    f.getBMachine());
        setText(bPressionAvField, f.getBPressionAvant());
        setText(bPressionArField, f.getBPressionArriere());
        setText(bSupportField,    f.getBSupport());
        setText(bNombreField,     f.getBNombre());
        setText(bRacleField,      f.getBRacle());
        setAreaText(bInfoField,   f.getBInfoTechniques());
        setAreaText(bNatureField, f.getBNatureEvolution());
        setText(bVisaField,       f.getBVisa());

        // Passage C
        setText(cDateField,       f.getCDate());
        setText(cMachineField,    f.getCMachine());
        setText(cPressionAvField, f.getCPressionAvant());
        setText(cPressionArField, f.getCPressionArriere());
        setText(cSupportField,    f.getCSupport());
        setText(cNombreField,     f.getCNombre());
        setText(cRacleField,      f.getCRacle());
        setAreaText(cInfoField,   f.getCInfoTechniques());
        setAreaText(cNatureField, f.getCNatureEvolution());
        setText(cVisaField,       f.getCVisa());

        // Passage D
        setText(dDateField,       f.getDDate());
        setText(dMachineField,    f.getDMachine());
        setText(dPressionAvField, f.getDPressionAvant());
        setText(dPressionArField, f.getDPressionArriere());
        setText(dSupportField,    f.getDSupport());
        setText(dNombreField,     f.getDNombre());
        setText(dRacleField,      f.getDRacle());
        setAreaText(dInfoField,   f.getDInfoTechniques());
        setAreaText(dNatureField, f.getDNatureEvolution());
        setText(dVisaField,       f.getDVisa());
    }

    @FXML
    private void handleRetour() {
        navigateBack();
    }

    @FXML
    private void handleAnnuler() {
        navigateBack();
    }

    @FXML
    private void handleEnregistrer() {
        String client  = clientField.getText().trim();
        String produit = produitField.getText().trim();

        if (client.isEmpty() || produit.isEmpty()) {
            showAlert("Erreur", "Client et Produit sont obligatoires.", Alert.AlertType.ERROR);
            return;
        }

        FicheSerigraphie f = new FicheSerigraphie();
        f.setId(fiche.getId());
        f.setNumeroFiche(fiche.getNumeroFiche());

        f.setClient(client);
        f.setProduit(produit);
        f.setFace(faceField.getText());
        f.setNumeroProgramme(programmeField.getText());
        f.setNumeroPcb(pcbField.getText());
        f.setIndex(client + "_" + produit + "_" + faceField.getText() + "_" + programmeField.getText());

        f.setMatiereEcran(matiereEcranField.getText());
        f.setNumeroEcran(numeroEcranField.getText());
        f.setEpaisseur(epaisseurField.getText());
        f.setFournisseurCreme(fournisseurCremeField.getText());
        f.setDesignationCreme(designationCremeField.getText());
        f.setRefLacroixCreme(refLacroixField.getText());
        f.setCodeBarre(codeBarreField.getText());

        // Passage A
        f.setADate(aDateField.getText());
        f.setAMachine(aMachineField.getText());
        f.setAPressionAvant(aPressionAvField.getText());
        f.setAPressionArriere(aPressionArField.getText());
        f.setASupport(aSupportField.getText());
        f.setANombre(aNombreField.getText());
        f.setARacle(aRacleField.getText());
        f.setAInfoTechniques(aInfoField.getText());
        f.setAVisa(aVisaField.getText());

        // Passage B
        f.setBDate(bDateField.getText());
        f.setBMachine(bMachineField.getText());
        f.setBPressionAvant(bPressionAvField.getText());
        f.setBPressionArriere(bPressionArField.getText());
        f.setBSupport(bSupportField.getText());
        f.setBNombre(bNombreField.getText());
        f.setBRacle(bRacleField.getText());
        f.setBInfoTechniques(bInfoField.getText());
        f.setBNatureEvolution(bNatureField.getText());
        f.setBVisa(bVisaField.getText());

        // Passage C
        f.setCDate(cDateField.getText());
        f.setCMachine(cMachineField.getText());
        f.setCPressionAvant(cPressionAvField.getText());
        f.setCPressionArriere(cPressionArField.getText());
        f.setCSupport(cSupportField.getText());
        f.setCNombre(cNombreField.getText());
        f.setCRacle(cRacleField.getText());
        f.setCInfoTechniques(cInfoField.getText());
        f.setCNatureEvolution(cNatureField.getText());
        f.setCVisa(cVisaField.getText());

        // Passage D
        f.setDDate(dDateField.getText());
        f.setDMachine(dMachineField.getText());
        f.setDPressionAvant(dPressionAvField.getText());
        f.setDPressionArriere(dPressionArField.getText());
        f.setDSupport(dSupportField.getText());
        f.setDNombre(dNombreField.getText());
        f.setDRacle(dRacleField.getText());
        f.setDInfoTechniques(dInfoField.getText());
        f.setDNatureEvolution(dNatureField.getText());
        f.setDVisa(dVisaField.getText());

        try {
            service.update(f);
            showAlert("Succès", "Fiche modifiée avec succès.", Alert.AlertType.INFORMATION);
            navigateBack();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private StackPane resolveContentArea() {
        if (contentArea != null) return contentArea;
        if (MainController.getInstance() != null) return MainController.getInstance().getContentArea();
        return null;
    }

    private void navigateBack() {
        StackPane target = resolveContentArea();
        if (target == null) return;
        contentArea = target;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fiche-serigraphie-view.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /** Remplit un TextField en gérant les valeurs null. */
    private void setText(TextField field, String value) {
        field.setText(value != null ? value : "");
    }

    /** Remplit un TextArea en gérant les valeurs null. */
    private void setAreaText(TextArea area, String value) {
        area.setText(value != null ? value : "");
    }
}
