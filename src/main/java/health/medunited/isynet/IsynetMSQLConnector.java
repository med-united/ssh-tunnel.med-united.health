package health.medunited.isynet;

import java.sql.*;

public class IsynetMSQLConnector {

    public static void main(String[] args) {
        // Create a variable for the connection string.
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String connectionUrl = "jdbc:sqlserver://192.168.178.50:1433;databaseName=WINACS;user=AP31;password=722033800; trustServerCertificate=true";
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {

            String IDvalue = "1";
            String SQL_transaction_to_insert_medication = "" +
//                    SET XACT_ABORT ON: in case of an error, rollback will be issued automatically
                    "SET XACT_ABORT ON\n" +
                    "begin transaction\n" +
//                    Insert into VerordnungsmodulMedikamentDbo table (Prescription module medication table)
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikamentDbo] ON\n" +
                    "INSERT [dbo].[VerordnungsmodulMedikamentDbo] ([Id], [Pzn], [HandelsnameOderFreitext], [Hersteller], [AtcCodes], [AtcCodeBedeutungen], [Darreichungsform], [DarreichungsformAsFreitext], [PackungsgroesseText], [PackungsgroesseWert], [PackungsgroesseEinheit], [PackungsgroesseEinheitCode], [Normgroesse], [Preis_IsSet], [Preis_ApothekenVerkaufspreisCent], [Preis_FestbetragCent], [Preis_MehrkostenCent], [Preis_ZuzahlungCent], [Preis_GesamtzuzahlungCent], [Typ], [Farbe], [IsPriscus], [Created], [DatasetCreated], [UserCreated], [LastChanged], [DatasetLastChanged], [UserLastChanged], [IsArchiviert], [Hilfsmittelpositionsnummer])" +
                    "VALUES (" + IDvalue + ", 16849772, N'Penicillin G HEXAL 1MIO I.E.Pu.z.H.e.Inj.o.Inf.Lsg', N'Hexal AG', N'J01CE01', N'Benzylpenicillin', N'PII', N'Pulver zur Herstellung einer Injektions- oder Infusionslsg.', N'10', N'10', N'Stück', N'St', 3, 1, 3296, NULL, NULL, 500, 500, 1, NULL, 0, CAST(N'2022-07-18T16:02:46.6187778+02:00' AS DateTimeOffset), N'1', N'ANW-1', CAST(N'2022-07-18T16:02:46.6187778+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0, NULL)\n" +
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
            "N3 PZN16849772 »Dj«', 1, NULL, 0, 0, 0, 0, 0, 0, NULL, 0, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, 0, NULL, NULL, NULL, CAST(N'2022-07-18T16:02:46.6337384+02:00' AS DateTimeOffset), N'1', N'ANW-1', CAST(N'2022-07-18T16:25:13.0978550+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0, NULL, N'101', 1, 0, 0, NULL, 0, 1, NULL)\n" +
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulRezeptDbo] OFF\n" +
//                    Insert into VerordnungsmodulMedikationDbo table (Prescription module medication table)
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikationDbo] ON\n" +
                    "INSERT [dbo].[VerordnungsmodulMedikationDbo] ([Id], [RezeptId], [MedikamentId], [PatientId], [DatumVerordnet], [IsDauermedikation], [MpKennzeichen], [DatumAbgesetzt], [GrundAbgesetzt], [Created], [DatasetCreated], [UserCreated], [LastChanged], [DatasetLastChanged], [UserLastChanged], [IsArchiviert])" +
                    "VALUES (" + IDvalue + "," + IDvalue + ", " + IDvalue + ", N'1', CAST(N'2022-07-18T16:02:46.4803941' AS DateTime2), 0, 0, NULL, NULL, CAST(N'2022-07-18T16:02:46.6477011+02:00' AS DateTimeOffset), N'1', N'ANW-1', CAST(N'2022-07-18T16:02:46.6477011+02:00' AS DateTimeOffset), N'1', N'ANW-1', 0)\n" +
                    "SET IDENTITY_INSERT [dbo].[VerordnungsmodulMedikationDbo] OFF\n" +
