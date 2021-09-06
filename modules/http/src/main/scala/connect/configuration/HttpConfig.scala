package connect.configuration

import ciris._
import com.comcast.ip4s.{ Host, Port }
import types._
import eu.timepit.refined.types.string.NonEmptyString

object types {
  type TwitterConsumerTokenKey    = NonEmptyString
  type TwitterConsumerTokenSecret = NonEmptyString
  type TwitterAccessTokenKey      = NonEmptyString
  type TwitterAccessTokenSecret   = NonEmptyString
  type GitHubToken                = NonEmptyString
}

final case class HttpServerConfig(
    host: Host,
    port: Port
)

final case class HttpConfig(
    twitterConsumerTokenKey: TwitterConsumerTokenKey,
    twitterConsumerTokenSecret: Secret[TwitterConsumerTokenSecret],
    twitterAccessTokenKey: TwitterAccessTokenKey,
    twitterAccessTokenSecret: Secret[TwitterAccessTokenSecret],
    githubToken: Secret[GitHubToken],
    httpServerConfig: HttpServerConfig
)
