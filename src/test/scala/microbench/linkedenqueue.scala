package microbench
import scala.testing.Benchmark
import java.util.concurrent.ConcurrentLinkedQueue

object linkedenqueue extends Benchmark {
  var clQUEUE: ConcurrentLinkedQueue[AnyRef] = null
  val OBJ = new AnyRef

  override def setUp() = {
    clQUEUE = new ConcurrentLinkedQueue[AnyRef]
  }

  override def run = {
    val inserts = 20000000
    val clQueue = clQUEUE
    val obj = OBJ
    var i = 0
    while (i < inserts) {
      clQueue.offer(obj)
      i += 1
    }
  }

  override def tearDown() = {
    clQUEUE = null
  }

}

