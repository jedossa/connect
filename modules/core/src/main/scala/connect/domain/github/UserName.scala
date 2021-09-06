package connect.domain.github

import cats.syntax.either._
import eu.timepit.refined.types.string.NonEmptyString

import scala.util.control.NoStackTrace

final case class UserName private (value: NonEmptyString)

object UserName {
  def apply(string: String): Either[NoStackTrace, UserName] = {
    NonEmptyString
      .from(string)
      .bimap(
        _ => InvalidUserName("An empty string is not a valid user name in GitHub"),
        userName => UserName(userName)
      )
  }
}

final case class InvalidUserName private (cause: String) extends NoStackTrace
