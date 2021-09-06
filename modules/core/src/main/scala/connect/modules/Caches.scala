package connect.modules

import connect.domain.github.Organization
import connect.domain.twitter.Friendship
import scalacache.Cache
import scalacache.caffeine._

sealed trait Caches {
  def friendshipCache: Cache[Friendship]
  def organizationsCache: Cache[Set[Organization]]
}

object Caches {
  // TODO handle cache lifecycle with brackets
  def apply: Caches = new Caches {
    override def friendshipCache: Cache[Friendship] = CaffeineCache[Friendship]

    override def organizationsCache: Cache[Set[Organization]] = CaffeineCache[Set[Organization]]
  }
}
