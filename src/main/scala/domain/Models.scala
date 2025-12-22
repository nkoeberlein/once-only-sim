package domain

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Citizen(
  id: String,
  firstname: String,
  surname: String,
  nationality: String,
  var address: String,
  dateOfBirth: String,
  placeOfBirth: String
) {
  def fullName() : String = s"$firstname $surname"
  def birthDetails() : String = s"$dateOfBirth in $placeOfBirth"
}

case class Vehicle(
  licensePlate: String,
  citizenId: String,
  vin: Int,
  brand: String,
  model: String
)

case class DataAccessLog(
  citizenId: String,
  timestamp: LocalDateTime,
  consumer: String,
  provider: String,
  dataTransferred: String,
  purpose: String
) {
  private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
  
  override def toString: String = {
    s"[$citizenId] ${timestamp.format(formatter)} | $consumer --> $provider: $dataTransferred [Zweck: $purpose]"
  }
}

/**
 * Handles the persistence of data access logs.
 * Logs are stored in 'logs.json' to survive application restarts.
 * Uses uPickle for JSON serialization.
 */
object DataAccessLog {
  import upickle.default.{ReadWriter => RW, macroRW}
  import java.nio.file.{Files, Paths}
  import java.nio.charset.StandardCharsets
  import scala.util.Try

  implicit val rw: RW[DataAccessLog] = macroRW
  implicit val rwDateTime: RW[LocalDateTime] = upickle.default.readwriter[String].bimap[LocalDateTime](
    _.toString,
    LocalDateTime.parse(_)
  )

  private var logs: List[DataAccessLog] = loadLogs()
  private val logFile = "logs.json"

  def add(log: DataAccessLog): Unit = synchronized {
    logs = log :: logs
    saveLogs()
  }

  def getLogsForCitizen(citizenId: String): List[DataAccessLog] = synchronized {
    logs.filter(_.citizenId == citizenId).sortBy(_.timestamp).reverse
  }

  def getAllLogs: List[DataAccessLog] = synchronized {
    logs.sortBy(_.timestamp).reverse
  }

  // Saves current logs to JSON file
  private def saveLogs(): Unit = {
    Try {
      val json = upickle.default.write(logs)
      Files.write(Paths.get(logFile), json.getBytes(StandardCharsets.UTF_8))
    }
  }

  // Loads logs from JSON file on startup (if exists)
  private def loadLogs(): List[DataAccessLog] = {
    Try {
      if (Files.exists(Paths.get(logFile))) {
        val json = new String(Files.readAllBytes(Paths.get(logFile)), StandardCharsets.UTF_8)
        upickle.default.read[List[DataAccessLog]](json)
      } else {
        List.empty
      }
    }.getOrElse(List.empty)
  }

  // Clears all logs (Memory + File)
  def clear(): Unit = synchronized {
    logs = List.empty
    Try {
      val logFilePath = Paths.get(logFile)
      if (Files.exists(logFilePath)) {
        Files.delete(logFilePath)
      }
    }
  }
}