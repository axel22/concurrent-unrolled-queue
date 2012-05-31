package test

import scala.actors.Actor._
import scala.concurrent.ConcurrentUnrolledQueue

object SimultaneousAddRemoveTest {
  import System.currentTimeMillis

  def main(args: Array[String]) = {
    var nReaderWriter = NB_READERS + NB_WRITERS
    var totalRead = 0
    var totalWrite = 0
    val queue = new ConcurrentUnrolledQueue[AnyRef]
    read(queue)
    write(queue)
    while (nReaderWriter != 0) {
      ? match {
        case NRead(nRead) => {
          println("One reader has read    " + nRead + " elements")
          totalRead += nRead
        }
        case NWriten(nWriten) => {
          println("One writer has written " + nWriten + " elements")
          totalWrite += nWriten
        }
      }
      nReaderWriter -= 1
    }
    val nRemaining = totalWrite - totalRead

    println("    => " + nRemaining  + " remaining elements in the queue")

    println("Checking predicates...")
    var predicateError = false
    try {
//      queue.checkPredicates()
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
    val nGot = checkNRemaining(queue, nRemaining)
    if (nGot != nRemaining) {
      println("    => ERROR: Expected " + nRemaining + ", got " + nGot + " => missing " + (nRemaining - nGot) + " element(s)")
    } else {
      println("    => OK")
    }
  }

  def write(queue: ConcurrentUnrolledQueue[AnyRef]) = {
    val main = self
    val elem = new AnyRef
    0 until NB_WRITERS foreach {
      _ =>
      actor {
        var nWriten = 0
        val startTime = currentTimeMillis
        while (currentTimeMillis - startTime < WRITE_TIME) {
          queue.enqueue(elem)
          nWriten += 1
        }
        main ! NWriten(nWriten)
      }
    }
  }

  def read(queue: ConcurrentUnrolledQueue[AnyRef]) = {
    val main = self
    0 until NB_READERS foreach {
      _ =>
      actor {
        var nRead = 0
        val startTime = currentTimeMillis
        while (currentTimeMillis - startTime < WRITE_TIME) {
          val elem = queue.dequeue
          if (elem != null) {
            nRead += 1
          }
        }
        main ! NRead(nRead)
      }
    }
  }

  def checkNRemaining(queue: ConcurrentUnrolledQueue[AnyRef], nRemaining: Int): Int = {
    var elem: AnyRef = null
    var nGot = 0
    0 until (nRemaining) foreach {
      _ =>
      elem = queue.dequeue()
      if (elem != null) {
        nGot += 1
      } else {
        return nGot
      }
    }
    return nGot
  }

//  val sourceElems = (0 until (1 << 20)).map(_.toString).sliding(1 << 2).toArray
  private case class NRead(nRead: Int)
  private case class NWriten(nWriten: Int)

  val NB_WRITERS = 1 << 3
  val NB_READERS = 1 << 1
  val WRITE_TIME = 5 * 1000
  val READ_TIME = WRITE_TIME
}
