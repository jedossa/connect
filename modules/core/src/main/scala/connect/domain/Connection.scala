package connect.domain

import cats.Eq
import monocle.Iso

sealed trait Connection

object Connection {
  final case object Connected    extends Connection
  final case object NotConnected extends Connection

  val bool: Iso[Connection, Boolean] = Iso[Connection, Boolean] {
    case Connected    => true
    case NotConnected => false
  }(if (_) Connected else NotConnected)

  implicit def eq: Eq[Connection] = Eq.fromUniversalEquals[Connection]
}
