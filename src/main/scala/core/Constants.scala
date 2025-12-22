package core

/**
 * Global constants ensures consistency across the simulation.
 * Prevents magic strings for service names and client identifiers in X-Road messages.
 */
object Constants {
  object Services {
    val GetCitizenData = "get-citizen-data"
    val UpdateCitizenData = "update-citizen-data"
    val CheckLicensePlate = "check-license-plate"
    val RegisterVehicle = "register-vehicle"
    val GetCitizenVehicles = "get-citizen-vehicles"
    val DeregisterVehicle = "deregister-vehicle"
    val GetVehicleOwner = "get-vehicle-owner"
  }

  object Clients {
    val CitizenPortal = "BUERGERPORTAL"
    val VehicleRegistry = "KFZ-ZULASSUNG"
    val ResidenceRegistry = "EINWOHNERMELDEAMT"
    val Police = "POLIZEI"
  }
}
