package health.medunited.isynet;

import health.medunited.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;

public class IsynetMSQLConnector {

    public static void main(String[] args) {

        String bundleString = "{\"resourceType\": \"Bundle\",\t\"id\": \"\", \"meta\": { \"lastUpdated\": \"\", \"profile\": [\t\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2\"\t]},\t\"identifier\": { \"system\": \"https://gematik.de/fhir/NamingSystem/PrescriptionID\", \"value\": \"\"}, \"type\": \"document\",\"timestamp\": \"2022-08-01T19:41:55.47Z\",\"entry\" : [{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"Practitioner\",\"id\":\"1429\",\"meta\":{\"versionId\":\"11\",\"lastUpdated\":\"2022-07-29T13:46:49.326+01:00\",\"source\":\"#IpFF8lgXn51VD1hL\"},\"extension\":[{\"url\":null,\"valueString\":\"isynet\"}],\"name\":[{\"use\":\"official\",\"family\":\"Hoffmann\",\"given\":[\"Emma\"]}],\"telecom\":[{\"value\":\"beatriz.correia@incentergy.de\"},{\"value\":\"111111111\"},{\"value\":\"30 123456789\"}],\"address\":[{\"use\":\"home\",\"line\":[\"Alt-Moabit\"],\"city\":\"Berlin\",\"postalCode\":\"10555\"}]}},{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"Patient\",\"id\":\"2061\",\"meta\":{\"versionId\":\"1\",\"lastUpdated\":\"2022-06-27T18:13:32.099+01:00\",\"source\":\"#zLn26PfiLgUR54w2\"},\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Max <b>MUSTERMANN </b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Address</td><td><span>Orange street </span><br/><span>Berlin </span></td></tr><tr><td>Date of birth</td><td><span>01 January 1995</span></td></tr></tbody></table></div>\"},\"name\":[{\"use\":\"official\",\"family\":\"Mustermann\",\"given\":[\"Max\"]}],\"gender\":\"male\",\"birthDate\":\"1995-01-01\",\"address\":[{\"use\":\"home\",\"line\":[\"Orange street\"],\"city\":\"Berlin\",\"postalCode\":\"12345\"}],\"generalPractitioner\":[{\"reference\":\"Practitioner/1429\"}],\"managingOrganization\":{\"reference\":\"Organization/155\"}}},{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"MedicationStatement\",\"id\":\"2164\",\"meta\":{\"versionId\":\"1\",\"lastUpdated\":\"2022-06-29T14:16:58.222+01:00\",\"source\":\"#8vvT7jy5XQ8aQNJZ\"},\"identifier\":[{\"value\":\"16849772\"}],\"medicationCodeableConcept\":{\"text\":\"Penicillin G HEXAL 1MIO I.E.Pu.z.H.e.Inj.o.Inf.Lsg\"},\"subject\":{\"reference\":\"Patient/2061\"},\"informationSource\":{\"reference\":\"Practitioner/1429\"},\"derivedFrom\":[{\"reference\":\"Organization/555\"}],\"dosage\":[{\"text\":\"0-1-0-1\"}]}},{\"fullUrl\": \"\",\"resource\": {\"resourceType\":\"Organization\",\"id\":\"555\",\"meta\":{\"versionId\":\"3\",\"lastUpdated\":\"2022-06-29T12:24:06.088+01:00\",\"source\":\"#q7CatK72HI4CXj8u\"},\"name\":\"Kaiser Apotheke\",\"telecom\":[{\"value\":\"+49 300000000\"},{\"value\":\"beatriz.correia@incentergy.de\"}],\"address\":[{\"line\":[\"Bergmannstraße\"],\"city\":\"Berlin\",\"postalCode\":\"10999\"}]}}]}";
        JSONObject json = new JSONObject(bundleString);
        JSONObject practitionerJSON = (JSONObject) ((JSONArray) json.get("entry")).get(0);
        JSONObject patientJSON = (JSONObject) ((JSONArray) json.get("entry")).get(1);
        JSONObject medicationStatementJSON = (JSONObject) ((JSONArray) json.get("entry")).get(2);
        JSONObject pharmacyJSON = (JSONObject) ((JSONArray) json.get("entry")).get(3);

        Practitioner practitioner = parsePractitioner(practitionerJSON);
        System.out.println("[ PRACTITIONER ]" + " first name: " + practitioner.getFirstName() + " // last name: " + practitioner.getLastName() + " // street: " + practitioner.getStreet() + " // house number: " + practitioner.getHouseNumber() + " // city: " + practitioner.getCity() + " // postal code: " + practitioner.getPostalCode() + " // e-mail: " + practitioner.getEmail() + " // phone: " + practitioner.getPhone() + " // fax: " + practitioner.getFax() + " // modality: " + practitioner.getModality());

        Patient patient = parsePatient(patientJSON);
        System.out.println("[ PATIENT ]" + " first name: " + patient.getFirstName() + " // last name: " + patient.getLastName() + " // street: " + patient.getStreet() + " // house number: " + patient.getHouseNumber() + " // city: " + patient.getCity() + " // postal code: " + patient.getPostalCode() + " // gender: " + patient.getGender() + " // birthDate: " + patient.getBirthDate());

        MedicationStatement medicationStatement = parseMedicationStatement(medicationStatementJSON);
        System.out.println("[ MEDICATION STATEMENT ]" + " medication name: " + medicationStatement.getMedicationName() + " // PZN: " + medicationStatement.getPZN() + " // dosage: " + medicationStatement.getDosage());

        Pharmacy pharmacy = parsePharmacy(pharmacyJSON);
        System.out.println("[ PHARMACY ]" + " name: " + pharmacy.getName() + " // street: " + pharmacy.getStreet() + " // house number: " + pharmacy.getHouseNumber() + " // city: " + pharmacy.getCity() + " // postal code: " + pharmacy.getPostalCode() + " // phone: " + pharmacy.getPhone() + " // email: " + pharmacy.getEmail());

        Bundle bundle = new Bundle(practitioner, patient, medicationStatement, pharmacy);

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String connectionUrl = "jdbc:sqlserver://192.168.178.50:1433;databaseName=WINACS;user=AP31;password=722033800; trustServerCertificate=true";
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {

            String SQL_get_patient_nummer = "" +
//                    SET XACT_ABORT ON: in case of an error, rollback will be issued automatically
                    "SET XACT_ABORT ON\n" +
                    "begin transaction\n" +
                    "SELECT Nummer FROM Patient WHERE (Vorname = '" + bundle.getPatient().getFirstName() + "' AND Name = '" + bundle.getPatient().getLastName() + "' AND Geburtsdatum='"+ bundle.getPatient().getBirthDate() + " 00:00:00.000"+"');" +
                    "commit transaction";

//            QUERY EXECUTED: Get PatientNummer
            ResultSet rs = stmt.executeQuery(SQL_get_patient_nummer);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            String patientNummer = "";
            while (rs.next()) {
                System.out.println("SQL_get_patient_nummer:");
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    patientNummer = rs.getString(i);
                    System.out.print(patientNummer + " (" + rsmd.getColumnName(i) + ")");
                }
            }

            String SQL_delete_VerordnungsmodulMedikamentDbo = "DELETE FROM VerordnungsmodulMedikamentDbo WHERE Id > 0";
            String SQL_delete_VerordnungsmodulRezepturWirkstoffDbo = "DELETE FROM VerordnungsmodulRezepturWirkstoffDbo WHERE Id > 0";
            String SQL_delete_VerordnungsmodulMedikationDbo = "DELETE FROM VerordnungsmodulMedikationDbo WHERE Id > 0";
            String SQL_delete_VerordnungsmodulRezeptDbo = "DELETE FROM VerordnungsmodulRezeptDbo WHERE Id > 0";
            String SQL_delete_ScheinMed = "DELETE FROM ScheinMed WHERE Nummer > 0";
            String SQL_delete_KrablLink = "DELETE FROM KrablLink WHERE Nummer > 0";
            String SQL_delete_KrablLinkID = "DELETE FROM KrablLinkID WHERE Nummer > 0";
            String SQL_delete_ScheinMedDaten = "DELETE FROM ScheinMedDaten WHERE Nummer > 0";

//            QUERIES EXECUTED: Delete entries in tables
            stmt.execute(SQL_delete_ScheinMedDaten);
            stmt.execute(SQL_delete_KrablLinkID);
            stmt.execute(SQL_delete_KrablLink);
            stmt.execute(SQL_delete_ScheinMed);
            stmt.execute(SQL_delete_VerordnungsmodulMedikationDbo);
            stmt.execute(SQL_delete_VerordnungsmodulRezeptDbo);
            stmt.execute(SQL_delete_VerordnungsmodulRezepturWirkstoffDbo);
            stmt.execute(SQL_delete_VerordnungsmodulMedikamentDbo);

            String IDvalue = "3";
            String SQL_insert_medication = "" +
//                    SET XACT_ABORT ON: in case of an error, rollback will be issued automatically
                    "SET XACT_ABORT ON\n" +
                    "begin transaction\n" +
//                    Insert into VerordnungsmodulMedikamentDbo table (Prescription module medication table)
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikamentDbo] ON\n" +
                    "INSERT [dbo].[VerordnungsmodulMedikamentDbo] ([Id], [Pzn], [HandelsnameOderFreitext], [Hersteller], [AtcCodes], [AtcCodeBedeutungen], [Darreichungsform], [DarreichungsformAsFreitext], [PackungsgroesseText], [PackungsgroesseWert], [PackungsgroesseEinheit], [PackungsgroesseEinheitCode], [Normgroesse], [Preis_IsSet], [Preis_ApothekenVerkaufspreisCent], [Preis_FestbetragCent], [Preis_MehrkostenCent], [Preis_ZuzahlungCent], [Preis_GesamtzuzahlungCent], [Typ], [Farbe], [IsPriscus], [Created], [DatasetCreated], [UserCreated], [LastChanged], [DatasetLastChanged], [UserLastChanged], [IsArchiviert], [Hilfsmittelpositionsnummer])" +
                    "VALUES (" + IDvalue + ", " + bundle.getMedicationStatement().getPZN() + ", N'Penicillin G HEXAL 1MIO I.E.Pu.z.H.e.Inj.o.Inf.Lsg', N'Hexal AG', N'J01CE01', N'Benzylpenicillin', N'PII', N'Pulver zur Herstellung einer Injektions- oder Infusionslsg.', N'10', N'10', N'Stück', N'St', 3, 1, 3296, NULL, NULL, 500, 500, 1, NULL, 0, CAST(N'2022-07-18T16:02:46.6187778+02:00' AS DateTimeOffset), N'1', N'ANW-1', CAST(N'2022-07-18T16:02:46.6187778+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0, NULL)\n" +
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikamentDbo] OFF\n" +
//                    Insert into VerordnungsmodulRezepturWirkstoffDbo table (Prescription module active ingredient table)
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezepturWirkstoffDbo] ON\n" +
                    "INSERT [dbo].[VerordnungsmodulRezepturWirkstoffDbo] ([Id], [AtcCode], [AtcCodeBedeutung], [Freitext], [WirkstaerkeWert], [WirkstaerkeEinheit], [WirkstaerkeEinheitCode], [ProduktmengeWert], [ProduktmengeEinheit], [ProduktmengeEinheitCode], [MedikamentDbo_Id])" +
                    "VALUES ("+ IDvalue +", NULL, NULL, N'Benzylpenicillin-Natrium', N'599', N'Milligramm', N'mg', CAST(1.00 AS Decimal(18, 2)), N'PIjIf', N'1', "+ IDvalue +")\n" +
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezepturWirkstoffDbo] OFF\n" +
//                    Insert into VerordnungsmodulRezeptDbo table (Prescription module recipe table)
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezeptDbo] ON\n" +
                    "INSERT [dbo].[VerordnungsmodulRezeptDbo] ([Id], [RezeptGruppierung], [OnRezeptWeight], [MedikamentId], [PatientId], [Ausstellungsdatum], [Erstellungsdatum], [BehandlerId], [KostentraegerId], [BetriebsstaetteId], [AnzahlWert], [AnzahlEinheit], [AnzahlEinheitCode], [RezeptZusatzinfos_IsSet], [RezeptZusatzinfos_Gebuehrenfrei], [RezeptZusatzinfos_Unfall], [RezeptZusatzinfos_Arbeitsunfall], [RezeptZusatzinfos_Noctu], [RezeptTyp], [KennzeichenStatus], [MPKennzeichen], [AutIdem], [RezeptZeile], [VerordnungsStatus], [BtmSonderkennzeichen], [TRezeptZusatzinfos_IsSet], [TRezeptZusatzinfos_SicherheitsbestimmungenEingehalten], [TRezeptZusatzinfos_InformationfsmaterialAusgegeben], [TRezeptZusatzinfos_InOffLabel], [HilfsmittelRezeptZusatzinfos_IsSet], [HilfsmittelRezeptZusatzinfos_ProduktnummerPrintType], [HilfsmittelRezeptZusatzinfos_DiagnoseText], [HilfsmittelRezeptZusatzinfos_Zeitraum], [AdditionalText], [Annotation], [ReasonForTreatment], [HasToApplyAdditionalTextToRezept], [HasToApplyAnnotationTextToRezept], [IsHilfsmittelRezept], [IsImpfstoffRezept], [VertragsZusatzinfos_ZusatzhinweisHzvGruen], [VertragsZusatzinfos_BvgKennzeichen], [VertragsZusatzinfos_Begruendungspflicht], [VertragsZusatzinfos_StellvertreterMitgliedsNr], [VertragsZusatzinfos_StellvertreterMediId], [VertragsZusatzinfos_StellvetreterLanr], [Created], [DatasetCreated], [UserCreated], [LastChanged], [DatasetLastChanged], [UserLastChanged], [IsArchiviert], [AsvTeamNummer], [StempelId], [DosierungsPflichtAuswahl], [IsKuenstlicheBefruchtung], [VertragsZusatzinfos_IsSet], [VertragsZusatzinfos_Wirkstoffzeile], [VertragsZusatzinfos_IsWirkstoffzeileActivated], [IsErezept], [AbgabehinweisApotheke])" +
                    "VALUES (" + IDvalue + ", N'f4835bad-c18b-4653-b706-89b6f5b06772', 1, " + IDvalue + ", N'1', CAST(N'2022-07-18T16:02:46.4803941' AS DateTime2), CAST(N'2022-07-18T16:02:46.5821216' AS DateTime2), N'BEH-1', N'1', N'1', N'1', N'Pckg', N'1', 1, 0, 0, 0, 0, 0, 0, 0, 1, N'PENICILLIN G HEXAL 1 MIO 599mg PII 10St\n" +
                    "N3 PZN" + bundle.getMedicationStatement().getPZN() + " »Dj«', 1, NULL, 0, 0, 0, 0, 0, 0, NULL, 0, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, 0, NULL, NULL, NULL, CAST(N'2022-07-18T16:02:46.6337384+02:00' AS DateTimeOffset), N'1', N'ANW-1', CAST(N'2022-07-18T16:25:13.0978550+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0, NULL, N'101', 1, 0, 0, NULL, 0, 1, NULL)\n" +
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezeptDbo] OFF\n" +
//                    Insert into VerordnungsmodulMedikationDbo table (Prescription module medication table)
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikationDbo] ON\n" +
                    "INSERT [dbo].[VerordnungsmodulMedikationDbo] ([Id], [RezeptId], [MedikamentId], [PatientId], [DatumVerordnet], [IsDauermedikation], [MpKennzeichen], [DatumAbgesetzt], [GrundAbgesetzt], [Created], [DatasetCreated], [UserCreated], [LastChanged], [DatasetLastChanged], [UserLastChanged], [IsArchiviert])" +
                    "VALUES (" + IDvalue + "," + IDvalue + ", " + IDvalue + ", N'1', CAST(N'2022-07-18T16:02:46.4803941' AS DateTime2), 0, 0, NULL, NULL, CAST(N'2022-07-18T16:02:46.6477011+02:00' AS DateTimeOffset), N'1', N'ANW-1', CAST(N'2022-07-18T16:02:46.6477011+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0)\n" +
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikationDbo] OFF\n" +
//                    Insert into ScheinMed table
                    "SET IDENTITY_INSERT [dbo].[ScheinMed] ON\n" +
                    "INSERT [dbo].[ScheinMed] ([Nummer], [ScheinNummer], [PatientNummer], [Suchwort], [PZN], [Verordnungstyp], [Betragsspeicher], [AVP], [Festbetrag], [Bruttobetrag], [Nettobetrag], [Diagnose], [ICD], [AutIdem], [Grenzpreis], [MandantGeändert], [UserGeändert], [DatumÄnderung], [UnteresPreisdrittel], [KrablLinkNrRezept], [BTMGebühr], [DDDPackung], [Übertragen], [FarbKategorie], [Merkmal], [KrablLinkNr])" +
                    "VALUES (" + IDvalue + ", 1," + patientNummer + ", N'" + bundle.getMedicationStatement().getPZN() + "', N'" + bundle.getMedicationStatement().getPZN() + "', N'LM', 100, 32.9600, 0.0000, 32.9600, 27.9600, N'', N'', 1, 0.0000, 1, 1, CAST(N'2022-07-18T16:02:46.707' AS DateTime), 0, 0, 0.0000, 0, 0, N'', N'', 5)\n" +
                    "SET IDENTITY_INSERT [dbo].[ScheinMed] OFF\n" +
