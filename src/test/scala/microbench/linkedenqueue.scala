package microbench
import scala.testing.Benchmark
import java.util.concurrent.ConcurrentLinkedQueue

object linkedenqueue extends Benchmark {
  var clQUEUE: ConcurrentLinkedQueue[AnyRef] = null
  val OBJ = new AnyRef
  val nElements = sys.props("bench.elements").toInt

  override def setUp() = {
    clQUEUE = new ConcurrentLinkedQueue[AnyRef]
  }

  override def run = {
    val nElements = this.nElements
    val clQueue = clQUEUE
    val obj = OBJ
    var i = 0
    while (i < nElements) {
      clQueue.offer(obj)
      i += 1
    }
  }

  override def tearDown() = {
    clQUEUE = null
  }

}

