package microbench

import scala.testing.Benchmark
import concurrent.ConcurrentUnrolledQueue

object enqueue extends Benchmark {

  var cuQUEUE: ConcurrentUnrolledQueue[AnyRef] = null
  val OBJ = new AnyRef
  val nElements = sys.props("bench.elements").toInt

  override def setUp() = {
    cuQUEUE = new ConcurrentUnrolledQueue[AnyRef]
  }

  override def run = {
    val nElements = this.nElements
    val cuQueue = cuQUEUE
    val obj = OBJ
    var i = 0
    while (i < nElements) {
      cuQueue.enqueue(obj)
      i += 1
    }
  }

  override def tearDown() = {
    cuQUEUE = null
  }

}

