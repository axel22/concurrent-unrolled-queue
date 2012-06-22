package microbench

import scala.concurrent.ConcurrentUnrolledQueue
import scala.testing.Benchmark

object dequeue extends Benchmark {

  val totalElements = sys.props("bench.elements").toInt
  val OBJ = new AnyRef()
  var queue: ConcurrentUnrolledQueue[AnyRef] = null

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]

    val obj = OBJ
    var i = 0
    while (i < totalElements) {
      queue.enqueue(obj)
      i += 1
    }
  }

  override def run() = {
    val queue = this.queue
    val totalElements = this.totalElements

    var i = 0
    while (i < totalElements) {
      queue.dequeue()
      i += 1
    }
  }

  override def tearDown() = {
    queue = null
  }

}

