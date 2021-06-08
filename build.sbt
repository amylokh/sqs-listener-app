name := "sqs-listener-app"

version := "0.1"

scalaVersion := "2.12.11"

val AkkaVersion = "2.6.14"
val AkkaHttpVersion = "10.1.11"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "3.0.0",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.0-RC3"
)