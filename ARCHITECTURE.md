# Architektur-Übersicht: Once-Only Simulation

## Komponenten-Diagramm

```
┌──────────────────────────────────────────────────────────────────────┐
│                          MAIN APPLICATION                            │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    Event Loop & UI Layer                       │  │
│  │  - Command Processing (start, police-tickethunt, etc.)         │  │
│  │  - Terminal.scala (ANSI Colors, Formatting)                    │  │
│  │  - SimulationContext.scala (Dependency Injection Container)    │  │
│  └────────────────────────┬───────────────────────────────────────┘  │
│                           │                                          │
│  ┌────────────────────────▼────────────────────────────────────────┐ │
│  │              X-ROAD INFRASTRUCTURE LAYER                        │ │
│  │                                                                 │ │
│  │  ┌─────────────────┐                                            │ │
│  │  │ CentralServer   │  ← Service Registry & Configuration        │ │
│  │  └────────┬────────┘                                            │ │
│  │           │                                                     │ │
│  │  ┌────────┴───────────────────────────────────────────────────┐ │ │
│  │  │         Security Server Network                            │ │ │
│  │  │                                                       │    │ │ |
│  │  │  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐ │ │ |
│  │  │  │SS-POLIZEI│◄─►│  SS-EMA  │◄─►│  SS-KFZ  │◄─►│SS-PORTAL │ │ │ |
│  │  │  └─────┬────┘   └─────┬────┘   └─────┬────┘   └────┬─────┘ │ │ |
│  │  │        │              │              │             │       │ │ |
│  │  └────────┼──────────────┼──────────────┼─────────────┼───────┘ │ │
│  │           │              │              │             │         │ │
│  └───────────┼──────────────┼──────────────┼─────────────┼─────────┘ │
│              │              │              │             │           │
│  ┌───────────▼──────────────▼──────────────▼─────────────▼─────────┐ │
│  │                      CLIENT LAYER                               │ │
│  │                                                                 │ │
│  │  ┌────────────┐  ┌─────────────┐  ┌──────────┐  ┌───────────┐   │ │
│  │  │  Polizei   │  │ Einwohner-  │  │   KFZ-   │  │  Bürger-  │   │ │
│  │  │  München   │  │ meldeamt    │  │ Zulassung│  │  portal   │   │ │
│  │  │            │  │             │  │          │  │           │   │ │
│  │  │ • Ticket   │  │ • Citizens  │  │ • Vehicle│  │ • Login   │   │ │
│  │  │   Creation │  │   Registry  │  │   Owner  │  │ • Update  │   │ │
│  │  │ • Query    │  │ • Update    │  │   Lookup │  │   Address │   │ │
│  │  │   Data     │  │   Address   │  │          │  │           │   │ │
│  │  └────────────┘  └─────────────┘  └──────────┘  └───────────┘   │ │
│  │                                                                 │ │
│  └──────────────────────────────┬──────────────────────────────────┘ │
│                                 │                                    │
│  ┌──────────────────────────────▼──────────────────────────────────┐ │
│  │                      DOMAIN LAYER                               │ │
│  │                                                                 │ │
│  │  ┌──────────┐  ┌──────────┐  ┌─────────────────┐                │ │
│  │  │ Citizen  │  │ Vehicle  │  │ DataAccessLog   │                │ │
│  │  │          │  │          │  │                 │                │ │
│  │  │ • ID     │  │ • Plate  │  │ • citizenId     │                │ │
│  │  │ • Name   │  │ • Owner  │  │ • timestamp     │                │ │
│  │  │ • Address│  │ • Brand  │  │ • consumer      │                │ │
│  │  │ • DOB    │  │ • Model  │  │ • provider      │                │ │
│  │  └──────────┘  └──────────┘  │ • dataTransf.   │                │ │
│  │                              │ • purpose       │                │ │
│  │                              └─────────────────┘                │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

## Message Flow: Strafzettel-Erstellung

```
TIME →

T0: Polizei erfasst Kennzeichen
    │
    ├─► PoliceClient.processTicket("M-AB-1234")
    │
T1: Erste X-Road Anfrage
    │
    ├─► XRoadMessage(
    │       sender: "POLIZEI"
    │       recipient: "KFZ-ZULASSUNG"
    │       service: "get-vehicle-owner"
    │       payload: {"licensePlate": "M-AB-1234"}
    │   )
    │
