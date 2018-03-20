import sbt.Keys.version

// loads the server project at sbt startup
onLoad in Global ~= (_ andThen ("project server" :: _))

val commonSettings = Seq(
  organization := "com.pawnrule",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.4"
)


lazy val server = project
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "com.vmunier" %% "scalajs-scripts" % "1.1.1",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.webjars" %% "webjars-play" % "2.6.3",
      "org.webjars" % "bootstrap" % "4.0.0",
      "org.webjars" % "animate.css" % "3.5.2"
    ),
    name := """wintacky""",
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    routesGenerator := InjectedRoutesGenerator,
    scalafmtOnCompile := true
  )
  .dependsOn(sharedJvm)

lazy val client = project
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(commonSettings: _ *)
  .settings(
    scalaJSUseMainModuleInitializer := false,
    scalaJSUseMainModuleInitializer in Test := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    )
  )

lazy val shared = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.5.1",
      "com.lihaoyi" %%% "autowire" % "0.2.6",
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    )
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm

lazy val sharedJs = shared.js