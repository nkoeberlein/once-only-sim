package ui


/**
 * Provides utility methods for terminal-based output with ANSI styling
 * for simulation purposes. Includes functions for formatted messages,
 * headers, sub-headers, boxes, and simulation-specific outputs.
 *
 * This object is designed for creating a better user interface experience
 * in a terminal environment, with textual and color-coded enhancements.
 */
object Terminal {
  // ANSI Color Codes
  private val RESET = "\u001b[0m"
  private val BOLD = "\u001b[1m"
  
  // Farben
  private val RED = "\u001b[31m"
  private val GREEN = "\u001b[32m"
  private val YELLOW = "\u001b[33m"
  private val BLUE = "\u001b[34m"
  private val MAGENTA = "\u001b[35m"
  private val CYAN = "\u001b[36m"
  private val WHITE = "\u001b[37m"
  
  // Hintergrundfarben
  private val BG_RED = "\u001b[41m"
  private val BG_GREEN = "\u001b[42m"
  private val BG_BLUE = "\u001b[44m"
  private val BG_CYAN = "\u001b[46m"
  
  def clearScreen(): Unit = {
    print("\u001b[H\u001b[2J")
    System.out.flush()
  }
  
  /**
   * Prints a styled header to the console for organizing output sections.
   * The header includes a bold and colored title encapsulated by horizontal separator lines.
   *
   * @param text The title of the header to be displayed.
   * @return Unit, as the method performs its function as a side effect by printing the formatted header to the console.
   */
  def printHeader(text: String): Unit = {
    println()
    println(s"$BOLD$CYAN═${"═" * 60}═$RESET")
    println(s"$BOLD$CYAN  $text$RESET")
    println(s"$BOLD$CYAN═${"═" * 60}═$RESET")
    println()
  }
  
  /**
   * Prints a styled subheader to the console for organizing output sections.
   * The subheader includes a bolded and colored title followed by a horizontal separator line.
   *
   * @param text The text of the subheader to be displayed.
   * @return Unit, as the method performs a side effect by printing the formatted subheader to the console.
   */
  def printSubHeader(text: String): Unit = {
    println()
    println(s"$BOLD$BLUE▶ $text$RESET")
    println(s"$BLUE${"─" * 60}$RESET")
  }
  
  /**
   * Logs a success message with a green-colored "✓" prefix and resets the console color after the message.
   * The message is printed as a side effect of this method.
   *
   * @param text The success message to be logged.
   * @return Unit, as the method only performs a side effect by printing the formatted message to the console.
   */
  def logSuccess(text: String): Unit = {
    println(s"$GREEN✓ $text$RESET")
  }
  
  /**
   * Logs an informational message with a cyan-colored "●" prefix and resets the console color after the message.
   * The message is printed as a side effect of this method.
   *
   * @param text The informational message to be logged.
   * @return Unit, as the method only performs a side effect by printing the formatted message to the console.
   */
  def logInfo(text: String): Unit = {
    println(s"$CYAN● $text$RESET")
  }
  
  /**
   * Logs a warning message with a yellow-colored "⚠" prefix and resets the console color after the message.
   * The message is printed as a side effect of this method.
   *
   * @param text The warning message to be logged.
   * @return Unit, as the method only performs a side effect by printing the formatted message to the console.
   */
  def logWarning(text: String): Unit = {
    println(s"$YELLOW⚠ $text$RESET")
  }
  
  /**
   * Logs an error message with a red-colored "✗" prefix and resets the console color after the message.
   * The message is printed as a side effect of this method.
   *
   * @param text The error message to be logged.
   * @return Unit, as the method only performs a side effect by printing the formatted message to the console.
   */
  def logError(text: String): Unit = {
    println(s"$RED✗ $text$RESET")
  }
  
  /**
   * Logs a message with a distinctive X-Road simulation format.
   *
   * @param text The message to be logged, providing relevant information or updates
   *             about the operation or interaction within the X-Road-like infrastructure.
   * @return Unit as the method performs the logging as a side effect, printing the formatted
   *         message to the console.
   */
  def logXRoad(text: String): Unit = {
    println(s"$MAGENTA◆ $text$RESET")
  }
  
