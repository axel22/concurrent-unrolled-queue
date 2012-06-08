package microbench

import java.util.concurrent.ConcurrentLinkedQueue
import scala.testing.Benchmark

class LinkedDequeueThread(queue: ConcurrentLinkedQueue[AnyRef], nElements: Int) extends java.lang.Thread {

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
  val nElementsPerThread = sys.props("bench.elements").toInt / nThreads
  var queue: ConcurrentLinkedQueue[AnyRef] = null
  var threads: List[java.lang.Thread] = null

  override def setUp() = {
    queue = new ConcurrentLinkedQueue[AnyRef]

    val obj = new AnyRef
    var i = 0
    while (i < nElementsPerThread * nThreads) {
      queue.offer(obj)
      i += 1
    }

    threads = List.range(0, nThreads).map { _ => new LinkedDequeueThread(queue, nElementsPerThread) }
    println("--------- start")
  }

  override def run() = {
    threads foreach { _.start }
    threads foreach { _.join }
  }

  override def tearDown() = {
    println("--------- end")
    queue = null
    threads = null
  }

}

