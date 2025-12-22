# Technische Dokumentation - Once-Only Simulation

## Konzeptionelle Architektur

### 1. Schichtenmodell

```
┌─────────────────────────────────────────────────────┐
│              Presentation Layer (UI)                │
│  - Terminal-basierte Benutzerinteraktion            │
│  - Farbige, strukturierte Ausgabe (ANSI Codes)      │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│           Application Layer (Main)                  │
│  - Event-Loop für Kommando-Verarbeitung             │
│  - Orchestrierung der Simulationskomponenten        │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│         X-Road Infrastructure Layer                 │
│  - Central Server (Service Registry)                │
│  - Security Server (Vermittler)                     │
│  - Message Format (XRoadMessage)                    │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Client Layer                           │
│  - Polizei, Einwohnermeldeamt, KFZ-Zulassung        │
│  - Bürgerportal                                     │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Domain Layer                           │
│  - Datenmodelle (Citizen, Vehicle)                  │
│  - Logging-Mechanismus (DataAccessLog)              │
└─────────────────────────────────────────────────────┘
```

### 2. Kommunikationsfluss

#### Beispiel: Polizei erstellt Strafzettel

```
┌──────────┐                                    ┌──────────────┐
│ Polizei  │                                    │ SS-POLIZEI   │
│          │─(1) Kennzeichen M-AB-1234─────────▶│              │
└──────────┘                                    │              │
                                                │ (2) Erstelle │
                                                │  XRoadMessage│
                                                └──────┬───────┘
                                                       │
                    ┌──────────────────────────────────▼
                    │ (3) Suche Ziel-SS in Registry
                    │     Ziel: KFZ-ZULASSUNG → SS-KFZ
                    ▼
            ┌───────────────┐
            │    SS-KFZ     │
            │               │◀──(4) Empfange Request
            │ (5) Leite an  │     mit Service: get-vehicle-owner
            │  Client weiter│
            └───────┬───────┘
                    │
            ┌───────▼───────────┐
            │ KFZ-Zulassungs-   │
            │ stelle            │
            │                   │
            │ (6) Verarbeite    │
            │     Anfrage       │
            │ (7) Finde Halter  │
            │     ID in Map     │
            └───────┬───────────┘
                    │
                    │ (8) Response mit ownerId
                    ▼
            ┌───────────────┐                                 ┌──────────────┐
            │    SS-KFZ     │                                 |  SS-POLIZEI  |
            │               │─(9) Sende Response zurück─────▶ |              |
            │ (10) Logge    │                                 │(11) Empfange | 
            │ Datenzugriff  │                                 │ Response     │
            └───────────────┘                                 └──────┬───────┘
                                                                     │
              ┌──────────┐                                           │
              │ Polizei  │◀─────(12) ownerId: DE-BG-2001-M-001───────┘
              │          │
              │ (13) Wiederhole für Adresse                         
              │      beim Einwohnermeldeamt                          
              └──────────┘                                          
```

### 3. Datenfluss-Diagramm

```
PHASE 1: Initialisierung
═══════════════════════════
Central Server
    ↓ (Config Pull)
Security Servers (4x)
    ↓ (Peer Registration)
X-Road Netzwerk etabliert
    ↓ (Attach)
Clients (4x) verbunden


PHASE 2: Adressänderung durch Bürger
════════════════════════════════════════
Bürger (Portal) → "Neue Adresse: Hauptstraße 42"
    ↓
Einwohnermeldeamt
    ↓ (Update in Map)
citizen.address = "Hauptstraße 42"
    ↓
DataAccessLog.add(...)
    ↓
Änderung persistiert


PHASE 3: Polizei-Abfrage
═══════════════════════════
Polizei → "Kennzeichen: M-AB-1234"
    ↓
SS-POLIZEI → SS-KFZ
    ↓
KFZ-Zulassung
    ↓ (Map Lookup)
vehicles.get("M-AB-1234") → ownerId
    ↓
SS-KFZ → SS-POLIZEI
    ↓
Polizei erhält: "DE-BG-2001-M-001"
    ↓
SS-POLIZEI → SS-EMA
    ↓
Einwohnermeldeamt
    ↓ (Map Lookup)
citizens.get("DE-BG-2001-M-001") → address
    ↓
SS-EMA → SS-POLIZEI
    ↓
Polizei erhält: "Hauptstraße 42"
    ↓
Strafzettel erstellt ✓
    ↓
2x DataAccessLog.add(...)
```

## Implementierungs-Details

### Netzwerksimulation

Da eine echte Netzwerkkommunikation für eine lokale Demo zu komplex wäre, wird die Kommunikation wie folgt simuliert:

```scala
// Statt TCP/IP Sockets:
class SecurityServer {
  private var securityServers: Map[String, SecurityServer] = Map.empty
  
  def sendRequest(message: XRoadMessage): Option[XRoadMessage] = {
    // Direkte Referenz statt Netzwerk-Call
    val targetSS = securityServers.get(message.recipient)
    targetSS.map(_.receiveRequest(message))
  }
}
```

