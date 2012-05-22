//package test
//
//import concurrent.ConcurrentUnrolledQueue
//import java.util.concurrent.ConcurrentLinkedQueue
//
//object MultipleAddRemoveTimingTest {
//  import AddRemoveOps._
//  import System.currentTimeMillis
//
//  def main(args: Array[String]) = {
//    println("Creating the working sets...")
//    val data = defaultTestSet(NB_ELEMENTS)
//    val unrolledQueue = new ConcurrentUnrolledQueue[String]
//    val linkedQueue = new ConcurrentLinkedQueue[String]
//
//
//    println("Enqueuing elements with one thread...")
//    /* Enqueue operations... */
//    val startFillUnrolled = currentTimeMillis
//    fillQueue(unrolledQueue.enqueue(_: String), data)
//    val stopFillUnrolled = currentTimeMillis
//    println("enqueue time unrolled: " + (stopFillUnrolled - startFillUnrolled))
//
//    val startFillLinked = currentTimeMillis
//    fillQueue(linkedQueue.offer(_: String), data)
//    val stopFillLinked = currentTimeMillis
//    println("enqueue time linked: " + (stopFillLinked - startFillLinked))
//
//
//    println("Dequeuing elements with one thread...")
//    /* Dequeue operations... */
//    val startReadUnrolled = currentTimeMillis
//    readQueue(unrolledQueue.dequeue, None)
//    val stopReadUnrolled = currentTimeMillis
//    println("dequeue time unrolled: " + (stopReadUnrolled - startReadUnrolled))
//
//    val startReadLinked = currentTimeMillis
//    readQueue(linkedQueue.poll, None)
//    val stopReadLinked = currentTimeMillis
//    println("dequeue time linked: " + (stopReadLinked - startReadLinked))
//  }
//
//  val NB_ELEMENTS = 1 << 24
//}