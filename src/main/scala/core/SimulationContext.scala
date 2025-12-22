package core

import clients.{CitizenPortal, PoliceClient, ResidenceRegistry, VehicleRegistry, Client}
import ui.Terminal

/**
 * Manages the lifecycle and dependencies of the X-Road simulation.
 * Handles initialization, wiring of components, and provides access to services.
 */
class SimulationContext {
  
  var centralServer: Option[CentralServer] = None
  
  // Security Servers
  var ssPolice: Option[SecurityServer] = None
  var ssResidence: Option[SecurityServer] = None
  var ssVehicle: Option[SecurityServer] = None
  var ssPortal: Option[SecurityServer] = None
  
  // Clients
  var policeClient: Option[PoliceClient] = None
  var residenceRegistry: Option[ResidenceRegistry] = None
  var vehicleRegistry: Option[VehicleRegistry] = None
  var citizenPortal: Option[CitizenPortal] = None
  
  def initialize(): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader("X-Road Simulation wird gestartet")

    // 1. Central Server
    Terminal.logInfo("Initialisiere Central Server...")
    Thread.sleep(500)
    val cs = new CentralServer()
    centralServer = Some(cs)
    Terminal.logSuccess("Central Server gestartet")
    Thread.sleep(300)

    // 2. Clients
    Terminal.printSubHeader("Initialisiere Clients")
    Thread.sleep(400)

    val resReg = new ResidenceRegistry()
    residenceRegistry = Some(resReg)
    Terminal.logSuccess("Einwohnermeldeamt München")
    Thread.sleep(200)

    val vehReg = new VehicleRegistry()
    vehicleRegistry = Some(vehReg)
    Terminal.logSuccess("KFZ-Zulassungsstelle München")
    Thread.sleep(200)

    // Polizei needs reference to registries for legacy/direct lookups (if any) or validation? 
    // In original Main: val policeClient = new PoliceClient(vehicleRegistry, residenceRegistry)
    val polClient = new PoliceClient(vehReg, resReg)
    policeClient = Some(polClient)
    Terminal.logSuccess("Polizei München")
    Thread.sleep(200)

    val portal = new CitizenPortal(resReg)
    citizenPortal = Some(portal)
    Terminal.logSuccess("Bürgerportal")
    Thread.sleep(300)

    // 3. Security Servers
    Terminal.printSubHeader("Initialisiere Security Server")
    Thread.sleep(400)

    val ssPol = new SecurityServer("SS-POLIZEI", "POLIZEI", cs)
    initSS(ssPol, cs, "POLIZEI")
    ssPolice = Some(ssPol)

    val ssRes = new SecurityServer("SS-EMA", "EINWOHNERMELDEAMT", cs)
    initSS(ssRes, cs, "EINWOHNERMELDEAMT")
    ssResidence = Some(ssRes)

    val ssVeh = new SecurityServer("SS-KFZ", "KFZ-ZULASSUNG", cs)
    initSS(ssVeh, cs, "KFZ-ZULASSUNG")
    ssVehicle = Some(ssVeh)

    val ssPor = new SecurityServer("SS-PORTAL", "BUERGERPORTAL", cs)
    initSS(ssPor, cs, "BUERGERPORTAL")
    ssPortal = Some(ssPor)

    // 4. Peering (Mesh)
    Terminal.printSubHeader("Registriere Security Server im X-Road Netzwerk")
    Thread.sleep(400)
    val allSS = List(ssPol, ssRes, ssVeh, ssPor)
    allSS.foreach { ss1 =>
      allSS.foreach { ss2 =>
        if (ss1 != ss2) ss1.registerPeer(ss2)
      }
    }
    Terminal.logSuccess("Alle Security Server sind miteinander verbunden")
    Thread.sleep(300)

    // 5. Connect Clients to SS
    Terminal.printSubHeader("Verbinde Clients mit Security Servern")
    Thread.sleep(400)
    
    polClient.attachSecurityServer(ssPol)
    Terminal.logSuccess("Polizei <-> SS-POLIZEI")
    Thread.sleep(200)

    resReg.attachSecurityServer(ssRes)
    Terminal.logSuccess("Einwohnermeldeamt <-> SS-EMA")
    Thread.sleep(200)

    vehReg.attachSecurityServer(ssVeh)
    Terminal.logSuccess("KFZ-Zulassung <-> SS-KFZ")
    Thread.sleep(200)

    portal.attachSecurityServer(ssPor)
    Terminal.logSuccess("Bürgerportal <-> SS-PORTAL")
    Thread.sleep(500)
    
    println()
    Terminal.printBox(List(
      "✓ X-Road Infrastruktur erfolgreich initialisiert",
      "",
      "Das System ist jetzt bereit für Datenaustausch!",
      "Alle Zugriffe werden protokolliert."
    ), Console.GREEN)
  }
  
  private def initSS(ss: SecurityServer, cs: CentralServer, clientName: String): Unit = {
    Terminal.logInfo(s"${ss.name} lädt Konfiguration vom Central Server...")
    cs.getConfig(clientName)
    Terminal.logSuccess(s"${ss.name} konfiguriert")
    Terminal.newLine()
    Thread.sleep(300)
  }
}
