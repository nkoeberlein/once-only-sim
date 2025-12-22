package clients

import domain.Citizen
import domain.DomainMessages._
import upickle.default._
import core.{XRoadMessage, MessageType, Constants}
import ui.Terminal
import ui.Terminal.{RESET, YELLOW, pressEnter}

import scala.io.StdIn

/**
 * Represents a citizen-facing portal for the Munich Residence Registry. This portal
 * allows citizens to view and update their personal information or review data access logs.
 *
 * @param residenceRegistry An instance of the residence registry used for retrieving
 *                          and updating citizen data.
 */
class CitizenPortal(residenceRegistry: ResidenceRegistry) extends Client {
  override val name = "Bürgerportal"
  override val clientType = Constants.Clients.CitizenPortal
  
  /**
   * Displays the main portal interface to the user. This includes clearing the terminal,
   * showing headers, and prompting the user for their citizen ID for authentication.
   * If the provided citizen ID is found in the `residenceRegistry`, it welcomes the user
   * and navigates them to their specific menu. If the ID is not found, an error message
   * is shown, and the user is given the option to retry or exit.
   *
   * @return this method does not return a value
   */
  def showPortal(): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader("=== BÜRGERPORTAL ===")
    Terminal.printSubHeader("Login")
    println()
    
    print("Bitte geben Sie Ihre Bürger-ID ein: ")
    val citizenId = StdIn.readLine().trim()
    
