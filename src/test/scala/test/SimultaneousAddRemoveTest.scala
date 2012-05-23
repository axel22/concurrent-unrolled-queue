package test

import scala.actors.Actor._
import scala.concurrent.ConcurrentUnrolledQueue

object SimultaneousAddRemoveTest {
  import System.currentTimeMillis

  def main(args: Array[String]) = {
    var nReaderWriter = NB_READERS + NB_WRITERS
    var totalRead1 = 0
    var totalRead2 = 0
    var totalWrite1 = 0
    var totalWrite2 = 0
    val queue = new ConcurrentUnrolledQueue[AnyRef]
    write(queue)
    read(queue)
    while (nReaderWriter != 0) {
      ? match {
        case NRead(nRead1, nRead2) => {
          println("One reader has read    " + nRead1 + " ELEM1 elements, " + nRead2 + " ELEM2 elements, total = " + (nRead1 + nRead2))
          totalRead1 += nRead1
          totalRead2 += nRead2
        }
        case NWriten(nWriten1, nWriten2) => {
          println("One writer has written " + nWriten1 + " ELEM1 elements, " + nWriten2 + " ELEM2 elements, total = " + (nWriten1 + nWriten2))
          totalWrite1 += nWriten1
          totalWrite2 += nWriten2
        }
      }
      nReaderWriter -= 1
    }
    val nRemaining1 = (totalWrite1 - totalRead1)
    val nRemaining2 = (totalWrite2 - totalRead2)
    val nRemaining = (nRemaining1 + nRemaining2)

    println("    => (" + nRemaining1 + ", " + nRemaining2 + ") remaining elements in the queue")

    println("Checking predicates...")
    var predicateError = false
    try {
      queue.checkPredicates()
    } catch {
      case e => {
        println("    => ERROR: " + e)
        predicateError = true
      }
    }
    if (!predicateError) {
      println("    => OK")
    }


    println("Checking that there is indeed " + nRemaining  + " elements in the queue")
    val nGot = checkNRemaining(queue, nRemaining1, nRemaining2)
    if (nGot._1 + nGot._2 != nRemaining) {
      println("    => ERROR: Expected (" + nRemaining1 + ", " + nRemaining2 + "), got " + nGot + " => missing (" + (nRemaining1 - nGot._1) + ", " + (nRemaining2 - nGot._2) + ") element(s)")
    } else {
      println("    => OK")
    }

    println("total nodes that are still reachable: " + queue.countNode + ", expected: " + ((totalWrite1 + totalWrite2 + 1)/2))
  }

  def write(queue: ConcurrentUnrolledQueue[AnyRef]) = {
    val main = self
    val elem = new AnyRef
    0 until NB_WRITERS foreach {
      _ =>
      actor {
        var nWriten1 = 0
        var nWriten2 = 0
        val startTime = currentTimeMillis
        while (currentTimeMillis - startTime < WRITE_TIME) {
          val w = queue.enqueue(ELEM1, ELEM2)
          if (w == ELEM1) nWriten1 += 1
          else            nWriten2 += 1
        }
        main ! NWriten(nWriten1, nWriten2)
      }
    }
  }

  val ELEM1 = "0"
  val ELEM2 = "1"

  def read(queue: ConcurrentUnrolledQueue[AnyRef]) = {
    val main = self
    0 until NB_READERS foreach {
      _ =>
      actor {
        var nRead1 = 0
        var nRead2 = 0
        val startTime = currentTimeMillis
        while (currentTimeMillis - startTime < WRITE_TIME) {
          val elem = queue.dequeue
          if (elem == ELEM1) {
            nRead1 += 1
          } else if (elem == ELEM2) {
            nRead2 += 1
          }
        }
        main ! NRead(nRead1, nRead2)
      }
    }
  }

  def checkNRemaining(queue: ConcurrentUnrolledQueue[AnyRef], nRemaining1: Int, nRemaining2: Int): (Int, Int) = {
    var elem: AnyRef = null
    var nGot1 = 0
    var nGot2 = 0
    0 until (nRemaining1 + nRemaining2) foreach {
      _ =>
      elem = queue.dequeue()
//      if (elem != null) {
//        nGot += 1
      if (elem == ELEM1) {
        nGot1 += 1
      } else if (elem == ELEM2) {
        nGot2 += 1
      } else {
        return (nGot1, nGot2)
      }
    }
    return (nGot1, nGot2)
  }

//  val sourceElems = (0 until (1 << 20)).map(_.toString).sliding(1 << 2).toArray
  private case class NRead(nRead1: Int, nRead2: Int)
  private case class NWriten(nWriten1: Int, nWriten2: Int)

  val NB_WRITERS = 1 << 0
  val NB_READERS = 1 << 0
  val WRITE_TIME = 4000
  val READ_TIME = WRITE_TIME
}
