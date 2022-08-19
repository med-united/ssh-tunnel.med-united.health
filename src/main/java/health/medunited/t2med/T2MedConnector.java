package health.medunited.t2med;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;

import health.medunited.client.T2MedClient;

@ApplicationScoped
public class T2MedConnector {

    private static Logger log = Logger.getLogger(T2MedConnector.class.getName());

    @Inject
    @RestClient
    T2MedClient t2MedClient;

    public void createPrescriptionFromBundle(Bundle prescription) {

        JsonObject loginResponseJson = t2MedClient.login();
        
        log.info("Login successfully? "+loginResponseJson.getBoolean("successful"));

        // $userReference = $response | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String userReference = loginResponseJson.getJsonObject("benutzer").getJsonObject("benutzer").getJsonObject("ref").getJsonObject("objectId").getJsonString("id").getString();

        log.info("User reference: "+userReference);

        JsonObject findVerwaltJson = Json.createObjectBuilder()
            .add("benutzerRef", 
                Json.createObjectBuilder().add("objectId",
                    Json.createObjectBuilder().add("id", userReference)
                )
            ).add("findOnlyAssigned", true).build();
        
        JsonObject doctorReferenceResponseJson = t2MedClient.getDoctorRole(findVerwaltJson);

        String lanr = prescription.getEntry().get(0).getResource().getChildByName("identifier").getValues().get(0).getChildByName("value").getValues().get(0).toString();
        // Select-Object -ExpandProperty "benutzerBearbeitenDTO" | Select-Object -ExpandProperty "arztrollen" | Select-Object -ExpandProperty "arztrolle" | Where-Object -Property lanr -eq -Value $lanr | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String doctorRoleReference = doctorReferenceResponseJson.getJsonObject("benutzerBearbeitenDTO").getJsonArray("arztrollen").stream().filter(jv -> jv instanceof JsonObject && ((JsonObject)jv).getJsonObject("arztrolle").getString("lanr").equals(lanr)).findFirst().get().asJsonObject().getJsonObject("arztrolle").getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Doctor Role reference: "+doctorRoleReference);

        Patient patient = (Patient) prescription.getEntry().stream().filter(p -> p.getResource().getResourceType() == ResourceType.Patient).findFirst().get().getResource();

        JsonObject searchPatient = Json.createObjectBuilder().add("searchString", patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGivenAsSingleString()).build();
        
        JsonObject searchPatientResponseJson = t2MedClient.filterPatients(searchPatient);

        // $patientReference = $response | Select-Object -ExpandProperty "patientSearchResultDTOS" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String patientReference = searchPatientResponseJson.getJsonArray("patientSearchResultDTOS").get(0).asJsonObject().getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Patient reference: "+patientReference);

        JsonObject searchCase = Json.createObjectBuilder().add("objectId", Json.createObjectBuilder().add("id", patientReference)).build();

        JsonObject searchCaseResponseJson = t2MedClient.getCase(searchCase);

        // $caseReference = $response | Select-Object -ExpandProperty "zeilenMaps" | Select-Object -ExpandProperty "AKTUELL" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
        String caseReference = searchCaseResponseJson.getJsonObject("zeilenMaps").getJsonArray("AKTUELL").get(0).asJsonObject().getJsonObject("ref").getJsonObject("objectId").getString("id");

        log.info("Case Reference: "+caseReference);

        JsonObject caseLocationResponseJson = t2MedClient.getCaseLocation();
        // $caseLocationReference = $response | Select-Object -ExpandProperty "behandlungsorte" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" -First 1 | Select-Object -ExpandProperty "id"
        String caseLocationReference = caseLocationResponseJson.getJsonArray("behandlungsorte").get(0).asJsonObject().getJsonObject("ref").getJsonObject("objectId").getString("id");
        log.info("Case Location Reference: "+caseLocationReference);

        String pzn = prescription.getEntry().get(2).getResource().getChildByName("identifier").getValues().get(0).getNamedProperty("value").getValues().get(0).primitiveValue();

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
        String atcCode0 = medicationPacking.getJsonArray("atcCodes").getString(0);
        // $einheitenname = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "einheitenname"
        String einheitenname = medicationPacking.getString("einheitenname");
        // $einheitennameFuerReichweitenberechnung = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "einheitennameFuerReichweitenberechnung"
        String einheitennameFuerReichweitenberechnung = medicationPacking.getString("einheitennameFuerReichweitenberechnung");
        // $wirkstoff = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstoff" -First 1
        JsonObject wirkstoffWirkstaerken0 = medicationPacking.getJsonArray("wirkstoffWirkstaerken").get(0).asJsonObject();
        String wirkstoff = wirkstoffWirkstaerken0.getString("wirkstoff");
        // $wirkstaerkeWert = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstaerke" -First 1 | Select-Object -ExpandProperty "wert"
        JsonObject wirkstaerke = wirkstoffWirkstaerken0.getJsonObject("wirkstaerke");
        int wirkstaerkeWert = wirkstaerke.getInt("wert");
        // $wirkstaerkeEinheit = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstaerke" -First 1 | Select-Object -ExpandProperty "einheit"
        String wirkstaerkeEinheit = wirkstaerke.getString("einheit");

        JsonObject eRezept = buildErezept(patientReference, doctorRoleReference, caseReference, caseLocationReference, userReference, pzn);
        // JsonObject eRezeptResponseJson = t2MedClient.createAndSavePrescription(eRezept);
    }

