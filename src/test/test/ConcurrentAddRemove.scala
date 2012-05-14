package tests
import java.util.concurrent.ConcurrentLinkedQueue
import scala.actors.Actor._
import scala.concurrent.ConcurrentUnrolledQueue
import scala.collection._

object ConcurrentAddRemove {
  def main(args: Array[String]): Unit = {
    //TODO make sure that this test does always run on multiple cpu cores
    var data = testSet()
    val testedQueue = new ConcurrentUnrolledQueue[String]()

    println("Enqueuing " + NB_ELEMENTS + " elements to the unrolled queue...")
    fillTestedQueue(testedQueue, data)

    println("Done enqueuing. Now reading elements from unrolled queue...")
    val retrievedData = readFullyTestedQueue(testedQueue)

    if (data == retrievedData) {
      println("OK: Initial data set retrieved correctly from the concurrent unrolled queue !")
    } else {
      println("ERROR: Missing " + (data.size - retrievedData.size) + " elements after reading the data set from the concurrent unrolled queue")
    }

    // Very basic statistics indeed
    val totalDequeueAttempts = (testedQueue.QueueStats.statHandler !? testedQueue.QueueStats.SEND_STATISTICS) match {
      case n: Int => n
    }
    println("Ran " + totalDequeueAttempts + " attemps to delete before emptying queue")

    testedQueue.QueueStats.statHandler ! testedQueue.QueueStats.EXIT
  }

  def testSet(): Set[String] = {
    new mutable.HashSet[String] ++ (0 until NB_ELEMENTS).map { _.toString }
  }

  def toConcurrentLinkedQueue[A](elems: TraversableOnce[A]) = {
    val source = new ConcurrentLinkedQueue[A]()
    elems foreach { source.offer(_) }
    source
  }

  def sort(elems: Set[String]) = {
    new collection.immutable.TreeSet[String]()(Ordering.by(_.toInt)) ++ elems
  }

//  def missing(resultSet: collection.mutable.Set[String]) = {
//    val prev = -1
//    resultSet.foldLeft(-1) {
//      prev2: Int, curr: String
//    }
//  }

  def shouldPrintSet() = NB_ELEMENTS <= SET_SIZE_PRINT_THRESHOLD

  /* Doesn't work with basic types... */
  def fillTestedQueue[A](testedQueue: ConcurrentUnrolledQueue[A], data: Set[A]) = {
    val dataSource = toConcurrentLinkedQueue(data)
    val mainActor = self

    0 until NB_WRITERS foreach {
      _ =>
      actor {
        var elem: A = null.asInstanceOf[A]
        while ({ elem = dataSource.poll; elem != null }) {
          testedQueue.enqueue(elem)
        }
        mainActor ! self
      }
    }

    /* Wait for all the writers to finish */
    var runningWriters = NB_WRITERS
    while ({runningWriters != 0}) {
      ? match {
        case writer: actors.Actor => runningWriters -= 1
      }
    }
  }

  def readFullyTestedQueue[A](testedQueue: ConcurrentUnrolledQueue[A]): Set[A] = {
    val mainActor = self

    0 until NB_READERS foreach {
      _ =>
      var elem: A = null.asInstanceOf[A]
      actor {
        var readData = new collection.mutable.HashSet[A]()
        while ({ elem = testedQueue.dequeue(); elem != null }) {
          //TODO: if an element has been added twice, and is removed twice, this test won't notice it.
          readData += elem
        }
        mainActor ! readData
      }
    }

    var allReadData = new mutable.HashSet[A]
    var runningReaders = NB_READERS
    while (runningReaders != 0) {
      ? match {
        case readData: Set[A] => {
          allReadData ++= readData
          runningReaders -= 1
          println("One reader is done.")// + (if (shouldPrintSet) "Read: " + sort(readData) else ""))
        }
      }
    }
    allReadData
  }

  val NB_ELEMENTS = 1 << 20
  val NB_WRITERS = 1 << 4
  val NB_READERS = 1 << 4
  val SET_SIZE_PRINT_THRESHOLD = 1 << 10
}
