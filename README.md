# ETL-Processor for bwHC data [![Run Tests](https://github.com/CCC-MF/etl-processor/actions/workflows/test.yml/badge.svg)](https://github.com/CCC-MF/etl-processor/actions/workflows/test.yml)

Diese Anwendung versendet ein bwHC-MTB-File an das bwHC-Backend und pseudonymisiert die Patienten-ID.

## Pseudonymisierung der Patienten-ID

Wenn eine URI zu einer gPAS-Instanz angegeben ist, wird diese verwendet.
Ist diese nicht gesetzt. wird intern eine Anonymisierung der Patienten-ID vorgenommen.

* `APP_PSEUDONYMIZE_PREFIX`: Standortbezogenes Prefix - `UNKNOWN`, wenn nicht gesetzt
* `APP_PSEUDONYMIZE_GENERATOR`: `BUILDIN` oder `GPAS` - `BUILDIN`, wenn nicht gesetzt

### Eingebaute Pseudonymisierung

Wurde keine oder die Verwendung der eingebauten Pseudonymisierung konfiguriert, so wird für die Patienten-ID der
entsprechende SHA-256-Hash gebildet und Base64-codiert - hier ohne endende "=" - zuzüglich des konfigurierten Prefixes
als Patienten-Pseudonym verwendet.

### Pseudonymisierung mit gPAS

Wurde die Verwendung von gPAS konfiguriert, so sind weitere Angaben zu konfigurieren.

* `APP_PSEUDONYMIZE_GPAS_URI`: URI der gPAS-Instanz inklusive Endpoint (z.B. `http://localhost:8080/ttp-fhir/fhir/gpas/$pseudonymizeAllowCreate`)
* `APP_PSEUDONYMIZE_GPAS_TARGET`: gPas Domänenname
* `APP_PSEUDONYMIZE_GPAS_USERNAME`: gPas Basic-Auth Benutzername
* `APP_PSEUDONYMIZE_GPAS_PASSWORD`: gPas Basic-Auth Passwort
* `APP_PSEUDONYMIZE_GPAS_SSLCALOCATION`: Root Zertifikat für gPas, falls es dediziert hinzugefügt werden muss.

## Mögliche Endpunkte

Für REST-Requests als auch zur Nutzung von Kafka-Topics können Endpunkte konfiguriert werden.

Es ist dabei nur die Konfiguration eines Endpunkts zulässig.
Werden sowohl REST als auch Kafka-Endpunkt konfiguriert, wird nur der REST-Endpunkt verwendet.

### REST

Folgende Umgebungsvariablen müssen gesetzt sein, damit ein bwHC-MTB-File an das bwHC-Backend gesendet wird:

* `APP_REST_URI`: URI der zu benutzenden API der bwHC-Backend-Instanz. z.B.: `http://localhost:9000/bwhc/etl/api`

### Kafka-Topics

Folgende Umgebungsvariablen müssen gesetzt sein, damit ein bwHC-MTB-File an ein Kafka-Topic übermittelt wird:

* `APP_KAFKA_TOPIC`: Zu verwendendes Topic zum Versenden von Anfragen
* `APP_KAFKA_RESPONSE_TOPIC`: Topic mit Antworten über den Erfolg des Versendens. Standardwert: `APP_KAFKA_TOPIC` mit Anhang "_response".
* `APP_KAFKA_GROUP_ID`: Kafka GroupID des Consumers. Standardwert: `APP_KAFKA_TOPIC` mit Anhang "_group".
* `APP_KAFKA_SERVERS`: Zu verwendende Kafka-Bootstrap-Server als kommagetrennte Liste

Wird keine Rückantwort über Apache Kafka empfangen und es gibt keine weitere Möglichkeit den Status festzustellen, verbleibt der Status auf `UNKNOWN`.

Weitere Einstellungen können über die Parameter von Spring Kafka konfiguriert werden.

Lässt sich keine Verbindung zu dem bwHC-Backend aufbauen, wird eine Rückantwort mit Status-Code `900` erwartet, welchen es
für HTTP nicht gibt.

## Docker-Images

Diese Anwendung ist auch als Docker-Image verfügbar: https://github.com/CCC-MF/etl-processor/pkgs/container/etl-processor