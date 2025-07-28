scalaVersion := "2.13.14"

name := "MANN"

organization := "com.mann"

version := "0.1.1-SNAPSHOT"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test"

Compile / run / mainClass := Some("AdvancedTest")
