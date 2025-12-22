package clients

import domain.Vehicle
import domain.DomainMessages._
import upickle.default._
import core.{XRoadMessage, MessageType, Constants}
import ui.Terminal

/**
 * The `VehicleRegistry` class represents a client that serves as a vehicle registration office.
 * This client can process requests related to vehicle information, interact with a security server,
 * and maintain a local registry of vehicles.
 *
 * Key responsibilities include:
 * - Storing and managing information about registered vehicles and their owners.
 * - Handling incoming requests via the `handleRequest` method.
 * - Providing a list of all registered vehicles.
 *
 * Overrides:
 * - `name`: Set to "KFZ-Zulassungsstelle München".
 * - `clientType`: Identified as "KFZ-ZULASSUNG".
 *
 * Key Methods:
 * - `getVehicleOwner`: Retrieves the owner ID of a vehicle based on its license plate.
 * - `handleRequest`: Processes incoming `XRoadMessage` service requests, such as fetching vehicle owner information.
 * - `listAllVehicles`: Outputs a list of all registered vehicles, including their details.
 *
 * Note:
 * - Private state `vehicles` holds a map of license plates to `Vehicle` objects.
 * - This class demonstrates interaction with a broader X-Road messaging structure and relies on the `Terminal` utility for logging and output.
 */
class VehicleRegistry extends Client {
  override val name = "KFZ-Zulassungsstelle München"
  override val clientType = Constants.Clients.VehicleRegistry
  
  private var vehicles: Map[String, Vehicle] = Map(
    "M-AB1234" -> Vehicle("M-AB1234", "DE-001", 123456789, "BMW", "3er"),
    "B-CD5678" -> Vehicle("B-CD5678", "DE-002", 987654321, "Volkswagen", "Golf"),
    "HH-EF9012" -> Vehicle("HH-EF9012", "DE-003", 456789123, "Mercedes-Benz", "C-Klasse"),
    "K-GH3456" -> Vehicle("K-GH3456", "DE-004", 789123456, "Audi", "A4"),
    "S-IJ7890" -> Vehicle("S-IJ7890", "DE-005", 321654987, "Porsche", "Cayenne"),
    "M-KL2468" -> Vehicle("M-KL2468", "DE-006", 654987321, "BMW", "X5"),
    "F-MN1357" -> Vehicle("F-MN1357", "DE-007", 147258369, "Opel", "Astra"),
    "DD-OP9753" -> Vehicle("DD-OP9753", "DE-008", 963852741, "Volkswagen", "Passat"),
    "M-QR8642" -> Vehicle("M-QR8642", "DE-001", 258369147, "Mini", "Cooper"),
    "B-ST7531" -> Vehicle("B-ST7531", "DE-003", 741852963, "Tesla", "Model 3")
  )
  
  /**
   * Retrieves the owner of a vehicle based on its license plate.
   * The method looks up the vehicle by its license plate in the registry
   * and returns the associated owner's citizen ID, if available.
   *
   * @param licensePlate The license plate of the vehicle to look up.
   * @return An `Option[String]` containing the citizen ID of the vehicle owner
   *         if the vehicle is found in the registry; otherwise, `None`.
   */
  def getVehicleOwner(licensePlate: String): Option[String] = {
    vehicles.get(licensePlate).map(_.citizenId)
  }
  
  /**
   * Handles incoming XRoad service requests by processing the provided message
   * and generating an appropriate response based on the service type.
   *
   * @param message The input XRoadMessage containing details of the service request,
   *                such as the service name, payload, and sender/recipient information.
   * @return An XRoadMessage representing the response to the processed request, including
   *         relevant data or error messages based on the request handling logic.
   */
  def handleRequest(message: XRoadMessage): XRoadMessage = {
    Terminal.logInfo(s"$name: Verarbeite Anfrage '${message.service}'")
    Thread.sleep(400)
    
    // Use import to allow simple case matching
    import Constants.Services._

    message.service match {
      case GetVehicleOwner =>
        val request = read[GetVehicleOwnerRequest](message.payload)
        val licensePlate = request.licensePlate
        getVehicleOwner(licensePlate) match {
          case Some(citizenId) =>
            val vehicle = vehicles(licensePlate)
            Terminal.logSuccess(s"$name: Halter für $licensePlate gefunden: $citizenId")
            message.createResponse(write(VehicleOwnerResponse(citizenId, licensePlate)))
          case None =>
            Terminal.logError(s"$name: Fahrzeug $licensePlate nicht gefunden")
            // Using GenericResponse for errors:
            message.createResponse(write(GenericResponse(false, "Fahrzeug nicht gefunden")))
        }

      case CheckLicensePlate =>
        val request = read[CheckLicensePlateRequest](message.payload)
        val licensePlate = request.licensePlate
        val available = !vehicles.contains(licensePlate)
        Terminal.logInfo(s"$name: Prüfe Kennzeichen $licensePlate: ${if(available) "Frei" else "Belegt"}")
        message.createResponse(write(LicensePlateAvailabilityResponse(available, licensePlate)))

      case RegisterVehicle =>
        val request = read[RegisterVehicleRequest](message.payload)
        
        if (vehicles.contains(request.licensePlate)) {
           message.createResponse(write(GenericResponse(false, "Kennzeichen belegt")))
        } else {
           val newVehicle = Vehicle(request.licensePlate, request.citizenId, scala.util.Random.nextInt(999999999), request.brand, request.model)
           vehicles = vehicles + (request.licensePlate -> newVehicle)
           Terminal.logSuccess(s"$name: Fahrzeug angemeldet: ${request.licensePlate} für ${request.citizenId}")
           message.createResponse(write(GenericResponse(true)))
        }

      case GetCitizenVehicles =>
        val request = read[GetCitizenVehiclesRequest](message.payload)
        val citizenVehicles = vehicles.values.filter(_.citizenId == request.citizenId).toList
        val encodedVehicles = citizenVehicles.map(v => s"${v.licensePlate}:${v.brand}:${v.model}")
        Terminal.logInfo(s"$name: ${citizenVehicles.size} Fahrzeuge für ${request.citizenId} gefunden")
        message.createResponse(write(
          CitizenVehiclesResponse(citizenVehicles.size, encodedVehicles)
        ))

      case DeregisterVehicle =>
        val request = read[DeregisterVehicleRequest](message.payload)
        if (vehicles.contains(request.licensePlate)) {
          vehicles = vehicles - request.licensePlate
          Terminal.logSuccess(s"$name: Fahrzeug abgemeldet: ${request.licensePlate}")
          message.createResponse(write(GenericResponse(true)))
        } else {
          message.createResponse(write(GenericResponse(false, "Fahrzeug nicht gefunden")))
        }
      case _ =>
        message.createResponse(write(GenericResponse(false, "Unbekannter Service")))
    }
  }
  
  /**
   * Prints a formatted list of all registered vehicles in the console.
   * The output includes the license plate, brand, model, and owner (citizen ID)
   * for each vehicle in the registry.
   *
   * @return Unit, as the method performs a side effect by printing the list to the console.
   */
  def listAllVehicles(): Unit = {
    Terminal.printSubHeader("KFZ-Zulassungsstelle - Fahrzeugdaten")
    vehicles.values.foreach { vehicle =>
      println(s"  ${vehicle.licensePlate}: ${vehicle.brand} ${vehicle.model}")
      println(s"    Halter-ID: ${vehicle.citizenId}")
      println()
    }
  }
}
