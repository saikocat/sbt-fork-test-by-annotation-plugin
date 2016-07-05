package testing

import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers
import java.io.File

trait Ensemble extends FlatSpec with MustMatchers {
  def i: Int
  def prefix = System.getProperty("group.prefix")

  "an ensemble" must "create all files" in {
    val f = new File(prefix + i)
    f.createNewFile
  }
}


// annotation is not inherited in trait so we have to manual tag each of them
@tags.RequiresSpark
class Ensemble1 extends Ensemble { def i = 1 }

@tags.RequiresSpark
class Ensemble2 extends Ensemble { def i = 2 }

@tags.RequiresSpark
class Ensemble3 extends Ensemble { def i = 3 }
