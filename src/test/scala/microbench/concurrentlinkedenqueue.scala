package microbench

import java.util.concurrent.ConcurrentLinkedQueue
import scala.testing.Benchmark

class LinkedEnqueueThread(queue: ConcurrentLinkedQueue[AnyRef], nElements: Int) extends Thread {

  override def run(): Unit = {
    val obj = new AnyRef()
    val queue = this.queue
    val nElements = this.nElements
    var i = 0

    while (i < nElements) {
      queue.offer(obj)
      i += 1
    }
  }

}

object concurrentlinkedenqueue extends Benchmark {

  val nThreads = sys.props("bench.threads").toInt
  val totalElements = sys.props("bench.elements").toInt

  var queue: ConcurrentLinkedQueue[AnyRef] = null
  var threads: IndexedSeq[Thread] = null

  override def setUp() = {
    queue = new ConcurrentLinkedQueue[AnyRef]
    threads = for (threadid <- 0 until nThreads)
        yield new LinkedEnqueueThread(queue, totalElements / nThreads)
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

