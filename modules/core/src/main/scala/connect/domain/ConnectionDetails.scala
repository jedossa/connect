package connect.domain

import connect.domain.github.Organization

final case class ConnectionDetails private (connection: Connection, organization: Set[Organization])
