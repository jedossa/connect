package connect.resources

import cats.effect.{ Async, Resource }
import cats.syntax.applicative._
import cats.syntax.option._
import cats.syntax.parallel._
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{ AccessToken, ConsumerToken }
import com.danielasfregola.twitter4s.http.clients.rest.friendships.TwitterFriendshipClient
import connect.configuration.HttpConfig
import github4s.GithubConfig
import github4s.http.HttpClient
import github4s.interpreters.StaticAccessToken
import org.http4s.ember.client.EmberClientBuilder

sealed abstract class HttpResources[F[_]] private (
    val githubClient: HttpClient[F],
    val twitterClient: TwitterFriendshipClient
)

object HttpResources {

  def apply[F[_]: Async](config: HttpConfig): Resource[F, HttpResources[F]] = {
    // TODO configure timeouts, idle connections, etc.
    val githubClient = {
      EmberClientBuilder
        .default[F]
        .build
        .map { client =>
          new HttpClient[F](client, GithubConfig.default, new StaticAccessToken(config.githubToken.value.value.some))
        }
    }

    val twitterRestClient = Resource.make[F, TwitterRestClient] {
      val consumerToken =
        ConsumerToken(
          key = config.twitterConsumerTokenKey.value,
          secret = config.twitterConsumerTokenSecret.value.value
        )
      val accessToken =
        AccessToken(key = config.twitterAccessTokenKey.value, secret = config.twitterAccessTokenSecret.value.value)
      TwitterRestClient(consumerToken, accessToken).pure
    }(
      client => Async[F].fromFuture(client.shutdown().pure)
    )

    (githubClient, twitterRestClient).parMapN(new HttpResources(_, _) {})
  }
}
