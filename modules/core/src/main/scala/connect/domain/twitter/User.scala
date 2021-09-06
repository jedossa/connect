package connect.domain.twitter

import eu.timepit.refined.types.string.NonEmptyString

import scala.util.control.NoStackTrace

import cats.syntax.either._

final case class User private (screenName: ScreenName)

object User {
  def apply(string: String): Either[NoStackTrace, User] =
    NonEmptyString
      .from(string)
      .bimap(
        _ => InvalidUserName("An empty string is not a valid user name in Twitter"),
        screenName => User(ScreenName(screenName))
      )

  def apply(nonEmptyString: NonEmptyString): User = User(ScreenName(nonEmptyString))
}

final case class ScreenName private (value: NonEmptyString)

final case class InvalidUserName private (cause: String) extends NoStackTrace
