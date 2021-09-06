package connect

import connect.domain.github.{ Login, Organization }
import connect.domain.{ Connection, UserHandle }
import eu.timepit.refined.scalacheck.string._
import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{ Arbitrary, Gen }

object generators {

  val userHandle: Gen[UserHandle] = arbitrary[NonEmptyString].map(UserHandle.apply)
  val organizations: Gen[Set[Organization]] =
    Gen.listOfN(4, Gen.stringOfN(10, Gen.alphaNumChar)).map(orgs => orgs.map(org => Organization(Login(org))).toSet)

  val genCheckConnection: Gen[(UserHandle, UserHandle, Set[Organization])] = for {
    sourceHandle  <- userHandle
    targetHandle  <- userHandle
    organizations <- organizations
  } yield (sourceHandle, targetHandle, organizations)

  implicit val connection: Arbitrary[Connection] = Arbitrary(Gen.oneOf(Connection.Connected, Connection.NotConnected))
}