T2: SS-POLIZEI → SS-KFZ
    │
    ├─► securityServer.sendRequest(message)
    │   │
    │   ├─► Terminal.logXRoad("SS-POLIZEI --> SS-KFZ")
    │   └─► Thread.sleep(500) [Visualisierung]
    │
T3: KFZ-Zulassung verarbeitet
    │
    ├─► VehicleRegistry.handleRequest(message)
    │   │
    │   ├─► vehicles.get("M-AB-1234")
    │   │   └─► Some(Vehicle(..., ownerId="DE-BG-2001-M-001"))
    │   │
    │   └─► message.createResponse(
    │           {"ownerId": "DE-BG-2001-M-001"}
    │       )
    │
T4: DataAccessLog protokolliert
    │
    ├─► DataAccessLog.add(
    │       citizenId: "DE-BG-2001-M-001"
    │       consumer: "POLIZEI"
    │       provider: "KFZ-ZULASSUNG"
    │       purpose: "Strafzettel wegen Falschparken"
    │   )
    │
T5: Zweite X-Road Anfrage
    │
    ├─► XRoadMessage(
    │       sender: "POLIZEI"
    │       recipient: "EINWOHNERMELDEAMT"
    │       service: "get-citizen-data"
    │       payload: {"citizenId": "DE-BG-2001-M-001"}
    │   )
    │
T6: SS-POLIZEI → SS-EMA
    │
    ├─► securityServer.sendRequest(message)
    │
T7: Einwohnermeldeamt verarbeitet
    │
    ├─► ResidenceRegistry.handleRequest(message)
    │   │
    │   ├─► citizens.get("DE-BG-2001-M-001")
    │   │   └─► Some(Citizen(..., address="Musterstraße 1"))
    │   │
    │   └─► message.createResponse(
    │           {"address": "Musterstraße 1", "name": "Max Mustermann"}
    │       )
    │
T8: DataAccessLog protokolliert erneut
    │
    ├─► DataAccessLog.add(
    │       citizenId: "DE-BG-2001-M-001"
    │       consumer: "POLIZEI"
    │       provider: "EINWOHNERMELDEAMT"
    │       purpose: "Strafzettel-Zustellung"
    │   )
    │
T9: Strafzettel wird erstellt
    │
    └─► Terminal.printBox(
            "STRAFZETTEL"
            "Kennzeichen: M-AB-1234"
            "Halter: Max Mustermann"
            "Adresse: Musterstraße 1"
            "Betrag: 25,00 EUR"
        )

RESULTAT: 
✓ Strafzettel erstellt
✓ 2 Datenzugriffe protokolliert
✓ Bürger kann Log einsehen
```

## Klassen-Hierarchie

```
trait Client
├── extends: PoliceClient
├── extends: CitizenPortal
├── extends: ResidenceRegistry
└── extends: VehicleRegistry

class SecurityServer
├── has: CentralServer (reference)
├── has: Map[String, SecurityServer] (peers)
└── sends: XRoadMessage

case class XRoadMessage
├── enum: MessageType (Request/Response)
└── method: createResponse()

object DataAccessLog
├── stores: List[DataAccessLog]
├── method: add()
├── method: getLogsForCitizen()
└── method: getAllLogs()
```

## Package-Struktur

```
once-only-sim/
│
├── Main.scala                    ← Entry Point, Event Loop
│
├── core/                         ← X-Road Infrastructure
│   ├── CentralServer.scala       ← Service Registry
│   ├── SecurityServer.scala      ← Message Broker
│   ├── XRoadMessage.scala        ← Data Transfer Object
│   └── SimulationContext.scala   ← DI Container & State
│
├── clients/                      ← Government Services
│   ├── Client.scala              ← Base Trait
│   ├── PoliceClient.scala        ← Law Enforcement
│   ├── CitizenPortal.scala       ← Citizen Interface
│   ├── ResidenceRegistry.scala   ← Population Register
│   └── VehicleRegistry.scala     ← Motor Vehicle Dept
│
├── domain/                       ← Business Logic
│   └── Models.scala              ← Citizen, Vehicle, Logs
│
└── ui/                           ← User Interface
    └── Terminal.scala            ← ANSI Terminal Output
