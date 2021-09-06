package connect.domain.github

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

// TODO validate organization creation according to github's documentation
final case class Organization private (login: Login)

final case class Login private (value: String)

object Organization {
  implicit val decodeOrganization: Decoder[Organization] = deriveDecoder[Organization]
}

object Login {
  implicit val decodeLogin: Decoder[Login] = deriveDecoder[Login]
}
