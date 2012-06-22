package test

import java.util.concurrent.ConcurrentLinkedQueue
import scala.actors.Actor._
import scala.collection._
import scala.concurrent.ConcurrentUnrolledQueue

object AddRemoveOps {

  def fillQueue[A](enqueue: A => Unit, data: ConcurrentLinkedQueue[A], nWriters: Int): Unit = {
    val mainActor = self

    0 until nWriters foreach {
      _ =>
      actor {
        var elem: A = null.asInstanceOf[A]
        while ({ elem = data.poll; elem != null }) {
          enqueue(elem)
        }
        mainActor ! self
      }
    }

    /* Wait for all writers to finish */
    var runningWriters = nWriters
    while (runningWriters != 0) {
      ? match {
        case _: actors.Actor => runningWriters -= 1
      }
    }
  }

  def fillQueue[A](enqueue: A => Unit, data: TraversableOnce[A]): Unit = {
    data.foreach(enqueue(_))
  }

  def readQueue[A](dequeue: () => A, totalReadResult: Option[mutable.Set[A]], nReaders: Int): Unit = {
    val mainActor = self

    0 until nReaders foreach {
      _ =>
      var elem: A = null.asInstanceOf[A]
      actor {
        var readData: Option[mutable.Set[A]] = None
        if (totalReadResult.isDefined) {
          readData = Some(new mutable.HashSet[A]())
        }
        var nReadItems = 0
        while ({ elem = dequeue(); elem != null }) {
          //TODO: if an element has been added twice, and is removed twice, this test won't notice it.
          readData foreach { _ += elem }
          nReadItems += 1
        }

        mainActor ! ReadResult(readData, nReadItems)
      }
    }

    var runningReaders = nReaders
    while (runningReaders != 0) {
      ? match {
        case ReadResult(readData, nItems) => {
          totalReadResult foreach { _ ++= readData.get.asInstanceOf[Set[A]] }
          runningReaders -= 1
          println("One reader is done and has read "
              + nItems + " elements")
        }
      }
    }
  }

  def readQueue[A](dequeue: () => A, readResult: Option[mutable.Set[A]]): Unit = {
    var elem: A = null.asInstanceOf[A]
    while ({elem = dequeue(); elem != null}) {
      readResult foreach { _ += elem }
    }
  }

  def defaultTestSet(setSize: Int) = {
    new mutable.HashSet[String] ++ (0 until setSize).map { _.toString }
  }

  implicit def traversableOnce2ConcurrentLinkedQueue[A](elems: TraversableOnce[A]) = {
    val source = new ConcurrentLinkedQueue[A]()
    elems foreach { source.offer(_) }
    source
  }

  private case class ReadResult(set: Option[Set[_]], nRead: Int)

}
