import sbt.Project.projectToRef

lazy val scalaVs = Seq("2.11.6")

lazy val exampleServer = (project in file("example-server")).settings(
  crossScalaVersions := scalaVs,
  scalaVersion := scalaVs.head,
  scalaJSProjects := Seq(exampleClient),
  scalacOptions ++= Settings.scalacOptions,
  pipelineStages := Seq(scalaJSProd, gzip),
  libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0-3",
    "com.vmunier" %% "play-scalajs-scripts" % "0.1.0",
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
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  ),
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
