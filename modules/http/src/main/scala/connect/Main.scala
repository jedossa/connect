package connect

import cats.effect._
import connect.api.HttpApi
import connect.configuration.Configuration
import connect.modules.{ Caches, HttpClients, Programs }
import connect.resources.HttpResources
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{ Logger, SelfAwareStructuredLogger }

object Main extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private def showBanner(server: Server): IO[Unit] =
    Logger[IO].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${server.address.toString}")

  override def run: IO[Unit] =
    Configuration.load[IO].flatMap { config =>
      HttpResources[IO](config)
        .map { resources =>
          val clients  = HttpClients[IO](resources.twitterClient, resources.githubClient)
          val caches   = Caches.apply
          val programs = Programs[IO](clients, caches)
          val api      = HttpApi[IO](programs)
          config.httpServerConfig -> api.httpApp
        }
        .flatMap {
          case (config, httpApp) =>
            EmberServerBuilder
              .default[IO]
              .withHost(config.host)
              .withPort(config.port)
              .withHttpApp(httpApp)
              .build
              .evalTap(showBanner)
        }
        .useForever

    }
}
