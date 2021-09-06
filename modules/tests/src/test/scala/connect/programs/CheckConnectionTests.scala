package connect.programs

import cats.Show
import cats.effect._
import connect.clients.{ GitHub, Twitter }
import connect.domain.github.{ Organization, UserName }
import connect.domain.twitter.{ Friendship, User }
import connect.domain.{ Connection, ConnectionDetails, UserHandle }
import connect.generators._
import connect.modules.Caches
import github4s.GHError
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import scalacache.Cache
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object CheckConnectionTests extends SimpleIOSuite with Checkers {

  private[this] lazy val caches                                           = Caches.apply
  private[this] implicit val noLogger: SelfAwareStructuredLogger[IO]      = NoOpLogger[IO]
  private[this] implicit val friendshipCache: Cache[Friendship]           = caches.friendshipCache
  private[this] implicit val organizationsCache: Cache[Set[Organization]] = caches.organizationsCache

  private[this] implicit val userShow: Show[UserHandle]           = _.value.value
  private[this] implicit val organizationShow: Show[Organization] = _.login.value

  def sameUserTwitter: Twitter[IO] =
    (sourceUser: User, _: User) => IO.pure(Friendship(sourceUser, sourceUser, Connection.Connected))

  def connectedTwitter: Twitter[IO] =
    (sourceUser: User, targetUser: User) => IO.pure(Friendship(sourceUser, targetUser, Connection.Connected))

  def notConnectedTwitter: Twitter[IO] =
    (sourceUser: User, targetUser: User) => IO.pure(Friendship(sourceUser, targetUser, Connection.NotConnected))

  def failedTwitter: Twitter[IO] =
    (_: User, _: User) =>
      IO.raiseError(new IllegalArgumentException("requirement failed: please, provide at least one screen name"))

  def githubOrgs(orgs: Set[Organization]): GitHub[IO] = (_: UserName) => IO.pure(orgs)

  def githubNoOrgs: GitHub[IO] = (_: UserName) => IO.pure(Set.empty)

  def failedGitHub: GitHub[IO] = (_: UserName) => IO.raiseError(new GHError.NotFoundError("resource not found", ""))

  test("same user") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, organizations) =>
        CheckConnection[IO](sameUserTwitter, githubOrgs(organizations))
          .fullyConnected(sourceHandle, targetHandle)
          .map(details => expect.same(details, ConnectionDetails(Connection.Connected, organizations)))
    }
  }

  test("fully connected users") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, organizations) =>
        CheckConnection[IO](connectedTwitter, githubOrgs(organizations))
          .fullyConnected(sourceHandle, targetHandle)
          .map(details => expect.same(details, ConnectionDetails(Connection.Connected, organizations)))
    }
  }

  test("partially connected users - twitter") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, _) =>
        CheckConnection[IO](connectedTwitter, githubNoOrgs)
          .fullyConnected(sourceHandle, targetHandle)
          .map(details => expect.same(details, ConnectionDetails(Connection.NotConnected, Set.empty)))
    }
  }

  test("partially connected users - github") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, organizations) =>
        CheckConnection[IO](notConnectedTwitter, githubOrgs(organizations))
          .fullyConnected(sourceHandle, targetHandle)
          .map(details => expect.same(details, ConnectionDetails(Connection.NotConnected, Set.empty)))
    }
  }

  test("not connected users") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, _) =>
        CheckConnection[IO](notConnectedTwitter, githubNoOrgs)
          .fullyConnected(sourceHandle, targetHandle)
          .map(details => expect.same(details, ConnectionDetails(Connection.NotConnected, Set.empty)))
    }
  }

  test("twitter bad request") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, organizations) =>
        CheckConnection[IO](failedTwitter, githubOrgs(organizations))
          .fullyConnected(sourceHandle, targetHandle)
          .attempt
          .map {
            case Left(_: IllegalArgumentException) => success
            case _                                 => failure("exception expected")
          }
    }
  }

  test("github not found") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, _) =>
        CheckConnection[IO](connectedTwitter, failedGitHub)
          .fullyConnected(sourceHandle, targetHandle)
          .attempt
          .map {
            case Left(_: GHError.NotFoundError) => success
            case _                              => failure("exception expected")
          }
    }
  }

  test("just failures") {
    forall(genCheckConnection) {
      case (sourceHandle, targetHandle, _) =>
        CheckConnection[IO](failedTwitter, failedGitHub)
          .fullyConnected(sourceHandle, targetHandle)
          .attempt
          .map {
            case Left(_) => success
            case _       => failure("exception expected")
          }
    }
  }
}
