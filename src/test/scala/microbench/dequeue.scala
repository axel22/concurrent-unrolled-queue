package microbench

import scala.concurrent.ConcurrentUnrolledQueue
import scala.testing.Benchmark

object dequeue extends Benchmark {

  val nElements = sys.props("bench.elements").toInt
  val OBJ = new AnyRef()
  var queue: ConcurrentUnrolledQueue[AnyRef] = null

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]

    val obj = OBJ
    var i = 0
    while (i < nElements) {
      queue.enqueue(obj)
      i += 1
    }
  }

  override def run() = {
    var i = 0
    while (i < nElements) {
      queue.dequeue()
      i += 1
    }
  }

  override def tearDown() = {
    queue = null
  }

}