  /**
   * Prints the main landing screen of the X-Road simulation. This method clears the console
   * and displays a formatted menu, providing an overview of the system and available commands.
   * The header includes the simulation's title and description, followed by a list of commands
   * with brief descriptions for each. It also includes a notice about the simulation's scope.
   *
   * @return Unit, as the method performs its function by printing content directly to the console using side effects.
   */
  def printLandingScreen(): Unit = {
    clearScreen()
    println()
    println(s"$BOLD$CYAN")
    println("╔═════════════════════════════════════════════════════════════════════════════════╗")
    println("║                                                                                 ║")
    println("║     ██████╗ ███╗   ██╗ ██████╗███████╗      ██████╗ ███╗   ██╗██╗  ██╗   ██╗    ║")
    println("║    ██╔═══██╗████╗  ██║██╔════╝██╔════╝     ██╔═══██╗████╗  ██║██║   ██╗ ██╔╝    ║")
    println("║    ██║   ██║██╔██╗ ██║██║     █████╗       ██║   ██║██╔██╗ ██║██║    ████╔╝     ║")
    println("║    ██║   ██║██║╚██╗██║██║     ██╔══╝       ██║   ██║██║╚██╗██║██║     ██╔╝      ║")
    println("║    ╚██████╔╝██║ ╚████║╚██████╗███████╗     ╚██████╔╝██║ ╚████║███████╗██║       ║")
    println("║     ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝╚══════╝      ╚═════╝ ╚═╝  ╚═══╝╚══════╝╚═╝       ║")
    println("║                                                                                 ║")
    println("║                     X-Road Simulation - E-Estonia Prinzipien                    ║")
    println("║                                                                                 ║")
    println("╚═════════════════════════════════════════════════════════════════════════════════╝")
    println(s"$RESET")
    println()
    println(s"$BOLD${WHITE}Demonstration des Once-Only-Prinzips und der Funktionsweise der X-Road$RESET")
    println()
    println(s"$GREEN▶ Verfügbare Kommandos:$RESET")
    println()
    println(s"  $CYAN[1]$RESET start-simulation      - Startet die X-Road Infrastruktur")
    println(s"  $CYAN[2]$RESET buergerportal         - Öffnet das Bürgerportal")
    println(s"  $CYAN[3]$RESET police-tickethunt     - Polizei erstellt Strafzettel (incl. Halterabfrage über das Kennzeichen)")
    println(s"  $CYAN[4]$RESET datalogs              - Zeigt den systemweiten Datenzugriffs-Log")
    println(s"  $CYAN[5]$RESET status                - Zeigt System-Status und Konfiguration")
    println(s"  $CYAN[6]$RESET help                  - Zeigt detaillierte Hilfe")
    println(s"  $CYAN[7]$RESET restart-simulation    - Neustart der Simulation")
    println(s"  $CYAN[0]$RESET exit                  - Beendet die Simulation")
    println()
    println(s"$YELLOW⚠  Hinweis: Die Simulation läuft lokal und demonstriert vereinfachte X-Road Mechanismen$RESET")
    println()
  }
  
  /**
   * Displays the help section for the simulation, explaining its purpose, core principles,
   * architecture, and providing an example scenario for understanding the Once-Only
   * Simulation concept. The method outputs detailed information directly to the console.
   *
   * @return Unit, as the method performs its function by printing help content using side effects.
   */
  def printHelp(): Unit = {
    clearScreen()
    printHeader("HILFE - Once-Only Simulation")
    
    println(s"$BOLD${WHITE}Was demonstriert diese Simulation?$RESET")
    println()
    println("Diese Simulation zeigt das Once-Only-Prinzip von E-Estonia:")
    println()
    println(s"  $GREEN● Datenhoheit:    $RESET Behörden sind souveräne Datenquellen (kein zentraler Speicher)")
    println(s"  $GREEN● Datenkonsistenz:$RESET Einmalige Erfassung, Verbot der Doppelerfassung")
    println(s"  $GREEN● Transparenz:    $RESET Jeder Datenzugriff wird protokolliert")
    println(s"  $GREEN● Effizienz:      $RESET Automatisierter behördenübergreifender Datenaustausch")
    println()
    
    printSubHeader("Architektur")
    println("  Central Server    - Verwaltet Service-Registry und Konfiguration")
    println("  Security Server   - Gateway zur X-Road")
    println("  Clients           - Polizei, Bürgerportal, Einwohnermeldeamt, KFZ-Zulassung")
    println()
    
    printSubHeader("Beispiel-Szenario: Strafzettel wegen Falschparken")
    println("  1. Polizei erfasst Kennzeichen des Falschparkers")
    println("  2. Abfrage bei KFZ-Zulassung: Wer ist der Halter?")
    println("  3. Abfrage beim Einwohnermeldeamt: Aktuelle Adresse des Halters?")
    println("  4. Strafzettel wird erstellt und kann zugestellt werden")
    println("  5. Alle Zugriffe werden im Bürger-Log protokolliert")
    println()

    if(Terminal.pressEnter()) {
      Terminal.printLandingScreen()
    }
  }
  
  /**
   * Prints a box with the given text lines, formatted with a border and optional color.
   * The box adjusts its width dynamically based on the length of the longest line.
   *
   * @param lines A list of strings representing the lines of text to be displayed inside the box.
   * @param color An optional string specifying the color code for the box and text. Defaults to CYAN.
   * @return Unit, as the method performs formatting and printing to the console as a side effect.
   */
  def printBox(lines: List[String], color: String = CYAN): Unit = {
    val maxLength = lines.map(_.length).maxOption.getOrElse(0)
    val width = maxLength + 4
    
    println(s"$color┌${"─" * width}┐$RESET")
    lines.foreach { line =>
      val padding = " " * (maxLength - line.length)
      println(s"$color│  $line$padding  │$RESET")
    }
    println(s"$color└${"─" * width}┘$RESET")
  }

  /**
   * Prompts the user to press the Enter key to continue, displaying a styled message in the console.
   * The method waits for user input and returns a boolean value once the Enter key is pressed.
   *
   * @return Boolean always returns true after the Enter key is pressed, as a confirmation of user interaction.
   */
  def pressEnter(): Boolean = {
    println()
    println(s"$YELLOW⚡ Drücken Sie Enter zum Fortfahren...$RESET")
    scala.io.StdIn.readLine()
    // printLandingScreen()
    true
  }

  /**
   * Prints a new line to the console.
   *
   * @return Unit, as the method performs a side effect by printing a new line to the console.
   */
  def newLine(): Unit = {
    println()
  }
}
