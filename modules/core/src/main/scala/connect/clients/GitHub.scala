package connect.clients

import cats.MonadThrow
import cats.syntax.applicative._
import cats.syntax.flatMap._
import connect.domain.github.{ Organization, UserName }
import github4s.GHResponse
import github4s.http.HttpClient

trait GitHub[F[_]] {
  def getOrganizations(user: UserName): F[Set[Organization]]
}

object GitHub {
  def apply[F[_]: GitHub]: GitHub[F] = implicitly

  def apply[F[_]: MonadThrow](client: HttpClient[F]): GitHub[F] = {
    case userName: UserName =>
      client.get[List[Organization]](s"users/${userName.value.toString}/orgs") flatMap {
        case GHResponse(result, _, _) => result.fold(MonadThrow[F].raiseError, _.toSet.pure)
      }
  }
}
