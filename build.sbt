import sbt.Project.projectToRef

lazy val scalaVs = Seq("2.11.6")

val jsCompileDependencies = Seq(
  "org.webjars" % "react" % "0.13.1" / "react-with-addons.min.js" commonJSName "React",
  "org.webjars" % "bootstrap" % "3.3.2-1" / "bootstrap.min.js" dependsOn "jquery.min.js",
  "org.webjars" % "holderjs" % "2.4.0" / "holderjs/2.4.0/holder.js",
  "org.webjars" % "chartjs" % "1.0.2" / "Chart.min.js",
  "org.webjars" % "showdown" % "0.3.1" / "compressed/showdown.js"
)

lazy val exampleServer = (project in file("example-server")).settings(
  crossScalaVersions := scalaVs,
  scalaVersion := scalaVs.head,
  scalaJSProjects := Seq(exampleClient),
  scalacOptions ++= Settings.scalacOptions,
  pipelineStages := Seq(scalaJSProd, gzip),
  libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0-3",
    "com.vmunier" %% "play-scalajs-scripts" % "0.1.0",
    "org.webjars" % "bootstrap" % "3.3.2-1",
    "org.webjars" % "holderjs" % "2.4.0",
    "org.webjars" % "loglevel" % "1.1.0",
    "org.webjars" % "chartjs" % "1.0.2",
    "org.webjars" % "font-awesome" % "4.3.0-1",
    "org.webjars" % "showdown" % "0.3.1",
    "com.codemettle.jsactor" %% "jsactor-bridge-server" % "0.6.1"
  )).
  enablePlugins(PlayScala).
  aggregate(exampleClient).
  dependsOn(exampleSharedJvm)

lazy val exampleClient = (project in file("example-client")).settings(
  crossScalaVersions := scalaVs,
  scalaVersion := scalaVs.head,
  scalacOptions ++= Settings.scalacOptions,
  persistLauncher := true,
  persistLauncher in Test := false,
  sourceMapsDirectories += exampleSharedJs.base / "..",
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  libraryDependencies ++= Seq(
    "com.codemettle.jsactor" %%% "jsactor" % "0.6.1",
    "com.codemettle.jsactor" %%% "jsactor-bridge-client" % "0.6.1",
    "com.codemettle.jsactor" %%% "jsactor-loglevel" % "0.6.1",
    "org.scala-js" %%% "scalajs-dom" % "0.8.0",
    "org.querki" %%% "jquery-facade" % "0.3",
    "com.lihaoyi" %%% "scalarx" % "0.2.8",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.8.3",
    "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.3",
    "com.github.japgolly.scalacss" %%% "ext-react" % "0.1.0",
    "org.webjars" % "font-awesome" % "4.3.0-1"
  ),
    jsDependencies := jsCompileDependencies,
    skip in packageJSDependencies := false
    ).
  enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(exampleSharedJs)

lazy val exampleShared = (crossProject.crossType(CrossType.Pure) in file("example-shared")).
  settings(
    crossScalaVersions := scalaVs,
    scalaVersion := scalaVs.head,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Seq(
      "com.codemettle.jsactor" %%% "jsactor-bridge-shared" % "0.6.1"
    )).
  jsConfigure(_ enablePlugins ScalaJSPlay).
  jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val exampleSharedJvm = exampleShared.jvm
lazy val exampleSharedJs = exampleShared.js

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project exampleServer", _: State)) compose (onLoad in Global).value
