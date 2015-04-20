val scala_version = "2.11.6"
val gatling = "io.gatling" % "gatling-core" % "2.1.5"
val netty = "io.netty" % "netty" % "3.10.1.Final"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.3.7"
val scalalogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
def scalaLibrary = "org.scala-lang" % "scala-library" % scala_version

lazy val root = (project in file(".")).
  settings(
    version := "0.1.0",
    scalaVersion := scala_version,
    name := "gatling-udp-extension",
    libraryDependencies += gatling,
    libraryDependencies += netty,
    libraryDependencies += akkaActor,
    libraryDependencies += scalalogging
  )
