package microbench

import scala.concurrent.ConcurrentUnrolledQueue
import scala.testing.Benchmark

object enqueue extends Benchmark {

  var queue: ConcurrentUnrolledQueue[AnyRef] = null
  val totalElements = sys.props("bench.elements").toInt

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]
  }

  override def run = {
    val totalElements = this.totalElements
    val queue = this.queue
    val obj = new AnyRef()

    var i = 0
    while (i < totalElements) {
      queue.enqueue(obj)
      i += 1
    }
  }

  override def tearDown() = {
    queue = null
  }

}

