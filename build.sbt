import sbt.Keys.version

// loads the server project at sbt startup
onLoad in Global ~= (_ andThen ("project server" :: _))

val commonSettings = Seq(
  organization := "com.pawnrule",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.5"
)

lazy val server = project
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      //Java
      "org.elasticsearch"        % "elasticsearch"                        % "6.0.1",
      "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.0.1",
      "org.webjars"              % "bootstrap"                            % "4.0.0-2",
      "org.webjars"              % "animate.css"                          % "3.5.2",
      "org.webjars"              % "jquery"                               % "3.3.1-1",
      "org.webjars.npm"          % "popper.js"                            % "1.14.3",
      //Scala
      ws,
      guice,
      ehcache,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.typelevel"          %% "cats-core"          % "1.1.0",
      "com.vmunier"            %% "scalajs-scripts"    % "1.1.2",
      "org.webjars"            %% "webjars-play"       % "2.6.3",
      "org.mongodb.scala"      %% "mongo-scala-driver" % "2.2.1"
    ),
    name := """wintacky""",
    scalaJSProjects := Seq(client),
    scalacOptions += "-Ypartial-unification",
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    routesGenerator := InjectedRoutesGenerator,
    scalafmtOnCompile := true,
    // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
    EclipseKeys.preTasks := Seq(compile in Compile)
  )
  .dependsOn(sharedJvm)

lazy val client = project
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(commonSettings: _*)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    scalacOptions ++= Seq("-P:scalajs:sjsDefinedByDefault", "-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % "1.1.0",
      "org.scala-js"  %%% "scalajs-dom" % "0.9.5",
      "com.lihaoyi"   %%% "scalatags"   % "0.6.7"
    )
  )

lazy val shared = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle"   % "0.5.1",
      "com.lihaoyi" %%% "autowire"  % "0.2.6",
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    )
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs  = shared.js
