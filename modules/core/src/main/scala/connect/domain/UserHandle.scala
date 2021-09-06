package connect.domain

import cats.Eq
import cats.syntax.either._
import eu.timepit.refined.types.string.NonEmptyString

import scala.util.control.NoStackTrace

final case class UserHandle private (value: NonEmptyString)

object UserHandle {
  implicit def eq: Eq[UserHandle] = Eq.fromUniversalEquals[UserHandle]

  def apply(string: String): Either[NoStackTrace, UserHandle] = {
    NonEmptyString
      .from(string)
      .bimap(
        _ => InvalidUserHandle("An empty string is not a valid user name in GitHub"),
        handle => UserHandle(handle)
      )
  }
}

final case class InvalidUserHandle private (cause: String) extends NoStackTrace
