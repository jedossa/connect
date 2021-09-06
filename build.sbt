import Dependencies._
import com.typesafe.sbt.packager.docker.DockerChmodType
import scoverage.ScoverageKeys._

ThisBuild / turbo := true
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "dev.jedossa"
ThisBuild / organizationName := "jedossa"

ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalafixDependencies += Libraries.organizeImports

lazy val commonSettings = Seq(
  Docker / daemonUser := "daemon",
  dockerChmodType := DockerChmodType.UserGroupWriteExecute,
  scalacOptions in Global += "-Ymacro-annotations",
  dockerBaseImage := "openjdk:18-slim-buster",
  dockerUpdateLatest := true,
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  coverageMinimumStmtTotal := 70,
  coverageMinimumBranchTotal := 70,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  scalafmtOnCompile := true,
  wartremoverErrors ++= CustomWarts.all
)

val scalafixCommonSettings =
  inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

lazy val root = (project in file("."))
  .settings(name := "connect")
  .aggregate(core, http, tests)

lazy val core = (project in file("modules/core"))
  .settings(
    name := "connect-core",
    commonSettings,
    Defaults.itSettings,
    scalafixCommonSettings,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      Libraries.cats,
      Libraries.caffeine,
      Libraries.catsCache,
      Libraries.catsEffect,
      Libraries.github4s,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.monocleCore,
      Libraries.monocleMacro,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.scalaCache,
      Libraries.twitter4s
    )
  )

lazy val tests = (project in file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "connect-tests",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Defaults.itSettings,
    scalafixCommonSettings,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      Libraries.catsLaws,
      Libraries.log4catsNoOp,
      Libraries.monocleLaw,
      Libraries.refinedScalacheck,
      Libraries.weaverCats,
      Libraries.weaverDiscipline,
      Libraries.weaverScalaCheck
    )
  )
  .dependsOn(core % "compile->compile")

lazy val http = (project in file("modules/http"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "connect-http",
    Docker / packageName := "connect-http",
    commonSettings,
    Defaults.itSettings,
    scalafixCommonSettings,
    dockerExposedPorts ++= Seq(8080),
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.cirisCore,
      Libraries.cirisEnum,
      Libraries.cirisRefined,
      Libraries.fs2,
      Libraries.github4s,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sCirce,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.twitter4s
    )
  )
  .dependsOn(core % "compile->compile;test->test")

addCommandAlias("runLinter", ";scalafixAll --rules OrganizeImports")
