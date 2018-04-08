// Comment to get more information during initialization
logLevel := Level.Warn

// Resolvers
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.2")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.4.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")
//> sbt dependencyUpdates

// Scala.js plugins

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")

// Web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.2")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.11")