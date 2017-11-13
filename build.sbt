name := "fixer-client"

organization := "com.snapswap"

version := "1.0.2"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-encoding",
  "UTF-8"
)

resolvers ++= Seq(
  "SnapSwap repo" at "https://dev.snapswap.vc/artifactory/libs-release/",
  "SnapSwap snapshot repo" at "https://dev.snapswap.vc/artifactory/libs-snapshot/"
)

libraryDependencies ++= {
  val akkaV = "2.4.11"
  Seq(
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "joda-time" % "joda-time" % "2.9.4",
    "org.joda" % "joda-convert" % "1.8.1",
    "com.google.code.findbugs" % "jsr305" % "3.0.1" % "provided",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
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
