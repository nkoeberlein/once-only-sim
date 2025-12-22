package core

import ui.Terminal
import domain.DataAccessLog
import java.time.LocalDateTime
import java.util.UUID
import upickle.default._
import ujson.Value

/**
 * Represents a Security Server in an X-Road-like infrastructure.
 * A SecurityServer is responsible for managing connections to peer SecurityServers,
 * sending service requests, and handling incoming service requests. It acts as an
 * intermediary between clients and the CentralServer for communication and data exchange.
 *
 * @constructor Creates a new SecurityServer instance.
 * @param name          The name of the SecurityServer instance.
 * @param clientType    The client type associated with this SecurityServer.
 * @param centralServer The CentralServer instance that this SecurityServer interacts with for configurations.
 */
class SecurityServer (
                       val name: String,
                       val clientType: String,
                       val centralServer: CentralServer
                     ) {
  private var securityServers: Map[String, SecurityServer] = Map.empty
  private var client: Option[clients.Client] = None

  // Constants for logging and data access
  private val UnknownId = "UNBEKANNT"
  private val UnknownPurpose = "Nicht angegeben"

  // Access Control List (Sender -> Allowed Services)
  // Defines which client is allowed to consume which service.
  // In a real X-Road system, this would be managed via Central Server configuration.
  private val accessControlList: Map[String, Set[String]] = Map(
    Constants.Clients.Police -> Set(Constants.Services.GetVehicleOwner, Constants.Services.GetCitizenData),
    Constants.Clients.CitizenPortal -> Set(
      Constants.Services.UpdateCitizenData,
      Constants.Services.CheckLicensePlate,
      Constants.Services.RegisterVehicle,
      Constants.Services.GetCitizenVehicles,
      Constants.Services.DeregisterVehicle
    )
  )

  def registerClient(c: clients.Client): Unit = {
    client = Some(c)
  }

  /**
   * Pulls the global configuration from the Central Server for this client type.
   * This simulates the initialization phase of a Security Server in the X-Road network.
   */
  private def pullGlobalConfig(): Unit = {
    Terminal.logXRoad(s"$name: Verbinde mit Central Server...")
    centralServer.getConfig(clientType) match {
      case Some(config) =>
        Terminal.logSuccess(s"$name: Konfiguration erhalten für: ${config.clientName}")
      // In a real system, we would update local matching rules or allowed peers here.
      case None =>
        Terminal.logError(s"$name: Keine Konfiguration auf Central Server gefunden!")
    }
  }

  // Initial Configuration Pull on startup
  pullGlobalConfig()


  /**
   * Registers a peer security server to allow communication and collaboration.
   *
   * @param peer The SecurityServer instance representing the peer to be registered.
   * @return Unit, as the method performs the side effect of updating the internal state.
   */
  def registerPeer(peer: SecurityServer): Unit = {
    securityServers = securityServers + (peer.clientType -> peer)
  }

  /**
   * Sends a request to the appropriate security server and handles the response.
   * The method prepares the request, determines the target security server based
   * on the recipient of the message, and transmits the request. If the target
   * security server is found, the request is processed, and a response, if any,
   * is returned. Logs related to the transmission and response are recorded.
   *
   * @param recipient The recipient client type
   * @param service The service to be called
   * @param payload The JSON payload as string
   * @return An `Either[String, String]` representing the response payload or an error message.
   */
  def sendRequest(recipient: String, service: String, payload: String): Either[String, String] = {
    // Wrap payload in XRoadMessage
    val message = XRoadMessage(
      messageType = MessageType.Request,
      sender = clientType,
      recipient = recipient,
      service = service,
      payload = payload
    )

    Terminal.logXRoad(s"$name: Bereite Anfrage vor")
    Thread.sleep(400)

    // Finde Ziel-Security-Server
    val targetClientType = message.recipient
    val targetSS = securityServers.get(targetClientType)

    targetSS match {
      case Some(ss) =>
        Terminal.logXRoad(s"$name --> ${ss.name}: Sende Nachricht (Service: ${message.service})")
        Thread.sleep(500)

        // Empfänger verarbeitet Anfrage
        val result = ss.receiveRequest(message)

        result match {
          case Right(response) =>
            Terminal.logXRoad(s"${ss.name} --> $name: Antwort empfangen")
            Thread.sleep(400)

            // Add requested log
            client.foreach(c => Terminal.logInfo(s"$name: Leite Nachricht weiter an ${c.name}"))

            Right(response.payload)

          case Left(error) =>
            Terminal.logError(s"$name: Fehler von Remote-Server: $error")
            Left(error)
        }

      case None =>
        Terminal.logError(s"$name: Kein Security Server für $targetClientType gefunden")
        Left(s"Kein Security Server für $targetClientType gefunden")
    }
  }

  /**
   * Receives an X-Road request message from a sender and processes it.
   * Logs the receipt of the message and simulates a delay to mimic processing time.
   *
   * @param message The `XRoadMessage` instance representing the incoming request message,
   *                including details about the sender, recipient, service, and payload.
   * @return An `Either[String, XRoadMessage]`.
   */
  def receiveRequest(message: XRoadMessage): Either[String, XRoadMessage] = {
    Terminal.logXRoad(s"$name: Anfrage empfangen von ${message.sender}")
    Thread.sleep(300)

    // Check ACL
    val allowedServices = accessControlList.getOrElse(message.sender, Set.empty)
    if (!allowedServices.contains(message.service)) {
      Terminal.logError(s"$name: Zugriff verweigert! ${message.sender} darf ${message.service} nicht aufrufen.")
      return Left(s"Zugriff verweigert: Client ${message.sender} darf Service ${message.service} nicht nutzen.")
    }

    client match {
      case Some(c) =>
        Terminal.logInfo(s"$name: Leite Nachricht weiter an ${c.name}")
        val response = c.handleRequest(message)
        Terminal.logInfo(s"${c.name}: Erstelle Antwort")
        
        // Logge den Zugriff auf der Empfängerseite
        logDataAccess(message, Some(response))
        
        // Simuliere Senden der Antwort
        Terminal.logXRoad(s"${c.name} --> $name: Sende Antwort")
        Right(response)
      case None =>
        Terminal.logError(s"$name: Kein Client registriert!")
        Left("Kein Client registriert")
    }
  }

  /**
   * Logs the access of citizen-related data by recording relevant details about the interaction.
   * The log includes information such as the citizen ID, timestamp of access, the sender,
   * the recipient, the data accessed, and the purpose of the access.
   *
   * If no citizen ID is found in the payload, a default value of "UNBEKANNT" is used,
   * and no log entry is created.
   *
   * @param message The instance of `XRoadMessage` representing the interaction
   *                containing the sender, recipient, service, and payload.
   *                The payload is expected to potentially include "citizenId" or
   *                as well as a "purpose" field describing the reason for access.
   * @return Boolean indicating if a log entry was created.
   */
  private def logDataAccess(request: XRoadMessage, response: Option[XRoadMessage] = None): Boolean = {
    // Helper to extract citizenId from JSON string safely
    // Since payloads vary (different Case Classes), we inspect the JSON structure dynamically.
    def extractId(json: String): String = {
      try {
        val data = ujson.read(json)
        if (data.obj.contains("citizenId")) data("citizenId").str
        else if (data.obj.contains("ownerId")) data("ownerId").str // Legacy fallback logic for JSON?
        else UnknownId
      } catch {
        case _: Exception => UnknownId
      }
    }

    var citizenId = extractId(request.payload)

    // Wenn im Request nicht gefunden, suche in Response
    if (citizenId == UnknownId && response.isDefined) {
      citizenId = extractId(response.get.payload)
    }

    if (citizenId != UnknownId) {
      val purpose = try {
        val data = ujson.read(request.payload)
        if (data.obj.contains("purpose")) data("purpose").str else UnknownPurpose
      } catch { case _: Exception => UnknownPurpose }

      val log = DataAccessLog(
        citizenId = citizenId,
        timestamp = LocalDateTime.now(),
        consumer = request.sender,
        provider = request.recipient,
        dataTransferred = request.service,
        purpose = purpose
      )
      DataAccessLog.add(log)
      true
    } else {
      false
    }
  }
}