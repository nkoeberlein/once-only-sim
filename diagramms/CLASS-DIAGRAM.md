# Klassendiagramm - Once-Only Simulation

## Vollständiges UML-Klassendiagramm

```mermaid
classDiagram
    %% ==================== MAIN APPLICATION ====================
    class Main {
        -Option~CentralServer~ centralServer
        -Map~String, SecurityServer~ securityServers
        -Map~String, Any~ clients
        -Boolean isInitialized
        +main(args: Array~String~) void
        -runEventLoop() void
        -startSimulation() void
        -policeTicketHunt() void
        -showDataTracker() void
        -showStatus() void
        -checkInitialized() Boolean
    }

    %% ==================== CORE LAYER ====================
    class CentralServer {
        -Map~String, ServiceConfig~ config
        +getConfig(clientType: String) Option~ServiceConfig~
        +findSecurityServer(clientType: String) Option~String~
        +listAllServices() void
    }

    class ServiceConfig {
        +String clientName
        +String securityServerName
        +List~String~ services
    }

    class SecurityServer {
        +String name
        +String clientType
        -CentralServer centralServer
        -Map~String, SecurityServer~ securityServers
        +registerPeer(peer: SecurityServer) void
        +sendRequest(message: XRoadMessage) Option~XRoadMessage~
        +receiveRequest(message: XRoadMessage) Option~XRoadMessage~
        -logDataAccess(message: XRoadMessage) void
    }

    class XRoadMessage {
        +String id
        +MessageType messageType
        +String sender
        +String recipient
        +String service
        +Map~String, String~ payload
        +LocalDateTime timestamp
        +createResponse(responsePayload: Map~String, String~) XRoadMessage
    }

    class MessageType {
        <<enumeration>>
        Request
        Response
    }

    %% ==================== CLIENT LAYER ====================
    class Client {
        <<trait>>
        +String name
        +String clientType
        +Option~SecurityServer~ securityServer
        +attachSecurityServer(ss: SecurityServer) void
        #sendMessage(recipient: String, service: String, payload: Map~String, String~) Option~XRoadMessage~
    }

    class PoliceClient {
        -VehicleRegistry vehicleRegistry
        -ResidenceRegistry residenceRegistry
        +String name
        +String clientType
        +processTicket(licensePlate: String) void
    }

    class CitizenPortal {
        -ResidenceRegistry residenceRegistry
        +String name
        +String clientType
        +showPortal() void
        -showCitizenMenu(citizen: Citizen) void
        -changeAddress(citizen: Citizen) void
        -showDataAccess(citizenId: String) void
    }

    class ResidenceRegistry {
        -Map~String, Citizen~ citizens
        +String name
        +String clientType
        +getCitizen(citizenId: String) Option~Citizen~
        +updateAddress(citizenId: String, newAddress: String) Boolean
        +handleRequest(message: XRoadMessage) XRoadMessage
        +listAllCitizens() void
    }

    class VehicleRegistry {
        -Map~String, Vehicle~ vehicles
        +String name
        +String clientType
        +getVehicleOwner(licensePlate: String) Option~String~
        +handleRequest(message: XRoadMessage) XRoadMessage
        +listAllVehicles() void
    }

    %% ==================== DOMAIN LAYER ====================
    class Citizen {
        +String id
        +String name
        +String address
        +String dateOfBirth
    }

    class Vehicle {
        +String licensePlate
        +String ownerId
        +String brand
        +String model
    }

    class DataAccessLog {
        +String citizenId
        +LocalDateTime timestamp
        +String consumer
        +String provider
        +String dataTransferred
        +String purpose
        +toString() String
    }

    class DataAccessLogObject {
        <<object>>
        -List~DataAccessLog~ logs
        +add(log: DataAccessLog) void
        +getLogsForCitizen(citizenId: String) List~DataAccessLog~
        +getAllLogs() List~DataAccessLog~
    }

    %% ==================== UI LAYER ====================
    class Terminal {
        <<object>>
        +clearScreen() void
        +printHeader(text: String) void
        +printSubHeader(text: String) void
        +logSuccess(text: String) void
        +logInfo(text: String) void
        +logWarning(text: String) void
        +logError(text: String) void
        +logXRoad(text: String) void
        +printLandingScreen() void
        +printHelp() void
        +printBox(lines: List~String~, color: String) void
    }

    %% ==================== RELATIONSHIPS ====================
    
    %% Main relationships
    Main --> CentralServer : creates
    Main --> SecurityServer : manages
    Main --> Client : orchestrates
    Main --> Terminal : uses

    %% Core layer relationships
    CentralServer --> ServiceConfig : contains
    SecurityServer --> CentralServer : queries
    SecurityServer --> XRoadMessage : sends/receives
    SecurityServer --> DataAccessLogObject : logs to
    XRoadMessage --> MessageType : has

    %% Client inheritance
    Client <|-- PoliceClient : implements
    Client <|-- CitizenPortal : implements
    Client <|-- ResidenceRegistry : implements
    Client <|-- VehicleRegistry : implements

    %% Client dependencies
    Client --> SecurityServer : uses
    Client --> XRoadMessage : sends/receives
    PoliceClient --> VehicleRegistry : queries
    PoliceClient --> ResidenceRegistry : queries
    CitizenPortal --> ResidenceRegistry : uses
    ResidenceRegistry --> Citizen : manages
    VehicleRegistry --> Vehicle : manages

    %% Domain relationships
    DataAccessLogObject --> DataAccessLog : stores
    Vehicle --> Citizen : ownerId references

    %% UI usage
    PoliceClient --> Terminal : logs
    CitizenPortal --> Terminal : logs
    ResidenceRegistry --> Terminal : logs
    VehicleRegistry --> Terminal : logs
    SecurityServer --> Terminal : logs
```

