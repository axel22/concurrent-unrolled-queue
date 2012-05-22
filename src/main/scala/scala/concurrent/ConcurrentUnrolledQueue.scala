package scala.concurrent

import java.util.concurrent.atomic._
import java.util.concurrent.ConcurrentLinkedQueue

class ConcurrentUnrolledQueue[A] {
//  override def companion: scala.collection.generic.GenericCompanion[ConcurrentUnrolledQueue] = ConcurrentUnrolledQueue

  def enqueue(elem1: A, elem2: A): A = {
//    if (elem == null) {
//      throw new NullPointerException("Queue cannot hold null values.")
//    }

    while (true) {
      val t = tail()
      val n = t.next()
      if (n == null) {
        var i = 0// t.addHint
        while (i < Node.NODE_SIZE && t.get(i) != null) {
          i += 1
        }

        if (i < Node.NODE_SIZE) {
          assert(i == 1)
          if (t.atomicElements.compareAndSet(i, null, elem2)) {
            t.addHint = i
            return elem2
          } // else: could not insert elem in node, try again
        } else { // if (i == Node.NODE_SIZE)
          val n_ = new Node(elem1)
          if (t.atomicNext.compareAndSet(null, n_)) {
            atomicTail.compareAndSet(t, n_)
            return elem1
          } // else: could not add Node at end of queue, try again
        }
      } else { // tail does not point to end of list, try to advance it, then try again
        atomicTail.compareAndSet(t, n)
      }
    }

    assert(false)
    return null.asInstanceOf[A]
  }

  def dequeue(): A = {
//  var nDequeueTries = 0
//  def dequeue_(): A = {
    while (true) {
//      nDequeueTries += 1
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
        var i = 0//nh.deleteHint
        var v : Any = null

        while (i < Node.NODE_SIZE && {v = nh.get(i); v == DELETED}) {
          i += 1
        }

        if (i < Node.NODE_SIZE_MIN_ONE) {
          if (nh.atomicElements.compareAndSet(i, v, DELETED)) {
            nh.deleteHint = i
            return v.asInstanceOf[A]
          }
        } else if (i == Node.NODE_SIZE_MIN_ONE) { // if the element being removed is the last element of the node...
          /* This needs some more careful thinking. */
          if (atomicHead.compareAndSet(h, nh)) {
            nh.set(Node.NODE_SIZE_MIN_ONE, DELETED)
//            removedNodes.add(nh)
            return v.asInstanceOf[A]
          }
        } else { // if (i == Node.NODE_SIZE)
          // All the elements of this node have been deleted : node has been deleted.
        }
      }
    }

    return null.asInstanceOf[A] // should never happen, maybe throw an exception instead ?
//  }
//  val r = dequeue_
//  QueueStats.statHandler ! nDequeueTries
//  r
  }

  val removedNodes = new ConcurrentLinkedQueue[Node]

  def checkPredicates() = {
    /* check removed nodes. */
    var removedNode: Node = null
    while ({ removedNode = removedNodes.poll; removedNode != null }) {
      0 until Node.NODE_SIZE foreach {
        i => assert(removedNode.get(i) == DELETED, "Element " + i + " is not marked as DELETED")
      }
    }

    /* check head */
    0 until Node.NODE_SIZE foreach { i => assert(head.get(i) == DELETED) }

    /* check nodes */
    var currentNode: Node = head
    while ({ currentNode = currentNode.next; currentNode != null }) {
//      println("checking node")
      var i = 0
      while (currentNode.get(i) == DELETED) {
        i += 1
      }
      while (i < Node.NODE_SIZE && currentNode.get(i) != null) {
        assert(currentNode.get(i) != DELETED)
        i += 1
      }
      while (i < Node.NODE_SIZE) {
        assert(currentNode.get(i) == null)
        i += 1
      }
    }
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
    val NODE_SIZE = 2
    val NODE_SIZE_MIN_ONE = NODE_SIZE - 1
  }
}
