package microbench

import scala.testing.Benchmark
import concurrent.ConcurrentUnrolledQueue

class DequeueThread(queue: ConcurrentUnrolledQueue[AnyRef], nElements: Int) extends java.lang.Thread {

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
  val nElementsPerThread = sys.props("bench.elements").toInt / nThreads
  var queue: ConcurrentUnrolledQueue[AnyRef] = null
  var threads: List[java.lang.Thread] = null

  override def setUp() = {
    queue = new ConcurrentUnrolledQueue[AnyRef]

    val obj = new AnyRef
    var i = 0
    while (i < nElementsPerThread * nThreads) {
      queue.enqueue(obj)
      i += 1
    }

    threads = List.range(0, nThreads).map { _ => new DequeueThread(queue, nElementsPerThread) }
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

