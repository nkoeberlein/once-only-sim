@echo off
echo ========================================
echo Once-Only Simulation - Build ^& Start
echo ========================================
echo.

REM Prüfe ob SBT installiert ist
where sbt >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X SBT ist nicht installiert!
    echo Bitte installieren Sie SBT: https://www.scala-sbt.org/download.html
    pause
    exit /b 1
)

REM Prüfe ob Java installiert ist
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Java ist nicht installiert!
    echo Bitte installieren Sie Java 17 oder hoeher
    pause
    exit /b 1
)

echo + SBT gefunden
echo + Java gefunden
echo.

REM Baue das Projekt
echo Baue Projekt...
call sbt assembly

if %ERRORLEVEL% EQU 0 (
    echo.
    echo + Build erfolgreich!
    echo.
    echo Starte Simulation...
    echo.
    
    REM Finde die JAR-Datei
    for /r target %%i in (once-only-simulation.jar) do set JAR_FILE=%%i
    
    if exist "%JAR_FILE%" (
        java -jar "%JAR_FILE%"
    ) else (
        echo X JAR-Datei nicht gefunden!
        pause
        exit /b 1
    )
) else (
    echo.
    echo X Build fehlgeschlagen!
    pause
    exit /b 1
)

pause
