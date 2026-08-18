package services;

import entities.FicheSerigraphie;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de lecture du fichier Excel "Fiche S-r-graphie".
 * Lit l'onglet COMPIL_FICHES et retourne la liste des fiches sérigraphie.
 *
 * La ligne d'en-tête (INDEX, Client, Produit, ...) est ignorée.
 * Les lignes avec la colonne INDEX = "0" ou vide sont ignorées.
 */
public class ExcelFicheService {

    private static final String SHEET_NAME = "COMPIL_FICHES";
    private static final int COL_INDEX = 0;
    private static final int COL_CLIENT = 1;
    private static final int COL_PRODUIT = 2;
    private static final int COL_FACE = 3;
    private static final int COL_PROGRAMME = 4;
    private static final int COL_PCB = 5;
    private static final int COL_FOURNISSEUR = 6;
    private static final int COL_DESIGNATION_CREME = 7;
    private static final int COL_REF_LX_CREME = 8;
    private static final int COL_CB = 9;
    private static final int COL_MATIERE_ECRAN = 10;
    private static final int COL_NUMERO_ECRAN = 11;
    private static final int COL_EPAISSEUR = 12;

    // Passage A : colonnes 13-22
    private static final int COL_A_DATE = 13;
    private static final int COL_A_MACHINE = 14;
    private static final int COL_A_PRESS_AV = 15;
    private static final int COL_A_PRESS_AR = 16;
    private static final int COL_A_SUPPORT = 17;
    private static final int COL_A_NOMBRE = 18;
    private static final int COL_A_RACLE = 19;
    private static final int COL_A_INFO = 20;
    // col 21 = vide dans le fichier
    private static final int COL_A_VISA = 22;

    // Passage B : colonnes 23-32
    private static final int COL_B_DATE = 23;
    private static final int COL_B_MACHINE = 24;
    private static final int COL_B_PRESS_AV = 25;
    private static final int COL_B_PRESS_AR = 26;
    private static final int COL_B_SUPPORT = 27;
    private static final int COL_B_NOMBRE = 28;
    private static final int COL_B_RACLE = 29;
    private static final int COL_B_INFO = 30;
    private static final int COL_B_NATURE = 31;
    private static final int COL_B_VISA = 32;

    // Passage C : colonnes 33-42
    private static final int COL_C_DATE = 33;
    private static final int COL_C_MACHINE = 34;
    private static final int COL_C_PRESS_AV = 35;
    private static final int COL_C_PRESS_AR = 36;
    private static final int COL_C_SUPPORT = 37;
    private static final int COL_C_NOMBRE = 38;
    private static final int COL_C_RACLE = 39;
    private static final int COL_C_INFO = 40;
    private static final int COL_C_NATURE = 41;
    private static final int COL_C_VISA = 42;

    // Passage D : colonnes 43-52
    private static final int COL_D_DATE = 43;
    private static final int COL_D_MACHINE = 44;
    private static final int COL_D_PRESS_AV = 45;
    private static final int COL_D_PRESS_AR = 46;
    private static final int COL_D_SUPPORT = 47;
    private static final int COL_D_NOMBRE = 48;
    private static final int COL_D_RACLE = 49;
    private static final int COL_D_INFO = 50;
    private static final int COL_D_NATURE = 51;
    private static final int COL_D_VISA = 52;

    private static final int COL_NUM_FICHE = 53;

    private final File excelFile;

    public ExcelFicheService(File excelFile) {
        this.excelFile = excelFile;
    }

    /**
     * Lit toutes les fiches de l'onglet COMPIL_FICHES.
     * Retourne une map INDEX → FicheSerigraphie pour accès rapide.
     */
    public Map<String, FicheSerigraphie> lireFiches() throws IOException {
        Map<String, FicheSerigraphie> map = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = ouvrirWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IOException("Onglet '" + SHEET_NAME + "' introuvable dans le fichier.");
            }

