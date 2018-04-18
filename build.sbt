import sbt.Keys.version

// loads the server project at sbt startup
onLoad in Global ~= (_ andThen ("project server" :: _))

val commonSettings = Seq(
  organization := "com.pawnrule",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.5"
)

scalacOptions in Test ++= Seq("-Yrangepos")
scalacOptions ++= Seq(
  "-encoding",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
  "-P:scalajs:sjsDefinedByDefault",
  "utf8"
)

lazy val server = project
  .enablePlugins(PlayScala, ScoverageSbtPlugin, SbtWeb)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      //Java
      "org.logback-extensions"   % "logback-ext-loggly"                   % "0.1.5",
      "org.elasticsearch"        % "elasticsearch"                        % "6.0.1",
      "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.0.1",
      //Scala
      ws,
      guice,
      ehcache,
      specs2              % Test,
      "org.typelevel"     %% "cats-core" % "1.1.0",
      "com.vmunier"       %% "scalajs-scripts" % "1.1.2",
      "org.webjars"       %% "webjars-play" % "2.6.3",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1",
      "com.mohiva"        %% "play-html-compressor" % "0.7.1"
    ),
    name := """wintacky""",
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(scalaJSPipeline, uglify, digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    routesGenerator := InjectedRoutesGenerator,
    coverageEnabled in(Test, compile) := true,
    coverageEnabled in(Compile, compile) := false,
    coverageExcludedPackages := "<empty>;controllers.javascript;router;models;view.*;config.*;.*(AuthService|BuildInfo|Routes).*",
    scalafmtOnCompile := true,
    // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
    EclipseKeys.preTasks := Seq(compile in Compile)
  )
  .dependsOn(sharedJvm)

lazy val client = project
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(commonSettings: _*)
  .settings(
    coverageEnabled := false,
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
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
    coverageEnabled := false,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle"   % "0.5.1",
      "com.lihaoyi" %%% "autowire"  % "0.2.6",
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    )
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs  = shared.js
