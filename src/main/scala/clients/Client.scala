package clients

import core.{SecurityServer, XRoadMessage, MessageType}

/**
 * Represents a client in a system that communicates with a Security Server
 * and sends messages using the X-Road messaging structure.
 *
 * A Client has a specific `name` and `clientType` and can optionally be associated
 * with a `SecurityServer`. Once attached to a Security Server, the Client can send
 * messages to recipients using the services provided by the server.
 */
abstract class Client {
  val name: String
  val clientType: String
  var securityServer: Option[SecurityServer] = None
  
  def attachSecurityServer(ss: SecurityServer): Unit = {
    securityServer = Some(ss)
    ss.registerClient(this)
  }

  /**
   * Abstract method that every client must implement to handle incoming X-Road requests.
   * 
   * @param message The incoming XRoadMessage
   * @return The response XRoadMessage
   */
  def handleRequest(message: XRoadMessage): XRoadMessage
  
  /**
   * Sends a message to the specified recipient via the attached Security Server, using the 
   * provided service and payload. The method constructs an `XRoadMessage` and transmits it 
   * through the Security Server if one is available. If no Security Server is attached, 
   * an error is logged and no message is sent.
   *
   * @param recipient The identifier of the target recipient for the message.
   * @param service   The name of the service being requested in the message.
   * @param payload   A JSON string representing the structured payload of the message.
   *                  Prefer using `upickle.default.write` with Case Classes from `DomainMessages`.
   * @return An `Either[String, String]` containing the response payload (as JSON String)
   *         or an error message.
   */
  protected def sendServiceRequest(
    recipient: String,
    service: String,
    payload: String
  ): Either[String, String] = {
    securityServer match {
      case Some(ss) =>
        ss.sendRequest(recipient, service, payload)
      case None =>
        println(s"[ERROR] $name: Kein Security Server verfügbar!")
        Left("Kein Security Server verfügbar!")
    }
  }
}