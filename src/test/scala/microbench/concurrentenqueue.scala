package microbench
import scala.testing.Benchmark
import concurrent.ConcurrentUnrolledQueue

class EnqueueThread(queue: ConcurrentUnrolledQueue[AnyRef], nEnqueues: Int) extends java.lang.Thread {

  override def run(): Unit = {
    val OBJ = new AnyRef()
    val queue = this.queue
    val nEnqueues = this.nEnqueues
    var i = 0

    while (i < nEnqueues) {
      queue.enqueue(OBJ)
      i += 1
    }
  }

}

object concurrentenqueue extends Benchmark {
  val nThreads = sys.props("bench.threads").toInt
  val nElementsPerThread = sys.props("bench.elements").toInt / nThreads
  var queue: ConcurrentUnrolledQueue[AnyRef] = null
  var threads: List[java.lang.Thread] = null

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]
    threads = List.range(0, nThreads).map { _ => new EnqueueThread(queue, nElementsPerThread) }
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

