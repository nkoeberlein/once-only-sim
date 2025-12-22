package domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import java.time.LocalDateTime

class DataAccessLogSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  // Clean up state before each test
  override def beforeEach(): Unit = {
    DataAccessLog.clear()
  }

  "DataAccessLog" should "add and retrieve logs for specific citizen" in {
    val log1 = DataAccessLog(
      citizenId = "DE-001",
      timestamp = LocalDateTime.now(),
      consumer = "POLIZEI",
      provider = "KFZ-ZULASSUNG",
      dataTransferred = "get-vehicle-owner",
      purpose = "Strafzettel"
    )

    val log2 = DataAccessLog(
      citizenId = "DE-001",
      timestamp = LocalDateTime.now().plusMinutes(1),
      consumer = "POLIZEI",
      provider = "EINWOHNERMELDEAMT",
      dataTransferred = "get-citizen-data",
      purpose = "Strafzettel"
    )

    val log3 = DataAccessLog(
      citizenId = "DE-002",
      timestamp = LocalDateTime.now(),
      consumer = "BUERGERPORTAL",
      provider = "KFZ-ZULASSUNG",
      dataTransferred = "register-vehicle",
      purpose = "Neuanmeldung"
    )

    DataAccessLog.add(log1)
    DataAccessLog.add(log2)
    DataAccessLog.add(log3)

    val logsDE001 = DataAccessLog.getLogsForCitizen("DE-001")
    logsDE001 should have size 2
    logsDE001.head.citizenId should be ("DE-001")
    logsDE001.last.citizenId should be ("DE-001")
  }

  it should "return empty list for citizen with no logs" in {
    val logs = DataAccessLog.getLogsForCitizen("DE-999")
    logs should be (empty)
  }

  it should "return all logs sorted by timestamp (newest first)" in {
    val now = LocalDateTime.now()

    val log1 = DataAccessLog("DE-001", now.minusHours(2), "A", "B", "service1", "purpose1")
    val log2 = DataAccessLog("DE-002", now.minusHours(1), "C", "D", "service2", "purpose2")
    val log3 = DataAccessLog("DE-001", now, "E", "F", "service3", "purpose3")

    DataAccessLog.add(log1)
    DataAccessLog.add(log2)
    DataAccessLog.add(log3)

    val allLogs = DataAccessLog.getAllLogs
    allLogs should have size 3
    allLogs.head.timestamp should be (now) // Newest first
    allLogs.last.timestamp should be (now.minusHours(2)) // Oldest last
  }

  it should "persist logs to file" in {
    import java.nio.file.{Files, Paths}

    val log = DataAccessLog(
      citizenId = "DE-001",
      timestamp = LocalDateTime.now(),
      consumer = "TEST",
      provider = "TEST",
      dataTransferred = "test-service",
      purpose = "test"
    )

    DataAccessLog.add(log)

    val logFile = Paths.get("logs.json")
    Files.exists(logFile) should be (true)

    val content = new String(Files.readAllBytes(logFile))
    content should include ("DE-001")
    content should include ("TEST")
  }

  it should "load logs from file on initialization" in {
    val log1 = DataAccessLog("DE-TEST", LocalDateTime.now(), "A", "B", "s1", "p1")
    DataAccessLog.add(log1)

    val logs = DataAccessLog.getAllLogs
    logs should have size 1
    logs.head.citizenId should be ("DE-TEST")
  }

  it should "format toString correctly" in {
    val timestamp = LocalDateTime.of(2025, 1, 15, 14, 30, 45)
    val log = DataAccessLog(
      citizenId = "DE-001",
      timestamp = timestamp,
      consumer = "POLIZEI",
      provider = "KFZ-ZULASSUNG",
      dataTransferred = "get-vehicle-owner",
      purpose = "Strafzettel wegen Falschparken"
    )

    val str = log.toString
    str should include ("DE-001")
    str should include ("15.01.2025 14:30:45")
    str should include ("POLIZEI --> KFZ-ZULASSUNG")
    str should include ("get-vehicle-owner")
    str should include ("Strafzettel wegen Falschparken")
  }

  it should "handle concurrent access safely" in {
    import scala.concurrent.{Future, Await}
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global

    val futures = (1 to 10).map { i =>
      Future {
        val log = DataAccessLog(
          s"DE-00$i",
          LocalDateTime.now(),
          "CONSUMER",
          "PROVIDER",
          "service",
          "purpose"
        )
        DataAccessLog.add(log)
      }
    }

    Await.ready(Future.sequence(futures), 5.seconds)

    val allLogs = DataAccessLog.getAllLogs
    allLogs should have size 10
  }

  it should "correctly clear all logs when clear() is called" in {
    DataAccessLog.add(DataAccessLog("DE-CLEAR", LocalDateTime.now(), "A", "B", "C", "D"))
    DataAccessLog.getAllLogs should have size 1
    
    DataAccessLog.clear()
    
    DataAccessLog.getAllLogs should be (empty)
    import java.nio.file.{Files, Paths}
    Files.exists(Paths.get("logs.json")) should be (false)
  }
}