//                    Insert into KrablLink table
                    "SET IDENTITY_INSERT [dbo].[KrablLink] ON\n" +
                    "INSERT [dbo].[KrablLink] ([Nummer], [PatientNummer], [Satzart], [Datum], [Kategorie], [Kurzinfo], [Passwort], [MandantGeändert], [UserGeändert], [DatumÄnderung], [ScheinNummer], [GruppenNummer], [Hintergrundfarbe], [Detail], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantFremd], [FreigabeStatus], [VersandStatus], [Uhrzeitanlage])" +
                    "VALUES (" + IDvalue + "," + patientNummer + ", 40, CAST(N'2022-07-15T00:00:00.000' AS DateTime), N'KKEIN', N'', 0, 1, 1, CAST(N'2022-07-15T15:19:58.000' AS DateTime), 0, 0, 0, N'<KKData MANDANT=\"1\"></KKData>\n" +
                    "', 1, 1, CAST(N'2022-07-11T00:00:00.000' AS DateTime), 0, 0, 0, CAST(N'1899-12-30T15:04:31.000' AS DateTime))\n" +
                    "SET IDENTITY_INSERT [dbo].[KrablLink] OFF\n" +
//                    Insert into KrablLinkID table
                    "SET IDENTITY_INSERT [dbo].[KrablLinkID] ON\n" +
                    "INSERT [dbo].[KrablLinkID] ([Nummer], [PatientNummer], [KrablLinkNummer], [IDType], [ID], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantGeändert], [UserGeändert], [DatumÄnderung], [Fremdsystem], [Erzeugersystem], [Status], [Bemerkung])" +
                    "VALUES (" + IDvalue + ", " + patientNummer + ", 5, 7, "+ IDvalue +", 1, 1, CAST(N'2022-07-18T00:00:00.000' AS DateTime), 1, 1, CAST(N'2022-07-18T16:02:46.753' AS DateTime), 1, 1, 0, N'f4835bad-c18b-4653-b706-89b6f5b06772')\n" +
                    "SET IDENTITY_INSERT [dbo].[KrablLinkID] OFF\n" +
