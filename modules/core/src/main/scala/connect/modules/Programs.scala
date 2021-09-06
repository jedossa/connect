package connect.modules

import cats.effect.Async
import connect.domain.github.Organization
import connect.programs
import connect.programs.CheckConnection
import connect.domain.twitter.Friendship
import org.typelevel.log4cats.Logger
import scalacache.Cache

sealed abstract class Programs[F[_]: Async: Logger] private (
    private val clients: HttpClients[F],
    private val caches: Caches
) {
  def checkConnection: CheckConnection[F] = {

    implicit val friendshipCache: Cache[Friendship]           = caches.friendshipCache
    implicit val organizationsCache: Cache[Set[Organization]] = caches.organizationsCache

    programs.CheckConnection(twitter = clients.twitter, gitHub = clients.github)
  }
}

object Programs {
  def apply[F[_]: Async: Logger](clients: HttpClients[F], caches: Caches): Programs[F] =
    new Programs[F](clients, caches) {}
}
