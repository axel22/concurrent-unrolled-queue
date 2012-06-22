package test
import scala.collection._
import scala.concurrent.ConcurrentUnrolledQueue

object MultipleAddRemoveTest {
  import AddRemoveOps._

  def main(args: Array[String]) = {
    val data = defaultTestSet(NB_ELEMENTS)
    val unrolledQueue = new ConcurrentUnrolledQueue[String]
    val retrievedSet = new mutable.HashSet[String]

    fillQueue(unrolledQueue.enqueue(_: String), data)
    readQueue(unrolledQueue.dequeue, Some(retrievedSet))

    if (retrievedSet == data) {
      println("OK!")
    } else {
      println("Not OK!")
    }
  }

  val NB_ELEMENTS = 1 << 10
}

