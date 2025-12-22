package clients

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import core.{XRoadMessage, MessageType, Constants}
import domain.DomainMessages._
import upickle.default._

class VehicleRegistrySpec extends AnyFlatSpec with Matchers {

  "VehicleRegistry" should "return vehicle owner for existing vehicle" in {
    val vr = new VehicleRegistry()
    val owner = vr.getVehicleOwner("M-AB1234")

    owner should be (Some("DE-001"))
  }

  it should "return None for non-existing vehicle" in {
    val vr = new VehicleRegistry()
    val owner = vr.getVehicleOwner("M-NOTFOUND")

    owner should be (None)
  }

  it should "handle GetVehicleOwner request successfully" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.GetVehicleOwner,
      payload = write(GetVehicleOwnerRequest("M-AB1234", "Strafzettel"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val data = read[VehicleOwnerResponse](response.payload)
    data.citizenId should be ("DE-001")
    data.licensePlate should be ("M-AB1234")
  }

  it should "handle GetVehicleOwner request for non-existing vehicle" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.GetVehicleOwner,
      payload = write(GetVehicleOwnerRequest("M-NOTFOUND", "test"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Fahrzeug nicht gefunden")
  }

  it should "check license plate availability for free plate" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.CheckLicensePlate,
      payload = write(CheckLicensePlateRequest("M-NEW123", "DE-001", "Verfügbarkeitsprüfung"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val data = read[LicensePlateAvailabilityResponse](response.payload)
    data.available should be (true)
    data.licensePlate should be ("M-NEW123")
  }

  it should "check license plate availability for taken plate" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.CheckLicensePlate,
      payload = write(CheckLicensePlateRequest("M-AB1234", "DE-001", "test"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val data = read[LicensePlateAvailabilityResponse](response.payload)
    data.available should be (false)
  }

  it should "register new vehicle successfully" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.RegisterVehicle,
      payload = write(RegisterVehicleRequest("M-TEST999", "DE-001", "Tesla", "Model 3", "Neuanmeldung"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (true)

    // Verify vehicle was actually registered
    vr.getVehicleOwner("M-TEST999") should be (Some("DE-001"))
  }

  it should "reject registration of already existing license plate" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.RegisterVehicle,
      payload = write(RegisterVehicleRequest("M-AB1234", "DE-001", "BMW", "3er", "test"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Kennzeichen belegt")
  }

  it should "get all vehicles for a citizen" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.GetCitizenVehicles,
      payload = write(GetCitizenVehiclesRequest("DE-001", "Übersicht"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val data = read[CitizenVehiclesResponse](response.payload)
    data.count should be (2) // DE-001 owns M-AB1234 and M-QR8642
    data.vehicles should have size 2
  }

  it should "deregister existing vehicle successfully" in {
    val vr = new VehicleRegistry()

    // First verify vehicle exists
    vr.getVehicleOwner("M-AB1234") should be (Some("DE-001"))

    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.DeregisterVehicle,
      payload = write(DeregisterVehicleRequest("M-AB1234", "DE-001", "Abmeldung"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (true)

    // Verify vehicle was actually deregistered
    vr.getVehicleOwner("M-AB1234") should be (None)
  }

  it should "reject deregistration of non-existing vehicle" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.DeregisterVehicle,
      payload = write(DeregisterVehicleRequest("M-NOTFOUND", "DE-001", "test"))
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Fahrzeug nicht gefunden")
  }

  it should "return error for unknown service" in {
    val vr = new VehicleRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.VehicleRegistry,
      service = "unknown-service",
      payload = "{}"
    )

    val response = vr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Unbekannter Service")
  }
}