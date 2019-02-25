import sbt._

object Version {
  val spark = "2.4.0"
  val slf4j = "1.7.25"
  val scalaLogging = "2.1.2"
  val typesafeConfig = "1.3.3"
  val pureconfig = "0.10.2"
  val commonsCompress = "1.18"
}

object Dependencies {
  val spark: Seq[ModuleID] = Seq(
    "org.apache.spark" %% "spark-core" % Version.spark % Provided,
    "org.apache.spark" %% "spark-sql" % Version.spark % Provided,
    "org.apache.spark" %% "spark-streaming" % Version.spark % Provided
  )

  val logging: Seq[ModuleID] = Seq(
    "org.slf4j" % "slf4j-api" % Version.slf4j,
    "org.slf4j" % "slf4j-log4j12" % Version.slf4j,
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % Version.scalaLogging
  )

  val config: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % Version.typesafeConfig,
    "com.github.pureconfig" %% "pureconfig" % Version.pureconfig
  )

  val compression: Seq[ModuleID] = Seq(
    "org.apache.commons" % "commons-compress" % Version.commonsCompress
  )

  val testing: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.apache.spark" %% "spark-core" % Version.spark % Test classifier "tests",
    "org.apache.spark" %% "spark-sql" % Version.spark % Test classifier "tests",
    "org.apache.spark" %% "spark-catalyst" % Version.spark % Test classifier "tests",
    "org.apache.spark" %% "spark-streaming" % Version.spark % Test classifier "tests"
  )
}
