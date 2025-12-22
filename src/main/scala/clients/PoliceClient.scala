package clients

import ui.Terminal
import core.{XRoadMessage, MessageType, Constants}
import domain.DomainMessages._
import upickle.default._

/**
 * The `PoliceClient` class represents the interaction of the Munich Police with other registries
 * for processing traffic tickets. It extends the `Client` class and operates as an X-Road client.
 *
 * @constructor Creates a new PoliceClient instance.
 * @param vehicleRegistry   the vehicle registry used to query vehicle owner data.
 * @param residenceRegistry the residence registry used to query citizen data.
 */
class PoliceClient(
  vehicleRegistry: VehicleRegistry,
  residenceRegistry: ResidenceRegistry
) extends Client {

  override val name = "Polizei München"
  override val clientType = Constants.Clients.Police
  
  /**
   * Processes a parking violation ticket for a given vehicle license plate.
   * The method interacts with external systems to retrieve the owner information
   * and their address, and subsequently issues a ticket.
   *
   * @param licensePlate The license plate of the vehicle for which the parking
   *                     violation ticket is to be processed.
   * @return Unit, as the method performs actions through side effects such as
   *         logging and sending requests to external systems.
   */
  def processTicket(licensePlate: String): Unit = {
    Terminal.printHeader(s"Strafzettel-Erstellung für Kennzeichen: $licensePlate")
    Terminal.logInfo(s"$name: Starte Halterabfrage...")
    Thread.sleep(500)
    
    // Schritt 1: Halter-ID vom KFZ-Zulassungsamt anfragen
    Terminal.printSubHeader("Schritt 1: Halterabfrage bei KFZ-Zulassungsstelle")
    
    val ownerRequest = GetVehicleOwnerRequest(licensePlate, "Strafzettel wegen Falschparken")
    
    sendServiceRequest(Constants.Clients.VehicleRegistry, Constants.Services.GetVehicleOwner, write(ownerRequest)) match {
      case Right(ownerResponseJson) =>
        val ownerResponse = read[VehicleOwnerResponse](ownerResponseJson)
        val ownerId = ownerResponse.citizenId
        
        // DEBUG LOG
        Terminal.logSuccess(s"$name: Halter-ID ermittelt: $ownerId")
        Thread.sleep(800)
        
        // Schritt 2: Adresse vom Einwohnermeldeamt anfragen
        Terminal.printSubHeader("Schritt 2: Adressabfrage beim Einwohnermeldeamt")
        
        val addressRequest = GetCitizenDataRequest(ownerId, "Strafzettel-Zustellung wegen Falschparken")
        
        sendServiceRequest(Constants.Clients.ResidenceRegistry, Constants.Services.GetCitizenData, write(addressRequest)) match {
          case Right(addressResponseJson) =>
             // VERWENDUNG DER X-ROAD ANTWORT (kein lokaler Aufruf mehr)
             val addressResponse = read[CitizenDataResponse](addressResponseJson)
             val address = addressResponse.address
             val citizenName = s"${addressResponse.firstname} ${addressResponse.surname}"
             Thread.sleep(800)
                
             // Strafzettel erstellen 
             Terminal.printSubHeader("Strafzettel erstellt")
                
             val title = "STRAFZETTEL - FALSCHPARKEN"
             val line1 = s"Kennzeichen: $licensePlate"
             val line2 = s"Halter:      $citizenName"
             val line3 = s"Adresse:     $address"
             val line4 = "Betrag:      25,00 EUR"
                
             val lines = List(line1, line2, line3, line4)
             // Berechne maximale Breite (mindestens so breit wie der Titel)
             val contentWidth = (lines.map(_.length) :+ title.length).max
             val boxWidth = contentWidth + 4 // +4 für padding (2 links, 2 rechts)
                
             def padRight(s: String, w: Int): String = s + " " * (w - s.length)
             def center(s: String, w: Int): String = {
               val padding = (w - s.length) / 2
               " " * padding + s + " " * (w - s.length - padding)
             }
                
             println(s"┌${"─" * boxWidth}┐")
             println(s"│${center(title, boxWidth)}│")
             println(s"├${"─" * boxWidth}┤")
             lines.foreach(line => println(s"│  ${padRight(line, boxWidth - 2)}│"))
             println(s"└${"─" * boxWidth}┘")

          case Left(error) =>
            Terminal.logError(s"$name: Keine Antwort vom Einwohnermeldeamt: $error")
        }
            
      case Left(error) =>
        Terminal.logError(s"$name: Keine Antwort von KFZ-Zulassungsstelle: $error")
    }
    
    println()
    Terminal.logInfo("Vorgang abgeschlossen. Alle Zugriffe wurden protokolliert.")
    Thread.sleep(1000)
  }
  /**
   * Handles incoming X-Road requests.
   * Since the PoliceClient is primarily a consumer in this simulation, 
   * it does not support incoming service requests.
   */
  def handleRequest(message: XRoadMessage): XRoadMessage = {
    Terminal.logError(s"$name: Empfängt keine Anfragen!")
    message.createResponse(write(GenericResponse(success = false, message = "Service nicht unterstützt")))
  }
}
