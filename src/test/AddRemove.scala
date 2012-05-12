package tests
import scala.concurrent.ConcurrentUnrolledQueue

object AddRemove {
  def main(args: Array[String]): Unit = {
    val queue = new ConcurrentUnrolledQueue[AnyRef]
    val elem = new AnyRef
    queue.enqueue(elem)
    val retrieved = queue.dequeue()
    if (elem == retrieved) {
      println("Success !")
    } else {
      println("Failure !")
    }
  }
}