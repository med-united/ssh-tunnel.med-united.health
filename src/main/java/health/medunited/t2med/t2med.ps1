$code= @"
        using System.Net;
        using System.Security.Cryptography.X509Certificates;
        public class TrustAllCertsPolicy : ICertificatePolicy {
            public bool CheckValidationResult(ServicePoint srvPoint, X509Certificate certificate, WebRequest request, int certificateProblem) {
                return true;
            }
        }
"@
Add-Type -TypeDefinition $code -Language CSharp
[System.Net.ServicePointManager]::CertificatePolicy = New-Object TrustAllCertsPolicy

#----------------------------------------------------------------------------------------------------------------------
#Ask doctor for credentials

Write-Host "Bitte geben Sie Ihren Benutzernamen ein:"
$doctorUsername = Read-Host
Write-Host "Bitte geben Sie Ihr Passwort ein:"
$doctorPassword = Read-Host
Write-Host "Bitte geben Sie Ihre Lebenslange Arztnummer ein:"
$lanr = Read-Host
Write-Host "Bitte geben Sie die IP-Adresse des Servers ein:"
$serverAddress = Read-Host

#----------------------------------------------------------------------------------------------------------------------
#Login into the system

$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))

$URI = "https://" + $serverAddress + ":16567/aps/rest/benutzer/login/authenticate"
$response = Invoke-RestMethod -Uri $URI  -Method 'GET' -Headers $headers

$userReference = $response | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "benutzer" | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
Write-Host "User reference: " $userReference

#----------------------------------------------------------------------------------------------------------------------
#Get Doctor's role
$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))
$headers.Add("Content-Type", "application/json")

$body = "{
    `n    `"benutzerRef`": {
    `n        `"objectId`": {
    `n            `"id`": `"$userReference`"
    `n        }
    `n    },
    `n    `"findOnlyAssigned`": true
    `n}"

$URI = "https://" + $serverAddress + ":16567/aps/rest/benutzer/verwalten/find"
$response = Invoke-RestMethod -Uri $URI -Method 'POST' -Headers $headers -Body $body

$doctorReference = $response | Select-Object -ExpandProperty "benutzerBearbeitenDTO" | Select-Object -ExpandProperty "arztrollen" | Select-Object -ExpandProperty "arztrolle" | Where-Object -Property lanr -eq -Value $lanr | Select-Object -ExpandProperty "ref" | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"

Write-Host "Doctor reference: " $doctorReference

#----------------------------------------------------------------------------------------------------------------------
#Filter patients by surname, name

$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))
$headers.Add("Content-Type", "application/json")

$body = "{
`n    `"searchString`": `"$patientSurname, $patientName`"
`n}"

$URI = "https://" + $serverAddress + ":16567/aps/rest/praxis/patient/liste/pagefilter"
$response = Invoke-RestMethod -Uri $URI -Method 'POST' -Headers $headers -Body $body

$patientReference = $response | Select-Object -ExpandProperty "patientSearchResultDTOS" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"

Write-Host "Patient reference: " $patientReference

#Get most recent behandlungsfall
$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))
$headers.Add("Content-Type", "application/json")

$body = "{
`n    `"objectId`": {
`n        `"id`": `"$patientReference`"
`n    }
`n}"

$URI = "https://" + $serverAddress + ":16567/aps/rest/praxis/behandlungsfaelle/faellefuerpatientinkrementell"
$response = Invoke-RestMethod -Uri $URI -Method 'POST' -Headers $headers -Body $body

$caseReference = $response | Select-Object -ExpandProperty "zeilenMaps" | Select-Object -ExpandProperty "AKTUELL" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" | Select-Object -ExpandProperty "id"
Write-Host "Case reference: " $caseReference

# #----------------------------------------------------------------------------------------------------------------------
# #Get Behandlungsort
$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))
$headers.Add("Content-Type", "application/json;charset=UTF-8")

$URI = "https://" + $serverAddress + ":16567/aps/rest/praxis/praxisstruktur/kontextauswaehlen/arztrollenbehandlungorte"
$response = Invoke-RestMethod -Uri $URI -Method 'GET' -Headers $headers

$caseLocationReference = $response | Select-Object -ExpandProperty "behandlungsorte" | Select-Object -ExpandProperty "ref" -First 1 | Select-Object -ExpandProperty "objectId" -First 1 | Select-Object -ExpandProperty "id"
Write-Host "Case location reference: " $caseLocationReference

# #----------------------------------------------------------------------------------------------------------------------
# #Search medication by PZN
$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))
$headers.Add("Content-Type", "application/json;charset=UTF-8")

