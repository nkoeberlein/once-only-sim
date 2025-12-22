package core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.LocalDateTime

class XRoadMessageSpec extends AnyFlatSpec with Matchers {

  "XRoadMessage" should "create response with swapped sender/recipient" in {
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = "CLIENT_A",
      recipient = "CLIENT_B",
      service = "test-service",
      payload = "request-payload"
    )

    val response = request.createResponse("response-payload")

    response.messageType should be (MessageType.Response)
    response.sender should be ("CLIENT_B")
    response.recipient should be ("CLIENT_A")
    response.service should be ("test-service")
    response.payload should be ("response-payload")
  }

  it should "generate unique IDs for different messages" in {
    val msg1 = XRoadMessage(
      messageType = MessageType.Request,
      sender = "A",
      recipient = "B",
      service = "service1",
      payload = "{}"
    )

    val msg2 = XRoadMessage(
      messageType = MessageType.Request,
      sender = "A",
      recipient = "B",
      service = "service1",
      payload = "{}"
    )

    msg1.id should not be msg2.id
  }

  it should "have timestamp close to current time" in {
    val before = LocalDateTime.now()
    val msg = XRoadMessage(
      messageType = MessageType.Request,
      sender = "A",
      recipient = "B",
      service = "test",
      payload = "{}"
    )
    val after = LocalDateTime.now()

    msg.timestamp.isAfter(before.minusSeconds(1)) should be (true)
    msg.timestamp.isBefore(after.plusSeconds(1)) should be (true)
  }

  it should "preserve service name in response" in {
    val request = XRoadMessage(
      messageType = MessageType.Request,
      sender = "POLICE",
      recipient = "VR",
      service = Constants.Services.GetVehicleOwner,
      payload = "{}"
    )

    val response = request.createResponse("response")

    response.service should be (Constants.Services.GetVehicleOwner)
  }
}