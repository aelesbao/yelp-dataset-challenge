import sbtassembly.MergeStrategy

organization in ThisBuild := "io.github.aelesbao"
scalaVersion in ThisBuild := "2.11.12"
version      in ThisBuild := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(name := "yelp-dataset-challenge")
  .aggregate(importer)
  .settings(commonSettings: _*)
  .disablePlugins(AssemblyPlugin)

lazy val common = (project in file("common"))
  .disablePlugins(AssemblyPlugin)
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++=
              Dependencies.spark ++
              Dependencies.logging ++
              Dependencies.testing)

lazy val importer = (project in file("importer"))
  .dependsOn(common)
  .settings(commonSettings: _*)
  .settings(assemblySettings: _*)
  .settings(libraryDependencies ++=
              Dependencies.spark ++
              Dependencies.testing)

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

  // Let "run" task uses all the libraries, including the ones marked with "provided"
  run in Compile := Defaults.runTask(
    fullClasspath in Compile,
    mainClass in (Compile, run),
    runner in (Compile, run)
  ).evaluated
)

def assemblySettings: Seq[Setting[_]] = Seq(
  assemblyJarName in assembly := s"yelp-dataset-${name.value}-${version.value}.jar",

  // without this explicit merge strategy code you get a lot of noise from sbt-assembly
  // complaining about not being able to de-duplicate files
  assemblyMergeStrategy in assembly := {
    case PathList("org", "aopalliance", xs@_ *) => MergeStrategy.last
    case PathList("javax", "inject", xs@_ *) => MergeStrategy.last
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
    case PathList("javax", "activation", xs@_*) => MergeStrategy.last
    case PathList("org", "apache", xs@_*) => MergeStrategy.last
    case PathList("com", "google", xs@_*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
    case PathList("com", "codahale", xs@_*) => MergeStrategy.last
    case PathList("com", "yammer", xs@_*) => MergeStrategy.last
    case "about.html" => MergeStrategy.rename
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case "overview.html" => MergeStrategy.last
    case "git.properties" => MergeStrategy.last
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },

  // including scala bloats the assembly jar unnecessarily, and may interfere with spark runtime
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
)
