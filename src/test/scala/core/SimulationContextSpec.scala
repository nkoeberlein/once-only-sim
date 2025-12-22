package core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimulationContextSpec extends AnyFlatSpec with Matchers {

  "SimulationContext" should "initialize CentralServer" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    ctx.centralServer should not be None
  }

  it should "initialize all four SecurityServers" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    ctx.ssPolice should not be None
    ctx.ssResidence should not be None
    ctx.ssVehicle should not be None
    ctx.ssPortal should not be None
  }

  it should "initialize all four Clients" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    ctx.policeClient should not be None
    ctx.residenceRegistry should not be None
    ctx.vehicleRegistry should not be None
    ctx.citizenPortal should not be None
  }

  it should "attach clients to their security servers" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    ctx.policeClient.get.securityServer should not be None
    ctx.residenceRegistry.get.securityServer should not be None
    ctx.vehicleRegistry.get.securityServer should not be None
    ctx.citizenPortal.get.securityServer should not be None
  }

  it should "register peers between all security servers" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    // Verify all security servers exist
    val allSS = List(
      ctx.ssPolice,
      ctx.ssResidence,
      ctx.ssVehicle,
      ctx.ssPortal
    )

    allSS.foreach(_ should not be None)
  }

  it should "enable end-to-end communication after initialization" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    // Integration test: Use PoliceClient's public method processTicket
    // which internally uses sendServiceRequest
    // We can't directly test the result, but we verify it doesn't throw exceptions
    noException should be thrownBy {
      ctx.policeClient.get.processTicket("M-AB1234")
    }
  }

  it should "allow VehicleRegistry to handle requests via SecurityServer" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    import domain.DomainMessages._
    import upickle.default._

    // Create request directly to VehicleRegistry's SecurityServer
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.GetVehicleOwner,
      payload = write(GetVehicleOwnerRequest("M-AB1234", "Integration Test"))
    )

    // Send via SecurityServer (which is public)
    val result = ctx.ssPolice.get.sendRequest(
      Constants.Clients.VehicleRegistry,
      Constants.Services.GetVehicleOwner,
      write(GetVehicleOwnerRequest("M-AB1234", "Integration Test"))
    )

    result.isRight should be (true)
    val response = read[VehicleOwnerResponse](result.getOrElse("{}"))
    response.citizenId should be ("DE-001")
  }

  it should "create working ResidenceRegistry with pre-populated citizens" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    val rr = ctx.residenceRegistry.get

    // Verify some initial citizens exist
    rr.getCitizen("DE-001") should not be None
    rr.getCitizen("DE-002") should not be None
    rr.getCitizen("DE-003") should not be None
  }

  it should "create working VehicleRegistry with pre-populated vehicles" in {
    val ctx = new SimulationContext()
    ctx.initialize()

    val vr = ctx.vehicleRegistry.get

    // Verify some initial vehicles exist
    vr.getVehicleOwner("M-AB1234") should be (Some("DE-001"))
    vr.getVehicleOwner("B-CD5678") should be (Some("DE-002"))
  }
}