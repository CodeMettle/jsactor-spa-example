import sbt.Keys._
import sbt._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

lazy val scalaVs = Seq("2.12.5")

lazy val exampleServer = (project in file("example-server")).settings(
  scalaVersion := scalaVs.head,
  routesGenerator := InjectedRoutesGenerator,
  scalaJSProjects := Seq(exampleClient),
  scalacOptions ++= Settings.scalacOptions,
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    guice,
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
    "com.codemettle.jsactor" %% "jsactor-bridge-server-upickle" % "0.7.0"
  )).
  enablePlugins(PlayScala, LauncherJarPlugin).
  aggregate(exampleClient, exampleSharedJvm, exampleSharedJs).
  dependsOn(exampleSharedJvm)

lazy val exampleClient = (project in file("example-client")).settings(
  name := "example-client",
  scalaVersion := scalaVs.head,
  scalacOptions ++= Settings.scalacOptions,
  scalaJSUseMainModuleInitializer := true,
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  libraryDependencies ++= Seq(
    "com.codemettle.jsactor" %%% "jsactor" % "0.7.0",
    "com.codemettle.jsactor" %%% "jsactor-bridge-client-upickle" % "0.7.0",
    "org.scala-js" %%% "scalajs-dom" % "0.9.4"
  ),
    skip in packageJSDependencies := true
    ).
  enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(exampleSharedJs)

lazy val exampleShared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("example-shared")).
  settings(
    scalaVersion := scalaVs.head,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.4.4",
      "com.codemettle.jsactor" %%% "jsactor-bridge-shared-upickle" % "0.7.0"
    )).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val exampleSharedJvm = exampleShared.jvm
lazy val exampleSharedJs = exampleShared.js

// loads the jvm project at sbt startup
onLoad in Global := ((s: State) ⇒ "project exampleServer" :: s) compose (onLoad in Global).value
