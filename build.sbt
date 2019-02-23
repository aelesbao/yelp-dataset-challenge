organization in ThisBuild := "io.github.aelesbao"
scalaVersion in ThisBuild := "2.11.12"
version      in ThisBuild := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(name := "yelp-dataset-challenge")
  .aggregate(importer)
  .settings(commonSettings: _*)

lazy val common = (project in file("common"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
              Dependencies.spark)

lazy val importer = (project in file("importer"))
  .dependsOn(common)
  .settings(commonSettings: _*)

def commonSettings: Seq[Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),

  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters", "-Werror"),

  scalacOptions ++= Seq("-language:postfixOps",
                        "-language:implicitConversions",
                        "-language:existentials",
                        "-feature",
                        "-deprecation",
                        "-encoding",
                        "UTF-8",
                        "-Xlint",
                        "-unchecked"),

  // Display run times of individual tests
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),

  // The default SBT testing java options are too small to support running
  // many of the tests due to the need to launch Spark in local mode
  fork in Test := true,
  javaOptions in Compile ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
  )
