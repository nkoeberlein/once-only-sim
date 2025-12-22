package domain

import upickle.default.{ReadWriter => RW, macroRW}

/**
 * Defines the strict schema for data exchanged between X-Road clients.
 * 
 * Using Case Classes with uPickle macros ensures type safety and automatic JSON serialization,
 * preventing "magic string" errors common with untyped Maps.
 */
object DomainMessages {
  // --- Common ---
  case class GenericResponse(success: Boolean, message: String = "", data: String = "")
  object GenericResponse { implicit val rw: RW[GenericResponse] = macroRW }

  // --- Citizen Registry ---
  case class GetCitizenDataRequest(citizenId: String, purpose: String)
  object GetCitizenDataRequest { implicit val rw: RW[GetCitizenDataRequest] = macroRW }

  case class CitizenDataResponse(
    citizenId: String,
    firstname: String,
    surname: String,
    nationality: String,
    address: String,
    dateOfBirth: String, 
    placeOfBirth: String
  )
  object CitizenDataResponse { implicit val rw: RW[CitizenDataResponse] = macroRW }

  case class UpdateCitizenAddressRequest(citizenId: String, address: String, purpose: String)
  object UpdateCitizenAddressRequest { implicit val rw: RW[UpdateCitizenAddressRequest] = macroRW }

  // --- Vehicle Registry ---
  case class GetVehicleOwnerRequest(licensePlate: String, purpose: String)
  object GetVehicleOwnerRequest { implicit val rw: RW[GetVehicleOwnerRequest] = macroRW }

  case class VehicleOwnerResponse(citizenId: String, licensePlate: String)
  object VehicleOwnerResponse { implicit val rw: RW[VehicleOwnerResponse] = macroRW }

  case class CheckLicensePlateRequest(licensePlate: String, citizenId: String, purpose: String)
  object CheckLicensePlateRequest { implicit val rw: RW[CheckLicensePlateRequest] = macroRW }

  case class LicensePlateAvailabilityResponse(available: Boolean, licensePlate: String)
  object LicensePlateAvailabilityResponse { implicit val rw: RW[LicensePlateAvailabilityResponse] = macroRW }

  case class RegisterVehicleRequest(licensePlate: String, citizenId: String, brand: String, model: String, purpose: String)
  object RegisterVehicleRequest { implicit val rw: RW[RegisterVehicleRequest] = macroRW }

  case class GetCitizenVehiclesRequest(citizenId: String, purpose: String)
  object GetCitizenVehiclesRequest { implicit val rw: RW[GetCitizenVehiclesRequest] = macroRW }
  
  case class CitizenVehiclesResponse(count: Int, vehicles: List[String]) // vehicles as "plate:brand:model" string list for simplicity or case class
  object CitizenVehiclesResponse { implicit val rw: RW[CitizenVehiclesResponse] = macroRW }

  case class DeregisterVehicleRequest(licensePlate: String, citizenId: String, purpose: String)
  object DeregisterVehicleRequest { implicit val rw: RW[DeregisterVehicleRequest] = macroRW }
}