//                    Insert into ScheinMed table
                    "SET IDENTITY_INSERT [dbo].[ScheinMed] ON\n" +
                    "INSERT [dbo].[ScheinMed] ([Nummer], [ScheinNummer], [PatientNummer], [Suchwort], [PZN], [Verordnungstyp], [Betragsspeicher], [AVP], [Festbetrag], [Bruttobetrag], [Nettobetrag], [Diagnose], [ICD], [AutIdem], [Grenzpreis], [MandantGeändert], [UserGeändert], [DatumÄnderung], [UnteresPreisdrittel], [KrablLinkNrRezept], [BTMGebühr], [DDDPackung], [Übertragen], [FarbKategorie], [Merkmal], [KrablLinkNr])" +
                    "VALUES ("+ IDvalue +", 1, 1, N'16849772', N'16849772', N'LM', 100, 32.9600, 0.0000, 32.9600, 27.9600, N'', N'', 1, 0.0000, 1, 1, CAST(N'2022-07-18T16:02:46.707' AS DateTime), 0, 0, 0.0000, 0, 0, N'', N'', 5)\n" +
                    "SET IDENTITY_INSERT [dbo].[ScheinMed] OFF\n" +
//                    Insert into KrablLink table
                    "SET IDENTITY_INSERT [dbo].[KrablLink] ON\n" +
                    "INSERT [dbo].[KrablLink] ([Nummer], [PatientNummer], [Satzart], [Datum], [Kategorie], [Kurzinfo], [Passwort], [MandantGeändert], [UserGeändert], [DatumÄnderung], [ScheinNummer], [GruppenNummer], [Hintergrundfarbe], [Detail], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantFremd], [FreigabeStatus], [VersandStatus], [Uhrzeitanlage])" +
                    "VALUES ("+ IDvalue +", 1, 40, CAST(N'2022-07-15T00:00:00.000' AS DateTime), N'KKEIN', N'', 0, 1, 1, CAST(N'2022-07-15T15:19:58.000' AS DateTime), 0, 0, 0, N'<KKData MANDANT=\"1\"></KKData>\n" +
            "', 1, 1, CAST(N'2022-07-11T00:00:00.000' AS DateTime), 0, 0, 0, CAST(N'1899-12-30T15:04:31.000' AS DateTime))\n" +
                    "SET IDENTITY_INSERT [dbo].[KrablLink] OFF\n" +
//                    Insert into KrablLinkID table
                    "SET IDENTITY_INSERT [dbo].[KrablLinkID] ON\n" +
                    "INSERT [dbo].[KrablLinkID] ([Nummer], [PatientNummer], [KrablLinkNummer], [IDType], [ID], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantGeändert], [UserGeändert], [DatumÄnderung], [Fremdsystem], [Erzeugersystem], [Status], [Bemerkung])" +
                    "VALUES ("+ IDvalue +", 1, 5, 7, "+ IDvalue +", 1, 1, CAST(N'2022-07-18T00:00:00.000' AS DateTime), 1, 1, CAST(N'2022-07-18T16:02:46.753' AS DateTime), 1, 1, 0, N'f4835bad-c18b-4653-b706-89b6f5b06772')\n" +
                    "SET IDENTITY_INSERT [dbo].[KrablLinkID] OFF\n" +
