package test

import org.scalatest.FlatSpec
import scala.collection.immutable.HashSet
import scala.concurrent.ConcurrentUnrolledQueue

class ConcurrentAddRemoveSpec extends FlatSpec {

  val queue = new ConcurrentUnrolledQueue[Int]()
  var remainingElements: Set[Int] = _

  "A concurrent queue" should "concurrently dequeue at most as many elements that were concurrently enqueued" in {
    val enqueueThreads = concurrentEnqueue(queue)
    val threadsResults = concurrentDequeue(queue)
    val dequeueThreads = threadsResults._1
    val dequeueResults = threadsResults._2

    enqueueThreads.foreach { _.join }
    dequeueThreads.foreach { _.join }

    val enqueuedSet = enqueuedElements()
    val dequeuedSet = dequeueResults.foldLeft(new collection.immutable.HashSet[Int]())((acc, res) => acc ++ res)

    assert(enqueuedSet.size >= dequeuedSet.size)
    assert(queue.size() == enqueuedSet.size - dequeuedSet.size)

    remainingElements = enqueuedSet -- dequeuedSet
  }

  it should "still contain the elements that were not enqueued" in {
    assert(queue2Set(queue) == remainingElements)
  }

  def concurrentEnqueue(queue: ConcurrentUnrolledQueue[Int]) = {
    (for (threadid <- 0 until WRITERS) yield
      new Thread(new Runnable() {
        override def run(): Unit = {
          threadSet(threadid) foreach { queue enqueue _ }
        }
      })
    ) map { t => t.start(); t }
  }

  def concurrentDequeue(queue: ConcurrentUnrolledQueue[Int]) = {
    val results = new Array[collection.mutable.HashSet[Int]](READERS)

    val dequeueThreads = (for (threadid <- 0 until READERS) yield
      new Thread(new Runnable() {
        override def run() = {
          var i = 0
          val readSet = new collection.mutable.HashSet[Int]()
          while (i < (ELEMENTS_PER_THREAD * WRITERS) / READERS) {
            try {
              readSet += queue.dequeue()
            } catch {
              case e : NoSuchElementException => ()
            }

            i += 1
          }
          results(threadid) = readSet
        }
      })
    ).map { t => t.start; t }

    (dequeueThreads, results)
  }

  def enqueuedElements() = {
    Range(1, WRITERS * ELEMENTS_PER_THREAD + 1).toSet
  }

  def threadSet(threadid: Int) = {
    val offset = threadid * ELEMENTS_PER_THREAD
    Iterator.range(1 + offset, ELEMENTS_PER_THREAD + 1 + offset)
  }

  def queue2Set[A](queue: ConcurrentUnrolledQueue[A]) = {
    val retrievedSet = new collection.mutable.HashSet[A]()
    while (!queue.isEmpty()) {
      retrievedSet += queue.dequeue()
    }

    retrievedSet
  }

  val READERS = 4
  val WRITERS = 4
  val ELEMENTS_PER_THREAD = 200000

}