    private JsonObject buildAmdbSearchQueryJson(String patientReference, String doctorRoleReference, String caseReference, String caseLocationReference, String userReference, String pzn){
        //{
        //  "amdbSearchQueries": [
        //      {
        //          "searchtext": "$PZN"
        //      }
        //  ],
        //  "arzneimittelverordnungenAnzeigen": true,
        //  "ausserVetriebeAusblenden": false,
        //  "deaktivierteVerordnungenAnzeigen": false,
        //  "freitextverordnungenAnzeigen": true,
        //  "kontext": {
        //      "arztrolleRef": {
        //          "objectId": {
        //              "id": "$doctorReference"
        //          }
        //      },
        //      "behandlungsfallRef": {
        //          "objectId": {
        //              "id": "$caseReference"
        //          }
        //      },
        //      "behandlungsortRef": {
        //          "objectId": {
        //              "id": "$caseLocationReference"
        //          }
        //      },
        //      "benutzerRef": {
        //          "objectId": {
        //              "id": "$userReference"
        //          }
        //      },
        //      "patientRef": {
        //          "objectId": {
        //              "id": "$patientReference"
        //          }
        //      }
        //  },
        //  "reimportArzneimittelAusblenden": false,
        //  "searchTerm": "$PZN",
        //  "selectedFilters": [],
        //  "start": 0,
        //  "vorgangstyp": null,
        //  "wirkstoffverordnungenAnzeigen": true
        //}
        JsonObject query = Json.createObjectBuilder()
        .add("amdbSearchQueries", 
                Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("searchtext",pzn)
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
                            .add("id",doctorRoleReference)
                    )
                )
                .add("behandlungsfallRef",
                    Json.createObjectBuilder()
                    .add("objectId",
                            Json.createObjectBuilder()
                            .add("id",caseReference)
                    )
                )
                .add("behandlungsortRef",
                    Json.createObjectBuilder()
                    .add("objectId",
                            Json.createObjectBuilder()
                            .add("id",caseLocationReference)
                    )
                )
                .add("benutzerRef",
                    Json.createObjectBuilder()
                    .add("objectId",
                            Json.createObjectBuilder()
                            .add("id",userReference)
                    )
                )
                .add("patientRef",
                    Json.createObjectBuilder()
                    .add("objectId",
                            Json.createObjectBuilder()
                            .add("id",patientReference)
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

        return query;
    }

    private JsonObject buildErezept(String patientReference, 
                                    String doctorRoleReference, 
                                    String caseReference, 
                                    String caseLocationReference, 
                                    String userReference, 
                                    String pzn) {
        JsonObject erezept = Json.createObjectBuilder()
        .add("kontext",
            Json.createObjectBuilder()
            .add("arztrolleRef",
                Json.createObjectBuilder()
                .add("objectId",
                        Json.createObjectBuilder()
                        .add("id",doctorRoleReference)
                )
            )
            .add("aufrufenderVorgang", 4)
            .add("behandlungsfallRef",
                Json.createObjectBuilder()
                .add("objectId",
                        Json.createObjectBuilder()
                        .add("id",caseReference)
                )
            )
            .add("behandlungsortRef",
                Json.createObjectBuilder()
                .add("objectId",
                        Json.createObjectBuilder()
                        .add("id",caseLocationReference)
                )
            )
            .add("benutzerRef",
                Json.createObjectBuilder()
                .add("objectId",
                        Json.createObjectBuilder()
                        .add("id",userReference)
                )
            )
            .add("patientRef",
                Json.createObjectBuilder()
                .add("objectId",
                        Json.createObjectBuilder()
                        .add("id",patientReference)
                )
            )
            .add("stationRef", JsonValue.NULL)
        )
        .build();

        return erezept;
        // {
        //     "kontext": {
        //         "arztrolleRef": {
        //             "objectId": {
        //                 "id": "$doctorRoleReference"
        //             }
        //         },
        //         "aufrufenderVorgang": 4,
        //         "behandlungsfallRef": {
        //             "objectId": {
        //                 "id": "$caseReference"
        //             }
        //         },
        //         "behandlungsortRef": {
        //             "objectId": {
        //                 "id": "$caseLocationReference"
        //             }
        //         },
        //         "benutzerRef": {
        //             "objectId": {
        //                 "id": "$userReference"
        //             }
        //         },
        //         "patientRef": {
        //             "objectId": {
        //                 "id": "$patientReference"
        //             }
        //         },
        //         "stationRef": null
        //     },
        //     "rezepteUndVerordnungen": [
        //         {
        //             "first": {
        //                 "ausstellungszeitpunkt": null,
        //                 "begruendungspflicht": false,
        //                 "bvg": false,
        //                 "erezeptInfo": {
        //                     "absenderId": null,
        //                     "accessCode": null,
        //                     "erezeptId": null,
        //                     "ref": {
        //                         "objectId": null,
        //                         "revision": 0
        //                     },
        //                     "signaturHbaIccsn": null,
        //                     "signaturzeitpunkt": null,
        //                     "signiertesRezeptVerweis": null,
        //                     "taskId": null,
        //                     "versandzeitpunkt": null
        //                 },
        //                 "ersatzverordnung": false,
        //                 "hilfsmittel": false,
        //                 "impfstoff": false,
        //                 "informationszeitpunkt": null,
        //                 "notdienstgebuehrenfrei": false,
        //                 "ref": {
        //                     "objectId": null,
        //                     "revision": 0
        //                 },
        //                 "rezeptgebuehrenfrei": false,
        //                 "sonstigerKostentraeger": false,
        //                 "sprechstundenbedarf": false,
        //                 "uebertragungsweg": 2,
        //                 "unfallbetrieb": null,
        //                 "unfallstatus": 0,
        //                 "unfalltag": null
        //             },
        //             "second": {
        //                 "alsERezeptVerordnet": false,
        //                 "alternativeDosierangabe": "Dj",
        //                 "anzahlEinheiten": null,
        //                 "anzahlPackungen": 1,
        //                 "arzneimittelKategorie": null,
        //                 "autIdem": false,
        //                 "benutzeERezept": true,
        //                 "benutzeRezeptinformationstyp": 34,
        //                 "benutzeSekundaerenRezeptinformationstyp": false,
        //                 "btmKennzeichen": null,
        //                 "dosierschema": {
        //                     "$abends": 0,
        //                     "freitext": null,
        //                     "$mittags": 0,
        //                     "$morgens": 1,
        //                     "$nachts": 0
        //                 },
        //                 "dosierungAufRezept": true,
        //                 "erezeptZusatzdaten": {
        //                     "abgabehinweis": null,
        //                     "mehrfachverordnungen": []
        //                 },
        //                 "erezeptfaehig": true,
        //                 "ersatzverordnungGemaessParagraph31": false,
        //                 "farbmarkierung": null,
        //                 "farbmarkierungZumVerordnungszeitpunkt": null,
        //                 "freitext": null,
        //                 "hinweis": null,
        //                 "layerIndex": 1,
        //                 "letzterInformationstyp": null,
        //                 "letzterVerordnungszeitpunkt": null,
        //                 "medikationsplanBestellposition": null,
        //                 "mehrfachverordnungId": null,
        //                 "packung": {
        //                     "amrlHinweiseVorhanden": true,
        //                     "anlageIIIAnzeigen": true,
        //                     "anlageVIITeilB": false,
        //                     "anstaltspackung": false,
        //                     "anzahlEinheiten": 20,
        //                     "anzahlEinheitenFuerReichweitenberechnung": 20,
        //                     "anzahlTeilbareStuecke": 2,
        //                     "arzneimittelVertriebsstatus": null,
        //                     "atcCodes": [
        //                         "$atcCodes"
        //                     ],
        //                     "aufNegativliste": false,
        //                     "bilanzierteDiaet": false,
        //                     "darreichungsform": {
        //                         "freitext": null,
        //                         "ifaCode": null
        //                     },
        //                     "darreichungsformIfaCode": "TAB",
        //                     "einheitenname": "$einheitenname",
        //                     "einheitennameFuerReichweitenberechnung": "$einheitennameFuerReichweitenberechnung",
        //                     "erezeptName": "$erezeptName",
        //                     "fiktivZugelassenesMedikament": false,
        //                     "handelsname": "$handelsname",
        //                     "herstellername": "$herstellername",
        //                     "name": "$name",
        //                     "lifeStyleStatus": 0,
        //                     "lifestyleStatusAnzeigen": true,
        //                     "medizinprodukt": false,
        //                     "medizinproduktAnzeigen": false,
        //                     "negativlisteAnzeigen": true,
        //                     "otcOtxAnzeigen": true,
        //                     "otcStatus": false,
        //                     "otxStatus": false,
        //                     "packungsgroesse": "N1",
        //                     "pzn": "$PZN",
        //                     "reimport": false,
        //                     "removed": false,
        //                     "rezeptStatus": 2,
        //                     "teratogen": false,
        //                     "verbandmittel": false,
        //                     "verbandmittelAnzeigen": true,
        //                     "verordnungsfaehigesMedizinprodukt": false,
        //                     "vertriebsStatus": null,
        //                     "vertriebsstatusAnzeigen": true,
        //                     "wirkstoffWirkstaerken": []
        //                 },
        //                 "pimPraeparat": false,
        //                 "primaererRezeptinformationstyp": 34,
        //                 "requiresArzneimittelempfehlungenCheck": false,
        //                 "sekundaererRezeptinformationstyp": 98,
        //                 "verordnungsausschluss": false,
        //                 "verordnungseinschraenkung": true,
        //                 "wirkstoff": null
        //             }
        //         }
        //     ]
        // }
    }
}
