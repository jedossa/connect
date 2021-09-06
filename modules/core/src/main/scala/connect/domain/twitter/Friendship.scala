package connect.domain.twitter

import connect.domain.Connection

final case class Friendship private (source: User, target: User, connection: Connection)
