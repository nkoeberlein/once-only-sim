package core

import core.ServiceConfig
import ui.Terminal

/**
 * CentralServer class simulates a central server in an X-Road-like infrastructure.
 * It manages service configurations for different clients and provides functionalities
 * to retrieve client-specific configurations, identify associated security servers,
 * and list all available services.
 */
class CentralServer {
  private val config: Map[String, ServiceConfig] = Map(
    Constants.Clients.Police -> ServiceConfig(
      "Polizei München",
      "SS-POLIZEI",
      List(Constants.Services.GetVehicleOwner, Constants.Services.GetCitizenData)
    ),
    Constants.Clients.ResidenceRegistry -> ServiceConfig(
      "Einwohnermeldeamt München",
      "SS-EMA",
      List(Constants.Services.GetCitizenData, Constants.Services.UpdateCitizenData)
    ),
    Constants.Clients.VehicleRegistry -> ServiceConfig(
      "KFZ-Zulassungsstelle München",
      "SS-KFZ",
      List(Constants.Services.GetVehicleOwner, Constants.Services.RegisterVehicle, Constants.Services.CheckLicensePlate, Constants.Services.GetCitizenVehicles, Constants.Services.DeregisterVehicle)
    ),
    Constants.Clients.CitizenPortal -> ServiceConfig(
      "Bürgerportal",
      "SS-PORTAL",
      // CitizenPortal provides no services to others, but has rights. 
      // Listing services it *consumes* is not what this config usually does (it lists services *provided*).
      // But keeping it empty or as is for now.
      List()
    )
  )
  
  /**
   * Retrieves the service configuration for the specified client type.
   *
   * @param clientType the type of client for which the service configuration is requested
   * @return an Option containing the service configuration if it exists for the specified client type,
   *         or None if no configuration is found
   */
  def getConfig(clientType: String): Option[ServiceConfig] = {
    Terminal.logInfo(s"Central Server: Pull-Config-Anfrage von $clientType")
    Thread.sleep(300)
    config.get(clientType)
  }
  
  /**
   * Retrieves the security server name associated with a given client type.
   *
   * @param clientType the type of the client for which the security server name is to be retrieved
   * @return an Option containing the security server name if a corresponding configuration exists,
   *         or None if no configuration is found for the provided client type
   */
  def findSecurityServer(clientType: String): Option[String] = {
    config.get(clientType).map(_.securityServerName)
  }
  
  /**
   * Lists all available services across all client types with their associated security server names.
   * The services are categorized by client types, and each client's services are printed under the
   * respective security server name.
   *
   * @return Unit, as the method performs its function as a side effect by printing the list of services
   *         and their associated security server names to the console.
   */
  def listAllServices(): Unit = {
    Terminal.printHeader("X-Road Service Registry")
    config.foreach { case (clientType, cfg) =>
      Terminal.logSuccess(s"$clientType (${cfg.securityServerName})")
      cfg.services.foreach(s => println(s"  - $s"))
    }
  }
}
