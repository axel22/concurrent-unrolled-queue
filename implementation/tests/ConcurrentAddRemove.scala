package tests
import scala.actors.Actor
import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.ConcurrentUnrolledQueue

object ConcurrentAddRemove {
  def main(args: Array[String]): Unit = {
    //TODO make sure that this test does always run on multiple cpu cores
    val tSet = testSet()
    val sourceQueue = newSourceQueue(tSet)
    val unrolledQueue = new ConcurrentUnrolledQueue[String]()

    0 until NB_WRITER foreach {
      _ =>
      var elem: String = null
      Actor.actor {
        Actor.loopWhile ({elem = sourceQueue.poll(); elem != null}) {
          unrolledQueue.enqueue(elem)
        }
      }
    }

    println("Finished adding elements to the unrolled queue")

    val main = Actor.self

    0 until NB_READER foreach {
      _ =>
      var elem: String = null
      Actor.actor {
        var ownTestSet = new collection.mutable.HashSet[String]() ++ tSet
        while ({
          elem = unrolledQueue.dequeue()
          elem != null
        }) {
          //TODO: if an element has been added twice, and is removed twice, this test won't notice it.
          ownTestSet -= elem
        }
        println("One reader is done reading");
        main ! ownTestSet
      }
    }

    var runningReaders = NB_READER
    while (runningReaders != 0) {
      main ? match {
        case readerRemainingSet: collection.mutable.Set[String] => {
          tSet --= readerRemainingSet
          runningReaders -= 1
        }
      }
    }

    if (tSet.isEmpty) {
      println("OK!")
    } else {
      println("Not OK!")
      println(tSet)
    }
  }

  def testSet(): collection.mutable.Set[String] = {
    val set = new collection.mutable.HashSet[String]
    set ++= 0 until NB_ELEMENTS map { _.toString() }
  }

  def newSourceQueue[A](elems: TraversableOnce[A]) = {
    val source = new ConcurrentLinkedQueue[A]()
    elems foreach { source.offer(_) }
    source
  }

  val NB_ELEMENTS = 1 << 16
  val NB_WRITER = 1 << 4
  val NB_READER = 1 << 4
}
