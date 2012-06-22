package test

import org.scalatest.FlatSpec
import scala.collection.immutable.HashSet
import scala.concurrent.ConcurrentUnrolledQueue

class ConcurrentRemoveSpec extends FlatSpec {

  "A concurrent queue" should "concurrently dequeue the same set of elements that were initially enqueued" in {
    val queue = new ConcurrentUnrolledQueue[Int]()
    fillQueue(queue)
    val retrievedSet = concurrentDequeue(queue)
    
    assert(retrievedSet == expectedSet)
    assert(queue.isEmpty)
    assert(queue.size() == 0)
  }

  def concurrentDequeue(queue: ConcurrentUnrolledQueue[Int]) = {
    val results = new Array[IndexedSeq[Int]](READERS)

    (for (threadid <- 0 until READERS) yield
      new Thread(new Runnable() {
        override def run() = {
          results(threadid) = (for (i <- 0 until ELEMENTS_PER_THREAD) yield {
            queue.dequeue()
          })
        }
      })
    ).map { t => t.start; t }.foreach { _.join }

    results.foldLeft(new HashSet[Int]())((res1, res2) => res1 ++ res2)
  }

  def fillQueue(queue: ConcurrentUnrolledQueue[Int]) = {
    expectedSet().foreach { e => queue.enqueue(e) }
  }

  val ELEMENTS_PER_THREAD = 200000
  val READERS = 5

  def expectedSet() = {
    Range(1, READERS * ELEMENTS_PER_THREAD + 1).toSet
  }

}

