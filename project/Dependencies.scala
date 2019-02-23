import sbt._

object Version {
  val spark = "2.4.0"
}

object Dependencies {
  val spark: Seq[ModuleID] = Seq(
    "org.apache.spark" %% "spark-core" % Version.spark % Provided,
    "org.apache.spark" %% "spark-sql" % Version.spark % Provided,
    "org.apache.spark" %% "spark-streaming" % Version.spark % Provided
  )
}