//                    Insert into ScheinMedDaten
                    "SET IDENTITY_INSERT [dbo].[ScheinMedDaten] ON\n" +
                    "INSERT [dbo].[ScheinMedDaten] ([Nummer], [Suchwort], [Klasse], [Typ], [Langtext], [Packungsart], [NNummer], [Darreichungsform], [Packungsgröße], [PZNummer], [StandardDosierung], [Betrag], [Festbetrag], [Grenzpreis], [Anatomieklasse], [Hersteller], [Wirkstoff], [Generika], [NurPrivatrezept], [BTMPräparat], [Bevorzugt], [Geschützt], [AußerHandel], [Negativliste], [Rückruf], [Datenanbieter], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantGeändert], [UserGeändert], [DatumÄnderung], [UnteresPreisdrittel], [OTC], [Zuzahlungsbefreit], [WirkstoffMenge], [WirkstoffMengenEinheit], [LetzterPreis], [PreisÄnderung], [LifeStyle], [ApothekenPflicht], [VerschreibungsPflicht], [Reimport], [AlternativeVorhanden], [ZweitMeinung], [PNH], [PNHBezeichnung], [DDDKosten], [OTX], [TRezept], [KombiPraeparat], [AutIdemKennung], [PriscusListe], [NeueinfuehrungsDatum], [HerstellerID], [ErstattungsBetrag], [DokuPflichtTransfusion], [VOEinschraenkungAnlage3], [Therapiehinweis], [MedizinProdukt], [MPVerordnungsfaehig], [MPVOBefristung], [Verordnet], [MitReimport], [WSTZeile], [WSTNummer], [IMMVorhanden], [Sortierung], [ATCLangtext], [ErstattungStattAbschlag], [VertragspreisNach129_5], [NormGesamtzuzahlung], [Verordnungseinschraenkung], [Verordnungsausschluss], [VOAusschlussAnlage3])" +
                    "VALUES ("+ IDvalue +", N'" + bundle.getMedicationStatement().getPZN() + "', N'', 1, N'Penicillin G HEXAL 1MIO I.E.Pu.z.H.e.Inj.o.Inf.Lsg', 0, 3, 0, N'10', N'" + bundle.getMedicationStatement().getPZN() + "', N'', 32.9600, 0.0000, 0.0000, N'J01CE01', N'Hexal AG', N'Benzylpenicillin-Natrium', 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, CAST(N'2022-07-18T00:00:00.000' AS DateTime), 1, 1, CAST(N'2022-07-18T16:02:46.773' AS DateTime), 0, 0, 0, 599, N'mg', 0.0000, N'', 0, 0, 0, 0, 0, 0, N'', N'', 0.0000, 0, 0, 0, 0, 0, CAST(N'1899-12-30T00:00:00.000' AS DateTime), 0, 0.0000, 0, 0, 0, 0, 0, CAST(N'1899-12-30T00:00:00.000' AS DateTime), 0, 0, N'', N'', 0, N'', N'Benzylpenicillin', 0, 0, 0.0000, 0, 0, 0)\n" +
                    "SET IDENTITY_INSERT [dbo].[ScheinMedDaten] OFF\n" +
                    "commit transaction";

