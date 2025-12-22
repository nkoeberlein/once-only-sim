package clients

import domain.Citizen
import domain.DomainMessages._
import upickle.default._
import core.{XRoadMessage, MessageType, Constants}
import ui.Terminal

/**
 * The ResidenceRegistry class represents a client responsible for managing
 * citizen data for the Munich Residents' Registration Office ("Einwohnermeldeamt München").
 * It extends the `Client` trait and provides specific services related to citizen information.
 *
 * The class allows the following functionalities:
 * - Retrieving citizen data by ID.
 * - Updating a citizen's address.
 * - Handling requests via the X-Road messaging structure to perform operations like fetching
 * or updating citizen data, and responding with appropriate responses.
 * - Listing all registered citizens along with their details.
 *
 * This class operates using a private map of citizens identified by unique IDs, providing
 * controlled access and modification of citizen information.
 */
class ResidenceRegistry extends Client {
  override val name = "Einwohnermeldeamt München"
  override val clientType = Constants.Clients.ResidenceRegistry
  
  private var citizens: Map[String, Citizen] = Map(
    "DE-001" -> Citizen("DE-001", "Anna", "Schmidt", "DE", "Hauptstraße 42, 80331 München", "15.03.1985", "Berlin"),
    "DE-002" -> Citizen("DE-002", "Lukas", "Müller", "DE", "Bahnhofstraße 17, 10115 Berlin", "22.07.1990", "Nürnberg"),
    "DE-003" -> Citizen("DE-003", "Sophie", "Weber", "DE", "Rosenweg 8, 20095 Hamburg", "03.11.1978", "Marburg"),
    "DE-004" -> Citizen("DE-004", "Maximilian", "Fischer", "DE", "Gartenstraße 25, 50667 Köln", "18.09.1995", "Köln"),
    "DE-005" -> Citizen("DE-005", "Emma", "Wagner", "DE", "Lindenallee 33, 70173 Stuttgart", "29.01.1988", "München"),
    "DE-006" -> Citizen("DE-006", "Felix", "Becker", "DE", "Kirchplatz 12, 80331 München", "07.06.1982", "Stuttgart"),
    "DE-007" -> Citizen("DE-007", "Lena", "Schulz", "DE", "Marktstraße 5, 60311 Frankfurt", "14.12.1992", "Frankfurt am Main"),
    "DE-008" -> Citizen("DE-008", "Jonas", "Hoffmann", "DE", "Bergstraße 19, 01067 Dresden", "26.04.1987", "Dresden")
  )
  
  /**
   * Retrieves a citizen's information based on the provided citizen ID.
   *
   * @param citizenId the ID of the citizen whose information is being queried
   * @return an Option containing the citizen's information if found, otherwise None
   */
  def getCitizen(citizenId: String): Option[Citizen] = {
    citizens.get(citizenId)
  }
  
  /**
   * Updates the address of a citizen identified by the provided citizen ID.
   * If the citizen is found, their address will be updated and a success message will be logged.
   * If the citizen is not found, an error message will be logged, and the method will return false.
   *
   * @param citizenId  the ID of the citizen whose address is to be updated
   * @param newAddress the new address to be set for the citizen
   * @return true if the address was successfully updated, false if the citizen was not found
   */
  def updateAddress(citizenId: String, newAddress: String): Boolean = {
    citizens.get(citizenId) match {
      case Some(citizen) =>
        if (isValidAddress(newAddress)) {
          Terminal.logInfo(s"$name: Aktualisiere Adresse für $citizenId")
          Thread.sleep(300)
          citizen.address = newAddress
          true
        } else {
          Terminal.logError("Ungültiges Adressformat! Beispiel: Hauptstraße 42, 80331 München")
          false
        }
      case None =>
        Terminal.logError(s"$name: Bürger $citizenId nicht gefunden")
        false
    }
  }

  /**
   * Validates if the given address string conforms to the expected format with regex.
   * The address format is defined by a regular expression, which checks for:
   * - A street name (with optional umlauts and special characters).
   * - A house number with optional letter suffix.
   * - A postal code of exactly 5 digits.
   * - A city name (with optional umlauts and special characters).
   *
   * @param address the address string to be validated
   * @return true if the address matches the defined format, false otherwise
   */
  private def isValidAddress(address: String): Boolean = {
    val pattern = """^([A-Za-zÄäÖöÜüß\s.-]+)\s+(\d+[a-zA-Z]?),\s*(\d{5})\s+([A-Za-zÄäÖöÜüß\s.-]+)$""".r
    pattern.findFirstMatchIn(address).isDefined
  }
  
  /**
   * Handles an incoming request by processing the service specified in the `message` argument.
   * Depending on the service type, it retrieves or updates citizen data, or returns an error
   * for unknown services. The response is encapsulated in an `XRoadMessage`.
   *
   * @param message the incoming `XRoadMessage` containing service details and associated payload
   * @return an `XRoadMessage` containing the response to the processed request
   */
  def handleRequest(message: XRoadMessage): XRoadMessage = {
    Terminal.logInfo(s"$name: Verarbeite Anfrage '${message.service}'")
    Thread.sleep(400)
    
    import Constants.Services._

    message.service match {
      case GetCitizenData =>
        val request = read[GetCitizenDataRequest](message.payload)
        getCitizen(request.citizenId) match {
          case Some(citizen) =>
            Terminal.logSuccess(s"$name: Daten für ${citizen.fullName()} gefunden")
            message.createResponse(write(
              CitizenDataResponse(
                citizen.id,
                citizen.firstname,
                citizen.surname,
                citizen.nationality,
                citizen.address,
                citizen.dateOfBirth,
                citizen.placeOfBirth
              )
            ))
          case None =>
            Terminal.logError(s"$name: Bürger nicht gefunden")
            message.createResponse(write(GenericResponse(false, "Bürger nicht gefunden")))
        }
      
      case UpdateCitizenData =>
        val request = read[UpdateCitizenAddressRequest](message.payload)
        val success = updateAddress(request.citizenId, request.address)
        if (success) {
           message.createResponse(write(GenericResponse(true)))
        } else {
           message.createResponse(write(GenericResponse(false, "Update fehlgeschlagen")))
        }

      case _ =>
        message.createResponse(write(GenericResponse(false, "Unbekannter Service")))
    }
  }
  
  /**
   * Displays a list of all registered citizens with their respective details, 
   * including ID, name, address, and date of birth.
   * The information is printed to the console in a formatted manner.
   *
   * @return Unit, as the method performs a side effect by printing the list
   *         of citizens to the console.
   */
  def listAllCitizens(): Unit = {
    Terminal.printSubHeader("Einwohnermeldeamt - Bürgerdaten")
    citizens.values.foreach { citizen =>
      println(s"  ${citizen.id}: ${citizen.fullName()}")
      println(s"    Adresse: ${citizen.address}")
      println(s"    Geburtsdatum: ${citizen.birthDetails()}")
      println()
    }
  }
}
