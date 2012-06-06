package test

import scala.concurrent.ConcurrentUnrolledQueue
import org.scalatest.FlatSpec

class AddRemoveSpec extends FlatSpec {

  val queue = new ConcurrentUnrolledQueue[Int]

  "A queue" should "be of size 0 when it contains no elements" in {
    assert(queue.size() == 0)
  }

  it should "be empty when it contains no elements" in {
    assert(queue.isEmpty())
  }

  it should "be of size N after enqueuing N elements" in {
    queue.enqueue(1)
    queue.enqueue(2)
    assert(!queue.isEmpty())
    assert(queue.size() == 2)
  }

  it should "be of size 0 after dequeuing these N elements" in {
    queue.dequeue()
    queue.dequeue()
    assert(queue.isEmpty())
    assert(queue.size() == 0)
  }

  it should "retrieve the same elements that were added, in the same order they were added" in {
    val queue = new ConcurrentUnrolledQueue[Int]
    val nElements = 10000
    val list = List.range(0, nElements)
    list.foreach { x => queue.enqueue(x) }
    var retrieved: List[Int] = Nil

    var i = 0
    while (i < nElements) {
      retrieved ::= queue.dequeue()
      i += 1
    }
    retrieved = retrieved.reverse

    assert(list == retrieved)
    assert(queue.size() == 0)
  }

  it should "throw NoSuchElementException if an empty queue is dequeued" in {
    intercept[NoSuchElementException] {
      new ConcurrentUnrolledQueue().dequeue()
    }
  }

}

