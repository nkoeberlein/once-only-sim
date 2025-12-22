package core

/**
 * Represents the configuration details for a service provider within
 * an X-Road-like infrastructure. This case class encapsulates the
 * provider's client-specific information, including the client name,
 * associated security server, and the list of services provided.
 *
 * @param clientName         A human-readable name of the client entity
 *                           representing the service provider.
 * @param securityServerName The name of the security server associated
 *                           with the service provider.
 * @param services           A list of service names offered by the
 *                           service provider.
 */
case class ServiceConfig(
                          clientName: String,
                          securityServerName: String,
                          services: List[String]
                        )