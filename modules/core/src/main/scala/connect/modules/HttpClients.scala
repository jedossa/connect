package connect.modules

import cats.effect.Async
import com.danielasfregola.twitter4s.http.clients.rest.friendships.TwitterFriendshipClient
import connect.clients.{ GitHub, Twitter }
import github4s.http.HttpClient

sealed abstract class HttpClients[F[_]] private (
    val twitter: Twitter[F],
    val github: GitHub[F]
)

object HttpClients {
  def apply[F[_]: Async](twitterClient: TwitterFriendshipClient, githubClient: HttpClient[F]): HttpClients[F] =
    new HttpClients[F](
      twitter = Twitter[F](twitterClient),
      github = GitHub[F](githubClient)
    ) {}
}
