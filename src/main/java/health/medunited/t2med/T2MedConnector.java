package health.medunited.t2med;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import health.medunited.service.BundleParser;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;

import health.medunited.client.T2MedClient;

@ApplicationScoped
public class T2MedConnector {

    private static Logger log = Logger.getLogger(T2MedConnector.class.getName());
    private static final String PRACTITIONER = "practitioner";
    private static final String PATIENT = "patient";
    private static final String MEDICATIONSTATEMENT = "medicationStatement";

    @Inject
    @RestClient
    T2MedClient t2MedClient;

    public void createPrescriptionFromBundle(Bundle parsedBundle, String connectorUrl) {

        try {
            t2MedClient = RestClientBuilder.newBuilder()
                    .baseUrl(new URL(connectorUrl))
                    .build(T2MedClient.class);
        } catch (MalformedURLException e) {
            log.severe("Error creating T2MedClient: " + e.getMessage());
        }

        JsonObject loginResponseJson = t2MedClient.login();

        log.info("Login successfully? " + loginResponseJson.getBoolean("successful"));

        // $userReference = $response | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String userReference = loginResponseJson.getJsonObject("benutzer").getJsonObject("benutzer").getJsonObject("ref").getJsonObject("objectId").getJsonString("id").getString();

        log.info("User reference: " + userReference);

        JsonObject findVerwaltJson = Json.createObjectBuilder()
                .add("benutzerRef",
                        Json.createObjectBuilder().add("objectId",
                                Json.createObjectBuilder().add("id", userReference)
                        )
                ).add("findOnlyAssigned", true).build();

        JsonObject doctorReferenceResponseJson = t2MedClient.getDoctorRole(findVerwaltJson);

        String lanr = BundleParser.getLanr(PRACTITIONER, parsedBundle);
        // Select-Object -ExpandProperty "benutzerBearbeitenDTO" | Select-Object -ExpandProperty "arztrollen" | Select-Object -ExpandProperty "arztrolle" | Where-Object -Property lanr -eq -Value $lanr | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String doctorRoleReference = doctorReferenceResponseJson.getJsonObject("benutzerBearbeitenDTO").getJsonArray("arztrollen").stream().filter(jv -> jv instanceof JsonObject && ((JsonObject) jv).getJsonObject("arztrolle").getString("lanr").equals(lanr)).findFirst().get().asJsonObject().getJsonObject("arztrolle").getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Doctor Role reference: " + doctorRoleReference);

        JsonObject searchPatient = Json.createObjectBuilder().add("searchString", BundleParser.getLastName(PATIENT, parsedBundle) + ", " + BundleParser.getFirstName(PATIENT, parsedBundle)).build();

        JsonObject searchPatientResponseJson = t2MedClient.filterPatients(searchPatient);

        // $patientReference = $response | Select-Object -ExpandProperty "patientSearchResultDTOS" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String patientReference = searchPatientResponseJson.getJsonArray("patientSearchResultDTOS").get(0).asJsonObject().getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Patient reference: " + patientReference);

        JsonObject searchCase = Json.createObjectBuilder().add("objectId", Json.createObjectBuilder().add("id", patientReference)).build();

        JsonObject searchCaseResponseJson = t2MedClient.getCase(searchCase);

        // $caseReference = $response | Select-Object -ExpandProperty "zeilenMaps" | Select-Object -ExpandProperty "AKTUELL" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String caseReference = searchCaseResponseJson.getJsonObject("zeilenMaps").getJsonArray("AKTUELL").get(0).asJsonObject().getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Case Reference: " + caseReference);

        JsonObject caseLocationResponseJson = t2MedClient.getCaseLocation();
        // $caseLocationReference = $response | Select-Object -ExpandProperty "behandlungsorte" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" -First 1 | Select-Object -ExpandProperty "id"
        String caseLocationReference = caseLocationResponseJson.getJsonArray("behandlungsorte").get(0).asJsonObject().getJsonObject("ref").getJsonObject("objectId").getString("id");
        log.info("Case Location Reference: " + caseLocationReference);

        String pzn = BundleParser.getPzn(MEDICATIONSTATEMENT, parsedBundle);

        JsonObject amdbSearchQueryJson = buildAmdbSearchQueryJson(patientReference, doctorRoleReference, caseReference, caseLocationReference, userReference, pzn);
        JsonObject amdbResponseJson = t2MedClient.searchMedication(amdbSearchQueryJson);

        // $medicationName = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "name"
        JsonObject medicationPacking = amdbResponseJson.getJsonArray("entries").get(0).asJsonObject().getJsonObject("packung");
        String medicationName = medicationPacking.getString("name");
        // $handelsname = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "handelsname"
        String handelsname = medicationPacking.getString("handelsname");
        // $erezeptName = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "erezeptName"
        String erezeptName = medicationPacking.getString("erezeptName");
        // $herstellername = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "herstellername"
        String herstellername = medicationPacking.getString("herstellername");
        // $preis = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "preis"
        String preis = medicationPacking.getString("preis");
        // $preisReimportTeratogenFiktivZugelassen = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "preisReimportTeratogenFiktivZugelassen"
        String preisReimportTeratogenFiktivZugelassen = medicationPacking.getString("preisReimportTeratogenFiktivZugelassen");
        // $atcCodes = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "atcCodes"
        JsonArray atcCodes = medicationPacking.getJsonArray("atcCodes");
        // $einheitenname = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "einheitenname"
        String einheitenname = medicationPacking.getString("einheitenname");
        // $einheitennameFuerReichweitenberechnung = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "einheitennameFuerReichweitenberechnung"
        String einheitennameFuerReichweitenberechnung = medicationPacking.getString("einheitennameFuerReichweitenberechnung");
        // $wirkstoff = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstoff" -First 1
        JsonArray wirkstoffWirkstaerken = medicationPacking.getJsonArray("wirkstoffWirkstaerken");
        // JsonObject wirkstoffWirkstaerken0 = medicationPacking.getJsonArray("wirkstoffWirkstaerken").get(0).asJsonObject();
        // String wirkstoff = wirkstoffWirkstaerken0.getString("wirkstoff");
        // // $wirkstaerkeWert = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstaerke" -First 1 | Select-Object -ExpandProperty "wert"
        // JsonObject wirkstaerke = wirkstoffWirkstaerken0.getJsonObject("wirkstaerke");
        // int wirkstaerkeWert = wirkstaerke.getInt("wert");
        // // $wirkstaerkeEinheit = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstaerke" -First 1 | Select-Object -ExpandProperty "einheit"
        // String wirkstaerkeEinheit = wirkstaerke.getString("einheit");

        JsonObject eRezeptJson = buildErezept(
                patientReference,
                doctorRoleReference,
                caseReference,
                caseLocationReference,
                userReference,
                medicationName,
                handelsname,
                erezeptName,
                herstellername,
                pzn,
                preis,
                preisReimportTeratogenFiktivZugelassen,
                atcCodes,
                einheitenname,
                einheitennameFuerReichweitenberechnung,
                wirkstoffWirkstaerken,
                1,
                0,
                0,
                0
        );
        JsonObject eRezeptResponseJson = t2MedClient.createAndSavePrescription(eRezeptJson);
    }

