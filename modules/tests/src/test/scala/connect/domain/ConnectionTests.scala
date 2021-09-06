package connect.domain

import connect.generators._
import monocle.law.discipline._
import weaver.FunSuite
import weaver.discipline.Discipline

object ConnectionTests extends FunSuite with Discipline {

  checkAll("Iso[Connection]", IsoTests(Connection.bool))
}
