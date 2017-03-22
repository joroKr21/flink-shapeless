name := "flink-shapeless"

version := "1.2.0"

scalaVersion := "2.11.8"

val v = new {
  val flink = "1.2.0"
  val shapeless = "2.3.2"
  val scalatest = "3.0.1"
  val scalacheck = "1.13.4"
  val scalacheckShapeless = "1.1.5"
  val arm = "2.0"
}

// Flink
libraryDependencies += "org.apache.flink" %% "flink-scala" % v.flink

// Shapeless
libraryDependencies += "com.chuusai" %% "shapeless" % v.shapeless

// Test
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % v.scalatest,
  "org.scalacheck" %% "scalacheck" % v.scalacheck,
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % v.scalacheckShapeless,
  "com.jsuereth" %% "scala-arm" % v.arm
).map(_ % "test")
