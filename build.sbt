lazy val Benchmark = config("bench") extend Test

lazy val buildSettings = Seq(
  organization := "com.github",
  name := "flink-shapeless",
  scalaVersion in ThisBuild := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8")
)

lazy val benchSettings = Seq(
  testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
  parallelExecution in Benchmark := false,
  logBuffered := false
)

lazy val coverageSettings = Seq(
  coverageEnabled := true,
  coverageMinimum := 70,
  coverageFailOnMinimum := false,
  coverageExcludedFiles := ".*/src/test/.*;.*/src/bench/.*"
)

lazy val compileDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % "1.2.0",
  "com.chuusai" %% "shapeless" % "2.3.2"
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.5",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "org.ostermiller" % "utils" % "1.07.00"
).map(_ % "test")

lazy val benchDependencies = Seq(
  "com.storm-enroute" %% "scalameter" % "0.8.2"
).map(_ % "bench")

lazy val commonSettings = Seq(
  scalacOptions := Seq(
    "-language:higherKinds",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint"
  ),
  libraryDependencies ++=
    compileDependencies ++
    testDependencies ++
    benchDependencies
)

lazy val root = Project("flink-shapeless", file("."))
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(buildSettings: _*)
  .settings(commonSettings: _*)
  .settings(benchSettings: _*)
  .settings(coverageSettings: _*)
  .configs(Benchmark)
  .settings(inConfig(Benchmark)(Defaults.testSettings): _*)