$body = "{
    `n    `"amdbSearchQueries`": [
    `n        {
    `n            `"searchtext`": `"$PZN`"
    `n        }
    `n    ],
    `n    `"arzneimittelverordnungenAnzeigen`": true,
    `n    `"ausserVetriebeAusblenden`": false,
    `n    `"deaktivierteVerordnungenAnzeigen`": false,
    `n    `"freitextverordnungenAnzeigen`": true,
    `n    `"kontext`": {
    `n        `"arztrolleRef`": {
    `n            `"objectId`": {
    `n                `"id`": `"$doctorReference`"
    `n            }
    `n        },
    `n        `"behandlungsfallRef`": {
    `n            `"objectId`": {
    `n                `"id`": `"$caseReference`"
    `n            }
    `n        },
    `n        `"behandlungsortRef`": {
    `n            `"objectId`": {
    `n                `"id`": `"$caseLocationReference`"
    `n            }
    `n        },
    `n        `"benutzerRef`": {
    `n            `"objectId`": {
    `n                `"id`": `"$userReference`"
    `n            }
    `n        },
    `n        `"patientRef`": {
    `n            `"objectId`": {
    `n                `"id`": `"$patientReference`"
    `n            }
    `n        }
    `n    },
    `n    `"reimportArzneimittelAusblenden`": false,
    `n    `"searchTerm`": `"$PZN`",
    `n    `"selectedFilters`": [],
    `n    `"start`": 0,
    `n    `"vorgangstyp`": null,
    `n    `"wirkstoffverordnungenAnzeigen`": true
    `n}
    `n"

$URI = "https://" + $serverAddress + ":16567/aps/rest/verordnung/rezept/ausstellen/amdb/page"
$response = Invoke-RestMethod -Uri $URI -Method 'POST' -Headers $headers -Body $body

$name = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "name"
$handelsname = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "handelsname"
$erezeptName = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "erezeptName"
$herstellername = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "herstellername"
$preis = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "preis"
$preisReimportTeratogenFiktivZugelassen = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "preisReimportTeratogenFiktivZugelassen"
$atcCodes = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "atcCodes"
$einheitenname = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "einheitenname"
$einheitennameFuerReichweitenberechnung = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "einheitennameFuerReichweitenberechnung"
$wirkstoff = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstoff" -First 1
$wirkstaerkeWert = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstaerke" -First 1 | Select-Object -ExpandProperty "wert"
$wirkstaerkeEinheit = $response | Select-Object -ExpandProperty "entries" | Select-Object -ExpandProperty "packung" -First 1 | Select-Object -ExpandProperty "wirkstoffWirkstaerken" | Select-Object -ExpandProperty "wirkstaerke" -First 1 | Select-Object -ExpandProperty "einheit"

Write-Host "Medication name: " $name
Write-Host "Medication handelsname: " $handelsname
Write-Host "Medication erezeptName: " $erezeptName
Write-Host "Medication herstellername: " $herstellername
Write-Host "Medication wirkstoff: " $wirkstoff
Write-Host "Medication wirkstaerkeWert: " $wirkstaerkeWert
Write-Host "Medication wirkstaerkeEinheit: " $wirkstaerkeEinheit

# #----------------------------------------------------------------------------------------------------------------------
# #Create and save prescription

$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${doctorUsername}:${doctorPassword}")))
$headers.Add("Content-Type", "application/json;charset=UTF-8")
$headers.Add("Cookie", "JSESSIONID=732C7DCE9699BAE4710C3376814F386B")

$body = "{
`n    `"kontext`": {
`n        `"arztrolleRef`": {
`n            `"objectId`": {
`n                `"id`": `"$doctorReference`"
`n            }
`n        },
`n        `"aufrufenderVorgang`": 4,
`n        `"behandlungsfallRef`": {
`n            `"objectId`": {
`n                `"id`": `"$caseReference`"
`n            }
`n        },
`n        `"behandlungsortRef`": {
`n            `"objectId`": {
`n                `"id`": `"$caseLocationReference`"
`n            }
`n        },
`n        `"benutzerRef`": {
`n            `"objectId`": {
`n                `"id`": `"$userReference`"
`n            }
`n        },
`n        `"patientRef`": {
`n            `"objectId`": {
`n                `"id`": `"$patientReference`"
`n            }
`n        },
`n        `"stationRef`": null
`n    },
`n    `"rezepteUndVerordnungen`": [
`n        {
`n            `"first`": {
`n                `"ausstellungszeitpunkt`": null,
`n                `"begruendungspflicht`": false,
`n                `"bvg`": false,
`n                `"erezeptInfo`": {
`n                    `"absenderId`": null,
`n                    `"accessCode`": null,
`n                    `"erezeptId`": null,
`n                    `"ref`": {
`n                        `"objectId`": null,
`n                        `"revision`": 0
`n                    },
`n                    `"signaturHbaIccsn`": null,
`n                    `"signaturzeitpunkt`": null,
`n                    `"signiertesRezeptVerweis`": null,
`n                    `"taskId`": null,
`n                    `"versandzeitpunkt`": null
`n                },
`n                `"ersatzverordnung`": false,
`n                `"hilfsmittel`": false,
`n                `"impfstoff`": false,
`n                `"informationszeitpunkt`": null,
`n                `"notdienstgebuehrenfrei`": false,
`n                `"ref`": {
`n                    `"objectId`": null,
`n                    `"revision`": 0
`n                },
`n                `"rezeptgebuehrenfrei`": false,
`n                `"sonstigerKostentraeger`": false,
`n                `"sprechstundenbedarf`": false,
`n                `"uebertragungsweg`": 2,
`n                `"unfallbetrieb`": null,
`n                `"unfallstatus`": 0,
`n                `"unfalltag`": null
`n            },
`n            `"second`": {
`n                `"alsERezeptVerordnet`": false,
`n                `"alternativeDosierangabe`": `"Dj`",
`n                `"anzahlEinheiten`": null,
`n                `"anzahlPackungen`": 1,
`n                `"arzneimittelKategorie`": null,
`n                `"autIdem`": false,
`n                `"benutzeERezept`": true,
`n                `"benutzeRezeptinformationstyp`": 34,
`n                `"benutzeSekundaerenRezeptinformationstyp`": false,
`n                `"btmKennzeichen`": null,
`n                `"dosierschema`": {
`n                    `"$abends`": 0,
`n                    `"freitext`": null,
`n                    `"$mittags`": 0,
`n                    `"$morgens`": 1,
`n                    `"$nachts`": 0
`n                },
`n                `"dosierungAufRezept`": true,
`n                `"erezeptZusatzdaten`": {
`n                    `"abgabehinweis`": null,
`n                    `"mehrfachverordnungen`": []
`n                },
`n                `"erezeptfaehig`": true,
`n                `"ersatzverordnungGemaessParagraph31`": false,
`n                `"farbmarkierung`": null,
`n                `"farbmarkierungZumVerordnungszeitpunkt`": null,
`n                `"freitext`": null,
`n                `"hinweis`": null,
`n                `"layerIndex`": 1,
`n                `"letzterInformationstyp`": null,
`n                `"letzterVerordnungszeitpunkt`": null,
`n                `"medikationsplanBestellposition`": null,
`n                `"mehrfachverordnungId`": null,
`n                `"packung`": {
`n                    `"amrlHinweiseVorhanden`": true,
`n                    `"anlageIIIAnzeigen`": true,
`n                    `"anlageVIITeilB`": false,
`n                    `"anstaltspackung`": false,
`n                    `"anzahlEinheiten`": 20,
`n                    `"anzahlEinheitenFuerReichweitenberechnung`": 20,
`n                    `"anzahlTeilbareStuecke`": 2,
`n                    `"arzneimittelVertriebsstatus`": null,
`n                    `"atcCodes`": [
`n                        `"$atcCodes`"
`n                    ],
`n                    `"aufNegativliste`": false,
`n                    `"bilanzierteDiaet`": false,
`n                    `"darreichungsform`": {
`n                        `"freitext`": null,
`n                        `"ifaCode`": null
`n                    },
`n                    `"darreichungsformIfaCode`": `"TAB`",
`n                    `"einheitenname`": `"$einheitenname`",
`n                    `"einheitennameFuerReichweitenberechnung`": `"$einheitennameFuerReichweitenberechnung`",
`n                    `"erezeptName`": `"$erezeptName`",
`n                    `"fiktivZugelassenesMedikament`": false,
`n                    `"handelsname`": `"$handelsname`",
`n                    `"herstellername`": `"$herstellername`",
`n                    `"name`": `"$name`",
`n                    `"lifeStyleStatus`": 0,
`n                    `"lifestyleStatusAnzeigen`": true,
`n                    `"medizinprodukt`": false,
`n                    `"medizinproduktAnzeigen`": false,
`n                    `"negativlisteAnzeigen`": true,
`n                    `"otcOtxAnzeigen`": true,
`n                    `"otcStatus`": false,
`n                    `"otxStatus`": false,
`n                    `"packungsgroesse`": `"N1`",
`n                    `"pzn`": `"$PZN`",
`n                    `"reimport`": false,
`n                    `"removed`": false,
`n                    `"rezeptStatus`": 2,
`n                    `"teratogen`": false,
`n                    `"verbandmittel`": false,
`n                    `"verbandmittelAnzeigen`": true,
`n                    `"verordnungsfaehigesMedizinprodukt`": false,
`n                    `"vertriebsStatus`": null,
`n                    `"vertriebsstatusAnzeigen`": true,
`n                    `"wirkstoffWirkstaerken`": []
`n                },
`n                `"pimPraeparat`": false,
`n                `"primaererRezeptinformationstyp`": 34,
`n                `"requiresArzneimittelempfehlungenCheck`": false,
`n                `"sekundaererRezeptinformationstyp`": 98,
`n                `"verordnungsausschluss`": false,
`n                `"verordnungseinschraenkung`": true,
`n                `"wirkstoff`": null
`n            }
`n        }
`n    ]
`n}"

$postData = [System.Text.Encoding]::UTF8.GetBytes($body)

$URI = "https://" + $serverAddress + ":16567/aps/rest/verordnung/rezept/ausstellen/saveerezepte"
$response = Invoke-RestMethod -Uri $URI -Method 'POST' -Headers $headers -Body $postData
$response | ConvertTo-Json
