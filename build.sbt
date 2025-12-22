name := "once-only-sim"

version := "1.0.0"

scalaVersion := "3.7.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.lihaoyi" %% "upickle" % "4.4.1"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assembly / mainClass := Some("Main")
assembly / assemblyJarName := "once-only-simulation.jar"
