# 🚀 Quick Start Guide - Once-Only Simulation

## In 5 Minuten zur laufenden Demo!

### Schritt 1: Voraussetzungen prüfen ✓

```bash
# Java installiert?
java -version
# Sollte Java 17+ anzeigen

# SBT installiert?
sbt --version
# Sollte SBT 1.x anzeigen
```

**Nicht installiert?**
- Java: https://www.oracle.com/de/java/technologies/downloads/
- SBT: https://www.scala-sbt.org/download.html

### Schritt 2: Projekt entpacken 📦

```bash
unzip once-only-sim.zip
cd once-only-sim
```

### Schritt 3: Starten! 🎯

**Option A - Mit Start-Script (empfohlen):**

Linux/Mac:
```bash
./start.sh
```

Windows:
```cmd
start.bat
```

**Option B - Manuell:**

```bash
# Projekt bauen
sbt assembly

# JAR ausführen
java -jar target/scala-3.7.0/once-only-simulation.jar
```

**Option C - Direkt mit SBT (langsamerer Start):**

```bash
sbt run
```

### Schritt 4: Erste Schritte in der Simulation 🎮

Wenn die Simulation läuft, sehen Sie:

```
╔═══════════════════════════════════════════════════════════════════╗
║                   ONCE-ONLY SIMULATION                            ║
╚═══════════════════════════════════════════════════════════════════╝

simulation> _
```

**Kommando 1 - System starten:**
```
simulation> 1
```
oder
```
simulation> start-simulation
```

**Kommando 2 - Strafzettel erstellen:**
```
simulation> 3
```

Kennzeichen eingeben: `M-AB-1234`

**Kommando 3 - Zugriffs-Log ansehen:**
```
simulation> 4
```

Bürger-ID eingeben: `DE-BG-2001-M-001`

**Kommando 4 - Bürgerportal öffnen:**
```
simulation> 2
```

Bürger-ID: `DE-BG-2001-M-001`
Neue Adresse: `Hauptstraße 42, 80331 München`

### Demo-Szenario: Vollständiger Durchlauf 🎬

1. **Start** → `1` (Infrastruktur wird aufgebaut)
2. **Polizei** → `3` → `M-AB-1234` (Strafzettel erstellen)
3. **Log** → `4` → `DE-BG-2001-M-001` (Zugriffe anzeigen)
4. **Adresse** → `2` → `1` (Adresse ändern) → Neue Adresse eingeben
5. **Polizei** → `3` → `M-AB-1234` (Neue Adresse wird automatisch verwendet!)
6. **Log** → `4` → `DE-BG-2001-M-001` (Mehr Zugriffe protokolliert)

### Troubleshooting 🔧

**Problem: "SBT not found"**
```bash
# Linux/Mac - Installation via SDKMAN
curl -s "https://get.sdkman.io" | bash
sdk install sbt
```

**Problem: "Java version too old"**
```bash
# Upgrade auf Java 17+
# https://adoptium.net/de/temurin/releases/
```

**Problem: "Assembly failed"**
```bash
# Bereinigen und neu bauen
sbt clean
sbt assembly
```

**Problem: "Farben werden nicht angezeigt"**
- Windows: Nutzen Sie Windows Terminal oder PowerShell 7+
- Linux/Mac: Sollte in allen modernen Terminals funktionieren

### Tipps für die Präsentation 💡

1. **Terminal vergrößern** - Große Schrift für bessere Lesbarkeit
2. **Langsam vorgehen** - Die Delays sind eingebaut, nutzen Sie sie!
3. **Status checken** - Kommando `5` zeigt System-Übersicht
4. **Help nutzen** - Kommando `6` erklärt alles im Detail

### Demo-Daten zum Merken 📝

**Bürger:**
- Max Mustermann: `DE-BG-2001-M-001`
- Erika Musterfrau: `DE-BG-1995-F-042`
- Hans Schmidt: `DE-BG-1988-M-123`

**Kennzeichen:**
- `M-AB-1234` → Max Mustermann
- `M-XY-5678` → Erika Musterfrau
- `M-CD-9999` → Hans Schmidt

### Wichtige Kommandos

```
1 = start-simulation       System initialisieren
2 = buergerportal          Öffnet das Bürgerportal (Adressänderung, Log-Einsicht)
3 = police-tickethunt      Strafzettel erstellen
4 = buerger-datatracker    Zugriffs-Log
5 = status                 System-Status
6 = help                   Hilfe
0 = exit                   Beenden
```

### Nächste Schritte 📚

- Lesen Sie `README.md` für Details
- Schauen Sie `ARCHITECTURE.md` für technische Details
- Erkunden Sie `DOCUMENTATION.md` für Implementierung

### Viel Erfolg! 🎓

Diese Simulation demonstriert das Once-Only-Prinzip von E-Estonia
perfekt für Ihre Bachelor-Thesis!

Bei Fragen oder Problemen:
- Prüfen Sie die Dokumentation
- Schauen Sie in die Kommentare im Code
- Testen Sie verschiedene Szenarien

**Happy Simulating! 🚀**
