package microbench

import java.util.concurrent.ConcurrentLinkedQueue
import scala.testing.Benchmark

class LinkedDequeueThread(queue: ConcurrentLinkedQueue[AnyRef], nElements: Int) extends Thread {

  override def run(): Unit = {
    val queue = this.queue
    val nElements = this.nElements

    var i = 0
    while (i < nElements) {
      queue.poll()
      i += 1
    }
  }

}

object concurrentlinkeddequeue extends Benchmark {

  val nThreads = sys.props("bench.threads").toInt
  val totalElements = sys.props("bench.elements").toInt

  var queue: ConcurrentLinkedQueue[AnyRef] = null
  var threads: IndexedSeq[Thread] = null

  override def setUp() = {
    queue = new ConcurrentLinkedQueue[AnyRef]

    val obj = new AnyRef
    for (i <- 0 until totalElements) {
      queue.offer(obj)
    }

    threads = for (threadid <- 0 until nThreads)
        yield new LinkedDequeueThread(queue, totalElements / nThreads)
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