## Komponenten-Übersicht

```mermaid
graph TB
    subgraph "Presentation Layer"
        T[Terminal<br/>UI & Logging]
    end

    subgraph "Application Layer"
        M[Main<br/>Event Loop & Orchestration]
    end

    subgraph "X-Road Infrastructure"
        CS[CentralServer<br/>Service Registry]
        SS1[SecurityServer<br/>SS-POLIZEI]
        SS2[SecurityServer<br/>SS-EMA]
        SS3[SecurityServer<br/>SS-KFZ]
        SS4[SecurityServer<br/>SS-PORTAL]
        XRM[XRoadMessage<br/>DTO]
    end

    subgraph "Client Layer"
        PC[PoliceClient]
        CP[CitizenPortal]
        RR[ResidenceRegistry]
        VR[VehicleRegistry]
    end

    subgraph "Domain Layer"
        CIT[Citizen]
        VEH[Vehicle]
        DAL[DataAccessLog]
    end

    M --> T
    M --> CS
    M --> SS1
    M --> SS2
    M --> SS3
    M --> SS4

    CS -.config.-> SS1
    CS -.config.-> SS2
    CS -.config.-> SS3
    CS -.config.-> SS4

    SS1 <--> SS2
    SS1 <--> SS3
    SS1 <--> SS4
    SS2 <--> SS3
    SS2 <--> SS4
    SS3 <--> SS4

    SS1 --> PC
    SS2 --> RR
    SS3 --> VR
    SS4 --> CP

    PC --> XRM
    CP --> XRM
    RR --> XRM
    VR --> XRM

    RR --> CIT
    VR --> VEH
    SS1 --> DAL
    SS2 --> DAL
    SS3 --> DAL

    PC --> T
    CP --> T
    RR --> T
    VR --> T
    SS1 --> T
    SS2 --> T
    SS3 --> T
    SS4 --> T

    style M fill:#e1f5ff
    style CS fill:#fff3cd
    style SS1 fill:#d4edda
    style SS2 fill:#d4edda
    style SS3 fill:#d4edda
    style SS4 fill:#d4edda
    style T fill:#f8d7da
```

