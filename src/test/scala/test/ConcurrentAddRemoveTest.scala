package test

import scala.collection._
import scala.concurrent.ConcurrentUnrolledQueue

object ConcurrentAddRemoveTest {
  import AddRemoveOps._

  def main(args: Array[String]) = {
    val data = defaultTestSet(NB_ELEMENTS)
    val unrolledQueue = new ConcurrentUnrolledQueue[String]

    println("Enqueuing " + NB_ELEMENTS + " elements to the unrolled queue...")
    fillQueue(unrolledQueue.enqueue(_: String), data, NB_WRITERS)
    println("Done enqueuing. Reading elements from unrolled queue...")

    val retrievedData = new mutable.HashSet[String]
    readQueue(unrolledQueue.dequeue, Some(retrievedData), NB_READERS)

    if (retrievedData == data) {
      println("OK: Initial data set retrieved correctly from the concurrent unrolled queue !")
    } else {
      println("ERROR: Missing " + (data.size - retrievedData.size) + " elements after reading the data set from the concurrent unrolled queue")
    }
  }

  val NB_ELEMENTS = 1 << 19
  val NB_WRITERS  = 1 << 4
  val NB_READERS  = 1 << 4
}
