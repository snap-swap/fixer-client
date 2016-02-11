name := "fixer-client"

organization := "com.snapswap"

version := "0.1.3"

scalaVersion := "2.11.7"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "UTF-8")

resolvers ++= Seq(
  "SnapSwap repo" at "https://dev.snapswap.vc/artifactory/libs-release/",
  "SnapSwap snapshot repo" at "https://dev.snapswap.vc/artifactory/libs-snapshot/"
)

libraryDependencies ++= {
  val akkaStreamV = "2.0-M2"
  Seq(
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "joda-time" % "joda-time" % "2.8.1",
    "org.joda" % "joda-convert" % "1.7",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV % "test"
  )
}

lazy val UsageSample = config("UsageSample") extend Test
def testUsageSampleFilter(name: String): Boolean = name endsWith "UsageSample"
def testNotUsageSampleFilter(name: String): Boolean = !testUsageSampleFilter(name)
lazy val root = (project in file(".")).
  configs(UsageSample).
  settings(inConfig(UsageSample)(Defaults.testTasks): _*).
  settings(testOptions in UsageSample := Seq(Tests.Filter(testUsageSampleFilter))).
  settings(testOptions in Test := Seq(Tests.Filter(testNotUsageSampleFilter)))

lazy val unitTest = taskKey[Unit]("Unit tests task")
unitTest <<= test in Test
test <<= (test in UsageSample).dependsOn(unitTest)

fork in Test := true