    private JsonObject buildAmdbSearchQueryJson(String patientReference,
                                                String doctorRoleReference,
                                                String caseReference,
                                                String caseLocationReference,
                                                String userReference,
                                                String pzn) {

        return Json.createObjectBuilder()
                .add("amdbSearchQueries",
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("searchtext", pzn)
                                )
                ).add("arzneimittelverordnungenAnzeigen", JsonValue.TRUE)
                .add("ausserVetriebeAusblenden", JsonValue.FALSE)
                .add("deaktivierteVerordnungenAnzeigen", JsonValue.FALSE)
                .add("freitextverordnungenAnzeigen", JsonValue.TRUE)
                .add("kontext",
                        Json.createObjectBuilder()
                                .add("arztrolleRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder()
                                                                .add("id", doctorRoleReference)
                                                )
                                )
                                .add("behandlungsfallRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder()
                                                                .add("id", caseReference)
                                                )
                                )
                                .add("behandlungsortRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder()
                                                                .add("id", caseLocationReference)
                                                )
                                )
                                .add("benutzerRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder()
                                                                .add("id", userReference)
                                                )
                                )
                                .add("patientRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder()
                                                                .add("id", patientReference)
                                                )
                                )
                )
                .add("reimportArzneimittelAusblenden", JsonValue.FALSE)
                .add("searchTerm", pzn)
                .add("selectedFilters", JsonValue.EMPTY_JSON_ARRAY)
                .add("start", 0)
                .add("vorgangstyp", JsonValue.NULL)
                .add("wirkstoffverordnungenAnzeigen", JsonValue.TRUE)
                .build();

    }

    private JsonObject buildErezept(String patientReference,
                                    String doctorRoleReference,
                                    String caseReference,
                                    String caseLocationReference,
                                    String userReference,
                                    String medicationName,
                                    String handelsname,
                                    String erezeptName,
                                    String herstellername,
                                    String pzn,
                                    String preis,
                                    String preisReimportTeratogenFiktivZugelassen,
                                    JsonArray atcCodes,
                                    String einheitenname,
                                    String einheitennameFuerReichweitenberechnung,
                                    JsonArray wirkstoffWirkstaerken,
                                    int dosierschemaMorgens,
                                    int dosierschemaMittags,
                                    int dosierschemaAbends,
                                    int dosierschemaNachts
    ) {
        // created from file resources/.../eRezept.json using https://manuelb.github.io/json2jsr353/
        return Json.createObjectBuilder()
                .add("kontext",
                        Json.createObjectBuilder()
                                .add("arztrolleRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder().add("id", doctorRoleReference))
                                )
                                .add("aufrufenderVorgang", 4)
                                .add("behandlungsfallRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder().add("id", caseReference))
                                )
                                .add("behandlungsortRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder().add("id", caseLocationReference))
                                )
                                .add("benutzerRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder().add("id", userReference))
                                )
                                .add("patientRef",
                                        Json.createObjectBuilder()
                                                .add("objectId",
                                                        Json.createObjectBuilder().add("id", patientReference))
                                )
                                .add("stationRef", JsonValue.NULL)
                )
                .add("rezepteUndVerordnungen",
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("first",
                                                Json.createObjectBuilder()
                                                        .add("ausstellungszeitpunkt", JsonValue.NULL)
                                                        .add("begruendungspflicht", JsonValue.FALSE)
                                                        .add("bvg", JsonValue.FALSE)
                                                        .add("erezeptInfo",
                                                                Json.createObjectBuilder()
                                                                        .add("absenderId", JsonValue.NULL)
                                                                        .add("accessCode", JsonValue.NULL)
                                                                        .add("erezeptId", JsonValue.NULL)
                                                                        .add("ref",
                                                                                Json.createObjectBuilder()
                                                                                        .add("objectId", JsonValue.NULL)
                                                                                        .add("revision", 0)
                                                                        )
                                                                        .add("signaturHbaIccsn", JsonValue.NULL)
                                                                        .add("signaturzeitpunkt", JsonValue.NULL)
                                                                        .add("signiertesRezeptVerweis", JsonValue.NULL)
                                                                        .add("taskId", JsonValue.NULL)
                                                                        .add("versandzeitpunkt", JsonValue.NULL)

                                                        )
                                                        .add("ersatzverordnung", JsonValue.FALSE)
                                                        .add("hilfsmittel", JsonValue.FALSE)
                                                        .add("impfstoff", JsonValue.FALSE)
                                                        .add("ersatzverordnung", JsonValue.FALSE)
                                                        .add("informationszeitpunkt", JsonValue.NULL)
                                                        .add("notdienstgebuehrenfrei", JsonValue.FALSE)
                                                        .add("ref",
                                                                Json.createObjectBuilder()
                                                                        .add("objectId", JsonValue.NULL)
                                                                        .add("revision", 0)
                                                        )
                                                        .add("rezeptgebuehrenfrei", JsonValue.FALSE)
                                                        .add("sonstigerKostentraeger", JsonValue.FALSE)
                                                        .add("sprechstundenbedarf", JsonValue.FALSE)
                                                        .add("uebertragungsweg", 2)
                                                        .add("unfallbetrieb", JsonValue.NULL)
                                                        .add("unfallstatus", 0)
                                                        .add("unfalltag", JsonValue.NULL)
                                        )
                                        .add("second",
                                                Json.createObjectBuilder()
                                                        .add("alsERezeptVerordnet", JsonValue.FALSE)
                                                        .add("alternativeDosierangabe", "Dj")
                                                        .add("anzahlEinheiten", JsonValue.NULL)
                                                        .add("anzahlPackungen", 1)
                                                        .add("arzneimittelKategorie", JsonValue.NULL)
                                                        .add("autIdem", JsonValue.FALSE)
                                                        .add("benutzeERezept", JsonValue.TRUE)
                                                        .add("benutzeRezeptinformationstyp", 34)
                                                        .add("benutzeSekundaerenRezeptinformationstyp", JsonValue.FALSE)
                                                        .add("btmKennzeichen", JsonValue.NULL)
                                                        .add("dosierschema",
                                                                Json.createObjectBuilder()
                                                                        .add("freitext", JsonValue.NULL)
                                                                        .add("morgens", dosierschemaMorgens)
                                                                        .add("mittags", dosierschemaMittags)
                                                                        .add("abends", dosierschemaAbends)
                                                                        .add("nachts", dosierschemaNachts)
                                                        )
                                                        .add("dosierungAufRezept", JsonValue.TRUE)
                                                        .add("erezeptZusatzdaten",
                                                                Json.createObjectBuilder()
                                                                        .add("abgabehinweis", JsonValue.NULL)
                                                                        .add("mehrfachverordnungen",
                                                                                Json.createArrayBuilder()
                                                                        )
                                                        )
                                                        .add("erezeptfaehig", JsonValue.TRUE)
                                                        .add("ersatzverordnungGemaessParagraph31", JsonValue.FALSE)
                                                        .add("farbmarkierung", JsonValue.NULL)
                                                        .add("farbmarkierungZumVerordnungszeitpunkt", JsonValue.NULL)
                                                        .add("freitext", JsonValue.NULL)
                                                        .add("hinweis", JsonValue.NULL)
                                                        .add("layerIndex", 1)
                                                        .add("letzterInformationstyp", JsonValue.NULL)
                                                        .add("letzterVerordnungszeitpunkt", JsonValue.NULL)
                                                        .add("medikationsplanBestellposition", JsonValue.NULL)
                                                        .add("mehrfachverordnungId", JsonValue.NULL)
                                                        .add("packung",
                                                                Json.createObjectBuilder()
                                                                        .add("amrlHinweiseVorhanden", JsonValue.TRUE)
                                                                        .add("anlageIIIAnzeigen", JsonValue.TRUE)
                                                                        .add("anlageVIITeilB", JsonValue.FALSE)
                                                                        .add("anstaltspackung", JsonValue.FALSE)
                                                                        .add("anzahlEinheiten", 20)
                                                                        .add("anzahlEinheitenFuerReichweitenberechnung", 20)
                                                                        .add("anzahlTeilbareStuecke", 2)
                                                                        .add("arzneimittelVertriebsstatus", JsonValue.NULL)
                                                                        .add("atcCodes", atcCodes)
                                                                        .add("aufNegativliste", JsonValue.FALSE)
                                                                        .add("bilanzierteDiaet", JsonValue.FALSE)
                                                                        .add("darreichungsform",
                                                                                Json.createObjectBuilder()
                                                                                        .add("freitext", JsonValue.NULL)
                                                                                        .add("ifaCode", JsonValue.NULL)
                                                                        )
                                                                        .add("darreichungsformIfaCode", "")
                                                                        .add("einheitenname", einheitenname)
                                                                        .add("einheitennameFuerReichweitenberechnung", einheitennameFuerReichweitenberechnung)
                                                                        .add("erezeptName", erezeptName)
                                                                        .add("fiktivZugelassenesMedikament", JsonValue.FALSE)
                                                                        .add("handelsname", handelsname)
                                                                        .add("herstellername", herstellername)
                                                                        .add("name", medicationName)
                                                                        .add("lifeStyleStatus", 0)
                                                                        .add("lifestyleStatusAnzeigen", JsonValue.TRUE)
                                                                        .add("medizinprodukt", JsonValue.FALSE)
                                                                        .add("medizinproduktAnzeigen", JsonValue.FALSE)
                                                                        .add("negativlisteAnzeigen", JsonValue.TRUE)
                                                                        .add("otcOtxAnzeigen", JsonValue.TRUE)
                                                                        .add("otcStatus", JsonValue.FALSE)
                                                                        .add("otxStatus", JsonValue.FALSE)
                                                                        .add("packungsgroesse", JsonValue.NULL)
                                                                        .add("pzn", pzn)
                                                                        .add("reimport", JsonValue.FALSE)
                                                                        .add("removed", JsonValue.FALSE)
                                                                        .add("rezeptStatus", 2)
                                                                        .add("teratogen", JsonValue.FALSE)
                                                                        .add("verbandmittel", JsonValue.FALSE)
                                                                        .add("verbandmittelAnzeigen", JsonValue.TRUE)
                                                                        .add("verordnungsfaehigesMedizinprodukt", JsonValue.FALSE)
                                                                        .add("vertriebsStatus", JsonValue.NULL)
                                                                        .add("vertriebsstatusAnzeigen", JsonValue.TRUE)
                                                                        .add("wirkstoffWirkstaerken", wirkstoffWirkstaerken)
                                                        )
                                                        .add("pimPraeparat", JsonValue.FALSE)
                                                        .add("primaererRezeptinformationstyp", 34)
                                                        .add("requiresArzneimittelempfehlungenCheck", JsonValue.FALSE)
                                                        .add("sekundaererRezeptinformationstyp", 98)
                                                        .add("verordnungsausschluss", JsonValue.FALSE)
                                                        .add("verordnungseinschraenkung", JsonValue.TRUE)
                                                        .add("wirkstoff", JsonValue.NULL)
                                        )
                                )
                )
                .build();
    }

}
