import sbt.Keys._
import sbt._

object Settings {

  val jsactorVersion = "0.6.4-SNAPSHOT"

  val common = Seq[Setting[_]](
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalacOptions := Seq("-Xlint", "-unchecked", "-deprecation", "-feature"),
    scalaVersion := "2.11.6"
  )
}
