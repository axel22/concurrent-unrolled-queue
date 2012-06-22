package microbench

import scala.testing.Benchmark
import concurrent.ConcurrentUnrolledQueue

class DequeueThread(queue: ConcurrentUnrolledQueue[AnyRef], nElements: Int) extends Thread {

  override def run(): Unit = {
    val queue = this.queue
    val nElements = this.nElements

    var i = 0
    while (i < nElements) {
      queue.dequeue()
      i += 1
    }
  }

}

object concurrentdequeue extends Benchmark {

  val nThreads = sys.props("bench.threads").toInt
  val totalElements = sys.props("bench.elements").toInt

  var queue: ConcurrentUnrolledQueue[AnyRef] = null
  var threads: IndexedSeq[Thread] = null

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]

    val obj = new AnyRef
    for (i <- 0 until totalElements) {
      queue.enqueue(obj)
    }

    threads = for (threadid <- 0 until nThreads)
        yield new DequeueThread(queue, totalElements / nThreads)
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

