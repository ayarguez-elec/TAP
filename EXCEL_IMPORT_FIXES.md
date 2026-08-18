# Excel Import Fixes - Implementation Summary

## Issues Fixed

### Issue 1: N° Fiche shows 0 for all imported records
**Problem**: The `numeroFiche` column showed 0 for all imported records because:
- Excel column 53 (N° Fiche) might be empty or missing
- The value wasn't being auto-generated

**Solution**: Auto-generate `numeroFiche` during import if it's 0 or missing:
- For **new records**: After creating a fiche, use the auto-generated database ID as the numeroFiche
- For **existing records**: Preserve the existing numeroFiche value

### Issue 2: Data truncation errors
**Problem**: Errors like "Data too long for column 'b_pression_arriere'" indicated VARCHAR fields were too small for Excel data.

**Solution**: Increased VARCHAR sizes for all fields that may contain longer data:
- Index: VARCHAR(120) → VARCHAR(150)
- Fournisseur/Designation creme: VARCHAR(80) → VARCHAR(100)
- Code barre/Ref Lacroix: VARCHAR(60) → VARCHAR(80)
- Matiere ecran: VARCHAR(80) → VARCHAR(100)
- Numero ecran: VARCHAR(60) → VARCHAR(80)
- Epaisseur: VARCHAR(20) → VARCHAR(30)
- Machine fields (A/B/C/D): VARCHAR(60) → VARCHAR(100)
- Support fields (A/B/C/D): VARCHAR(60) → VARCHAR(100)
- Pression fields (A/B/C/D): VARCHAR(10) → VARCHAR(20)
- Nombre fields (A/B/C/D): VARCHAR(10) → VARCHAR(20)
- Racle fields (A/B/C/D): VARCHAR(20) → VARCHAR(30)
- Visa fields (A/B/C/D): VARCHAR(20) → VARCHAR(30)

## Files Modified

### 1. FicheSerigraphieService.java
**Location**: `src/main/java/services/FicheSerigraphieService.java`

**Changes**:
- Updated `creerTableSiAbsente()` method to increase VARCHAR sizes for all fields
- Added new method `getNextNumeroFiche()` to retrieve the next available fiche number

```java
/** Retourne le prochain numéro de fiche disponible (max + 1) */
public int getNextNumeroFiche() throws SQLException {
    String sql = "SELECT COALESCE(MAX(numero_fiche), 0) + 1 FROM fiche_serigraphie";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 1;
    }
}
```

### 2. FicheSerigraphieController.java
**Location**: `src/main/java/controller/FicheSerigraphieController.java`

**Changes**:
- Updated `handleImporterExcel()` method to auto-generate numeroFiche for new records
- Preserved existing numeroFiche when updating records

**Import Logic**:
```java
if (existingFiche != null) {
    // Fiche exists - update it
    excelFiche.setId(existingFiche.getId());
    
    // Preserve the existing numeroFiche when updating
    if (excelFiche.getNumeroFiche() == 0) {
        excelFiche.setNumeroFiche(existingFiche.getNumeroFiche());
    }
    
    service.update(excelFiche);
    countUpdated++;
} else {
    // Fiche doesn't exist - create new
    service.create(excelFiche);
    
    // Auto-generate numeroFiche if missing or 0
    if (excelFiche.getNumeroFiche() == 0) {
        // Use the auto-generated database ID as the fiche number
        excelFiche.setNumeroFiche(excelFiche.getId());
        service.update(excelFiche);
    }
    
    countCreated++;
}
```

## Expected Results

After these changes:
- ✅ N° column displays actual fiche numbers (not 0)
- ✅ No more "Data truncation" errors during import
- ✅ All Excel data imports successfully without errors
- ✅ Existing fiches keep their original numeroFiche values
- ✅ New fiches get auto-generated numeroFiche based on database ID

## Database Schema Changes

The table schema will be automatically updated when the application starts (if the table doesn't exist yet). 

**For existing tables**, you may need to run the following SQL to increase column sizes:

```sql
ALTER TABLE fiche_serigraphie 
  MODIFY COLUMN idx VARCHAR(150) NOT NULL,
  MODIFY COLUMN fournisseur_creme VARCHAR(100),
  MODIFY COLUMN designation_creme VARCHAR(100),
  MODIFY COLUMN ref_lacroix_creme VARCHAR(80),
  MODIFY COLUMN code_barre VARCHAR(80),
  MODIFY COLUMN matiere_ecran VARCHAR(100),
  MODIFY COLUMN numero_ecran VARCHAR(80),
  MODIFY COLUMN epaisseur VARCHAR(30),
  MODIFY COLUMN a_machine VARCHAR(100),
  MODIFY COLUMN a_pression_avant VARCHAR(20),
  MODIFY COLUMN a_pression_arriere VARCHAR(20),
  MODIFY COLUMN a_support VARCHAR(100),
  MODIFY COLUMN a_nombre VARCHAR(20),
  MODIFY COLUMN a_racle VARCHAR(30),
  MODIFY COLUMN a_visa VARCHAR(30),
  MODIFY COLUMN b_machine VARCHAR(100),
  MODIFY COLUMN b_pression_avant VARCHAR(20),
  MODIFY COLUMN b_pression_arriere VARCHAR(20),
  MODIFY COLUMN b_support VARCHAR(100),
  MODIFY COLUMN b_nombre VARCHAR(20),
  MODIFY COLUMN b_racle VARCHAR(30),
  MODIFY COLUMN b_visa VARCHAR(30),
  MODIFY COLUMN c_machine VARCHAR(100),
  MODIFY COLUMN c_pression_avant VARCHAR(20),
  MODIFY COLUMN c_pression_arriere VARCHAR(20),
  MODIFY COLUMN c_support VARCHAR(100),
  MODIFY COLUMN c_nombre VARCHAR(20),
  MODIFY COLUMN c_racle VARCHAR(30),
  MODIFY COLUMN c_visa VARCHAR(30),
  MODIFY COLUMN d_machine VARCHAR(100),
  MODIFY COLUMN d_pression_avant VARCHAR(20),
  MODIFY COLUMN d_pression_arriere VARCHAR(20),
  MODIFY COLUMN d_support VARCHAR(100),
  MODIFY COLUMN d_nombre VARCHAR(20),
  MODIFY COLUMN d_racle VARCHAR(30),
  MODIFY COLUMN d_visa VARCHAR(30);
```

## Testing Recommendations

1. **Backup your database** before testing the import
2. Test with a small Excel file first
3. Verify that:
   - New records get proper numeroFiche values
   - Existing records maintain their numeroFiche
   - No truncation errors occur
   - All data is imported correctly
4. Check the import summary dialog for any errors

## Notes

- The `getNextNumeroFiche()` method is available if you want to use sequential numbering instead of database IDs in the future
- The current implementation uses database IDs for simplicity and guaranteed uniqueness
- All changes are backward compatible with existing data
