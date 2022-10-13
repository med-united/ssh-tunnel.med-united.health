package health.medunited.t2med;

import javax.inject.Inject;

import health.medunited.client.AuthorizationHeaderFactory;
import io.quarkus.test.Mock;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import health.medunited.profile.CustomTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(CustomTestProfile.class)
class T2MedConnectorTest {

    @Inject
    T2MedConnector t2MedConnector;

    @Disabled("This test requires the right t2med credentials")
    @Test
    void testCreatePrescriptionFromBundle() {

        FhirContext ctx = FhirContext.forR4();

        String input = prepareInputBundle();

        IParser parser = ctx.newJsonParser();

        Bundle parsed = parser.parseResource(Bundle.class, input);

        t2MedConnector.createPrescriptionFromBundle(parsed);
    }

    private String prepareInputBundle() {
        return "{\n" +
                "  \"resourceType\": \"Bundle\",\n" +
                "  \"id\": \"\",\n" +
                "  \"meta\": {\n" +
                "    \"lastUpdated\": \"\",\n" +
                "    \"profile\": [\n" +
                "      \"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"identifier\": {\n" +
                "    \"system\": \"https://gematik.de/fhir/NamingSystem/PrescriptionID\",\n" +
                "    \"value\": \"\"\n" +
                "  },\n" +
                "  \"type\": \"document\",\n" +
                "  \"timestamp\": \"2022-07-29T14:06:06.56Z\",\n" +
                "  \"entry\": [\n" +
                "    {\n" +
                "      \"fullUrl\": \"\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Practitioner\",\n" +
                "        \"id\": \"2776\",\n" +
                "        \"meta\": {\n" +
                "          \"versionId\": \"1\",\n" +
                "          \"lastUpdated\": \"2022-07-29T13:59:39.854+01:00\",\n" +
                "          \"source\": \"#FOadBcXP3WefWviN\"\n" +
                "        },\n" +
                "        \"identifier\": [{\n" +
                "          \"type\": {\n" +
                "            \"coding\": [{\n" +
                "              \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\",\n" +
                "              \"code\": \"LANR\"\n" +
                "            }]\n" +
                "          },\n" +
                "          \"system\": \"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\",\n" +
                "          \"value\": \"123456601\"\n" +
                "        }],\n" +
                "        \"extension\": [\n" +
                "          {\n" +
                "            \"url\": null,\n" +
                "            \"valueString\": \"isynet\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"name\": [\n" +
                "          {\n" +
                "            \"use\": \"official\",\n" +
                "            \"family\": \"Hoffmann\",\n" +
                "            \"given\": [\n" +
                "              \"Theresa\"\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"telecom\": [\n" +
                "          {\n" +
                "            \"value\": \"beatriz.correia@incentergy.de\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"value\": \"111222333\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"value\": \"11111111\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"address\": [\n" +
                "          {\n" +
                "            \"use\": \"home\",\n" +
                "            \"line\": [\n" +
                "              \"Blue avenue\"\n" +
                "            ],\n" +
                "            \"city\": \"Berlin\",\n" +
                "            \"postalCode\": \"11122\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Patient\",\n" +
                "        \"id\": \"123457425\",\n" +
                "        \"meta\": {\n" +
                "          \"versionId\": \"1\",\n" +
                "          \"lastUpdated\": \"2022-07-29T14:00:51.137+01:00\",\n" +
                "          \"source\": \"#gxZ5BU56u8AJpfFP\"\n" +
                "        },\n" +
                "        \"text\": {\n" +
                "          \"status\": \"generated\",\n" +
                "          \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><div class=\\\"hapiHeaderText\\\">Dena <b>A HOSSEINI</b></div><table class=\\\"hapiPropertyTable\\\"><tbody><tr><td>Address</td><td><span>Orange avenue 5 </span><br/><span>Berlin </span></td></tr><tr><td>Date of birth</td><td><span>05 July 2022</span></td></tr></tbody></table></div>\"\n" +
                "        },\n" +
                "        \"name\": [\n" +
                "          {\n" +
                "            \"use\": \"official\",\n" +
                "            \"family\": \"A Hosseini\",\n" +
                "            \"given\": [\n" +
                "              \"Dena\"\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"gender\": \"female\",\n" +
                "        \"birthDate\": \"1977-05-22\",\n" +
                "        \"address\": [\n" +
                "          {\n" +
                "            \"use\": \"home\",\n" +
                "            \"line\": [\n" +
                "              \"Orange avenue 5\"\n" +
                "            ],\n" +
                "            \"city\": \"Berlin\",\n" +
                "            \"postalCode\": \"55555\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"generalPractitioner\": [\n" +
                "          {\n" +
                "            \"reference\": \"Practitioner/2776\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"managingOrganization\": {\n" +
                "          \"reference\": \"Organization/606\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"MedicationStatement\",\n" +
                "        \"id\": \"2778\",\n" +
                "        \"meta\": {\n" +
                "          \"versionId\": \"1\",\n" +
                "          \"lastUpdated\": \"2022-07-29T14:05:51.655+01:00\",\n" +
                "          \"source\": \"#oHE6vom7ZRZkbe87\"\n" +
                "        },\n" +
                "        \"identifier\": [\n" +
                "          {\n" +
                "            \"value\": \"3334262\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"medicationCodeableConcept\": {\n" +
                "          \"text\": \"APOGEN Ibuprofen 400 Schmerzmittel Filmtabletten \"\n" +
                "        },\n" +
                "        \"subject\": {\n" +
                "          \"reference\": \"Patient/123457425\"\n" +
                "        },\n" +
                "        \"informationSource\": {\n" +
                "          \"reference\": \"Practitioner/2776\"\n" +
                "        },\n" +
                "        \"derivedFrom\": [\n" +
                "          {\n" +
                "            \"reference\": \"Organization/606\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"dosage\": [\n" +
                "          {\n" +
                "            \"text\": \"2-0-1-0\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"fullUrl\": \"\",\n" +
                "      \"resource\": {\n" +
                "        \"resourceType\": \"Organization\",\n" +
                "        \"id\": \"555\",\n" +
                "        \"meta\": {\n" +
                "          \"versionId\": \"3\",\n" +
                "          \"lastUpdated\": \"2022-06-29T12:24:06.088+01:00\",\n" +
                "          \"source\": \"#q7CatK72HI4CXj8u\"\n" +
                "        },\n" +
                "        \"name\": \"Kaiser Apotheke\",\n" +
                "        \"telecom\": [\n" +
                "          {\n" +
                "            \"value\": \"+49 123456789\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"value\": \"beatriz.correia@incentergy.de\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"address\": [\n" +
                "          {\n" +
                "            \"line\": [\n" +
                "              \"Bergmannstraße\"\n" +
                "            ],\n" +
                "            \"city\": \"Berlin\",\n" +
                "            \"postalCode\": \"10961\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