//                    Insert into ScheinMedDaten
                    "SET IDENTITY_INSERT [dbo].[ScheinMedDaten] ON\n" +
                    "INSERT [dbo].[ScheinMedDaten] ([Nummer], [Suchwort], [Klasse], [Typ], [Langtext], [Packungsart], [NNummer], [Darreichungsform], [Packungsgröße], [PZNummer], [StandardDosierung], [Betrag], [Festbetrag], [Grenzpreis], [Anatomieklasse], [Hersteller], [Wirkstoff], [Generika], [NurPrivatrezept], [BTMPräparat], [Bevorzugt], [Geschützt], [AußerHandel], [Negativliste], [Rückruf], [Datenanbieter], [MandantAnlage], [UserAnlage], [DatumAnlage], [MandantGeändert], [UserGeändert], [DatumÄnderung], [UnteresPreisdrittel], [OTC], [Zuzahlungsbefreit], [WirkstoffMenge], [WirkstoffMengenEinheit], [LetzterPreis], [PreisÄnderung], [LifeStyle], [ApothekenPflicht], [VerschreibungsPflicht], [Reimport], [AlternativeVorhanden], [ZweitMeinung], [PNH], [PNHBezeichnung], [DDDKosten], [OTX], [TRezept], [KombiPraeparat], [AutIdemKennung], [PriscusListe], [NeueinfuehrungsDatum], [HerstellerID], [ErstattungsBetrag], [DokuPflichtTransfusion], [VOEinschraenkungAnlage3], [Therapiehinweis], [MedizinProdukt], [MPVerordnungsfaehig], [MPVOBefristung], [Verordnet], [MitReimport], [WSTZeile], [WSTNummer], [IMMVorhanden], [Sortierung], [ATCLangtext], [ErstattungStattAbschlag], [VertragspreisNach129_5], [NormGesamtzuzahlung], [Verordnungseinschraenkung], [Verordnungsausschluss], [VOAusschlussAnlage3])" +
                    "VALUES ("+ IDvalue +", N'16849772', N'', 1, N'Penicillin G HEXAL 1MIO I.E.Pu.z.H.e.Inj.o.Inf.Lsg', 0, 3, 0, N'10', N'16849772', N'', 32.9600, 0.0000, 0.0000, N'J01CE01', N'Hexal AG', N'Benzylpenicillin-Natrium', 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, CAST(N'2022-07-18T00:00:00.000' AS DateTime), 1, 1, CAST(N'2022-07-18T16:02:46.773' AS DateTime), 0, 0, 0, 599, N'mg', 0.0000, N'', 0, 0, 0, 0, 0, 0, N'', N'', 0.0000, 0, 0, 0, 0, 0, CAST(N'1899-12-30T00:00:00.000' AS DateTime), 0, 0.0000, 0, 0, 0, 0, 0, CAST(N'1899-12-30T00:00:00.000' AS DateTime), 0, 0, N'', N'', 0, N'', N'Benzylpenicillin', 0, 0, 0.0000, 0, 0, 0)\n" +
                    "SET IDENTITY_INSERT [dbo].[ScheinMedDaten] OFF\n" +
                    "commit transaction";

            String SQL_VerordnungsmodulMedikamentDbo_table = "SELECT * FROM VerordnungsmodulMedikamentDbo\n";

            String SQL_delete_VerordnungsmodulMedikamentDbo = "DELETE FROM VerordnungsmodulMedikamentDbo WHERE Id > 0";
            String SQL_delete_VerordnungsmodulRezepturWirkstoffDbo = "DELETE FROM VerordnungsmodulRezepturWirkstoffDbo WHERE Id > 0";
            String SQL_delete_VerordnungsmodulMedikationDbo = "DELETE FROM VerordnungsmodulMedikationDbo WHERE Id > 0";
            String SQL_delete_VerordnungsmodulRezeptDbo = "DELETE FROM VerordnungsmodulRezeptDbo WHERE Id > 0";
            String SQL_delete_ScheinMed = "DELETE FROM ScheinMed WHERE Nummer > 0";
            String SQL_delete_KrablLink = "DELETE FROM KrablLink WHERE Nummer > 0";
            String SQL_delete_KrablLinkID = "DELETE FROM KrablLinkID WHERE Nummer > 0";
            String SQL_delete_ScheinMedDaten = "DELETE FROM ScheinMedDaten WHERE Nummer > 0";

//            Clean everything in the tables used
            stmt.execute(SQL_delete_ScheinMedDaten);
            stmt.execute(SQL_delete_KrablLinkID);
            stmt.execute(SQL_delete_KrablLink);
            stmt.execute(SQL_delete_ScheinMed);
            stmt.execute(SQL_delete_VerordnungsmodulMedikationDbo);
            stmt.execute(SQL_delete_VerordnungsmodulRezeptDbo);
            stmt.execute(SQL_delete_VerordnungsmodulRezepturWirkstoffDbo);
            stmt.execute(SQL_delete_VerordnungsmodulMedikamentDbo);

//            Insert the medication through a transaction
            stmt.execute(SQL_transaction_to_insert_medication);

//            Check the content inside the VerordnungsmodulMedikamentDbo table
            ResultSet rs = stmt.executeQuery(SQL_VerordnungsmodulMedikamentDbo_table);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();

            // Iterate through the data in the result set and display it
            while (rs.next()) {
                System.out.println("VerordnungsmodulMedikamentDbo table:");
//                Prints the lines from the VerordnungsmodulMedikamentDbo table
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " (" + rsmd.getColumnName(i) + ")");
                }
            }
        }
        // Handle any errors that may have occurred
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
