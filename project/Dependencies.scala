import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.6.1"
    val catsEffect = "3.2.3"
    val catsRetry  = "3.1.0"
    val circe      = "0.14.1"
    val ciris      = "2.1.1"
    val fs2        = "3.1.1"
    val github4s   = "0.29.1"
    val http4s     = "0.23.1"
    val log4cats   = "2.1.1"
    val monocle    = "3.0.0"
    val refined    = "0.9.27"
    val scalaCache = "0.28.0"
    val twitter4s  = "7.0"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.1"
    val logback          = "1.2.5"
    val organizeImports  = "0.5.0"

    val weaver = "0.7.5"
  }

  object Libraries {
    def circe(artifact: String): ModuleID =
      "io.circe" %% s"circe-$artifact" % Versions.circe
    def ciris(artifact: String): ModuleID =
      "is.cir" %% artifact % Versions.ciris
    def http4s(artifact: String): ModuleID =
      "org.http4s" %% s"http4s-$artifact" % Versions.http4s

    val cats       = "org.typelevel"    %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel"    %% "cats-effect" % Versions.catsEffect
    val catsRetry  = "com.github.cb372" %% "cats-retry"  % Versions.catsRetry

    val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2

    val circeCore    = circe("core")
    val circeGeneric = circe("generic")
    val circeParser  = circe("parser")
    val circeRefined = circe("refined")

    val cirisCore    = ciris("ciris")
    val cirisEnum    = ciris("ciris-enumeratum")
    val cirisRefined = ciris("ciris-refined")

    val http4sDsl    = http4s("dsl")
    val http4sServer = http4s("ember-server")
    val http4sClient = http4s("ember-client")
    val http4sCirce  = http4s("circe")

    val monocleCore  = "dev.optics" %% "monocle-core"  % Versions.monocle
    val monocleMacro = "dev.optics" %% "monocle-macro" % Versions.monocle

    val refinedCore = "eu.timepit" %% "refined"      % Versions.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % Versions.refined

    val scalaCache = "com.github.cb372" %% "scalacache-core"        % Versions.scalaCache
    val catsCache  = "com.github.cb372" %% "scalacache-cats-effect" % Versions.scalaCache
    val caffeine   = "com.github.cb372" %% "scalacache-caffeine"    % Versions.scalaCache

    val github4s  = "com.47deg"           %% "github4s"  % Versions.github4s
    val twitter4s = "com.danielasfregola" %% "twitter4s" % Versions.twitter4s

    val log4cats = "org.typelevel"  %% "log4cats-slf4j" % Versions.log4cats
    val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback

    val catsLaws          = "org.typelevel"       %% "cats-laws"          % Versions.cats
    val log4catsNoOp      = "org.typelevel"       %% "log4cats-noop"      % Versions.log4cats
    val monocleLaw        = "dev.optics"          %% "monocle-law"        % Versions.monocle
    val refinedScalacheck = "eu.timepit"          %% "refined-scalacheck" % Versions.refined
    val weaverCats        = "com.disneystreaming" %% "weaver-cats"        % Versions.weaver
    val weaverDiscipline  = "com.disneystreaming" %% "weaver-discipline"  % Versions.weaver
    val weaverScalaCheck  = "com.disneystreaming" %% "weaver-scalacheck"  % Versions.weaver

    val organizeImports = "com.github.liancheng" %% "organize-imports" % Versions.organizeImports
  }

  object CompilerPlugin {
    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
    )
    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % Versions.kindProjector cross CrossVersion.full
    )
  }

}
