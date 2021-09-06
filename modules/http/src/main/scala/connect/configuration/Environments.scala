package connect.configuration

import enumeratum.EnumEntry._
import enumeratum._

sealed abstract class Environments extends EnumEntry with Lowercase

object Environments extends Enum[Environments] with CirisEnum[Environments] {
  case object Local extends Environments
  case object Live  extends Environments

  val values: IndexedSeq[Environments] = findValues
}
