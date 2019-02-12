import sbt.Keys.version

// loads the server project at sbt startup
onLoad in Global ~= (_ andThen ("project server" :: _))

val commonSettings = Seq(
  organization := "com.pawnrule",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.8"
)

scalacOptions in Test ++= Seq("-Yrangepos")
scalacOptions ++= Seq(
  "-encoding",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Ypartial-unification",
  "-Ybackend-parallelism",
  "-Xfatal-warnings",
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
      "org.typelevel"     %% "cats-core" % "1.6.0",
      "org.webjars"       %% "webjars-play" % "2.7.0",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0",
      "com.mohiva"        %% "play-html-compressor" % "0.7.1"
    ),
    name := """wintacky""",
    pipelineStages := Seq(uglify, digest, gzip),
    routesGenerator := InjectedRoutesGenerator,
    coverageEnabled in(Test, compile) := true,
    coverageEnabled in(Compile, compile) := false,
    coverageExcludedPackages := "<empty>;controllers.javascript;router;models;view.*;config.*;.*(AuthService|BuildInfo|Routes).*",
    scalafmtOnCompile := true,
    // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
    EclipseKeys.preTasks := Seq(compile in Compile)
  )