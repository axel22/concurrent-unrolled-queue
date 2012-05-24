package scala.concurrent

import java.util.concurrent.atomic._

class ConcurrentUnrolledQueue[A] {
//  override def companion: scala.collection.generic.GenericCompanion[ConcurrentUnrolledQueue] = ConcurrentUnrolledQueue

  def enqueue(elem: A): Unit = {
    if (elem == null) {
      throw new NullPointerException("Queue cannot hold null values.")
    }

    while (true) {
      val t = tail()
      val n = t.next()
      if (n == null) {
        var i = t.addHint
        while (i < Node.NODE_SIZE && t.get(i) != null) {
          i += 1
        }

        if (i < Node.NODE_SIZE) {
          if (t.atomicElements.compareAndSet(i, null, elem)) {
            t.addHint = i
            return
          } // else: could not insert elem in node, try again
        } else { // if (i == Node.NODE_SIZE)
          val n_ = new Node(elem)
          if (t.atomicNext.compareAndSet(null, n_)) {
            atomicTail.compareAndSet(t, n_)
            return
          } // else: could not add Node at end of queue, try again
        }
      } else { // tail does not point to end of list, try to advance it, then try again
        atomicTail.compareAndSet(t, n)
      }
    }
  }

  def dequeue(): A = {
    while (true) {
      val h = head
      val t = tail
      val nh = h.next

      if (h == t) {
        if (nh == null) {
          return null.asInstanceOf[A]
        }
        atomicTail.compareAndSet(t, nh) // Tail is falling behind.  Try to advance it
      } else {
        var i = nh.deleteHint
        var v : Any = null

        while (i < Node.NODE_SIZE && {v = nh.get(i); v == DELETED}) {
          i += 1
        }

        if (v == null) {
          /* this needs some more thinking. And an assert that would look like assert(nh == t,...) but it would'nt be thread safe */
          return null.asInstanceOf[A]
        }

        if (i < Node.NODE_SIZE_MIN_ONE) {
          if (nh.atomicElements.compareAndSet(i, v, DELETED)) {
            nh.deleteHint = i
            return v.asInstanceOf[A]
          }
        } else if (i == Node.NODE_SIZE_MIN_ONE) { // if the element being removed is the last element of the node...
          /* This needs some more careful thinking. */
          /* 23.05.2012, pretty sure about this one now */
          if (atomicHead.compareAndSet(h, nh)) {
            nh.set(Node.NODE_SIZE_MIN_ONE, DELETED)
            return v.asInstanceOf[A]
          }
        } else { // if (i == Node.NODE_SIZE)
          // All the elements of this node have been deleted : node has been deleted.
        }
      }
    }

    return null.asInstanceOf[A] // should never happen, maybe throw an exception instead ?
  }

  /* a simple size implementation. not very useful in a concurrent context */
  def size(): Int = {
    var count = 0
    var current = head
    while ({ current = current.next(); current != null }) {
      var i = 0
      while (i < Node.NODE_SIZE) {
        val elem = current.get(i)
        if (elem != DELETED && elem != null) {
          count += 1
        }
      }
    }
    count
  }

  def checkPredicates() = {

  }

  val DELETED = new AnyRef()

  @scala.inline
  private def head() = atomicHead.get()

  @scala.inline
  private def tail() = atomicTail.get()

  val atomicHead = new AtomicReference(new Node())

  val atomicTail = new AtomicReference(head())

  {
    var i = 0
    while (i < Node.NODE_SIZE) {
      head.set(i, DELETED)
      i += 1
    }
  }

  class Node () {
    import Node._

    /**
     * Creates a node with an initial element and sets the hint to 1
     */
    def this(firstElem: Any) = {
      this()
      atomicElements.set(0, firstElem)
      addHint = 1
    }

    def set(i : Int, elem : Any) = {
      atomicElements.set(i, elem)
    }

    def get(i : Int) = atomicElements.get(i)

    def next() = atomicNext.get()

//    def mkString(): String = {
//      var i = 0
//      var s = ""
//      while (i < atomicElements.length()) {
//        s += atomicElements.get(i)
//        i += 1
//      }
//      return s
//    }

    val atomicElements = new AtomicReferenceArray[Any](NODE_SIZE)

    var atomicNext = new AtomicReference[Node]

    @volatile
    var addHint = 0

    @volatile
    var deleteHint = 0
  }

  object Node {
    val NODE_SIZE = 1 << 10
    val NODE_SIZE_MIN_ONE = NODE_SIZE - 1
  }
}
