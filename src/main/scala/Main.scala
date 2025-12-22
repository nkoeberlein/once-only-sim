
import ui.Terminal
import core.SimulationContext
import domain.DataAccessLog
import scala.io.StdIn

object Main {
  private var context: Option[SimulationContext] = None

  def main(args: Array[String]): Unit = {
    Terminal.printLandingScreen()
    runEventLoop()
  }

  private def runEventLoop(): Unit = {
    var running = true

    while (running) {
      print(s"${Console.CYAN}simulation>${Console.RESET} ")
      val input = StdIn.readLine()

      if (input != null) {
        input.trim().toLowerCase() match {
          case "1" | "start-simulation" | "start" =>
            startSimulation()

          case "2" | "buerger-bürgerportal" | "bürgerportal" | "portal" =>
            withContext { ctx =>
              ctx.citizenPortal.foreach { portal => 
                portal.showPortal()
                Terminal.printLandingScreen()
              }
            }

          case "3" | "police-tickethunt" | "tickethunt" =>
            withContext { ctx =>
              policeTicketHunt(ctx)
            }

          case "4" | "buerger-datatracker" | "datatracker" =>
            withContext { ctx =>
               showDataTracker(ctx)
            }

          case "5" | "status" =>
             withContext { ctx => 
               showStatus(ctx)
             }
          
          case "6" | "help" =>
            Terminal.printHelp()
            Terminal.printLandingScreen()

          case "7" | "restart" | "restart-simulation" =>
            restartSimulation()

          case "0" | "exit" | "quit" =>
            Terminal.logInfo("Simulation wird beendet...")
            running = false

          case "" =>
          // Leerzeile ignorieren

          case unknown =>
            Terminal.logError(s"Unbekanntes Kommando: '$unknown'")
            Terminal.logInfo("Verwenden Sie 'help' für eine Liste aller Kommandos")
        }
      }
    }

    println()
    Terminal.logSuccess("Auf Wiedersehen!")
  }
  
  // Helper to safely access context
  private def withContext(f: SimulationContext => Unit): Unit = {
    context match {
      case Some(ctx) => f(ctx)
      case None =>
        Terminal.logWarning("System noch nicht initialisiert!")
        Terminal.logInfo("Bitte starten Sie zuerst die Simulation mit 'start-simulation'")
    }
  }

  private def restartSimulation(): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader("Simulation wird neu gestartet")
    println()

    if (context.isDefined) {
      Terminal.logInfo("Beende alle laufenden Komponenten...")
      Thread.sleep(500)

      context = None
      
      DataAccessLog.clear()
      
      Terminal.logSuccess("Alle Objekte wurden entfernt")
      Thread.sleep(500)

      System.gc()
      Terminal.logInfo("Speicher bereinigt")
      Thread.sleep(500)
    } else {
      Terminal.logWarning("System war nicht initialisiert")
    }

    println()
    Terminal.printBox(List(
      "✓ Simulation zurückgesetzt",
      "",
      "Das System kann jetzt neu gestartet werden.",
      "Verwenden Sie 'start-simulation' zum Neustart."
    ), Console.YELLOW)

    Thread.sleep(2000)
    if (Terminal.pressEnter()) {
      Terminal.printLandingScreen()
    }
  }

  private def startSimulation(): Unit = {
    if(context.isDefined) {
      Terminal.logSuccess("✓ X-Road Infrastruktur wurde bereits erfolgreich initialisiert")
      return
    }
    
    val ctx = new SimulationContext()
    ctx.initialize()
    context = Some(ctx)

    Thread.sleep(2000)
    if (Terminal.pressEnter()) {
      Terminal.printLandingScreen()
    }
  }

  private def policeTicketHunt(ctx: SimulationContext): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader("Polizei - Strafzettel-Erstellung")
    println()
    
    // Wir brauchen Registry für die Liste (Simulation)
    ctx.vehicleRegistry.foreach(_.listAllVehicles())

    print("Kennzeichen des Falschparkers eingeben: ")
    val licensePlate = StdIn.readLine().trim().toUpperCase()

    if (licensePlate.nonEmpty) {
      ctx.policeClient.foreach(_.processTicket(licensePlate))
    } else {
      Terminal.logError("Kein Kennzeichen eingegeben!")
    }

    Thread.sleep(2000)
    if (Terminal.pressEnter()) {
      Terminal.printLandingScreen()
    }
  }

  private def showDataTracker(ctx: SimulationContext): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader("Zentraler Datentracker (System-Log)")
    println()

    val allLogs = DataAccessLog.getAllLogs

    if (allLogs.isEmpty) {
      Terminal.logInfo("Noch keine Datenzugriffe im System protokolliert.")
    } else {
      Terminal.logSuccess(s"Systemweite Zugriffe: ${allLogs.size}")
      println()
      println("─" * 80)
      allLogs.foreach { log =>
        println(log.toString)
        println("─" * 80)
      }
    }

    Thread.sleep(2000)
    if (Terminal.pressEnter()) {
      Terminal.printLandingScreen()
    }
  }

  private def showStatus(ctx: SimulationContext): Unit = {
    Terminal.clearScreen()
    Terminal.printHeader("System-Status")

    println(s"${Console.GREEN}● System initialisiert: true${Console.RESET}")
    println()

    Terminal.printSubHeader("Registrierte Komponenten")
    println(s"  Central Server:  ${if (ctx.centralServer.isDefined) "✓ Aktiv" else "✗ Nicht verfügbar"}")
    println(s"  Security Server: 4 registriert") // Hardcoded for simplified check
    println(s"  Clients:         4 verbunden")
    println()

    Terminal.printSubHeader("Datenzugriffs-Statistik")
    val allLogs = DataAccessLog.getAllLogs
    println(s"  Protokollierte Zugriffe: ${allLogs.size}")

    if (allLogs.nonEmpty) {
      val byConsumer = allLogs.groupBy(_.consumer).view.mapValues(_.size).toMap
      println()
      println("  Zugriffe pro System:")
      byConsumer.foreach { case (consumer, count) =>
        println(s"    - $consumer: $count")
      }
    }

    println()
    ctx.centralServer.foreach(_.listAllServices())

    Thread.sleep(2000)
    if (Terminal.pressEnter()) {
      Terminal.printLandingScreen()
    }
  }
}