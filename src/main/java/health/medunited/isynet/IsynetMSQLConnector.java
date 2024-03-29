package health.medunited.isynet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;

import org.hl7.fhir.r4.model.Bundle;

import health.medunited.service.BundleParser;
import health.medunited.service.MedicationDbLookup;

@ApplicationScoped
public class IsynetMSQLConnector {

    private static final Logger log = Logger.getLogger(IsynetMSQLConnector.class.getName());
    private static final String PATIENT = "patient";
    private static final String MEDICATIONSTATEMENT = "medicationStatement";

    private String currentLog;

    public void insertToIsynet(Bundle parsedBundle, Map<String, Object> connectionParameter, JMSContext context)  {
        currentLog = "";
        String pznToLookup = BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle);
        List<String> tableEntry = MedicationDbLookup.lookupMedicationByPZN(pznToLookup);
        printMedicationInfo(pznToLookup, tableEntry);

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String connectionUrl = "jdbc:sqlserver://"+connectionParameter.get("hostname")+":"+connectionParameter.get("port")+";databaseName=WINACS;user="+connectionParameter.get("user")+";password="+connectionParameter.get("password")+";trustServerCertificate=true";
        String medicationRequestId = "";
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement()) {
            medicationRequestId = parsedBundle.getEntry().get(2).getResource().getId();
            // deleteAllMedications(stmt);
            // deleteAllPatients(stmt);

            int patientNumber = checkIfPatientExistsInTheSystem(parsedBundle, stmt);

            if (patientNumber == -1) { // Patient does not exist
                info("This patient does not exist in the Db.");
                // MED-412 
		// patientNumber = createPatient(parsedBundle, stmt);
		return;
            }

            // Creates medication only if it exists in the Db from where medication info is obtained + Patient is not dead + Patient is not at the hospital
            if (tableEntry != null && !isPatientDateOfDeathKnown(patientNumber, stmt)
                                   && !isPatientDeadButTheDateIsUnknown(patientNumber, stmt)
                                   && !checkIfPatientIsAtTheHospital(patientNumber, stmt)) {
                createMedication(tableEntry, patientNumber, parsedBundle, stmt);
            } else if (tableEntry == null) {
                info("The medication corresponding to PZN " + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) + " could not be found on the database, please insert it manually.");
            } else if (isPatientDateOfDeathKnown(patientNumber, stmt) || isPatientDeadButTheDateIsUnknown(patientNumber, stmt)) {
                info("The medication was not created because the Patient is dead.");
            } else if (checkIfPatientIsAtTheHospital(patientNumber, stmt)) {
                info("The medication was not created because the Patient is at the hospital.");
            }
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "SQL problem while inserting medication", e);
            currentLog += "SQL problem while inserting medication";
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "Problem while inserting medication", e);
            currentLog += "Problem while inserting medication";
        } finally {
            sendPrescriptionStatusReply(context, medicationRequestId);
            currentLog = "";
        }
    }

    private void sendPrescriptionStatusReply(JMSContext context, String medicationRequestId) {
        log.info("Sending message to queue PrescriptionStatus. For "+medicationRequestId);
        Queue queue = context.createQueue("PrescriptionStatus");
        JsonObject json = Json.createObjectBuilder().add("medicationRequestId", medicationRequestId).add("info", currentLog).build();
        context.createProducer().send(queue, json.toString());
    }

    public void printMedicationInfo(String pznToLookup, List<String> tableEntry) {

        if (tableEntry != null) {
            info("[ MEDICATION OBTAINED FROM DB ] PZN: " + pznToLookup +
                    " // name: " + MedicationDbLookup.getMedicationName(tableEntry) +
                    " // quantity: " + MedicationDbLookup.getQuantity(tableEntry) +
                    " // package size: " + MedicationDbLookup.getPackageSize(tableEntry) +
                    " // AVP: " + MedicationDbLookup.getAVP(tableEntry) +
                    " // ATC: " + MedicationDbLookup.getATC(tableEntry) +
                    " // composition: " + MedicationDbLookup.getComposition(tableEntry) +
                    " // pharmaceutical form code: " + MedicationDbLookup.getPharmaceuticalFormCode(tableEntry) +
                    " // pharmaceutical form text: " + MedicationDbLookup.getPharmaceuticalFormText(tableEntry) +
                    " // manufacturer: " + MedicationDbLookup.getManufacturer(tableEntry));
        }
    }

    public int checkIfPatientExistsInTheSystem(Bundle parsedBundle, Statement stmt) throws SQLException {

        String[] birthDate = BundleParser.getBirthDate(PATIENT, parsedBundle).split("-");
        String differentBirthDateFormat = birthDate[0] + "-" + birthDate[2] + "-" + birthDate[1];
        String SQL_getPatientNumber = "" +
                "SET XACT_ABORT ON\n" + // in case of an error, rollback will be issued automatically
                "BEGIN TRANSACTION\n" +
                "SELECT Nummer FROM Patient WHERE (Vorname = '" + BundleParser.getFirstName(PATIENT, parsedBundle) +
                "' AND Name = '" + BundleParser.getLastName(PATIENT, parsedBundle) +
                "' AND Geburtsdatum='"+ differentBirthDateFormat + " 00:00:00.000"+"');" +
                "COMMIT TRANSACTION";

        ResultSet rs = stmt.executeQuery(SQL_getPatientNumber);

        if (rs.next()) {
            int patientNumber = rs.getInt("Nummer");
            info("Patient number in the db: " + patientNumber);
            return patientNumber;
        } else {
            return -1;
        }
    }

    public int createPatient(Bundle parsedBundle, Statement stmt) throws SQLException {

        info("Attempting to create patient...");
        String anrede = "";
        String geschlecht = "";
        if (BundleParser.getGender(PATIENT, parsedBundle).equals("female")) {
            anrede = "Frau";
            geschlecht = "W";
        } else if (BundleParser.getGender(PATIENT, parsedBundle).equals("male")) {
            anrede = "Herrn";
            geschlecht = "M";
        }
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String timestamp1 = dtf1.format(now);
        String timestamp2 = dtf2.format(now);
        String timestamp3 = dtf3.format(now);
        String year = String.valueOf(now.getYear());
        LocalDate myLocal = LocalDate.now();
        String quarter = String.valueOf(myLocal.get(IsoFields.QUARTER_OF_YEAR));

        int publicNummer = getMaxIDFromTableAndColumn("Patient", "PublicNummer", stmt);
        int patientNummer = getMaxIDFromTableAndColumn("Patient", "Nummer", stmt);
        int scheinNummer = getMaxIDFromTableAndColumn("Schein", "Nummer", stmt);
        int stationsNummer = getMaxIDFromTableAndColumn("Lock", "StationsNummer", stmt);
        int recordNummer = getMaxIDFromTableAndColumn("Lock", "RecordNummer", stmt);

        String SQL_createNewPatient = "" +
            "SET XACT_ABORT ON\n" + // in case of an error, rollback will be issued automatically
            "BEGIN TRANSACTION\n" +
            // Patient table ----------------------------------------------------------------------------------------------------------
            "INSERT [dbo].[Patient] ([Nummer],[PublicNummer],[Suchwort],[Name],[Vorname],[Anrede],[Geburtsdatum],[Geschlecht]," +
                                    "[Straße],[LKZ],[PLZ],[Ort],[Versichertenart],[IKNr],[KasseSuchwort],[Versichertenstatus]," +
                                    "[OstWestStatus],[MandantAnlage],[DatumAnlage],[FarblicheKennzeichnung],[Raucher],[OIKennung]," +
                                    "[SoundexName],[AbrechnungPVS],[Hausnummer],[PostfachPLZ],[PostfachOrt],[PostfachLKZ]," +
                                    "[PibsAutoAkt],[KISFreigabe],[DatumÄnderung],[UserGeändert],[MandantGeändert],[UserAnlage])" +
            "VALUES(" + patientNummer + ", " + publicNummer + ", N'" + BundleParser.getFirstName(PATIENT, parsedBundle) + "', N'" +
                    BundleParser.getLastName(PATIENT, parsedBundle) + "', N'" + BundleParser.getFirstName(PATIENT, parsedBundle) + "','" +
                    anrede + "',{d '" + BundleParser.getBirthDate(PATIENT, parsedBundle) + "'},'" + geschlecht + "','" +
                    BundleParser.getStreet(PATIENT, parsedBundle) + "','D','" + BundleParser.getPostalCode(PATIENT, parsedBundle) +
                    "','" + BundleParser.getCity(PATIENT, parsedBundle) + "','M','101575519','TechnikerKra','1000',1,1,{d '" +
                    timestamp1 + "'},1,-2,-2,'S7500',-2,'" + BundleParser.getHouseNumber(PATIENT, parsedBundle) + "','','','D',1,0," +
                    "{ts '" + timestamp2 + "'},1,1,1)\n" +

            // KrablLink table --------------------------------------------------------------------------------------------------------
            "INSERT [dbo].[KrablLink] ([Uhrzeitanlage],[PatientNummer],[Satzart],[Datum],[Kategorie],[Kurzinfo],[Passwort]," +
                                      "[ScheinNummer],[Hintergrundfarbe],[Detail],[FreigabeStatus],[VersandStatus],[DatumÄnderung]," +
                                      "[UserGeändert],[MandantGeändert],[DatumAnlage],[UserAnlage],[MandantAnlage])" +
            "VALUES({ts '1899-12-30 " + timestamp3 + "'}," + patientNummer + ",40,{d '" + timestamp1 + "'},'KKEIN','',0," +
                    scheinNummer + ",0,'<KKData BEHANDLER=\"\"></KKData>',0,0,{ts '" + timestamp1 + " " + timestamp3 +
                    "'},1,1,{d '" + timestamp1 + "'},1,1)\n" +

            // TagProtokoll table -----------------------------------------------------------------------------------------------------
            "INSERT [dbo].[TagProtokoll] ([PatientNummer],[ScheinNummer],[Info],[Suchwort],[Datum],[LinkNummer],[Satzart]," +
                                         "[Text],[Mandant],[Benutzer],[Uhrzeit])" +
            "VALUES(" + patientNummer + "," + scheinNummer + ",'" + BundleParser.getLastName(PATIENT, parsedBundle) + ", " +
                    BundleParser.getFirstName(PATIENT, parsedBundle) + " (" + patientNummer + ")','Anmeldung',{d '" + timestamp1 +
                    "'},0,3000,'Neuer Patient erfasst.',1,1,{t '" + timestamp3 + "'})\n" +

            // Schein table -----------------------------------------------------------------------------------------------------------
            "INSERT [dbo].[Schein] ([Nummer],[PatientNummer],[KostenträgerTyp],[Kostenträgeruntergruppe],[Zonenkennzeichen]," +
                                   "[Scheinart],[Scheinuntergruppe],[Quartal],[Abrechnungsquartal],[VersichertenNr],[Anrede]," +
                                   "[Namenszusatz],[Titel],[Name],[Vorname],[Geburtsdatum],[LKZ],[PLZ],[Ort],[Straße]," +
                                   "[Versichertenart],[Geschlecht],[Chipkartenlesedatum],[Gültigkeitsdatum],[IKNr]," +
                                   "[KasseSuchwort],[Versichertenstatus],[OstWestStatus],[GO],[Abrechnungsgebiet],[Hinweis]," +
                                   "[Hinweisschalter],[Abgerechnet],[Abrechnungssperre],[Nachzügler],[Ersatzverfahren],[PVS]," +
                                   "[Markiert],[Ausgelagert],[MandantAnlage],[UserAnlage],[DatumAnlage],[MandantGeändert]," +
                                   "[UserGeändert],[DatumÄnderung],[KVDT],[KVPLZ],[WOP],[Rechnungsschema],[KartenleserZNr]," +
                                   "[KBVDatum],[DatumAusstellung],[DatumGültigkeit],[DatumAUBis],[DatumEntbindung]," +
                                   "[UrsacheDLeidens],[Behandler],[KennO1],[KennO3],[MuVoLSR],[MuVoAboRh],[MuVoHAH],[MuVoAK]," +
                                   "[SKTZusatz],[SKTGültigVon],[SKTGültigBis],[SKTPerson],[PTAnerkannt],[PTKlärungSU]," +
                                   "[PTDatumBescheid],[PLZErst],[ArztNrErst],[ÜberweisungArt],[ÜberweisungAn],[ÜberweisungVon]," +
                                   "[ÜberwVFremd],[Weiterbehandler],[DiagnoseVerdacht],[ErläuternderText],[Fallnummer]," +
                                   "[Bemerkung],[ScheinAbgegeben],[PQuittung],[ArztPatientenKontakt],[KHFallnummer]," +
                                   "[ChipCardType],[UhrzeitAnlage],[Abrechnungsverfahren],[KennzifferSA],[PflegekasseSuchwort]," +
                                   "[PflegekasseIKNr],[Überweisung],[ÜberweisungVonLANR],[ErstveranlasserLANR],[GUID],[ICCSN]," +
                                   "[IKNrGKV],[SAPVersVerhältnis],[SKTBemerkung],[Diagnose4207],[Befund4208],[Auftrag4205]," +
                                   "[EinschränkungLeistung4204],[VersichertenNrHistorisch],[HzVPraeventiv],[CDMVersion]," +
                                   "[Hausnummer],[Anschriftenzusatz],[Vorsatzwort],[PostfachPLZ],[PostfachOrt],[Postfach]," +
                                   "[PostfachLKZ],[BesonderePersonenGruppe],[DMPKennzeichnung],[VersicherungsschutzBeginn]," +
                                   "[Kostentraegername],[KISFreigabe])" +
            "VALUES(" + scheinNummer + "," + patientNummer + ",2,0,'',1,0,'" + quarter + year + "','" + quarter + year + "','','" +
                    anrede + "','','','" + BundleParser.getLastName(PATIENT, parsedBundle) + "','" + BundleParser.getFirstName(PATIENT, parsedBundle) +
                    "',{d '" + BundleParser.getBirthDate(PATIENT, parsedBundle) + "'},'D','" + BundleParser.getPostalCode(PATIENT, parsedBundle) +
                    "','" + BundleParser.getCity(PATIENT, parsedBundle) + "','" + BundleParser.getStreet(PATIENT, parsedBundle) + "','M','" +
                    geschlecht + "',{d '1899-12-30'},{d '1899-12-30'},'101575519','TechnikerKra','1000',1,27,0,'',0,{d '1899-12-30'}," +
                    "0,0,0,0,0,0,1,1,{d '" + timestamp1 + "'},1,1,{ts '" + timestamp1 + " " + timestamp3 + "'},1,'','','','',''," +
                    "{d '1899-12-30'},{d '1899-12-30'},{d '1899-12-30'},{d '1899-12-30'},0,'','','',0,0,0,0,'',{d '1899-12-30'}," +
                    "{d '1899-12-30'},0,0,0,'','','',0,'','','','','','','','',0,0,0,'',0,{t '" + timestamp3 + "'},0,'','',''," +
                    "0,'','','{4C037011-B14E-4182-BC6D-D789DF966944}','','','','','','','',0,'',0,'--','" +
                    BundleParser.getHouseNumber(PATIENT, parsedBundle) + "','','','','','','D','','',{d '1899-12-30'},'',0)\n" +

            // KrablLink table --------------------------------------------------------------------------------------------------------
            "INSERT [dbo].[KrablLink] ([PatientNummer],[Satzart],[Datum],[Kategorie],[Kurzinfo],[MandantGeändert],[UserGeändert]," +
                                      "[ScheinNummer],[Hintergrundfarbe],[MandantAnlage],[Uhrzeitanlage],[DatumÄnderung]," +
                                      "[DatumAnlage],[UserAnlage])" +
            "VALUES(" + patientNummer + ",1000,{d '" + timestamp1 + "'},'GKS','AS: Abrechnungsschein: Techniker Krankenkasse, " +
                    quarter + "/" + year.substring(year.length()-2) + ", M, Chipkarte fehlt !',1,1," + scheinNummer +
                    ",0,1,{ts '1899-12-30 " + timestamp3 + "'},{ts '" + timestamp1 + " " + timestamp3 + "'},{d '" + timestamp1 +
                    "'},1)\n" +

            // TagProtokoll table -----------------------------------------------------------------------------------------------------
            "INSERT [dbo].[TagProtokoll] ([PatientNummer],[ScheinNummer],[Info],[Suchwort],[Datum],[LinkNummer],[Satzart]," +
                                         "[Text],[Mandant],[Benutzer],[Uhrzeit])" +
            "VALUES(" + patientNummer + "," + scheinNummer + ",'" + BundleParser.getLastName(PATIENT, parsedBundle) + ", " +
                    BundleParser.getFirstName(PATIENT, parsedBundle) + " " + "(" + patientNummer + ")" + "','Anmeldung',{d '" +
                    timestamp1 + "'},0,4000,'Neuer ASA: Techniker Krankenkasse; " + quarter + "/" +
                    year.substring(year.length()-2) + "',1,1,{t '" + timestamp3 + "'})\n" +

            // Lock table -------------------------------------------------------------------------------------------------------------
            "INSERT [dbo].[Lock] ([Tabellenname],[StationsNummer],[RecordNummer],[DatumAnlage],[UhrzeitAnlage])" +
            "VALUES('SCHEIN'," + stationsNummer + "," + recordNummer + ",{d '" + timestamp1 + "'},{t '" + timestamp3 + "'})\n" +
            "COMMIT TRANSACTION";

        stmt.execute(SQL_createNewPatient);

        int patientNumber = checkIfPatientExistsInTheSystem(parsedBundle, stmt); // check if patient was created
        if (patientNumber > -1) {
            info("Patient was successfully created in the Db.");
            while (!counterTableWasUpdated(scheinNummer, patientNummer, stmt)) {
                updateCounterTable(scheinNummer, patientNummer, stmt);
            }
            info("Db counter successfully updated.");
            return patientNumber;
        } else {
            info("Some problem occurred and the patient was not created.");
            return createPatient(parsedBundle, stmt);
        }
    }

    public boolean isPatientDateOfDeathKnown(int patientNummer, Statement stmt) throws SQLException {

        String SQL_checkIfThereIsADateOfDeath = "SELECT VerstorbenAm FROM Patient WHERE Nummer = '" + patientNummer + "';";
        ResultSet rs = stmt.executeQuery(SQL_checkIfThereIsADateOfDeath);
        if (rs.next()) {
            String dateOfDeath = rs.getString("VerstorbenAm");
            if (!dateOfDeath.isEmpty() && !dateOfDeath.equals("1899-12-30 00:00:00.0")) {
                return true;
            }
        }
        return false;
    }

    public boolean isPatientDeadButTheDateIsUnknown(int patientNummer, Statement stmt) throws SQLException {

        String SQL_checkIfDateOfDeathIsUnknown = "SELECT RipDatumUnbekannt FROM Patient WHERE Nummer = '" + patientNummer + "';";
        ResultSet rs = stmt.executeQuery(SQL_checkIfDateOfDeathIsUnknown);
        if (rs.next()) {
            boolean isDateOfDeathUnknown = rs.getBoolean("RipDatumUnbekannt");
            if (isDateOfDeathUnknown) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfPatientIsAtTheHospital(int patientNummer, Statement stmt) throws SQLException {

        String SQL_getHospitalInfo = "SELECT Kurzinfo FROM KrablLink WHERE (" +
                                     " PatientNummer = '" + patientNummer + "'" +
                                     " AND (Kategorie = 'KH' OR Kategorie = 'SONO')" +
                                     " AND Nummer = (SELECT MAX(Nummer) FROM KrablLink WHERE PatientNummer = '" + patientNummer + "'));";
        ResultSet rs = stmt.executeQuery(SQL_getHospitalInfo);
        if (rs.next()) {
            String info = rs.getString("Kurzinfo");
            if (info.toLowerCase().replaceAll("\\s"," ").contains("im kh")) {
                return true;
            }
        }
        return false;
    }

    public void updateCounterTable(int scheinNummer, int patientNummer, Statement stmt) throws SQLException {

        String SQL_updateCounter = "" +
                "SET XACT_ABORT ON\n" + // in case of an error, rollback will be issued automatically
                "BEGIN TRANSACTION\n" +
                "UPDATE [dbo].[Counter] SET ID = " + scheinNummer + " WHERE [Key] = 'Schein.Nummer'\n" +
                "UPDATE [dbo].[Counter] SET ID = " + patientNummer + " WHERE [Key] = 'Patient.Nummer'\n" +
                "COMMIT TRANSACTION";

        stmt.execute(SQL_updateCounter);
    }

    public boolean counterTableWasUpdated(int scheinNummer, int patientNummer, Statement stmt) throws SQLException {

        String SQL_checkIfCounterTableWasUpdated = "SELECT * FROM Counter WHERE [Key] IN ('Schein.Nummer', 'Patient.Nummer')";
        ResultSet rs = stmt.executeQuery(SQL_checkIfCounterTableWasUpdated);
        boolean scheinNummerUpdated = false;
        boolean patientNummerUpdated = false;
        while (rs.next()) {
            if (Objects.equals(rs.getString("Key"), "Schein.Nummer")) {
                if (String.valueOf(scheinNummer).equals(rs.getString("ID"))) {
                    scheinNummerUpdated = true;
                }
            } else if (Objects.equals(rs.getString("Key"), "Patient.Nummer")) {
                if (String.valueOf(patientNummer).equals(rs.getString("ID"))) {
                    patientNummerUpdated = true;
                }
            }
        }
        return scheinNummerUpdated && patientNummerUpdated;
    }

    public void createMedication(List<String> tableEntry, int patientNummer, Bundle parsedBundle, Statement stmt) throws SQLException {

        info("Attempting to create medication...");

        // dosage
        String dosage = BundleParser.getDosage(MEDICATIONSTATEMENT, parsedBundle);
        String morgens;
        String mittags;
        String abends;
        String nachts;
        String dosierungsFreitext;
        String dosierEinheit;
        String dosierEinheitCode;
        if (dosage.contains("/")) {
            morgens = "NULL";
            mittags = "NULL";
            abends = "NULL";
            nachts = "NULL";
            dosierungsFreitext = "N'" + dosage + "'";
            dosierEinheit = "NULL";
            dosierEinheitCode = "NULL";
        } else {
            if (dosage.contains(",")) {
                dosage = dosage.replace(",", ".");
            }
            String[] dosageTimes = dosage.split("-");
            morgens = dosageTimes.length > 0 ? dosageTimes[0] : "0";
            mittags = dosageTimes.length > 1 ? dosageTimes[1] : "0";
            abends = dosageTimes.length > 2 ? dosageTimes[2] : "0";
            nachts = dosageTimes.length > 3 ? dosageTimes[3] : "0";
            dosierungsFreitext = "NULL";
            dosierEinheit = "1";
            dosierEinheitCode = "1";
        }

        // package size
        String packageSize = MedicationDbLookup.getPackageSize(tableEntry);
        if (!Objects.equals(packageSize, "")) {
            packageSize = packageSize.replace("N", "");
        } else if (MedicationDbLookup.getPharmaceuticalFormCode(tableEntry).equals("SMT")) {
            packageSize = "5";
        } else { // cases verified: "PUL", "KOM"
            packageSize = "6";
        }

        // ATC code
        String atcCode = MedicationDbLookup.getATC(tableEntry);
        String anatomieKlasse;
        if (Objects.equals(atcCode, "")) {
            atcCode = "NULL";
            anatomieKlasse = "N''";
        } else {
            atcCode = "N'" + MedicationDbLookup.getATC(tableEntry) + "'";
            anatomieKlasse = "N'" + MedicationDbLookup.getATC(tableEntry) + "'";
        }

        // composition
        String medicationIngredients = MedicationDbLookup.getComposition(tableEntry);
        String wirkstoff;
        String atcLangText = "";
        String allIngredientsString = "";
        List<String> allIngredientsList = new ArrayList<>();
        if (!Objects.equals(medicationIngredients, "")) {
            medicationIngredients = medicationIngredients.replaceAll("[\\\\n]*\\d+\\\\t*", "   ");
            medicationIngredients = medicationIngredients.trim();
            medicationIngredients = medicationIngredients.replaceAll("   ", " und ");

            allIngredientsString = "('" + medicationIngredients.replaceAll(" und ", "'), ('") + "')";
            allIngredientsList = Arrays.asList(medicationIngredients.split(" und ", -1));
            medicationIngredients = "N'" + medicationIngredients + "'";
            if (allIngredientsList.size() > 1) {
                wirkstoff = "N'Kombinationspräparat mit mehreren Wirkstoffen'";
                for (int i = 0; i < allIngredientsList.size(); i++) {
                    String ingredientNameSimplified = allIngredientsList.get(i);
                    if (ingredientNameSimplified.contains(" ")) {
                        String[] arr = ingredientNameSimplified.split(" ");
                        ingredientNameSimplified = arr[0];
                        atcLangText += ingredientNameSimplified + " und ";
                    }
                    if (i == allIngredientsList.size() - 1) {
                        atcLangText += ingredientNameSimplified;
                    }
                }
                atcLangText = "N'" + atcLangText + "'";
            } else {
                wirkstoff = medicationIngredients;
                atcLangText = wirkstoff;
            }
        } else {
            medicationIngredients = "NULL";
            wirkstoff = "N''";
            atcLangText = "N''";
        }

        // AVP
        String avp = MedicationDbLookup.getAVP(tableEntry);
        if (Objects.equals(avp, "")) {
            avp = "0.00";
        }

        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        String timestamp1 = dtf1.format(now).replace(" ", "T");
        String timestamp2 = dtf2.format(now).replace(" ", "T");

        int medikamentId = getMaxIDFromTableAndColumn("VerordnungsmodulMedikamentDbo", "Id", stmt);
        int verordnungsmodulRezepturWirkstoffId = getMaxIDFromTableAndColumn("VerordnungsmodulRezepturWirkstoffDbo", "Id", stmt);
        int rezeptId = getMaxIDFromTableAndColumn("VerordnungsmodulRezeptDbo", "Id", stmt);
        int medikationId = getMaxIDFromTableAndColumn("VerordnungsmodulMedikationDbo", "Id", stmt);
        int scheinMedNummer = getMaxIDFromTableAndColumn("ScheinMed", "Nummer", stmt);
        int krablLinkNummer = getMaxIDFromTableAndColumn("KrablLink", "Nummer", stmt);
        int krablLinkIDNummer = getMaxIDFromTableAndColumn("KrablLinkID", "Nummer", stmt);
        int scheinMedDatenNummer = getMaxIDFromTableAndColumn("ScheinMedDaten", "Nummer", stmt);
        int dosierungId = getMaxIDFromTableAndColumn("VerordnungsmodulDosierungDbo", "ID", stmt);

        String SQL_insertMedication = "" +
                "SET XACT_ABORT ON\n" + // in case of an error, rollback will be issued automatically
                "BEGIN TRANSACTION\n" +
                // VerordnungsmodulMedikamentDbo table (Prescription module medication table) ---------------------------------------------
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikamentDbo] ON\n" +
                "INSERT [dbo].[VerordnungsmodulMedikamentDbo] ([Id], [Pzn], [HandelsnameOderFreitext], [Hersteller], [AtcCodes], " +
                "[AtcCodeBedeutungen], [Darreichungsform], [DarreichungsformAsFreitext], " +
                "[PackungsgroesseText], [PackungsgroesseWert], [PackungsgroesseEinheit], " +
                "[PackungsgroesseEinheitCode], [Normgroesse], [Preis_IsSet], " +
                "[Preis_ApothekenVerkaufspreisCent], [Preis_FestbetragCent], " +
                "[Preis_MehrkostenCent], [Preis_ZuzahlungCent], [Preis_GesamtzuzahlungCent], " +
                "[Typ], [Farbe], [IsPriscus], [Created], [DatasetCreated], [UserCreated], " +
                "[LastChanged], [DatasetLastChanged], [UserLastChanged], [IsArchiviert], " +
                "[Hilfsmittelpositionsnummer])" +
                "VALUES (" + medikamentId + ", " + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) + ", N'" +
                MedicationDbLookup.getMedicationName(tableEntry) + "', N'" + MedicationDbLookup.getManufacturer(tableEntry) + "', " + atcCode +
                ", " + medicationIngredients + ", N'" + MedicationDbLookup.getPharmaceuticalFormCode(tableEntry) + "', N'" +
                MedicationDbLookup.getPharmaceuticalFormText(tableEntry) + "', N'', N'', N'', N'', " + packageSize + ", 1, 0, NULL," +
                " NULL, 500, 500, 1, NULL, 0, CAST(N'" + timestamp1 + "+00:00' AS DateTimeOffset), N'1', N'ANW-1'," +
                "CAST(N'" + timestamp1 + "+00:00' AS DateTimeOffset), N'1', N'ANW-1', 0, NULL)\n" +
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikamentDbo] OFF\n" +

                "DECLARE @idWirkstoff INT = " + verordnungsmodulRezepturWirkstoffId + ";" +
                "IF (" + allIngredientsList.size() + "> 0)\n" +

                "DECLARE @MyList TABLE (Value NVARCHAR(50))\n" +
                "INSERT INTO @MyList (Value) VALUES " + allIngredientsString + ";\n" +
                "DECLARE @value VARCHAR(50)\n" +
                "DECLARE db_cursor CURSOR FOR\n" +
                "SELECT Value FROM @MyList\n" +
                "OPEN db_cursor\n" +
                "FETCH NEXT FROM db_cursor INTO @value\n" +
                "WHILE @@FETCH_STATUS = 0\n" +

                "BEGIN\n" +

                // VerordnungsmodulRezepturWirkstoffDbo table (Prescription module active ingredient table) -------------------------------
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezepturWirkstoffDbo] ON\n" +
                "INSERT [dbo].[VerordnungsmodulRezepturWirkstoffDbo] ([Id], [AtcCode], [AtcCodeBedeutung], [Freitext], " +
                "[WirkstaerkeWert], [WirkstaerkeEinheit], [WirkstaerkeEinheitCode], " +
                "[ProduktmengeWert], [ProduktmengeEinheit], [ProduktmengeEinheitCode], " +
                "[MedikamentDbo_Id])" +
                "VALUES (" + "@idWirkstoff" + ", NULL, NULL, " + "@value" +
                ", N'599', N'Milligramm', N'mg', CAST(1.00 AS Decimal(18, 2)), N'FiTab', N'1', " + medikamentId + ")\n" +
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezepturWirkstoffDbo] OFF\n" +

                "SET @idWirkstoff = @idWirkstoff + 1;\n" +
                "FETCH NEXT FROM db_cursor INTO @value\n" +

                "END\n" +
                "CLOSE db_cursor\n" +
                "DEALLOCATE db_cursor\n" +

                // VerordnungsmodulRezeptDbo table (Prescription module recipe table) -----------------------------------------------------
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezeptDbo] ON\n" +
                "INSERT [dbo].[VerordnungsmodulRezeptDbo] ([Id], [RezeptGruppierung], [OnRezeptWeight], [MedikamentId], [PatientId], " +
                "[Ausstellungsdatum], [Erstellungsdatum], [BehandlerId], [KostentraegerId], " +
                "[BetriebsstaetteId], [AnzahlWert], [AnzahlEinheit], [AnzahlEinheitCode], " +
                "[RezeptZusatzinfos_IsSet], [RezeptZusatzinfos_Gebuehrenfrei], " +
                "[RezeptZusatzinfos_Unfall], [RezeptZusatzinfos_Arbeitsunfall], " +
                "[RezeptZusatzinfos_Noctu], [RezeptTyp], [KennzeichenStatus], [MPKennzeichen], " +
                "[AutIdem], [RezeptZeile], [VerordnungsStatus], [BtmSonderkennzeichen], " +
                "[TRezeptZusatzinfos_IsSet], [TRezeptZusatzinfos_SicherheitsbestimmungenEingehalten], " +
                "[TRezeptZusatzinfos_InformationfsmaterialAusgegeben], [TRezeptZusatzinfos_InOffLabel], " +
                "[HilfsmittelRezeptZusatzinfos_IsSet], [HilfsmittelRezeptZusatzinfos_ProduktnummerPrintType], " +
                "[HilfsmittelRezeptZusatzinfos_DiagnoseText], [HilfsmittelRezeptZusatzinfos_Zeitraum], " +
                "[AdditionalText], [Annotation], [ReasonForTreatment], [HasToApplyAdditionalTextToRezept], " +
                "[HasToApplyAnnotationTextToRezept], [IsHilfsmittelRezept], [IsImpfstoffRezept], " +
                "[VertragsZusatzinfos_ZusatzhinweisHzvGruen], [VertragsZusatzinfos_BvgKennzeichen], " +
                "[VertragsZusatzinfos_Begruendungspflicht], [VertragsZusatzinfos_StellvertreterMitgliedsNr], " +
                "[VertragsZusatzinfos_StellvertreterMediId], [VertragsZusatzinfos_StellvetreterLanr], " +
                "[Created], [DatasetCreated], [UserCreated], [LastChanged], [DatasetLastChanged], " +
                "[UserLastChanged], [IsArchiviert], [AsvTeamNummer], [StempelId], " +
                "[DosierungsPflichtAuswahl], [IsKuenstlicheBefruchtung], [VertragsZusatzinfos_IsSet], " +
                "[VertragsZusatzinfos_Wirkstoffzeile], [VertragsZusatzinfos_IsWirkstoffzeileActivated], " +
                "[IsErezept], [AbgabehinweisApotheke])" +
                "VALUES (" + rezeptId + ", N'', 0, " + medikamentId + ", N'" + patientNummer + "', CAST(N'" + timestamp1 + "' AS DateTime2), " +
                "CAST(N'" + timestamp1 + "' AS DateTime2), N'BEH-1', N'" + getPatientScheinNummer(patientNummer, stmt) + "', N'1', N'1', N'Pckg', N'1', 1, 0, 0, 0, 0, 0, 0, 0, 1, N'" +
                MedicationDbLookup.getMedicationName(tableEntry) + "\n" + "PZN" + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) +
                " »" + BundleParser.getDosage(MEDICATIONSTATEMENT, parsedBundle) + "«'" + ", 1, NULL, 0, 0, 0, 0, 0, 0, NULL, 0, NULL, " +
                "NULL, NULL, 1, 1, 0, 0, NULL, 0, 0, NULL, NULL, NULL, CAST(N'" + timestamp1 + "+02:00' AS DateTimeOffset), N'1', " +
                "N'ANW-1', CAST(N'" + timestamp1 + "+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0, NULL, N'101', 3, 0, 0, NULL, 0, " +
                "1, NULL)\n" +
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezeptDbo] OFF\n" +

                // VerordnungsmodulMedikationDbo table (Prescription module medication table) ---------------------------------------------
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikationDbo] ON\n" +
                "INSERT [dbo].[VerordnungsmodulMedikationDbo] ([Id], [RezeptId], [MedikamentId], [PatientId], [DatumVerordnet], " +
                "[IsDauermedikation], [MpKennzeichen], [DatumAbgesetzt], [GrundAbgesetzt], " +
                "[Created], [DatasetCreated], [UserCreated], [LastChanged], " +
                "[DatasetLastChanged], [UserLastChanged], [IsArchiviert])" +
                "VALUES (" + medikationId + "," + rezeptId + ", " + medikamentId + ", N'" + patientNummer + "', CAST(N'" + timestamp1 +
                "' AS DateTime2), 0, 0, NULL, NULL, CAST(N'" + timestamp1 + "+02:00' AS DateTimeOffset), N'1', N'ANW-1', " +
                "CAST(N'" + timestamp1 + "+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0)\n" +
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikationDbo] OFF\n" +

                // ScheinMed table -------------------------------------------------------------------------------------------------------
                "SET IDENTITY_INSERT [dbo].[ScheinMed] ON\n" +
                "INSERT [dbo].[ScheinMed] ([Nummer], [ScheinNummer], [PatientNummer], [Suchwort], [PZN], [Verordnungstyp], " +
                "[Betragsspeicher], [AVP], [Festbetrag], [Bruttobetrag], [Nettobetrag], [Diagnose], " +
                "[ICD], [AutIdem], [Grenzpreis], [MandantGeändert], [UserGeändert], [DatumÄnderung], " +
                "[UnteresPreisdrittel], [KrablLinkNrRezept], [BTMGebühr], [DDDPackung], [Übertragen], " +
                "[FarbKategorie], [Merkmal], [KrablLinkNr])" +
                "VALUES (" + scheinMedNummer + ", " + getPatientScheinNummer(patientNummer, stmt) + "," + patientNummer + ", N'" +
                BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) + "', N'" + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) +
                "', N'LM', 100, " + avp + ", 0.0000, " + avp +
                ", 11.6500, N'', N'', 1, 0.0000, 1, 1, CAST(N'" + timestamp2 + "' AS DateTime), 0, 0, 0.0000, 0, 0, N'', N''," +
                krablLinkNummer + ")\n" +
                "SET IDENTITY_INSERT [dbo].[ScheinMed] OFF\n" +

                // KrablLink table ------------------------------------------------------------------------------------------------------
                "SET IDENTITY_INSERT [dbo].[KrablLink] ON\n" +
                "INSERT [dbo].[KrablLink] ([Nummer], [PatientNummer], [Satzart], [Datum], [Kategorie], [Kurzinfo], [Passwort], " +
                "[MandantGeändert], [UserGeändert], [DatumÄnderung], [ScheinNummer], [GruppenNummer], " +
                "[Hintergrundfarbe], [Detail], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantFremd], " +
                "[FreigabeStatus], [VersandStatus], [Uhrzeitanlage])" +
                "VALUES (" + krablLinkNummer + "," + patientNummer + ", 4000, CAST(N'" + timestamp2 + "' AS DateTime), N'LM', N'Medikament: " + MedicationDbLookup.getMedicationName(tableEntry) + ", Dos.: " +
                BundleParser.getDosage(MEDICATIONSTATEMENT, parsedBundle) + ", PZN: " + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) +
                ", AVP: " + avp + "', 0, 1, 1, CAST(N'" + timestamp2 + "' AS DateTime), " + getPatientScheinNummer(patientNummer, stmt) + ", 0, 0, " +
                "N'\n', 1, 1, CAST(N'" + timestamp2 + "' AS DateTime), 0, 0, 0, " +
                "CAST(N'1899-12-30T15:04:31.000' AS DateTime))\n" +
                "SET IDENTITY_INSERT [dbo].[KrablLink] OFF\n" +

                // KrablLinkID table ---------------------------------------------------------------------------------------------------
                "SET IDENTITY_INSERT [dbo].[KrablLinkID] ON\n" +
                "INSERT [dbo].[KrablLinkID] ([Nummer], [PatientNummer], [KrablLinkNummer], [IDType], [ID], [MandantAnlage], [UserAnlage]," +
                "[DatumAnlage], [MandantGeändert], [UserGeändert], [DatumÄnderung], [Fremdsystem], " +
                "[Erzeugersystem], [Status], [Bemerkung])" +
                "VALUES (" + krablLinkIDNummer + ", " + patientNummer + "," + krablLinkNummer + ", 7, " + krablLinkIDNummer + ", 1, 1, " +
                "CAST(N'" + timestamp2 + "' AS DateTime), 1, 1, CAST(N'" + timestamp2 + "' AS DateTime), 1, 1, 0, " +
                "N'f4835bad-c18b-4653-b706-89b6f5b06772')\n" +
                "SET IDENTITY_INSERT [dbo].[KrablLinkID] OFF\n" +

                // ScheinMedDaten ------------------------------------------------------------------------------------------------------
                "SET IDENTITY_INSERT [dbo].[ScheinMedDaten] ON\n" +
                "INSERT [dbo].[ScheinMedDaten] ([Nummer], [Suchwort], [Klasse], [Typ], [Langtext], [Packungsart], [NNummer], " +
                "[Darreichungsform], [Packungsgröße], [PZNummer], [StandardDosierung], [Betrag], " +
                "[Festbetrag], [Grenzpreis], [Anatomieklasse], [Hersteller], [Wirkstoff], [Generika], " +
                "[NurPrivatrezept], [BTMPräparat], [Bevorzugt], [Geschützt], [AußerHandel], [Negativliste], " +
                "[Rückruf], [Datenanbieter], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantGeändert], " +
                "[UserGeändert], [DatumÄnderung], [UnteresPreisdrittel], [OTC], [Zuzahlungsbefreit], " +
                "[WirkstoffMenge], [WirkstoffMengenEinheit], [LetzterPreis], [PreisÄnderung], [LifeStyle], " +
                "[ApothekenPflicht], [VerschreibungsPflicht], [Reimport], [AlternativeVorhanden], [ZweitMeinung], " +
                "[PNH], [PNHBezeichnung], [DDDKosten], [OTX], [TRezept], [KombiPraeparat], [AutIdemKennung], " +
                "[PriscusListe], [NeueinfuehrungsDatum], [HerstellerID], [ErstattungsBetrag], " +
                "[DokuPflichtTransfusion], [VOEinschraenkungAnlage3], [Therapiehinweis], [MedizinProdukt], " +
                "[MPVerordnungsfaehig], [MPVOBefristung], [Verordnet], [MitReimport], [WSTZeile], [WSTNummer], " +
                "[IMMVorhanden], [Sortierung], [ATCLangtext], [ErstattungStattAbschlag], [VertragspreisNach129_5]," +
                "[NormGesamtzuzahlung], [Verordnungseinschraenkung], [Verordnungsausschluss], [VOAusschlussAnlage3])" +
                "VALUES (" + scheinMedDatenNummer + ", N'" + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) + "', N'', 1, N'" +
                MedicationDbLookup.getMedicationName(tableEntry) + "', 1, 1, 0, N'5', N'" + BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle) +
                "', N'', " + avp + ", 0.0000, 0.0000, " + anatomieKlasse + ", N'" + MedicationDbLookup.getManufacturer(tableEntry) + "', " +
                wirkstoff + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, CAST(N'" + timestamp2 + "' AS DateTime), " +
                "1, 1, CAST(N'" + timestamp2 + "' AS DateTime), 0, 0, 0, 3, N'mg', 0.0000, N'', 0, 0, 0, 0, 0, 0, N'', N'', 0.0000, 0, 0, " +
                "0, 0, 0, CAST(N'1899-12-30T00:00:00.000' AS DateTime), 0, 0.0000, 0, 0, 0, 0, 0, CAST(N'1899-12-30T00:00:00.000' AS DateTime), " +
                "0, 0, N'', N'', 0, N'', " + atcLangText + ", 0, 0, 0.0000, 0, 0, 0)\n" +
                "SET IDENTITY_INSERT [dbo].[ScheinMedDaten] OFF\n" +

                // VerordnungsmodulDosierungDbo table ----------------------------------------------------------------------------------
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulDosierungDbo] ON\n" +
                "INSERT [dbo].[VerordnungsmodulDosierungDbo] ([ID], [Morgens], [Mittags], [Abends], [Nachts], [DosierungsFreitext], " +
                "[StartOfTaking], [EndOfTaking], [Status], [DosierEinheit], " +
                "[DosierEinheitCode], [RezeptDbo_Id], [MedikationDbo_Id])" +
                "VALUES (" + dosierungId + "," + morgens + "," + mittags + "," + abends + "," + nachts + ", " + dosierungsFreitext + ", NULL, NULL, 1, " +
                dosierEinheit + "," + dosierEinheitCode + "," + rezeptId + "," + medikationId + ")\n" +
                "SET IDENTITY_INSERT [dbo].[VerordnungsmodulDosierungDbo] OFF\n" +
                "COMMIT TRANSACTION";

        stmt.execute(SQL_insertMedication);

        String SQL_getPatientMedication = "SELECT * FROM VerordnungsmodulMedikamentDbo A INNER JOIN VerordnungsmodulMedikationDbo B ON B.MedikamentId = A.Id WHERE PatientId = '" + patientNummer + "'; \n";

        ResultSet rs = stmt.executeQuery(SQL_getPatientMedication);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        info("Medications currently on the system for this Patient:");
        StringBuilder medicationOfPatient = new StringBuilder();
        while (rs.next()) {
            medicationOfPatient.append("\n");
            for (int i = 1; i <= columnsNumber; i++) {
                if ((i <= 3) || (i == 5) || (i == 6)) {
                    medicationOfPatient.append(rs.getString(i)).append(" (").append(rsmd.getColumnName(i)).append(")  ");
                }
            }
        }
        String medicationsOfPatient = String.valueOf(medicationOfPatient);
        info(medicationsOfPatient);
    }

    public int getMaxIDFromTableAndColumn(String table, String column, Statement stmt) throws SQLException {

        String SQL_getMaxId = "SELECT * FROM " + table + "\n" +
                "WHERE " + column + " = (\n" +
                "SELECT MAX(" + column + ") FROM " + table + ")";
        ResultSet rs = stmt.executeQuery(SQL_getMaxId);
        if (rs.next()) {
            int id = rs.getInt(column);
            // info("Id value for " + column + " = " + id);
            return id + 1;
        }
        return 1;
    }

    public int getPatientScheinNummer(int patientNummer, Statement stmt) throws SQLException {

        String SQL_getScheinNummer = "SELECT Nummer FROM Schein WHERE (PatientNummer = '" + patientNummer + "')";
        ResultSet rs = stmt.executeQuery(SQL_getScheinNummer);
        if (rs.next()) {
            int scheinNummer = rs.getInt("Nummer");
            // info("Scheinnummer = " + scheinNummer);
            return scheinNummer;
        }
        return -1;
    }

    public void deleteAllMedications(Statement stmt) throws SQLException {

        String SQL_deleteVerordnungsmodulMedikamentDbo = "DELETE FROM VerordnungsmodulMedikamentDbo WHERE Id >= 314";
        String SQL_deleteVerordnungsmodulRezepturWirkstoffDbo = "DELETE FROM VerordnungsmodulRezepturWirkstoffDbo WHERE Id >= 314";
        String SQL_deleteVerordnungsmodulMedikationDbo = "DELETE FROM VerordnungsmodulMedikationDbo WHERE Id >= 314";
        String SQL_deleteVerordnungsmodulRezeptDbo = "DELETE FROM VerordnungsmodulRezeptDbo WHERE PatientId >= 39";
        String SQL_deleteVerordnungsmodulDosierungDbo = "DELETE FROM VerordnungsmodulDosierungDbo WHERE Id >= 314";
        String SQL_deleteScheinMed = "DELETE FROM ScheinMed WHERE PatientNummer >= 39";
        // String SQL_deleteKrablLink = "DELETE FROM KrablLink WHERE PatientNummer >= 39"; // might also delete information related to patient insertion
        String SQL_deleteKrablLinkID = "DELETE FROM KrablLinkID WHERE PatientNummer >= 39";
        String SQL_deleteScheinMedDaten = "DELETE FROM ScheinMedDaten WHERE Nummer >= 363";
        stmt.execute(SQL_deleteScheinMedDaten);
        stmt.execute(SQL_deleteKrablLinkID);
        // stmt.execute(SQL_deleteKrablLink); // might also delete information related to patient insertion
        stmt.execute(SQL_deleteScheinMed);
        stmt.execute(SQL_deleteVerordnungsmodulDosierungDbo);
        stmt.execute(SQL_deleteVerordnungsmodulMedikationDbo);
        stmt.execute(SQL_deleteVerordnungsmodulRezeptDbo);
        stmt.execute(SQL_deleteVerordnungsmodulRezepturWirkstoffDbo);
        stmt.execute(SQL_deleteVerordnungsmodulMedikamentDbo);
    }

    public void deleteAllPatients(Statement stmt) throws SQLException {

        String SQL_deletePatient = "DELETE FROM Patient WHERE Nummer >= 39";
        String SQL_deleteTagProtokoll = "DELETE FROM TagProtokoll WHERE PatientNummer >= 39";
        String SQL_deleteKrablLink = "DELETE FROM KrablLink WHERE PatientNummer >= 39";
        String SQL_deleteSchein = "DELETE FROM Schein WHERE PatientNummer >= 39";
        String SQL_deleteLock = "DELETE FROM Lock WHERE RecordNummer >= 39";
        stmt.execute(SQL_deletePatient);
        stmt.execute(SQL_deleteTagProtokoll);
        stmt.execute(SQL_deleteKrablLink);
        stmt.execute(SQL_deleteSchein);
        stmt.execute(SQL_deleteLock);
        String SQL_updateCounterScheinNummer = "UPDATE [dbo].[Counter] SET ID = " + 40 + " WHERE [Key] = 'Schein.Nummer'";
        String SQL_updateCounterPatientNummer = "UPDATE [dbo].[Counter] SET ID = " + 38 + " WHERE [Key] = 'Patient.Nummer'";
        stmt.execute(SQL_updateCounterScheinNummer);
        stmt.execute(SQL_updateCounterPatientNummer);
    }

    public void info(String s) {
        currentLog += s;
        log.info(s);
    }
}
