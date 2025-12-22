#!/bin/bash

echo "========================================"
echo "Once-Only Simulation - Build & Start"
echo "========================================"
echo ""

# Prüfe ob SBT installiert ist
if ! command -v sbt &> /dev/null; then
    echo "❌ SBT ist nicht installiert!"
    echo "Bitte installieren Sie SBT: https://www.scala-sbt.org/download.html"
    exit 1
fi

# Prüfe ob Java installiert ist
if ! command -v java &> /dev/null; then
    echo "❌ Java ist nicht installiert!"
    echo "Bitte installieren Sie Java 17 oder höher"
    exit 1
fi

echo "✓ SBT gefunden: $(sbt --version | head -n 1)"
echo "✓ Java gefunden: $(java -version 2>&1 | head -n 1)"
echo ""

# Baue das Projekt
echo "🔨 Baue Projekt..."
sbt assembly

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build erfolgreich!"
    echo ""
    echo "🚀 Starte Simulation..."
    echo ""
    
    # Finde die JAR-Datei
    JAR_FILE=$(find target -name "once-only-simulation.jar" | head -n 1)
    
    if [ -f "$JAR_FILE" ]; then
        java -jar "$JAR_FILE"
    else
        echo "❌ JAR-Datei nicht gefunden!"
        exit 1
    fi
else
    echo ""
    echo "❌ Build fehlgeschlagen!"
    exit 1
fi