            for (Row row : sheet) {
                // Ignorer la première ligne (en-tête "INDEX, Client, ...")
                if (row.getRowNum() == 0) continue;

                String index = getCellString(row, COL_INDEX);
                if (index == null || index.isBlank() || index.equals("0")
                        || index.equalsIgnoreCase("INDEX")
                        || index.equalsIgnoreCase("0_0_0_0")) {
                    continue;
                }

                FicheSerigraphie fiche = new FicheSerigraphie();
                fiche.setIndex(index);
                fiche.setClient(getCellString(row, COL_CLIENT));
                fiche.setProduit(getCellString(row, COL_PRODUIT));
                fiche.setFace(getCellString(row, COL_FACE));
                fiche.setNumeroProgramme(getCellString(row, COL_PROGRAMME));
                fiche.setNumeroPcb(getCellString(row, COL_PCB));
                fiche.setFournisseurCreme(getCellString(row, COL_FOURNISSEUR));
                fiche.setDesignationCreme(getCellString(row, COL_DESIGNATION_CREME));
                fiche.setRefLacroixCreme(getCellString(row, COL_REF_LX_CREME));
                fiche.setCodeBarre(getCellString(row, COL_CB));
                fiche.setMatiereEcran(getCellString(row, COL_MATIERE_ECRAN));
                fiche.setNumeroEcran(getCellString(row, COL_NUMERO_ECRAN));
                fiche.setEpaisseur(getCellString(row, COL_EPAISSEUR));

                // Passage A
                fiche.setADate(getCellString(row, COL_A_DATE));
                fiche.setAMachine(getCellString(row, COL_A_MACHINE));
                fiche.setAPressionAvant(getCellString(row, COL_A_PRESS_AV));
                fiche.setAPressionArriere(getCellString(row, COL_A_PRESS_AR));
                fiche.setASupport(getCellString(row, COL_A_SUPPORT));
                fiche.setANombre(getCellString(row, COL_A_NOMBRE));
                fiche.setARacle(getCellString(row, COL_A_RACLE));
                fiche.setAInfoTechniques(getCellString(row, COL_A_INFO));
                fiche.setAVisa(getCellString(row, COL_A_VISA));

                // Passage B
                fiche.setBDate(getCellString(row, COL_B_DATE));
                fiche.setBMachine(getCellString(row, COL_B_MACHINE));
                fiche.setBPressionAvant(getCellString(row, COL_B_PRESS_AV));
                fiche.setBPressionArriere(getCellString(row, COL_B_PRESS_AR));
                fiche.setBSupport(getCellString(row, COL_B_SUPPORT));
                fiche.setBNombre(getCellString(row, COL_B_NOMBRE));
                fiche.setBRacle(getCellString(row, COL_B_RACLE));
                fiche.setBInfoTechniques(getCellString(row, COL_B_INFO));
                fiche.setBNatureEvolution(getCellString(row, COL_B_NATURE));
                fiche.setBVisa(getCellString(row, COL_B_VISA));

                // Passage C
                fiche.setCDate(getCellString(row, COL_C_DATE));
                fiche.setCMachine(getCellString(row, COL_C_MACHINE));
                fiche.setCPressionAvant(getCellString(row, COL_C_PRESS_AV));
                fiche.setCPressionArriere(getCellString(row, COL_C_PRESS_AR));
                fiche.setCSupport(getCellString(row, COL_C_SUPPORT));
                fiche.setCNombre(getCellString(row, COL_C_NOMBRE));
                fiche.setCRacle(getCellString(row, COL_C_RACLE));
                fiche.setCInfoTechniques(getCellString(row, COL_C_INFO));
                fiche.setCNatureEvolution(getCellString(row, COL_C_NATURE));
                fiche.setCVisa(getCellString(row, COL_C_VISA));

                // Passage D
                fiche.setDDate(getCellString(row, COL_D_DATE));
                fiche.setDMachine(getCellString(row, COL_D_MACHINE));
                fiche.setDPressionAvant(getCellString(row, COL_D_PRESS_AV));
                fiche.setDPressionArriere(getCellString(row, COL_D_PRESS_AR));
                fiche.setDSupport(getCellString(row, COL_D_SUPPORT));
                fiche.setDNombre(getCellString(row, COL_D_NOMBRE));
                fiche.setDRacle(getCellString(row, COL_D_RACLE));
                fiche.setDInfoTechniques(getCellString(row, COL_D_INFO));
                fiche.setDNatureEvolution(getCellString(row, COL_D_NATURE));
                fiche.setDVisa(getCellString(row, COL_D_VISA));

                // N° de fiche
                String numFicheStr = getCellString(row, COL_NUM_FICHE);
                if (numFicheStr != null && !numFicheStr.isBlank()) {
                    try { fiche.setNumeroFiche(Integer.parseInt(numFicheStr.trim())); }
                    catch (NumberFormatException ignored) {}
                }

                map.put(index, fiche);
            }
        }

        return map;
    }

    /** Retourne la liste ordonnée des fiches (sans doublons apparents). */
    public List<FicheSerigraphie> lireFichesListe() throws IOException {
        return new ArrayList<>(lireFiches().values());
    }

    /** Recherche une fiche par son index exact. */
    public FicheSerigraphie findByIndex(String index) throws IOException {
        return lireFiches().get(index);
    }

    /**
     * Recherche les fiches correspondant à un programme donné.
     * (le programme est la partie N°4 de l'index : CLIENT_PRODUIT_FACE_PROGRAMME)
     */
    public List<FicheSerigraphie> findByProgramme(String programme) throws IOException {
        List<FicheSerigraphie> result = new ArrayList<>();
        for (FicheSerigraphie f : lireFichesListe()) {
            if (programme.equals(f.getNumeroProgramme())) {
                result.add(f);
            }
        }
        return result;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Workbook ouvrirWorkbook(FileInputStream fis) throws IOException {
        String name = excelFile.getName().toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xlsm")) {
            return new XSSFWorkbook(fis);
        } else {
            // .xls (format HSSF)
            return new HSSFWorkbook(fis);
        }
    }

    /**
     * Lit la valeur d'une cellule sous forme de String propre.
     * Gère les types numérique, date, booléen, formule et vide.
     */
    private String getCellString(Row row, int colIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Formater les dates en dd/MM/yyyy
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        return sdf.format(cell.getDateCellValue());
                    } catch (Exception e) {
                        return String.valueOf((long) cell.getNumericCellValue());
                    }
                } else {
                    double d = cell.getNumericCellValue();
                    // Supprimer le ".0" pour les entiers
                    if (d == Math.floor(d) && !Double.isInfinite(d)) {
                        return String.valueOf((long) d);
                    }
                    return String.valueOf(d);
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double d = cell.getNumericCellValue();
                        if (d == Math.floor(d)) return String.valueOf((long) d);
                        return String.valueOf(d);
                    } catch (Exception ex) {
                        return "";
                    }
                }

            case BLANK:
            default:
                return "";
        }
    }
}
