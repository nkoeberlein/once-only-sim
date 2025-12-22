# Once-Only Prinzip Simulation - X-Road Demo

Eine interaktive Terminal-basierte Simulation des Once-Only-Prinzips von E-Estonia mit vereinfachter X-Road Infrastruktur.

## 🎯 Ziel der Simulation

Diese Demonstration zeigt die Kernfunktionalität des Once-Only-Prinzips:

- **Datenhoheit**: Behörden als souveräne Datenquellen ohne zentrale Datenspeicherung
- **Datenkonsistenz**: Einmalige Erfassung, automatische Verteilung
- **Transparenz**: Vollständige Protokollierung aller Datenzugriffe
- **Effizienzgewinn**: Automatisierter behördenübergreifender Datenaustausch

## 🏗️ Architektur

```
┌─────────────────┐
│ Central Server  │ ← Verwaltet Service-Registry & Konfiguration
└────────┬────────┘
         │
      ┌──┴──────────────┬────────────────┬───────────────┐
      │                 │                │               │
┌─────▼──────┐    ┌─────▼─────┐   ┌──────▼─────┐   ┌─────▼─────┐
│ SS-POLIZEI │    │  SS-EMA   │   │   SS-KFZ   │   │ SS-PORTAL │
└─────┬──────┘    └─────┬─────┘   └──────┬─────┘   └─────┬─────┘
      │                 │                │               │
┌─────▼──────┐    ┌─────▼─────┐   ┌──────▼─────┐   ┌─────▼─────┐
│  Polizei   │    │Einwohner- │   │KFZ-Zulas-  │   │  Bürger-  │
│  München   │    │meldeamt   │   │sungsstelle │   │  portal   │
└────────────┘    └───────────┘   └────────────┘   └───────────┘
```

## 📋 Szenario: Strafzettel wegen Falschparken

1. Polizei erfasst Kennzeichen des Falschparkers
2. **X-Road Abfrage 1**: KFZ-Zulassung → Wer ist der Halter?
3. **X-Road Abfrage 2**: Einwohnermeldeamt → Aktuelle Adresse des Halters
4. Strafzettel wird erstellt und kann zugestellt werden
5. **Transparenz**: Alle Zugriffe werden im Bürger-Log protokolliert

## 🚀 Installation & Start

### Voraussetzungen
- Java 17 oder höher
- SBT (Scala Build Tool)

### Build & Ausführung

```bash
# Projekt bauen
sbt assembly

# JAR ausführen
java -jar target/scala-3.7.0/once-only-simulation.jar
```

Oder direkt mit SBT:
```bash
sbt run
```

Oder mithilfe des Startscripts (empfohlen):
```bash
./start.sh
```

## 💻 Verfügbare Kommandos

```
[1] start-simulation      - Startet die X-Road Infrastruktur
[2] buergerportal         - Öffnet das Bürgerportal (Adressänderung)
[3] police-tickethunt     - Polizei erstellt Strafzettel (Kennzeichen-Abfrage)
[4] buerger-datatracker   - Zeigt Datenzugriffs-Log eines Bürgers
[5] status                - Zeigt System-Status und Konfiguration
[6] help                  - Zeigt detaillierte Hilfe
[0] exit                  - Beendet die Simulation
```

## 📊 Demo-Daten

### Bürger (Einwohnermeldeamt)
- `DE-BG-2001-M-001`: Max Mustermann, Musterstraße 1, 80331 München
- `DE-BG-1995-F-042`: Erika Musterfrau, Beispielweg 5, 80333 München
- `DE-BG-1988-M-123`: Hans Schmidt, Teststraße 10, 80335 München

### Fahrzeuge (KFZ-Zulassung)
- `M-AB1234`: BMW 320d (Halter: Max Mustermann)
- `M-XY5678`: Audi A4 (Halter: Erika Musterfrau)
- `M-CD9999`: Mercedes C-Klasse (Halter: Hans Schmidt)

## 🔍 Beispiel-Ablauf

1. **Start**: `start-simulation`
   - Central Server wird initialisiert
   - 4 Security Server werden konfiguriert
   - Clients werden verbunden

2. **Adressänderung**: `buerger-changeaddress`
   - Login mit Bürger-ID
   - Neue Adresse eingeben
   - Änderung wird über X-Road propagiert

3. **Strafzettel**: `police-tickethunt`
   - Kennzeichen eingeben (z.B. `M-AB-1234`)
   - Automatische Halterabfrage bei KFZ-Zulassung
   - Automatische Adressabfrage beim Einwohnermeldeamt
   - Strafzettel wird erstellt

4. **Zugriffs-Log**: `buerger-datatracker`
   - Bürger-ID eingeben
   - Alle Datenzugriffe werden angezeigt mit:
     - Zeitstempel
     - Welche Stelle hat zugegriffen
     - Welche Daten wurden übermittelt
     - Zu welchem Zweck

## 🎨 Terminal-Features

- **Farbige Ausgabe** im nala-Stil
- **Dynamische Updates** mit Verzögerungen für Nachvollziehbarkeit
- **Box-Darstellung** für wichtige Informationen
- **Strukturierte Logs** mit Icons (✓, ●, ⚠, ✗, ◆)

## 📦 Projektstruktur

```
once-only-sim/
├── src/main/scala/
│   ├── Main.scala                      # Hauptprogramm & Event-Loop
│   ├── core/
│   │   ├── CentralServer.scala         # Service Registry
│   │   ├── SecurityServer.scala        # X-Road Vermittler
│   │   └── XRoadMessage.scala          # Nachrichtenformat
│   │   ├── SimulationContext.scala     # Dependency Injection / State State
│   ├── clients/
│   │   ├── Client.scala                # Basis-Trait
│   │   ├── CitizenPortal.scala         # Bürgerportal
│   │   ├── PoliceClient.scala          # Polizei
│   │   ├── ResidenceRegistry.scala     # Einwohnermeldeamt
│   │   └── VehicleRegistry.scala       # KFZ-Zulassung
│   ├── domain/
│   │   └── Models.scala                # Datenmodelle & Logging
│   └── ui/
│       └── Terminal.scala              # Terminal-UI mit Farben
└── build.sbt
```

## 🔐 Vereinfachungen gegenüber echter X-Road

Diese Simulation fokussiert sich auf das **Prinzip** und verzichtet auf:
- Echte Netzwerkkommunikation (simuliert durch direkte Methodenaufrufe)
- Verschlüsselung und digitale Signaturen
- Authentifizierung und Autorisierung
- Persistente Datenbanken (In-Memory Maps)
- Fehlerbehandlung und Retry-Mechanismen

## 📝 Wissenschaftlicher Kontext

Diese Simulation dient als **Proof of Concept** für die Bachelor-Thesis über:
- E-Estonia's digitales Governance-System
- Once-Only-Prinzip in der Praxis
- Transferierbarkeit auf deutsche Verwaltungsstrukturen

## 🛠️ Technologie-Stack

- **Scala 3.7** - Moderne funktionale Programmierung
- **SBT** - Build-Tool
- **ANSI Escape Codes** - Terminal-Formatierung
- **Scala Standard Library** - Threading & Collections

## 📄 Lizenz

Diese Software wurde für akademische Zwecke erstellt im Rahmen einer Bachelor-Thesis an der Universität der Bundeswehr München.

## 👤 Autor

Nikolaus Köberlein
Bachelor-Student Informatik
Universität der Bundeswehr München

---

**Hinweis**: Diese Simulation dient ausschließlich Demonstrationszwecken und ist keine produktionsreife Implementierung von X-Road.
