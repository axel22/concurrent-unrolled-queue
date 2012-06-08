package microbench

import scala.concurrent.ConcurrentUnrolledQueue
import scala.testing.Benchmark

class EnqueueThread(queue: ConcurrentUnrolledQueue[AnyRef], nElements: Int) extends Thread {

  override def run(): Unit = {
    val obj = new AnyRef()
    val queue = this.queue
    val nElements = this.nElements
    var i = 0

    while (i < nElements) {
      queue.enqueue(obj)
      i += 1
    }
  }

}

object concurrentenqueue extends Benchmark {

  val nThreads = sys.props("bench.threads").toInt
  val totalElements = sys.props("bench.elements").toInt

  var queue: ConcurrentUnrolledQueue[AnyRef] = null
  var threads: IndexedSeq[Thread] = null

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]
    threads = for (threadid <- 0 until nThreads)
        yield new EnqueueThread(queue, totalElements / nThreads)
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

