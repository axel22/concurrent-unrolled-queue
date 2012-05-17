package test
import concurrent.ConcurrentUnrolledQueue

object AddRemoveTest {
  import AddRemoveOps._
  def main(args: Array[String]): Unit = {
    val queue = new ConcurrentUnrolledQueue[AnyRef]
    val elem = new AnyRef
    queue.enqueue(elem)
    val retrieved = queue.dequeue()
    if (elem == retrieved && queue.dequeue() == null) {
      println("Success !")
    } else {
      println("Failure !")
    }
  }
}
