# Klassendiagramm - Once-Only Simulation

## Vollständiges UML-Klassendiagramm

```mermaid
classDiagram
%% ==================== MAIN APPLICATION ====================
    class Main {
        -Option~SimulationContext~ context
        +main(args: Array~String~) void
        -runEventLoop() void
        -withContext(f: SimulationContext) void
        -startSimulation() void
        -restartSimulation() void
        -policeTicketHunt() void
        -showDataTracker() void
        -showStatus() void
    }

    class SimulationContext {
        -Option~CentralServer~ centralServer
        -Option~SecurityServer~ ssPolice
        -Option~SecurityServer~ ssResidence
        -Option~SecurityServer~ ssVehicle
        -Option~SecurityServer~ ssPortal
        -Option~PoliceClient~ policeClient
        -Option~ResidenceRegistry~ residenceRegistry
        -Option~VehicleRegistry~ vehicleRegistry
        -Option~CitizenPortal~ citizenPortal
        +initialize() void
        -initSS(ss: SecurityServer, cs: CentralServer, clientName: String) void
    }

%% ==================== CORE LAYER ====================
    class CentralServer {
        -Map~String, ServiceConfig~ config
        +getConfig(clientType: String) Option~ServiceConfig~
        +findSecurityServer(clientType: String) Option~String~
        +listAllServices() void
    }

    class Constants {
        <<object>>
        +Services Object
        +Clients Object
    }

    note for Constants "Services:\n- GetCitizenData\n- UpdateCitizenData\n- CheckLicensePlate\n- RegisterVehicle\n- GetCitizenVehicles\n- DeregisterVehicle\n- GetVehicleOwner\n\nClients:\n- CitizenPortal\n- VehicleRegistry\n- ResidenceRegistry\n- Police"

    class ServiceConfig {
        +String clientName
        +String securityServerName
        +List~String~ services
    }

    class SecurityServer {
        -String name
        -String clientType
        -CentralServer centralServer
        -Map~String, SecurityServer~ securityServers
        -Option~Client~ client
        -String UnknownId
        -String UnknownPurpose
        -Map~String, Set~String~~ accessControlList
        +registerClient(c: Client) void
        -pullGlobalConfig() void
        +registerPeer(peer: SecurityServer) void
        +sendRequest(recipient: String, service: String, payload: String) Either~String, String~
        +receiveRequest(message: XRoadMessage) Either~String, XRoadMessage~
        -logDataAccess(request: XRoadMessage, response: Option~XRoadMessage~) boolean
    }

    class XRoadMessage {
        +String id
        +MessageType messageType
        +String sender
        +String recipient
        +String service
        +String payload
        +LocalDateTime timestamp
        +createResponse(responsePayload: String) XRoadMessage
    }

    class MessageType {
        <<enumeration>>
        Request
        Response
    }

%% ==================== CLIENT LAYER ====================
    class Client {
        <<abstract>>
        +String name
        +String clientType
        +Option~SecurityServer~ securityServer
        +attachSecurityServer(ss: SecurityServer) void
        +handleRequest(message: XRoadMessage)* XRoadMessage
        #sendServiceRequest(recipient: String, service: String, payload: String) Either~String, String~
    }

    class PoliceClient {
        +String name = "Polizei München"
        +String clientType = Constants.Clients.Police
        -VehicleRegistry vehicleRegistry
        -ResidenceRegistry residenceRegistry
        +processTicket(licensePlate: String) void
        +handleRequest(message: XRoadMessage) XRoadMessage
    }

    class CitizenPortal {
        +String name
        +String clientType
        -ResidenceRegistry residenceRegistry
        +showPortal() void
        -printHeader(citizen: Citizen, text: String) void
        -showCitizenMenu(citizen: Citizen) void
        -changeAddress(citizen: Citizen) void
        -showDataAccess(citizen: Citizen) void
        -registerVehicle(citizen: Citizen) void
        -deregisterVehicle(citizen: Citizen) void
        +handleRequest(message: XRoadMessage) XRoadMessage
    }

    class ResidenceRegistry {
        +String name = "Einwohnermeldeamt München"
        +String clientType = Constants.Clients.ResidenceRegistry
        -Map~String, Citizen~ citizens
        +getCitizen(citizenId: String) Option~Citizen~
        +updateAddress(citizenId: String, newAddress: String) boolean
        -isValidAddress(address: String) boolean
        +handleRequest(message: XRoadMessage) XRoadMessage
        +listAllCitizens() void
    }

    class VehicleRegistry {
        +String name = "KFZ-Zulassungsstelle München"
        +String clientType = Constants.Clients.VehicleRegistry
        -Map~String, Vehicle~ vehicles
        +getVehicleOwner(licensePlate: String) Option~String~
        +handleRequest(message: XRoadMessage) XRoadMessage
        +listAllVehicles() void
    }

%% ==================== DOMAIN LAYER ====================
    class Citizen {
        -String id
        -String firstname
        -String surname
        -String nationality
        -String address
        -String dateOfBirth
        -String placeOfBirth
        +fullName() String
        +birthDetails(): String
    }

    class Vehicle {
        +String licensePlate
        +String citizenId
        +Int vin
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
        -DateTimeFormatter formatter
        +toString() String
    }

    class DataAccessLog_Object {
        <<object>>
        -List~DataAccessLog~ logs
        -String logFile = "logs.json"
        -RW~DataAccessLog~ rw
        -RW~LocalDateTime~ rwDateTime
        +add(log: DataAccessLog) void
        +getLogsForCitizen(citizenId: String) List~DataAccessLog~
        +getAllLogs() List~DataAccessLog~
        -saveLogs() void
        -loadLogs() List~DataAccessLog~
    }

    DataAccessLog_Object ..> DataAccessLog : manages

%% ==================== UI LAYER ====================
    class Terminal {
        <<object>>
        -String RESET
        -String BOLD
        -String RED
        -String GREEN
        -String YELLOW
        -String BLUE
        -String MAGENTA
        -String CYAN
        -String WHITE
        -String BG_RED
        -String BG_GREEN
        -String BG_BLUE
        -String BG_CYAN
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
        +pressEnter() boolean
        +newLine() void
    }

%% ==================== RELATIONSHIPS ====================

%% === MAIN ORCHESTRATION ===
    Main --> SimulationContext : creates & uses
    SimulationContext --> CentralServer : creates & manages
    SimulationContext --> SecurityServer : creates & wires (4x)
    SimulationContext --> PoliceClient : creates
    SimulationContext --> ResidenceRegistry : creates
    SimulationContext --> VehicleRegistry : creates
    SimulationContext --> CitizenPortal : creates
    SimulationContext ..> Terminal : uses for UI

%% === CORE LAYER ===
    CentralServer *-- ServiceConfig : contains
    SecurityServer --> CentralServer : pulls config from
    SecurityServer o-- SecurityServer : peers with (mesh network)
    SecurityServer --> Client : routes to
    SecurityServer ..> XRoadMessage : sends/receives
    SecurityServer ..> DataAccessLog_Object : logs to
    XRoadMessage --> MessageType : has type

%% === CLIENT INHERITANCE ===
    Client <|-- PoliceClient
    Client <|-- CitizenPortal
    Client <|-- ResidenceRegistry
    Client <|-- VehicleRegistry

%% === CLIENT DEPENDENCIES ===
    Client --> SecurityServer : attached to
    Client ..> XRoadMessage : handles
    PoliceClient --> VehicleRegistry : constructor dependency
    PoliceClient --> ResidenceRegistry : constructor dependency
    CitizenPortal --> ResidenceRegistry : constructor dependency

%% === CLIENT COMMUNICATION (via X-Road) ===
    PoliceClient ..> VehicleRegistry : queries
    PoliceClient ..> ResidenceRegistry : queries
    CitizenPortal ..> VehicleRegistry : queries

%% === DOMAIN MANAGEMENT ===
    ResidenceRegistry o-- Citizen : manages
    VehicleRegistry o-- Vehicle : manages
    Vehicle --> Citizen : references

%% === DATA ACCESS LOGGING ===
    DataAccessLog_Object *-- DataAccessLog : stores
    DataAccessLog --> Citizen : references

%% === UI/LOGGING ===
    PoliceClient ..> Terminal : logs
    CitizenPortal ..> Terminal : logs
    ResidenceRegistry ..> Terminal : logs
    VehicleRegistry ..> Terminal : logs
    SecurityServer ..> Terminal : logs
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
    participant Police as PoliceClient
    participant SS_P as SS-POLIZEI
    participant SS_K as SS-KFZ
    participant VR as VehicleRegistry
    participant SS_E as SS-EMA
    participant RR as ResidenceRegistry
    participant Log as DataAccessLog

    Note over Police,Log: Strafzettel-Erstellung für Kennzeichen M-AB1234

%% === SCHRITT 1: Halterabfrage ===
    Note over Police,VR: Schritt 1: Halterabfrage bei KFZ-Zulassung

    Police->>SS_P: sendRequest("KFZ-ZULASSUNG", "get-vehicle-owner", json)

    SS_P->>SS_K: XRoadMessage(Request)

    Note over SS_K: Prüfe Access Control List<br/>POLIZEI darf get-vehicle-owner nutzen
    SS_K->>VR: handleRequest(message)

    VR->>VR: vehicles.get("M-AB1234")
    VR->>VR: VehicleOwnerResponse(citizenId: "DE-001")
    VR-->>SS_K: XRoadMessage(Response, citizenId: "DE-001")

    SS_K->>SS_K: logDataAccess(request, response)
    SS_K->>Log: add(DataAccessLog)
    Note over Log: citizenId: DE-001<br/>consumer: POLIZEI<br/>provider: KFZ-ZULASSUNG<br/>service: get-vehicle-owner

    SS_K-->>SS_P: XRoadMessage(Response)
    SS_P-->>Police: Right(json: citizenId "DE-001")

%% === SCHRITT 2: Adressabfrage ===
    Note over Police,RR: Schritt 2: Adressabfrage beim Einwohnermeldeamt

    Police->>SS_P: sendRequest("EINWOHNERMELDEAMT", "get-citizen-data", json)

    SS_P->>SS_E: XRoadMessage(Request)

    Note over SS_E: Prüfe Access Control List<br/>POLIZEI darf get-citizen-data nutzen
    SS_E->>RR: handleRequest(message)

    RR->>RR: citizens.get("DE-001")
    RR->>RR: CitizenDataResponse(name, address, ...)
    RR-->>SS_E: XRoadMessage(Response, CitizenData)

    SS_E->>SS_E: logDataAccess(request, response)
    SS_E->>Log: add(DataAccessLog)
    Note over Log: citizenId: DE-001<br/>consumer: POLIZEI<br/>provider: EINWOHNERMELDEAMT<br/>service: get-citizen-data

    SS_E-->>SS_P: XRoadMessage(Response)
    SS_P-->>Police: Right(json: CitizenData)

%% === SCHRITT 3: Verarbeitung ===
    Note over Police: Strafzettel erstellt:<br/>Kennzeichen: M-AB1234<br/>Halter: Anna Schmidt<br/>Adresse: Hauptstraße 42<br/>Betrag: 25,00 EUR

    Note over Police,Log: Alle Datenzugriffe wurden protokolliert (2 Einträge)
```

## Package-Diagramm

```mermaid
graph LR
    subgraph "once-only-sim"
        
        MAIN[Main.scala]
        
        subgraph "core"
            CS[CentralServer.scala]
            SS[SecurityServer.scala]
            CONFIG[ServiceConfig.scala]
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
        RR_INST["residenceRegistry: ResidenceRegistry<br/>citizens: Map(8 entries)"]
        VR_INST["vehicleRegistry: VehicleRegistry<br/>vehicles: Map(10 entries)"]
        CP_INST["citizenPortal: CitizenPortal"]
        
        CIT1["Citizen<br/>DE-001<br/>Anna Schmidt"]
        CIT2["Citizen<br/>DE-002<br/>Lukas Müller"]
        CIT3["Citizen<br/>DE-003<br/>Sophie Weber"]
        
        VEH1["Vehicle<br/>M-AB1234<br/>BMW 3er"]
        VEH2["Vehicle<br/>B-CD5678<br/>VW Golf"]
        VEH3["Vehicle<br/>HH-EF9012<br/>Mercedes C-Klasse"]
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
