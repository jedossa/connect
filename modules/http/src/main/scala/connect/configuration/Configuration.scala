package connect.configuration

import cats.effect.Async
import cats.implicits._
import ciris._
import ciris.refined._
import com.comcast.ip4s._
import connect.configuration
import connect.configuration.Environments.{ Live, Local }
import connect.configuration.types._
import eu.timepit.refined.cats._

object Configuration {

  def load[F[_]: Async]: F[HttpConfig] =
    env("CO_ENV")
      .as[Environments]
      .flatMap {
        case Local => parse
        case Live  => parse
      }
      .load[F]
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial")) // false positive with host & port interpolator
  private def parse[F[_]]: ConfigValue[F, HttpConfig] =
    (
      env("CO_TWITTER_CONSUMER_TOKEN_KEY").as[TwitterConsumerTokenKey],
      env("CO_TWITTER_CONSUMER_TOKEN_SECRET").as[TwitterConsumerTokenSecret].secret,
      env("CO_TWITTER_ACCESS_TOKEN_KEY").as[TwitterAccessTokenKey],
      env("CO_TWITTER_ACCESS_TOKEN_SECRET").as[TwitterAccessTokenSecret].secret,
      env("CO_GITHUB_TOKEN").as[GitHubToken].secret
    ).parMapN {
      (
          twitterConsumerTokenKey,
          twitterConsumerTokenSecret,
          twitterAccessTokenKey,
          twitterAccessTokenSecret,
          githubToken
      ) =>
        configuration.HttpConfig(
          twitterConsumerTokenKey,
          twitterConsumerTokenSecret,
          twitterAccessTokenKey,
          twitterAccessTokenSecret,
          githubToken,
          httpServerConfig = HttpServerConfig(
            host = host"0.0.0.0",
            port = port"8080"
          )
        )
    }
}
