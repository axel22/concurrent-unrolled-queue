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
        var i = 0
        while (i < Node.NODE_SIZE && t.get(i) != null) {
          i += 1
        }
        if (i < Node.NODE_SIZE) {
          if (t.atomicElements.compareAndSet(i, null, elem)) {
            return
          }
        } else { // if (i == Node.NODE_SIZE)
          val n_ = new Node[A]
          n_.set(0, elem)
          if (t.atomicNext.compareAndSet(null, n_)) {
            atomicTail.compareAndSet(t, n_)
            return
          }
        }
      } else {
        atomicTail.compareAndSet(t, n)
      }
    }
  }

  def dequeue(): A = {
    while (true) {
      val h = head
      val t = tail
      val nh = h.next
      val nt = t.next
      if (h == t) {
        if (nh == null) {
          return null.asInstanceOf[A]
        }
        atomicTail.compareAndSet(t, nh) // Tail is falling behind.  Try to advance it
      } else {
        var i = 0
        var v : Any = null
        while (i < Node.NODE_SIZE && (v = nh.get(i)) == DELETED) {
          i += 1
        }

        if (i < Node.NODE_SIZE_MIN_ONE) {
          if (nh.atomicElements.compareAndSet(i, v, DELETED)) {
            return v.asInstanceOf[A]
          }
        } else if (i == Node.NODE_SIZE_MIN_ONE) { // if the element being removed is the last element of the node...
          /* This needs some more careful thinking. */
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

  val DELETED = new AnyRef()

  @scala.inline
  def head() = atomicHead.get()

  @scala.inline
  def tail() = atomicTail.get()

  val atomicHead = new AtomicReference(new Node[A])

  val atomicTail = new AtomicReference(head())

  {
    var i = 0
    while (i < Node.NODE_SIZE) {
      head.set(i, DELETED)
      i += 1
    }
  }

  class Node[A] () {
    import Node._

    def set(i : Int, elem : Any) = {
      atomicElements.set(i, elem)
    }

    def get(i : Int) = atomicElements.get(i : Int)

    def next() = atomicNext.get()

    val atomicElements = new AtomicReferenceArray[Any](NODE_SIZE)

    var atomicNext = new AtomicReference[Node[A]]
  }

  object Node {
    val NODE_SIZE = 1 << 4
    val NODE_SIZE_MIN_ONE = NODE_SIZE - 1
  }
}
