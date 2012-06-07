package test

import org.scalatest.FlatSpec
import scala.concurrent.ConcurrentUnrolledQueue

class ConcurrentAddSpec extends FlatSpec {

  "A concurrent queue" should "retrieve the exact same set of data that was added by multiple threads" in {
    val queue = new ConcurrentUnrolledQueue[Int]

    concurrentEnqueue(queue)

    assert(!queue.isEmpty)
    assert(queue.size() == WRITERS * ELEMENTS_PER_THREAD)
    assert(queue2Set(queue) == expectedSet())
  }

  def concurrentEnqueue(queue: ConcurrentUnrolledQueue[Int]) = {
    (for (threadid <- 0 until WRITERS) yield
      new Thread(new Runnable() {
        override def run()= {
          threadSet(threadid) foreach { queue enqueue _ }
        }
      })
    ) map { t => t.start(); t } foreach { _ join }
  }

  def threadSet(threadid: Int) = {
    val offset = threadid * ELEMENTS_PER_THREAD
    Iterator.range(1 + offset, ELEMENTS_PER_THREAD + 1 + offset)
  }

  def expectedSet() = {
    Range(1, WRITERS * ELEMENTS_PER_THREAD + 1).toSet
  }

  val WRITERS = 5

  val ELEMENTS_PER_THREAD = 200000

  def queue2Set[A](queue: ConcurrentUnrolledQueue[A]) = {
    val retrievedSet = new collection.mutable.HashSet[A]()
    while (!queue.isEmpty()) {
      retrievedSet += queue.dequeue()
    }

    retrievedSet
  }

}

