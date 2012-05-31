package microbench
import scala.testing.Benchmark
import concurrent.ConcurrentUnrolledQueue

object enqueue extends Benchmark {
  var cuQUEUE: ConcurrentUnrolledQueue[AnyRef] = null
  val OBJ = new AnyRef

  override def setUp() = {
    cuQUEUE = new ConcurrentUnrolledQueue[AnyRef]
  }

  override def run = {
    val inserts = 20000000
    val cuQueue = cuQUEUE
    val obj = OBJ
    var i = 0
    while (i < inserts) {
      cuQueue.enqueue(obj)
      i += 1
    }
  }

  override def tearDown() = {
    cuQUEUE = null
  }

}

