package clients

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import core.{XRoadMessage, MessageType, Constants}
import domain.DomainMessages._
import upickle.default._

class ResidenceRegistrySpec extends AnyFlatSpec with Matchers {

  "ResidenceRegistry" should "return existing citizen by ID" in {
    val rr = new ResidenceRegistry()
    val citizen = rr.getCitizen("DE-001")

    citizen should not be None
    citizen.get.id should be ("DE-001")
    citizen.get.firstname should be ("Anna")
    citizen.get.surname should be ("Schmidt")
  }

  it should "return None for non-existing citizen" in {
    val rr = new ResidenceRegistry()
    val citizen = rr.getCitizen("DE-999")

    citizen should be (None)
  }

  it should "update citizen address with valid format" in {
    val rr = new ResidenceRegistry()
    val newAddress = "Teststraße 99, 12345 Teststadt"

    val result = rr.updateAddress("DE-001", newAddress)

    result should be (true)
    rr.getCitizen("DE-001").get.address should be (newAddress)
  }

  it should "reject invalid address format (missing postal code)" in {
    val rr = new ResidenceRegistry()
    val invalidAddress = "Teststraße 99"

    val result = rr.updateAddress("DE-001", invalidAddress)

    result should be (false)
  }

  it should "reject invalid address format (wrong structure)" in {
    val rr = new ResidenceRegistry()
    val invalidAddress = "invalid address"

    val result = rr.updateAddress("DE-001", invalidAddress)

    result should be (false)
  }

  it should "return false when updating non-existing citizen" in {
    val rr = new ResidenceRegistry()
    val result = rr.updateAddress("DE-999", "Straße 1, 12345 Stadt")

    result should be (false)
  }

  it should "handle GetCitizenData request successfully" in {
    val rr = new ResidenceRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.ResidenceRegistry,
      service = Constants.Services.GetCitizenData,
      payload = write(GetCitizenDataRequest("DE-001", "test purpose"))
    )

    val response = rr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    response.sender should be (Constants.Clients.ResidenceRegistry)
    response.recipient should be (Constants.Clients.Police)

    val data = read[CitizenDataResponse](response.payload)
    data.citizenId should be ("DE-001")
    data.firstname should be ("Anna")
    data.surname should be ("Schmidt")
  }

  it should "handle GetCitizenData request for non-existing citizen" in {
    val rr = new ResidenceRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.ResidenceRegistry,
      service = Constants.Services.GetCitizenData,
      payload = write(GetCitizenDataRequest("DE-999", "test purpose"))
    )

    val response = rr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Bürger nicht gefunden")
  }

  it should "handle UpdateCitizenData request successfully" in {
    val rr = new ResidenceRegistry()
    val newAddress = "Neue Straße 42, 80331 München"
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.ResidenceRegistry,
      service = Constants.Services.UpdateCitizenData,
      payload = write(UpdateCitizenAddressRequest("DE-001", newAddress, "Umzug"))
    )

    val response = rr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (true)
    rr.getCitizen("DE-001").get.address should be (newAddress)
  }

  it should "handle UpdateCitizenData request with invalid address" in {
    val rr = new ResidenceRegistry()
    val invalidAddress = "invalid"
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.CitizenPortal,
      recipient = Constants.Clients.ResidenceRegistry,
      service = Constants.Services.UpdateCitizenData,
      payload = write(UpdateCitizenAddressRequest("DE-001", invalidAddress, "test"))
    )

    val response = rr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Update fehlgeschlagen")
  }

  it should "return error for unknown service" in {
    val rr = new ResidenceRegistry()
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.ResidenceRegistry,
      service = "unknown-service",
      payload = "{}"
    )

    val response = rr.handleRequest(request)

    response.messageType should be (MessageType.Response)
    val result = read[GenericResponse](response.payload)
    result.success should be (false)
    result.message should be ("Unbekannter Service")
  }
}