package connect.api

import cats.Monad
import connect.domain.{ ConnectionDetails, UserHandle }
import connect.modules.Programs
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class ConnectRoutes[F[_]: Monad](
    private val programs: Programs[F]
) extends Http4sDsl[F] {

  import ConnectRoutes._

  private[api] val prefixPath = "/connected"

  private[this] val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / sourceHandle / targetHandle =>
      val request = for {
        source <- UserHandle(sourceHandle)
        target <- UserHandle(targetHandle)
      } yield source -> target

      request.fold(
        error => BadRequest(error.getMessage),
        users => Ok((programs.checkConnection.fullyConnected _).tupled(users))
      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}

object ConnectRoutes {

  //TODO change the json format to be the same as requested
  implicit def connectionEncoder[F[_]]: EntityEncoder[F, ConnectionDetails] = jsonEncoderOf[F, ConnectionDetails]
}
