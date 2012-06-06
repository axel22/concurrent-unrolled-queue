package microbench

import java.util.concurrent.ConcurrentLinkedQueue
import scala.testing.Benchmark

object linkeddequeue extends Benchmark {

  val nElements = sys.props("bench.elements").toInt
  val OBJ = new AnyRef()
  var queue: ConcurrentLinkedQueue[AnyRef] = null

  override def setUp() = {
    queue = new ConcurrentLinkedQueue[AnyRef]

    val obj = OBJ
    var i = 0
    while (i < nElements) {
      queue.offer(obj)
      i += 1
    }
  }

  override def run() = {
    var i = 0
    while (i < nElements) {
      queue.poll()
      i += 1
    }
  }

  override def tearDown() = {
    queue = null
  }

}

