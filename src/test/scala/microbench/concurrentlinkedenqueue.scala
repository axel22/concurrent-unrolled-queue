/* 4 threads, 4'000'000 elements / thread :
 * 1333 1192 1294 1295 1293 1294 1282 1291 1291 1294 1293 1299 1291 1297 1295 1294 1295 1294 1293 1290 1292 1292 1289 1290 1295 1292 1293 1294 1293 1295 1292 1294 1291 1293 1293 1293 1294 1294 1295 1296
 *
 * 8 threads, 4'000'000 elements / thread :
 * 2745 2683 2660 2679 2680 2679 2674 2676 2677 2673 2673 2667 2672 2672 2680 2671 2671 2675 2671 2665 2672 2676 2662 2673 2678 2672 2676 2669 2671 2666 2671 2670 2672 2668 2674 2663 2677 2681 2672 2666
 */


package microbench
import scala.testing.Benchmark
import java.util.concurrent.ConcurrentLinkedQueue

class LinkedEnqueueThread(queue: ConcurrentLinkedQueue[AnyRef], nEnqueues: Int) extends java.lang.Thread {

  override def run(): Unit = {
    val OBJ = new AnyRef()
    val queue = this.queue
    val nEnqueues = this.nEnqueues
    var i = 0

    while (i < nEnqueues) {
      queue.offer(OBJ)
      i += 1
    }
  }

}

object concurrentlinkedenqueue extends Benchmark {
  val nThreads = sys.props("bench.threads").toInt
  val nElementsPerThread = sys.props("bench.elements").toInt / nThreads
  var queue: ConcurrentLinkedQueue[AnyRef] = null
  var threads: List[java.lang.Thread] = null

  override def setUp() = {
    queue = new ConcurrentLinkedQueue[AnyRef]
    threads = List.range(0, nThreads).map { _ => new LinkedEnqueueThread(queue, nElementsPerThread) }
  }

  override def run() = {
    threads foreach { _.start }
    threads foreach { _.join }
  }

  override def tearDown() = {
    queue = null
    threads = null
  }

}

