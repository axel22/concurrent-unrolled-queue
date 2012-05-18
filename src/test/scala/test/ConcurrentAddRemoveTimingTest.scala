package test
import java.util.concurrent.ConcurrentLinkedQueue
import scala.actors.Actor._
import scala.concurrent.ConcurrentUnrolledQueue
import scala.collection._

object ConcurrentAddRemoveTimingTest {
  import AddRemoveOps._
  import System.currentTimeMillis

  def main(args: Array[String]): Unit = {
    //TODO this test needs to be ran several times to get a realistic measurement of the time an operation takes
    println("Creating the working sets...")
    val unrolledData: ConcurrentLinkedQueue[String] = defaultTestSet(NB_ELEMENTS)
    val linkedData: ConcurrentLinkedQueue[String] = defaultTestSet(NB_ELEMENTS)
    val unrolledQueue = new ConcurrentUnrolledQueue[String]
    val linkedQueue = new ConcurrentLinkedQueue[String]

    println("Enqueuing elements with " + NB_WRITERS + " threads...")
    /* Enqueue operations... */
    val startFillLinked = currentTimeMillis
    fillQueue(linkedQueue.offer(_: String), linkedData, NB_WRITERS)
    val stopFillLinked = currentTimeMillis
    println("enqueue time linked: " + (stopFillLinked - startFillLinked))

    val startFillUnrolled = currentTimeMillis
    fillQueue(unrolledQueue.enqueue(_: String), unrolledData, NB_WRITERS)
    val stopFillUnrolled = currentTimeMillis
    println("enqueue time unrolled: " + (stopFillUnrolled - startFillUnrolled))


    println("Dequeuing elements with " + NB_READERS + " threads...")
    /* Dequeue operations... */
    val startReadUnrolled = currentTimeMillis
    readQueue(unrolledQueue.dequeue, None, NB_READERS)
    val stopReadUnrolled = currentTimeMillis
    println("dequeue time unrolled: " + (stopReadUnrolled - startReadUnrolled))

    val startReadLinked = currentTimeMillis
    readQueue(linkedQueue.poll, None, NB_READERS)
    val stopReadLinked = currentTimeMillis
    println("dequeue time linked: " + (stopReadLinked - startReadLinked))
  }

  val NB_ELEMENTS = 1 << 23
  val NB_WRITERS = 1 << 4
  val NB_READERS = 1 << 4

}