**Vorteile dieser Simulation:**
- Keine komplexe Socket-Programmierung notwendig
- Deterministisches Verhalten (kein Netzwerk-Timing)
- Einfaches Debugging
- Fokus auf das **Prinzip** statt Implementierungsdetails

### Verzögerungen für Nachvollziehbarkeit

```scala
Terminal.logInfo("SS-POLIZEI: Bereite Anfrage vor")
Thread.sleep(400)  // 400ms Pause für Lesbarkeit
```

Diese künstlichen Verzögerungen ermöglichen dem Zuschauer:
- Den Ablauf in Echtzeit zu verfolgen
- Jeden Schritt zu verstehen
- Die Komplexität zu erfassen

### Logging-Mechanismus

```scala
object DataAccessLog {
  private var logs: List[DataAccessLog] = List.empty
  
  def add(log: DataAccessLog): Unit = synchronized {
    logs = log :: logs  // Thread-safe Addition
  }
}
```

**Wichtig:** `synchronized` für Thread-Safety, auch wenn in dieser Simulation nur ein Thread aktiv ist. Best Practice für spätere Erweiterungen.

## Designentscheidungen

### 1. Scala als Programmiersprache

**Pro:**
- Funktionale Programmierung passt zu unveränderlichen Datenstrukturen
- Pattern Matching für elegante Message-Verarbeitung
- Case Classes für saubere Domain-Modelle
- JVM-Basis → JAR-Distribution einfach

**Contra:**
- Höhere Komplexität als Java/Kotlin
- Längere Compile-Zeiten

### 2. Terminal-basiert statt GUI

**Pro:**
- Fokus auf Funktionalität, nicht Design
- Schnelle Entwicklung
- Leicht nachvollziehbare Ausgaben
- Professioneller "Developer-Look"

**Contra:**
- Weniger visuell ansprechend
- ANSI-Codes nicht auf allen Terminals unterstützt

### 3. In-Memory statt Datenbank

**Pro:**
- Einfache Setup ohne externe Dependencies
- Schnelle Performance
- Ausreichend für Demo-Zwecke

**Contra:**
- Keine Persistenz (bei jedem Start neue Daten)
- Nicht skalierbar

## Erweiterungsmöglichkeiten

### 1. Persistenz
```scala
// Statt Map:
import slick.jdbc.H2Profile.api._

class CitizenRepository(db: Database) {
  // SQL-basierte Speicherung
}
```

### 2. Echte Netzwerkkommunikation
```scala
// Mit Akka HTTP:
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

class SecurityServerHTTP {
  val route = post {
    path("xroad" / "message") {
      entity(as[XRoadMessage]) { message =>
        // Verarbeitung
      }
    }
  }
}
```

### 3. Verschlüsselung
```scala
import javax.crypto.Cipher
import java.security.KeyPairGenerator

trait Encryption {
  def encrypt(data: Array[Byte]): Array[Byte]
  def decrypt(data: Array[Byte]): Array[Byte]
}
```

## Testing-Strategien

### Unit Tests (Beispiel)
```scala
class SecurityServerSpec extends AnyFlatSpec {
  "SecurityServer" should "route messages correctly" in {
    val cs = new CentralServer()
    val ss1 = new SecurityServer("SS-1", "CLIENT1", cs)
    val ss2 = new SecurityServer("SS-2", "CLIENT2", cs)
    
    ss1.registerPeer(ss2)
    
    val msg = XRoadMessage(...)
    val response = ss1.sendRequest(msg)
    
    assert(response.isDefined)
  }
}
```

### Integration Tests
```scala
class EndToEndSpec extends AnyFlatSpec {
  "Complete flow" should "work from Police to Citizen" in {
    // Setup gesamte Infrastruktur
    // Führe Strafzettel-Szenario aus
    // Prüfe Logs
  }
}
```

## Performance-Überlegungen

Aktuelle Simulation:
- **Startup**: ~2-3 Sekunden
- **Strafzettel-Erstellung**: ~5-6 Sekunden (inkl. Delays)
- **Memory**: < 50 MB

Optimierungspotential:
- Parallele Abfragen (aktuell sequenziell)
- Caching von häufig abgefragten Daten
- Lazy Loading von Client-Komponenten

## Lessons Learned

1. **Simplicity first**: Nicht zu früh optimieren
2. **Visual Feedback**: Terminal-Farben erhöhen Verständnis massiv
3. **Thread.sleep()**: Einfachste Lösung für Demo-Zwecke
4. **Trennung**: Klare Schichten-Architektur wichtig
5. **Documentation**: Code sollte selbsterklärend sein

## Fazit

Diese Simulation erfüllt alle Anforderungen:
- ✅ Demonstration Once-Only-Prinzip
- ✅ Nachvollziehbare X-Road Kommunikation
- ✅ Vollständiges Logging
- ✅ Interaktive Bedienung
- ✅ Lauffähig als JAR
- ✅ Professionelle Terminal-UI

Sie ist **bewusst vereinfacht**, um das **Prinzip** zu zeigen, nicht eine produktionsreife Lösung zu implementieren.
