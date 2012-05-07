package tests
import scala.concurrent.ConcurrentUnrolledQueue

object MultipleAddRemove {
  def main(args: Array[String]) = {
    val queue = new ConcurrentUnrolledQueue[String]
    val strings = Array.range(0, 100).map(_.toString())
    strings.foreach(i => queue.enqueue(i))
    val newStrings = new Array[String](strings.length)
    var deqVal = null.asInstanceOf[String]
    var i = 0
    while ({deqVal = queue.dequeue(); deqVal != null}) {
      newStrings.update(i, deqVal)
      i += 1
    }

    println("Expected: " + strings.mkString(", "))
    println("Received: " + newStrings.mkString(", "))

    if (strings.deepEquals(newStrings)) {
      println("OK!")
    } else {
      println("Not OK!")
    }
  }
}
