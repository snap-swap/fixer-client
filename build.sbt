name := "fixer-client"

organization := "com.snapswap"

version := "0.1.4"

scalaVersion := "2.11.7"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "UTF-8")

resolvers ++= Seq(
  "SnapSwap repo" at "https://dev.snapswap.vc/artifactory/libs-release/",
  "SnapSwap snapshot repo" at "https://dev.snapswap.vc/artifactory/libs-snapshot/"
)

libraryDependencies ++= {
  val akkaV = "2.4.2"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "joda-time" % "joda-time" % "2.9.1",
    "org.joda" % "joda-convert" % "1.8.1",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
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