```

## Datenfluss: Once-Only Prinzip

```
┌─────────────────────────────────────────────────────────────┐
│  1. SINGLE SOURCE OF TRUTH                                  │
│                                                             │
│  Einwohnermeldeamt           KFZ-Zulassung                  │
│  ┌─────────────────┐         ┌─────────────────┐            │
│  │ citizens: Map   │         │ vehicles: Map   │            │
│  │ ├─ DE-BG-001    │         │ ├─ M-AB-1234    │            │
│  │ │  └─ Address   │         │ │  └─ Owner     │            │
│  │ └─ ...          │         │ └─ ...          │            │
│  └─────────────────┘         └─────────────────┘            │
│         ▲                            ▲                      │
│         │ NO DUPLICATION             │                      │
│         │                            │                      │
└─────────┼────────────────────────────┼──────────────────────┘
          │                            │
┌─────────┼────────────────────────────┼──────────────────────┐
│  2. ON-DEMAND DATA EXCHANGE          |                      │
│         │                            │                      │
│         └──────────┬─────────────────┘                      │
│                    │                                        │
│           X-Road Message Bus                                │
│           (Security Servers)                                │
│                    │                                        │
│         ┌──────────┴─────────────┐                          │
│         │                        │                          │
│    ┌────▼────┐              ┌────▼────┐                     │
│    │ Polizei │              │ Bürger  │                     │
│    │         │              │ Portal  │                     │
│    └─────────┘              └─────────┘                     │
│      Consumer                 Consumer                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
          │                            │
┌─────────┼────────────────────────────┼──────────────────────┐
│  3. FULL TRANSPARENCY                |                      │
│         │                            │                      │
│         └──────────┬─────────────────┘                      │
│                    │                                        │
│            DataAccessLog.add()                              │
│                    │                                        │
│         ┌──────────▼─────────────┐                          │
│         │  Audit Log (Citizen)   │                          │
│         │  ├─ Who accessed?      │                          │
│         │  ├─ When?              │                          │
│         │  ├─ What data?         │                          │
│         │  └─ Why (purpose)?     │                          │
│         └────────────────────────┘                          │
│                    |                                        │
│                    ▼                                        │
│         Citizen can review anytime                          │
│         via "buerger-datatracker"                           │
└─────────────────────────────────────────────────────────────┘
```

## State Management

### Citizen State Update Flow

```
BEFORE:
citizens = Map(
  "DE-BG-2001-M-001" → Citizen(
    id: "DE-BG-2001-M-001"
    name: "Max Mustermann"
    address: "Musterstraße 1, 80331 München"  ← OLD
    dob: "01.01.1985"
  )
)

ACTION: Bürger ändert Adresse über Portal
  ↓
CitizenPortal → ResidenceRegistry.updateAddress(
  citizenId: "DE-BG-2001-M-001"
  newAddress: "Hauptstraße 42, 80331 München"
)
  ↓
citizen.address = "Hauptstraße 42, 80331 München"  ← UPDATE

AFTER:
citizens = Map(
  "DE-BG-2001-M-001" → Citizen(
    id: "DE-BG-2001-M-001"
    name: "Max Mustermann"
    address: "Hauptstraße 42, 80331 München"  ← NEW
    dob: "01.01.1985"
  )
)

RESULT:
✓ Nächste Polizei-Abfrage erhält automatisch neue Adresse
✓ KEINE manuelle Synchronisation notwendig
✓ KEINE Datenduplikation
✓ Once-Only Prinzip erfüllt!
```

## Zusammenfassung

Diese Architektur demonstriert:

1. **Dezentrale Datenhaltung** - Jede Behörde verwaltet ihre Daten selbst
2. **Föderierter Datenaustausch** - X-Road verbindet, speichert aber nicht
3. **Transparenz durch Logging** - Jeder Zugriff wird protokolliert
4. **Effizienz** - Automatisierte Abfragen ohne manuelle Prozesse
5. **Datenhoheit** - Bürger behält Kontrolle und Transparenz

Das ist das **Herzstück des Once-Only-Prinzips**: 
Daten nur einmal erfassen, aber überall verfügbar machen - 
sicher, transparent und effizient.