## Sequenzdiagramm: Strafzettel-Erstellung

```mermaid
sequenceDiagram
    actor User
    participant Main
    participant Police as PoliceClient
    participant SS_P as SS-POLIZEI
    participant SS_K as SS-KFZ
    participant VR as VehicleRegistry
    participant SS_E as SS-EMA
    participant RR as ResidenceRegistry
    participant Log as DataAccessLog
    participant Term as Terminal

    User->>Main: police-tickethunt
    Main->>User: Kennzeichen eingeben
    User->>Main: M-AB-1234
    Main->>Police: processTicket("M-AB-1234")
    
    Police->>Term: logInfo("Starte Halterabfrage")
    Police->>SS_P: sendRequest(XRoadMessage)
    Note over SS_P: service: get-vehicle-owner
    SS_P->>Term: logXRoad("Sende Nachricht")
    SS_P->>SS_K: sendRequest(message)
    SS_K->>VR: handleRequest(message)
    VR->>VR: vehicles.get("M-AB-1234")
    VR->>Term: logSuccess("Halter gefunden")
    VR-->>SS_K: XRoadMessage(ownerId: "DE-BG-2001-M-001")
    SS_K->>Log: add(DataAccessLog)
    SS_K-->>SS_P: Response
    SS_P-->>Police: Response
    
    Police->>Term: logSuccess("Halter-ID ermittelt")
    Police->>SS_P: sendRequest(XRoadMessage)
    Note over SS_P: service: get-citizen-data
    SS_P->>Term: logXRoad("Sende Nachricht")
    SS_P->>SS_E: sendRequest(message)
    SS_E->>RR: handleRequest(message)
    RR->>RR: citizens.get("DE-BG-2001-M-001")
    RR->>Term: logSuccess("Daten gefunden")
    RR-->>SS_E: XRoadMessage(address: "Musterstraße 1")
    SS_E->>Log: add(DataAccessLog)
    SS_E-->>SS_P: Response
    SS_P-->>Police: Response
    
    Police->>Term: printBox("STRAFZETTEL")
    Police->>Term: logSuccess("Strafzettel erstellt")
    Police-->>Main: done
    Main-->>User: Vorgang abgeschlossen
```

## Package-Diagramm

```mermaid
graph LR
    subgraph "once-only-sim"
        subgraph "main"
            MAIN[Main.scala]
        end

        subgraph "core"
            CS[CentralServer.scala]
            SS[SecurityServer.scala]
            XRM[XRoadMessage.scala]
        end

        subgraph "clients"
            CLI[Client.scala]
            PC[PoliceClient.scala]
            CP[CitizenPortal.scala]
            RR[ResidenceRegistry.scala]
            VR[VehicleRegistry.scala]
        end

        subgraph "domain"
            MOD[Models.scala]
        end

        subgraph "ui"
            TERM[Terminal.scala]
        end
    end

    MAIN --> core
    MAIN --> clients
    MAIN --> ui

    core --> domain
    clients --> core
    clients --> domain
    clients --> ui

    style MAIN fill:#e1f5ff
    style core fill:#fff3cd
    style clients fill:#d4edda
    style domain fill:#d1ecf1
    style ui fill:#f8d7da
```

## Datenfluss-Diagramm