    residenceRegistry.getCitizen(citizenId) match {
      case Some(citizen) =>
        Terminal.logSuccess(s"Willkommen, ${citizen.fullName()}!")
        Thread.sleep(1500)
        showCitizenMenu(citizen)
        
      case None =>
        Terminal.logError("Bürger-ID nicht gefunden!")
        if (pressEnter()) {
          showPortal()
        }
    }
  }

  /**
   * Prints a header and sub-header for the citizen portal interface.
   * The header includes the citizen's full name, while the sub-header includes
   * a descriptive text provided as input.
   *
   * @param citizen the citizen whose full name will be included in the header
   * @param text    the sub-header text to be displayed below the main header
   * @return this method does not return a value
   */
  private def printHeader(citizen: Citizen, text: String): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader(s"Bürgerportal - ${citizen.fullName()}")
    Terminal.printSubHeader(text)
  }

  /**
   * Displays a menu for a citizen providing options to view and update their details or return to the main menu.
   * The menu allows the citizen to change their address or view data access records.
   *
   * @param citizen the citizen for whom the menu is displayed, including their information such as ID, name, address, and date of birth
   * @return this method does not return a value
   */
  private def showCitizenMenu(citizen: Citizen): Unit = {
    var running = true
    
    while (running) {
      printHeader(citizen, "Ihre aktuellen Daten:")
      println(s"  ID:                  ${citizen.id}")
      println(s"  Name:                ${citizen.fullName()}")
      println(s"  Staatsangehörigkeit: ${citizen.nationality}")
      println(s"  Adresse:             ${citizen.address}")
      println(s"  Geburtsdatum:        ${citizen.birthDetails()}")
      println()
      println("─" * 60)
      println()
      println("Optionen:")
      println("  [1] Adresse ändern")
      println("  [2] Datenzugriffe anzeigen")
      println("  [3] Fahrzeug anmelden")
      println("  [4] Fahrzeug abmelden")
      println("  [0] Zurück zum Hauptmenü")
      println()
      print("Ihre Auswahl: ")
      
      StdIn.readLine().trim() match {
        case "1" =>
          changeAddress(citizen)
        case "2" =>
          showDataAccess(citizen)
        case "3" =>
          registerVehicle(citizen)
        case "4" =>
          deregisterVehicle(citizen)
        case "0" =>
          running = false
        case _ =>
          Terminal.logError("Ungültige Eingabe!")
          Thread.sleep(1500)
      }
    }
  }
  
  /**
   * Updates the address of a given citizen by interacting with an external registry system.
   * Prompts the user for a new address, verifies the input, and updates the address 
   * in the external system if the input is valid.
   *
   * @param citizen the citizen whose address is to be updated. This includes their current details such as ID and address
   * @return this method does not return a value
   */
  private def changeAddress(citizen: Citizen): Unit = {
    printHeader(citizen, "Adresse ändern")
    println()
    println(s"Aktuelle Adresse: ${citizen.address}")
    println()
    println("Bitte geben Sie die neue Adresse im Format 'Straße Hausnummer, PLZ Ort' ein.")
    print("Neue Adresse: ")
    val newAddress = StdIn.readLine().trim()

    if (newAddress.nonEmpty) {
      if (newAddress == "exit" || newAddress == "0") {
        return
      }
      Terminal.logInfo("Sende neue Adresse an das Einwohnermeldeamt via X-Road...")
      
      val payload = UpdateCitizenAddressRequest(citizen.id, newAddress, "Adressänderung durch Bürger")
      val jsonPayload = write(payload)

      sendServiceRequest(Constants.Clients.ResidenceRegistry, Constants.Services.UpdateCitizenData, jsonPayload) match {
        case Right(responseJson) =>
          val response = read[GenericResponse](responseJson)
          if (response.success) {
            Terminal.logSuccess("Adresse erfolgreich aktualisiert!")
            Terminal.pressEnter()
          } else {
             Terminal.logError(s"Fehler bei der Aktualisierung! ${response.message}")
             if (Terminal.pressEnter()) {
                changeAddress(citizen)
             }
          }
        case Left(error) =>
           Terminal.logError(s"Fehler: $error")
           Terminal.pressEnter()
      }

    } else {
      Terminal.logError("Keine Adresse eingegeben!")
      Thread.sleep(1500)
    }
  }

  /**
   * Displays the data access log for a given citizen. The method retrieves all recorded 
   * data access logs associated with the citizen's ID and displays them in a formatted 
   * manner. If no logs are found, an informational message is displayed instead. 
   * The method ensures proper console formatting and allows the user to review information 
   * before proceeding.
   *
   * @param citizen the citizen whose data access logs will be retrieved and displayed
   * @return this method does not return a value
   */
  private def showDataAccess(citizen: Citizen): Unit = {
    import domain.DataAccessLog

    printHeader(citizen, "Datenzugriffs-Protokoll")
    println()
    
    val logs = DataAccessLog.getLogsForCitizen(citizen.id)
    
    if (logs.isEmpty) {
      Terminal.logInfo("Noch keine Datenzugriffe protokolliert.")
    } else {
      Terminal.logSuccess(s"Gefundene Zugriffe: ${logs.size}")
      println()
      println("─" * 80)
      logs.foreach { log =>
        println(log.toString)
        println("─" * 80)
      }
    }
    
    println()
    Terminal.pressEnter()
  }

  /**
   * Registers a vehicle for the given citizen by interacting with external services.
   * The method prompts the citizen for a license plate and checks its availability.
   * If the license plate is available, the user is prompted for additional vehicle details
   * such as brand and model. The data is then sent to the vehicle registry service for registration.
   * In case of errors or invalid inputs, appropriate error messages are logged.
   *
   * @param citizen the citizen attempting to register the vehicle, including their personal details such as ID
   * @return this method does not return a value
   */
  private def registerVehicle(citizen: Citizen): Unit = {
    printHeader(citizen, "Fahrzeug anmelden")
    println()
    
    print("Wunschkennzeichen eingeben: ")
    val licensePlate = StdIn.readLine().trim().toUpperCase()
    
    if (licensePlate.isEmpty) {
      Terminal.logError("Kein Kennzeichen eingegeben!")
      Thread.sleep(1000)
      return
    }
    
    // Check availability via X-Road
    Terminal.logInfo("Prüfe Verfügbarkeit bei KFZ-Zulassungsstelle...")
    
    sendServiceRequest(
      Constants.Clients.VehicleRegistry, 
      Constants.Services.CheckLicensePlate, 
      write(CheckLicensePlateRequest(licensePlate, citizen.id, "Verfügbarkeitsprüfung"))
    ) match {
      case Right(responseJson) => 
        val response = read[LicensePlateAvailabilityResponse](responseJson)
        if (response.available) {
          Terminal.logSuccess(s"Kennzeichen $licensePlate ist verfügbar!")
          Thread.sleep(500)
          
          print("Marke: ")
          val brand = StdIn.readLine().trim()
          print("Modell: ")
          val model = StdIn.readLine().trim()
          
          if (brand.nonEmpty && model.nonEmpty) {
             Terminal.logInfo("Sende Anmeldedaten an KFZ-Zulassungsstelle...")
             
             val registerPayload = RegisterVehicleRequest(licensePlate, citizen.id, brand, model, "Fahrzeuganmeldung")
             
             sendServiceRequest(Constants.Clients.VehicleRegistry, Constants.Services.RegisterVehicle, write(registerPayload)) match {
               case Right(regResponseJson) =>
                 val regResponse = read[GenericResponse](regResponseJson)
                 if (regResponse.success) {
                   Terminal.logSuccess("Fahrzeug erfolgreich angemeldet!")
                 } else {
                   Terminal.logError(s"Fehler bei der Anmeldung: ${regResponse.message}")
                 }
               case Left(error) =>
                 Terminal.logError(s"Fehler: $error")
             }
             
          } else {
            Terminal.logError("Marke und Modell dürfen nicht leer sein!")
          }
          
        } else {
          Terminal.logError(s"Kennzeichen $licensePlate ist leider schon vergeben.")
        }
      case Left(error) =>
        Terminal.logError(s"Fehler: $error")
    }
    Terminal.pressEnter()
  }

  /**
   * Deregisters a vehicle associated with the given citizen by interacting with external
   * vehicle registration services. The method retrieves all vehicles registered to the citizen,
   * displays them to the user with an indexed list, and allows the user to select a vehicle
   * to deregister. If the deregistration process is successful, a success message is logged;
   * otherwise, appropriate error messages are displayed.
   *
   * @param citizen the citizen attempting to deregister a vehicle, including their personal details such as ID
   * @return this method does not return a value
   */
  private def deregisterVehicle(citizen: Citizen): Unit = {
    printHeader(citizen, "Fahrzeug abmelden")
    println()
    
    Terminal.logInfo("Lade Fahrzeugdaten von KFZ-Zulassungsstelle...")
    
    sendServiceRequest(
      Constants.Clients.VehicleRegistry,
      Constants.Services.GetCitizenVehicles,
      write(GetCitizenVehiclesRequest(citizen.id, "Übersicht zur Abmeldung"))
    ) match {
      case Right(responseJson) =>
        val response = read[CitizenVehiclesResponse](responseJson)
        val count = response.count
        if (count == 0) {
          Terminal.logInfo("Keine Fahrzeuge auf Ihren Namen zugelassen.")
        } else {
          val vehicleString = response.vehicles // Format: "plate:brand:model"
          
          val vehicles = vehicleString.zipWithIndex.map { case (s, idx) =>
             val parts = s.split(":")
             (idx + 1, parts(0), parts(1), parts(2)) // index, plate, brand, model
          }.toList
          
          println("Ihre Fahrzeuge:")
          vehicles.foreach { case (idx, plate, brand, model) =>
            println(s"  [$idx] $plate ($brand $model)")
          }
          println("  [0] Abbrechen")
          println()
          print("Fahrzeug-Nummer zur Abmeldung wählen: ")
          
          val selection = StdIn.readInt()
          if (selection > 0 && selection <= vehicles.size) {
            val selected = vehicles.find(_._1 == selection).get
            val plateToDeregister = selected._2
            
            Terminal.logInfo(s"Melde Fahrzeug $plateToDeregister ab...")
            
             sendServiceRequest(
               Constants.Clients.VehicleRegistry,
               Constants.Services.DeregisterVehicle,
               write(DeregisterVehicleRequest(plateToDeregister, citizen.id, "Fahrzeugabmeldung"))
             ) match {
               case Right(deregResponseJson) =>
                 val deregResponse = read[GenericResponse](deregResponseJson)
                 if (deregResponse.success) {
                   Terminal.logSuccess("Fahrzeug erfolgreich abgemeldet!")
                 } else {
                    Terminal.logError(s"Fehler bei der Abmeldung: ${deregResponse.message}")
                 }
               case Left(error) => Terminal.logError(s"Fehler: $error")
             }
            
          } else if (selection != 0) {
            Terminal.logError("Ungültige Auswahl!")
          }
        }
        
      case Left(error) =>
        Terminal.logError(s"Keine Antwort von der KFZ-Zulassungsstelle. Fehler: $error")
    }
    Terminal.pressEnter()
  }
  /**
   * Handles incoming X-Road requests.
   * The CitizenPortal is a user interface and does not process automated X-Road requests.
   */
  def handleRequest(message: XRoadMessage): XRoadMessage = {
    Terminal.logError(s"$name: Empfängt keine Anfragen!")
    Terminal.logError(s"$name: Empfängt keine Anfragen!")
    message.createResponse(write(GenericResponse(success = false, message = "Service nicht unterstützt")))
  }
}
