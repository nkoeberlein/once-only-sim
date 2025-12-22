package core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import clients.Client
import domain.DomainMessages._
import upickle.default._
class SecurityServerSpec extends AnyFlatSpec with Matchers {

  "SecurityServer" should "block unauthorized requests (ACL)" in {
    // Setup
    val centralServer = new CentralServer()
    val ss = new SecurityServer("Test-SS", "TEST-CLIENT", centralServer)
    
    // Create a message from an unauthorized sender (e.g. "HACKER")
    // trying to access "get-vehicle-owner" which is restricted
    val msg = XRoadMessage(
      messageType = MessageType.Request,
      sender = "HACKER", 
      recipient = "TEST-CLIENT",
      service = Constants.Services.GetVehicleOwner,
      payload = "{}" // Invalid payload for test
    )
    
    // Act
    // We call receiveRequest directly as if it came from another SS
    val result = ss.receiveRequest(msg)
    
    // Assert
    result should be (Left("Zugriff verweigert: Client HACKER darf Service get-vehicle-owner nicht nutzen."))
  }

  it should "allow authorized requests (ACL)" in {
    // Setup
    val centralServer = new CentralServer()
    val ss = new SecurityServer("Police-SS", Constants.Clients.Police, centralServer)
    
    // Mock a client attached to SS
    val mockClient = new Client {
      val name = "Mock Police"
      val clientType = Constants.Clients.Police
      def handleRequest(msg: XRoadMessage): XRoadMessage = msg.createResponse("{}")
    }
    ss.registerClient(mockClient)
    
    // Current ACL allows Police to access specific services. 
    // Wait, ACL in SecurityServer checks if INCOMING request sender is allowed.
    // So if "Police" sends a request to "VehicleRegistry", the VehicleRegistry's SS checks if "Police" is allowed.
    
    val vrSS = new SecurityServer("VR-SS", Constants.Clients.VehicleRegistry, centralServer)
    val vrClient = new Client {
      val name = "VR"
      val clientType = Constants.Clients.VehicleRegistry
      def handleRequest(msg: XRoadMessage): XRoadMessage = msg.createResponse("{}")
    }
    vrSS.registerClient(vrClient)
    
    val msg = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = Constants.Clients.VehicleRegistry,
      service = Constants.Services.GetVehicleOwner,
      payload = "{}" // Valid JSON
    )
    
    val result = vrSS.receiveRequest(msg)
    result.isRight should be (true)
  }

  it should "log access on the receiving side" in {
    // Setup
    import domain.DataAccessLog
    import java.time.LocalDateTime
    
    // Clear previous logs
    DataAccessLog.clear()
    
    val centralServer = new CentralServer()
    val receiverSS = new SecurityServer("Receiver-SS", "RECEIVER-CLIENT", centralServer)
    
    val mockClient = new Client {
      val name = "Mock Receiver"
      val clientType = "RECEIVER-CLIENT"
      def handleRequest(msg: XRoadMessage): XRoadMessage = {
         // Return a response that contains a citizenId to ensure logging works if not in request
         msg.createResponse("""{"citizenId": "DE-LOG-TEST", "data": "secret"}""")
      }
    }
    receiverSS.registerClient(mockClient)
    
    // Force ACL update or just mock ACL? 
    // The current implementation of SecurityServer has hardcoded ACL. 
    // We need to use a sender that is in the hardcoded ACL or modify the ACL mechanism.
    // The hardcoded ACL allows:
    // Police -> GetVehicleOwner, GetCitizenData
    // CitizenPortal -> UpdateCitizenData, CheckLicensePlate, RegisterVehicle, GetCitizenVehicles, DeregisterVehicle
    
    // Let's use Police -> GetVehicleOwner as a valid allowed request
    val msg = XRoadMessage(
      messageType = MessageType.Request,
      sender = Constants.Clients.Police,
      recipient = "RECEIVER-CLIENT",
      service = Constants.Services.GetVehicleOwner,
      payload = """{"citizenId": "DE-LOG-TEST", "purpose": "Test Logging"}"""
    )
    
    // Manually inject the RECEIVER-CLIENT into the ACL if it's not there? 
    // Wait, the ACL keys are SENDERS. The values are Set[String] of SERVICES.
    // The sender is Police. Police is in the ACL map. 
    // Allowed values for Police are: GetVehicleOwner, GetCitizenData.
    // So if we send service "GetVehicleOwner" from "Police", it should pass ACL check.
    // The recipient name doesn't matter for ACL check in this simple implementation, 
    // BUT the recipient client type must match the SecurityServer's clientType for it to be processed?
    // Actually SecurityServer.scala receiveRequest doesn't check if message.recipient == this.clientType explicitly,
    // but it assumes it is for its registered client.
    
    val result = receiverSS.receiveRequest(msg)
    
    result.isRight should be (true)
    
    // Verify Log
    val logs = DataAccessLog.getAllLogs
    logs should have size 1
    logs.head.citizenId should be ("DE-LOG-TEST")
    logs.head.consumer should be (Constants.Clients.Police)
  }
}
