package connect.clients

import cats.effect.Async
import cats.syntax.applicative._
import cats.syntax.flatMap._
import com.danielasfregola.twitter4s.http.clients.rest.friendships.TwitterFriendshipClient
import connect.domain.{ Connection, twitter }
import connect.domain.twitter.{ Friendship, User }

trait Twitter[F[_]] {
  def showFriendship(sourceUser: User, targetUser: User): F[Friendship]
}

object Twitter {
  def apply[F[_]: Twitter]: Twitter[F] = implicitly

  // TODO create a twitter response class to handle errors, similar to GHResponse
  def apply[F[_]: Async](client: TwitterFriendshipClient): Twitter[F] = {
    case (sourceUser: User, targetUser: User) =>
      Async[F].fromFuture(
        client
          .relationshipBetweenUsers(sourceUser.screenName.value.toString, targetUser.screenName.value.toString)
          .pure
      ) flatMap { ratedData =>
        val sourceRelationship = ratedData.data.relationship.source
        val targetRelationship = ratedData.data.relationship.target

        Async[F].fromEither(
          for {
            sourceUser <- User(sourceRelationship.screen_name)
            targetUser <- User(targetRelationship.screen_name)
            connection = sourceRelationship.following && targetRelationship.following
          } yield twitter.Friendship(
            source = sourceUser,
            target = targetUser,
            connection = Connection.bool(connection)
          )
        )
      }
  }
}
