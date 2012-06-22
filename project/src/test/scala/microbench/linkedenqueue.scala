package microbench

import java.util.concurrent.ConcurrentLinkedQueue
import scala.testing.Benchmark

object linkedenqueue extends Benchmark {

  var queue: ConcurrentLinkedQueue[AnyRef] = null
  val totalElements = sys.props("bench.elements").toInt

  override def setUp() = {
    queue = new ConcurrentLinkedQueue[AnyRef]
  }

  override def run = {
    val totalElements = this.totalElements
    val queue = this.queue
    val obj = new AnyRef()

    var i = 0
    while (i < totalElements) {
      queue.offer(obj)
      i += 1
    }
  }

  override def tearDown() = {
    queue = null
  }

}

