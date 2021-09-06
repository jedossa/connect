package connect.programs

import cats.MonadThrow
import cats.effect.syntax.spawn._
import cats.effect.{ Async, Spawn }
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._
import connect.clients.{ GitHub, Twitter }
import connect.domain.Connection.{ Connected, NotConnected }
import connect.domain.github.{ Organization, UserName => GitHubUser }
import connect.domain.twitter.{ Friendship, User => TwitterUser }
import connect.domain.{ ConnectionDetails, UserHandle }
import github4s.GHError
import org.typelevel.log4cats.Logger
import scalacache.Cache

import scala.concurrent.CancellationException

final case class CheckConnection[F[_]: Async: Logger] private (
    private val twitter: Twitter[F],
    private val gitHub: GitHub[F]
)(implicit friendshipCache: Cache[Friendship], organizationsCache: Cache[Set[Organization]]) {

  import CheckConnection._

  // TODO make external call cancellation-safe and add retries with full jitter for 5XX errors
  // TODO make caches' TTLs configurable, handle cache as ResourceF
  def fullyConnected(sourceHandle: UserHandle, targetHandle: UserHandle): F[ConnectionDetails] = {

    // TODO Fix binary imcompatibility with CE3. Create new instances
    // implicit val cacheMode: Mode[F] = scalacache.CatsEffect.modes.async
    val _ = friendshipCache
    val _ = organizationsCache

    val isTheSamePerson = sourceHandle === targetHandle

    val sourceTwitterUser = TwitterUser(sourceHandle.value)
    val targetTwitterUser = TwitterUser(targetHandle.value)
    val showFriendship =
      if (isTheSamePerson) Friendship(sourceTwitterUser, targetTwitterUser, Connected).pure
      else twitter.showFriendship(sourceTwitterUser, targetTwitterUser)
    // cachingF(sourceTwitterUser)(15.minutes.some)(twitter.showFriendship(sourceTwitterUser, targetTwitterUser))

    val sourceGitHubHandle = GitHubUser(sourceHandle.value)
    val targetGitHubHandle = GitHubUser(targetHandle.value)
    val getSourceOrgs      = gitHub.getOrganizations(sourceGitHubHandle) handleErrorWith ghNotFoundHandler(sourceHandle)
    /*  cachingF(sourceGitHubHandle)(15.minutes.some)(
        gitHub.getOrganizations(sourceGitHubHandle) handleErrorWith ghNotFoundHandler(sourceHandle)
      ) */

    val getTargetOrgs =
      if (isTheSamePerson) Set.empty[Organization].pure
      else gitHub.getOrganizations(targetGitHubHandle) handleErrorWith ghNotFoundHandler(sourceHandle)
    /* cachingF(targetGitHubHandle)(15.minutes.some)(
          gitHub.getOrganizations(targetGitHubHandle) handleErrorWith ghNotFoundHandler(sourceHandle)
        ) */

    for {
      friendshipFiber          <- showFriendship.start
      organizationsFiber       <- Spawn[F].both(getSourceOrgs, getTargetOrgs).start
      friendship               <- friendshipFiber.joinWith(onCancelHandler("Twitter"))
      (sourceOrgs, targetOrgs) <- organizationsFiber.joinWith(onCancelHandler("GitHub"))
      commonOrgs = sourceOrgs intersect targetOrgs
    } yield friendship.connection match {
      case Connected if commonOrgs.nonEmpty => ConnectionDetails(Connected, commonOrgs)
      case Connected                        => ConnectionDetails(NotConnected, Set.empty)
      case NotConnected                     => ConnectionDetails(NotConnected, Set.empty)
    }
  }

}

object CheckConnection {

  // TODO make services a sum type and derive them a show instance
  def onCancelHandler[F[_]: MonadThrow: Logger, A](service: String): F[A] =
    Logger[F].error(s"$service call was cancelled") *> MonadThrow[F].raiseError(new CancellationException)

  // TODO add error handlers for twitter and the other error types
  def ghNotFoundHandler[F[_]: MonadThrow: Logger, A](userHandle: UserHandle): Throwable => F[A] = {
    case error: GHError.NotFoundError =>
      Logger[F].error(s"${userHandle.value.value} is not a valid user in github") *> MonadThrow[F].raiseError(error)
    case error =>
      Logger[F].error(error)(s"github call failed with ${error.getMessage}") *> MonadThrow[F].raiseError(error)
  }
}
