package connect.api

import cats.effect.Async
import connect.modules.Programs
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._

import scala.concurrent.duration._

sealed abstract class HttpApi[F[_]: Async] private (
    programs: Programs[F]
) {

  private[api] val routes: HttpRoutes[F] = Router(
    "/developers" -> ConnectRoutes(programs).routes
  )

  private[api] val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private[api] val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}

object HttpApi {
  def apply[F[_]: Async](programs: Programs[F]): HttpApi[F] = new HttpApi[F](programs) {}
}