//            QUERY EXECUTED: Insert a prescription into isynet
            stmt.execute(SQL_insert_medication);

            String SQL_VerordnungsmodulMedikamentDbo_table = "SELECT * FROM VerordnungsmodulMedikamentDbo\n";
//            QUERY EXECUTED: Get content of one of the tables related to the prescription to check if it was added
            ResultSet rs2 = stmt.executeQuery(SQL_VerordnungsmodulMedikamentDbo_table);
            ResultSetMetaData rsmd2 = rs2.getMetaData();
            int columnsNumber2 = rsmd2.getColumnCount();

            String result = "";
            while (rs2.next()) {
                System.out.println("\nSQL_VerordnungsmodulMedikamentDbo_table:");
                for (int i = 1; i <= columnsNumber2; i++) {
                    if (i > 1) System.out.print(",  ");
                    result = rs2.getString(i);
                    System.out.print(result + " (" + rsmd2.getColumnName(i) + ")");
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Practitioner parsePractitioner(JSONObject practitionerJSON) {

        JSONObject practitionerResource = practitionerJSON.getJSONObject("resource");

        JSONObject name = (JSONObject) practitionerResource.getJSONArray("name").get(0);
        String firstName = ((JSONArray) name.get("given")).get(0).toString();
        String lastName = name.get("family").toString();

        JSONObject address = (JSONObject) practitionerResource.getJSONArray("address").get(0);
        String addressLine =  ((JSONArray) address.get("line")).get(0).toString();
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder street = new StringBuilder();
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
            for (int i = 1; i < addressParts.length; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
            for (int i = 0; i < addressParts.length - 1; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            street = new StringBuilder(addressLine);
        }
        String city =  address.get("city").toString();
        String postalCode =  address.get("postalCode").toString();

        String email = "";
        String phone = "";
        String fax = "";
//        TODO: currently there is no distinction between the e-mail, phone and fax, ["system": "phone"] and the others should be added in the frontend definition
        if (practitionerResource.has("telecom")) {
            if (practitionerResource.getJSONArray("telecom").length() == 3) {
                email = ((JSONObject) practitionerResource.getJSONArray("telecom").get(0)).get("value").toString();
                phone = ((JSONObject) practitionerResource.getJSONArray("telecom").get(1)).get("value").toString();
                fax = ((JSONObject) practitionerResource.getJSONArray("telecom").get(2)).get("value").toString();
            }
        }

        String modality = ((JSONObject) practitionerResource.getJSONArray("extension").get(0)).get("valueString").toString();

        return new Practitioner(firstName, lastName, street.toString().trim(), houseNumber, city, postalCode, email,  phone, fax, modality);
    }

    public static Patient parsePatient(JSONObject patientJSON) {

        JSONObject patientResource = patientJSON.getJSONObject("resource");

        JSONObject name = (JSONObject) patientResource.getJSONArray("name").get(0);
        String firstName = ((JSONArray) name.get("given")).get(0).toString();
        String lastName = name.get("family").toString();

        JSONObject address = (JSONObject) patientResource.getJSONArray("address").get(0);
        String addressLine =  ((JSONArray) address.get("line")).get(0).toString();
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder street = new StringBuilder();
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
            for (int i = 1; i < addressParts.length; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
            for (int i = 0; i < addressParts.length - 1; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            street = new StringBuilder(addressLine);
        }
        String city =  address.get("city").toString();
        String postalCode =  address.get("postalCode").toString();
        String gender = patientResource.get("gender").toString();
        String birthDate = patientResource.get("birthDate").toString();

        return new Patient(firstName, lastName, street.toString().trim(), houseNumber, city, postalCode, gender, birthDate);
    }

    public static MedicationStatement parseMedicationStatement(JSONObject medicationStatementJSON) {

        JSONObject medicationStatementResource = medicationStatementJSON.getJSONObject("resource");

        String medicationName = medicationStatementResource.getJSONObject("medicationCodeableConcept").get("text").toString();
        String PZN = ((JSONObject) medicationStatementResource.getJSONArray("identifier").get(0)).get("value").toString();
        String dosage = ((JSONObject) medicationStatementResource.getJSONArray("dosage").get(0)).get("text").toString();

        return new MedicationStatement(medicationName, PZN, dosage);
    }

    public static Pharmacy parsePharmacy(JSONObject pharmacyJSON) {

        JSONObject pharmacyResource = pharmacyJSON.getJSONObject("resource");

        String name = pharmacyResource.get("name").toString();

        JSONObject address = (JSONObject) pharmacyResource.getJSONArray("address").get(0);
        String addressLine =  ((JSONArray) address.get("line")).get(0).toString();
        String[] addressParts = addressLine.trim().split("\\s+");
        StringBuilder street = new StringBuilder();
        String houseNumber = "";
        if (addressParts[0].matches("^\\d*$")) {  // houseNumber in the beginning of the addressLine
            houseNumber = addressParts[0];
            for (int i = 1; i < addressParts.length; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else if (addressParts[addressParts.length - 1].matches("^[0-9]*$")) { // houseNumber in the end of the addressLine
            houseNumber = addressParts[addressParts.length - 1];
            for (int i = 0; i < addressParts.length - 1; i++) {
                street.append(addressParts[i]).append(" ");
            }
        } else { // no houseNumber in the addressLine
            street = new StringBuilder(addressLine);
        }

        String city =  address.get("city").toString();
        String postalCode =  address.get("postalCode").toString();

        String phone = "";
        String email = "";
        if (pharmacyResource.has("telecom")) {
            if (pharmacyResource.getJSONArray("telecom").length() == 2) {
                phone = ((JSONObject) pharmacyResource.getJSONArray("telecom").get(0)).get("value").toString();
                email = ((JSONObject) pharmacyResource.getJSONArray("telecom").get(1)).get("value").toString();
            }
            if (pharmacyResource.getJSONArray("telecom").length() == 1) {
                JSONObject phoneOrEmail = (JSONObject) pharmacyResource.getJSONArray("telecom").get(0);
                if (phoneOrEmail.get("value").toString().contains("@")) {
                    email = phoneOrEmail.get("value").toString();
                }
                else {
                    phone = phoneOrEmail.get("value").toString();
                }
            }
        }

        return new Pharmacy(name, street.toString().trim(), houseNumber, city, postalCode, phone, email);
    }
}
