import EndpointsSettings._
import LocalCrossProject._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val algebra =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra"))
    .settings(publishSettings ++ `scala 2.11 to latest`: _*)
    .settings(
      name := "endpoints-algebra",
      libraryDependencies ++= Seq(
        "com.github.tomakehurst" % "wiremock" % "2.6.0" % Test,
        "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
      )
    )
    .dependsOnLocalCrossProjectsWithScope("json-schema" -> "test->test;compile->compile")

val `algebra-js` = algebra.js

val `algebra-jvm` = algebra.jvm

val `algebra-circe` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra-circe"))
    .settings(publishSettings ++ `scala 2.11 to 2.12`: _*)
    .settings(
      name := "endpoints-algebra-circe",
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-parser" % circeVersion,
        "io.circe" %%% "circe-generic" % circeVersion % Test,
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" % Test cross CrossVersion.full)
      )
    )
    .dependsOn(`algebra` % "test->test;compile->compile")

val `algebra-circe-js` = `algebra-circe`.js

val `algebra-circe-jvm` = `algebra-circe`.jvm

val `algebra-playjson` =
  crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("algebra-playjson"))
    .settings(publishSettings ++ `scala 2.11 to 2.12`: _*)
    .settings(
      name := "endpoints-algebra-playjson",
      libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.6.9"
    )
    .dependsOn(`algebra` % "test->test;compile->compile")

val `algebra-playjson-js` = `algebra-playjson`.js
val `algebra-playjson-jvm` = `algebra-playjson`.jvm
