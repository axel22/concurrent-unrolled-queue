package test
import java.util.concurrent.ConcurrentLinkedQueue
import scala.actors.Actor._
import scala.concurrent.ConcurrentUnrolledQueue
import scala.collection._

object ConcurrentAddRemoveTimingTest {
  import AddRemoveOps._
  import System.currentTimeMillis

  def main(args: Array[String]): Unit = {
    //TODO this test needs to be ran several times to get a good estimation of the amount of time an operation takes
    val unrolledData: ConcurrentLinkedQueue[String] = defaultTestSet(NB_ELEMENTS)
    val linkedData: ConcurrentLinkedQueue[String] = defaultTestSet(NB_ELEMENTS)
    val unrolledQueue = new ConcurrentUnrolledQueue[String]
    val linkedQueue = new ConcurrentLinkedQueue[String]

    val startFillLinked = currentTimeMillis
    fillQueue(linkedQueue.offer(_: String), linkedData, NB_WRITERS)
    val stopFillLinked = currentTimeMillis
    val startFillUnrolled = currentTimeMillis
    fillQueue(unrolledQueue.enqueue(_: String), unrolledData, NB_WRITERS)
    val stopFillUnrolled = currentTimeMillis

    println("enqueue time unrolled: " + (stopFillUnrolled - startFillUnrolled))
    println("enqueue time linked: " + (stopFillLinked - startFillLinked))


    val startReadUnrolled = currentTimeMillis
    readQueue(unrolledQueue.dequeue, None, NB_READERS)
    val stopReadUnrolled = currentTimeMillis
    val startReadLinked = currentTimeMillis
    readQueue(linkedQueue.poll, None, NB_READERS)
    val stopReadLinked = currentTimeMillis

    println("dequeue time unrolled: " + (stopReadUnrolled - startReadUnrolled))
    println("enqueue time linked: " + (stopReadLinked - startReadLinked))
  }

  val NB_ELEMENTS = 1 << 21
  val NB_WRITERS = 1 << 4
  val NB_READERS = 1 << 4

}
