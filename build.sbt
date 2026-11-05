import org.scalajs.linker.interface.ModuleKind

ThisBuild / scalaVersion := "3.8.4"
ThisBuild / organization := "com.protocolocero"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "protocolo-cero",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)),
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "tyrian" % "0.30.0-M6",
      "org.scalameta" %%% "munit" % "1.0.2" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
