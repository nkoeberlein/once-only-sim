package core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CentralServerSpec extends AnyFlatSpec with Matchers {

  "CentralServer" should "return correct config for Police" in {
    val cs = new CentralServer()
    val config = cs.getConfig(Constants.Clients.Police)

    config should not be None
    config.get.clientName should be ("Polizei München")
    config.get.securityServerName should be ("SS-POLIZEI")
    config.get.services should contain (Constants.Services.GetVehicleOwner)
    config.get.services should contain (Constants.Services.GetCitizenData)
    config.get.services should have size 2
  }

  it should "return correct config for CitizenPortal" in {
    val cs = new CentralServer()
    val config = cs.getConfig(Constants.Clients.CitizenPortal)

    config should not be None
    config.get.clientName should be ("Bürgerportal")
    config.get.securityServerName should be ("SS-PORTAL")
    config.get.services should be (empty) // ✅ CitizenPortal has no services
  }

  it should "return correct config for ResidenceRegistry" in {
    val cs = new CentralServer()
    val config = cs.getConfig(Constants.Clients.ResidenceRegistry)

    config should not be None
    config.get.clientName should be ("Einwohnermeldeamt München")
    config.get.securityServerName should be ("SS-EMA")
    config.get.services should contain (Constants.Services.GetCitizenData)
    config.get.services should contain (Constants.Services.UpdateCitizenData)
    config.get.services should have size 2
  }

  it should "return correct config for VehicleRegistry" in {
    val cs = new CentralServer()
    val config = cs.getConfig(Constants.Clients.VehicleRegistry)

    config should not be None
    config.get.clientName should be ("KFZ-Zulassungsstelle München")
    config.get.securityServerName should be ("SS-KFZ")
    config.get.services should contain (Constants.Services.GetVehicleOwner)
    config.get.services should contain (Constants.Services.RegisterVehicle)
    config.get.services should contain (Constants.Services.CheckLicensePlate)
    config.get.services should contain (Constants.Services.GetCitizenVehicles)
    config.get.services should contain (Constants.Services.DeregisterVehicle)
    config.get.services should have size 5
  }

  it should "return None for unknown client" in {
    val cs = new CentralServer()
    val config = cs.getConfig("UNKNOWN_CLIENT")

    config should be (None)
  }

  it should "find security server name for registered client" in {
    val cs = new CentralServer()
    val ssName = cs.findSecurityServer(Constants.Clients.Police)

    ssName should be (Some("SS-POLIZEI"))
  }

  it should "return None when finding security server for unknown client" in {
    val cs = new CentralServer()
    val ssName = cs.findSecurityServer("HACKER")

    ssName should be (None)
  }

  it should "have config for all four clients" in {
    val cs = new CentralServer()

    cs.getConfig(Constants.Clients.Police) should not be None
    cs.getConfig(Constants.Clients.CitizenPortal) should not be None
    cs.getConfig(Constants.Clients.ResidenceRegistry) should not be None
    cs.getConfig(Constants.Clients.VehicleRegistry) should not be None
  }

  it should "return correct security server names for all clients" in {
    val cs = new CentralServer()

    cs.findSecurityServer(Constants.Clients.Police) should be (Some("SS-POLIZEI"))
    cs.findSecurityServer(Constants.Clients.CitizenPortal) should be (Some("SS-PORTAL"))
    cs.findSecurityServer(Constants.Clients.ResidenceRegistry) should be (Some("SS-EMA"))
    cs.findSecurityServer(Constants.Clients.VehicleRegistry) should be (Some("SS-KFZ"))
  }
}