```mermaid
flowchart TD
    Start([User startet Simulation])
    Start --> Init[Main: startSimulation]
    
    Init --> CS[CentralServer erstellen]
    Init --> SS[4x SecurityServer erstellen]
    Init --> Clients[4x Clients erstellen]
    
    CS --> Config[Config an SS verteilen]
    SS --> Peer[SS untereinander registrieren]
    Clients --> Attach[Clients an SS anbinden]
    
    Attach --> Ready{System bereit}
    
    Ready -->|Kommando 2| Portal[CitizenPortal.showPortal]
    Ready -->|Kommando 3| Ticket[PoliceClient.processTicket]
    Ready -->|Kommando 4| Tracker[DataAccessLog anzeigen]
    
    Portal --> Login[Bürger-Login]
    Login --> Change[Adresse ändern]
    Change --> Update[ResidenceRegistry.updateAddress]
    Update --> Success1[Erfolg]
    
    Ticket --> Query1[SS-POLIZEI → SS-KFZ]
    Query1 --> VR[VehicleRegistry.handleRequest]
    VR --> Owner[Halter-ID ermitteln]
    Owner --> Log1[DataAccessLog.add]
    
    Log1 --> Query2[SS-POLIZEI → SS-EMA]
    Query2 --> RR[ResidenceRegistry.handleRequest]
    RR --> Address[Adresse abrufen]
    Address --> Log2[DataAccessLog.add]
    
    Log2 --> Print[Strafzettel drucken]
    Print --> Success2[Erfolg]
    
    Tracker --> Fetch[DataAccessLog.getLogsForCitizen]
    Fetch --> Display[Logs anzeigen]
    Display --> Success3[Erfolg]
    
    Success1 --> Ready
    Success2 --> Ready
    Success3 --> Ready
    
    Ready -->|Kommando 0| End([Simulation beenden])
    
    style Start fill:#90EE90
    style End fill:#FFB6C1
    style Ready fill:#87CEEB
    style CS fill:#FFD700
    style SS fill:#FFD700
    style Clients fill:#98FB98
```

## Objektdiagramm: Runtime-Struktur

```mermaid
graph TB
    subgraph "Runtime Instance Structure"
        CS_INST["centralServer: CentralServer<br/>config: Map(...)"]
        
        SS1_INST["ssPolice: SecurityServer<br/>name: SS-POLIZEI<br/>clientType: POLIZEI"]
        SS2_INST["ssResidence: SecurityServer<br/>name: SS-EMA<br/>clientType: EINWOHNERMELDEAMT"]
        SS3_INST["ssVehicle: SecurityServer<br/>name: SS-KFZ<br/>clientType: KFZ-ZULASSUNG"]
        SS4_INST["ssPortal: SecurityServer<br/>name: SS-PORTAL<br/>clientType: BUERGERPORTAL"]
        
        PC_INST["policeClient: PoliceClient<br/>name: Polizei München"]
        RR_INST["residenceRegistry: ResidenceRegistry<br/>citizens: Map(3 entries)"]
        VR_INST["vehicleRegistry: VehicleRegistry<br/>vehicles: Map(3 entries)"]
        CP_INST["citizenPortal: CitizenPortal"]
        
        CIT1["Citizen<br/>DE-BG-2001-M-001<br/>Max Mustermann"]
        CIT2["Citizen<br/>DE-BG-1995-F-042<br/>Erika Musterfrau"]
        CIT3["Citizen<br/>DE-BG-1988-M-123<br/>Hans Schmidt"]
        
        VEH1["Vehicle<br/>M-AB-1234<br/>BMW 320d"]
        VEH2["Vehicle<br/>M-XY-5678<br/>Audi A4"]
        VEH3["Vehicle<br/>M-CD-9999<br/>Mercedes C-Klasse"]
    end
    
    CS_INST -.-> SS1_INST
    CS_INST -.-> SS2_INST
    CS_INST -.-> SS3_INST
    CS_INST -.-> SS4_INST
    
    SS1_INST <--> SS2_INST
    SS1_INST <--> SS3_INST
    SS1_INST <--> SS4_INST
    SS2_INST <--> SS3_INST
    SS2_INST <--> SS4_INST
    SS3_INST <--> SS4_INST
    
    SS1_INST --> PC_INST
    SS2_INST --> RR_INST
    SS3_INST --> VR_INST
    SS4_INST --> CP_INST
    
    RR_INST --> CIT1
    RR_INST --> CIT2
    RR_INST --> CIT3
    
    VR_INST --> VEH1
    VR_INST --> VEH2
    VR_INST --> VEH3
    
    VEH1 -.ownerId.-> CIT1
    VEH2 -.ownerId.-> CIT2
    VEH3 -.ownerId.-> CIT3
```
