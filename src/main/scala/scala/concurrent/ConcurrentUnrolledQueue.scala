package scala.concurrent

import java.util.concurrent.atomic._

class ConcurrentUnrolledQueue[A] {
//  override def companion: scala.collection.generic.GenericCompanion[ConcurrentUnrolledQueue] = ConcurrentUnrolledQueue

  import ConcurrentUnrolledQueue._

  def enqueue(elem: A): Unit = {
    if (elem == null) {
      throw new NullPointerException("Queue cannot hold null values.")
    }

/*
    val optimisticTail = tail
    val optimisticHint = optimisticTail.addHint
    if (optimisticTail.compareAndSwap(optimisticHint, null, elem)) {
      if (optimisticHint < Node.NODE_SIZE_MIN_ONE) optimisticTail.addHint = optimisticHint + 1
      return
    } else
*/
    while(true) {
      val t = tail
      val n = t.next
      if (n == null) {
        var i = t.addHint
        while (i < Node.NODE_SIZE && t.get(i) != null) {
          i += 1
        }

        if (i < Node.NODE_SIZE) {
          if (t.compareAndSwapElem(i, null, elem)) {
            /* if (i < Node.NODE_SIZE_MIN_ONE) */ t.addHint = i + 1
            return
          } // else: could not insert elem in node, try again
        } else { // if (i == Node.NODE_SIZE)
          val n_ = new Node(elem)
          if (t.compareAndSwapNext(null, n_)) {
            compareAndSwapTail(t, n_)
            return
          } // else: could not add Node at end of queue, try again
        }
      } else { // tail does not point to end of list, try to advance it, then try again
        compareAndSwapTail(t, n)
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
        compareAndSwapTail(t, nh) // Tail is falling behind.  Try to advance it
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
          if (nh.compareAndSwapElem(i, v, DELETED)) {
            nh.deleteHint = i
            return v.asInstanceOf[A]
          }
        } else if (i == Node.NODE_SIZE_MIN_ONE) { // if the element being removed is the last element of the node...
          /* This needs some more careful thinking. */
          /* 23.05.2012, pretty sure about this one now */
          if (compareAndSwapHead(h, nh)) {
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

  @scala.inline
  private def compareAndSwapHead(expect: Node, update: Node) = {
    UNSAFE.compareAndSwapObject(this, HEAD_OFFSET, expect, update)
  }

  @scala.inline
  private def compareAndSwapTail(expect: Node, update: Node) = {
    UNSAFE.compareAndSwapObject(this, TAIL_OFFSET, expect, update)
  }

  private val DELETED = new AnyRef()

  @volatile
  private var head = new Node()

  @volatile
  private var tail = head

  {
    var i = 0
    while (i < Node.NODE_SIZE) {
      head.set(i, DELETED)
      i += 1
    }
  }

  final class Node () {
    import Node._

    /**
     * Creates a node with an initial element and sets the hint to 1
     */
    def this(firstElem: Any) = {
      this()
      set(0, firstElem)
      addHint = 1
    }

    @scala.inline
    def compareAndSwapElem(i: Int, expect: Any, update: Any) = {
      UNSAFE.compareAndSwapObject(elements, ELEMENTS_OFFSET + i * ELEMENTS_INDEX_STEP, expect, update)
    }

    @scala.inline
    def compareAndSwapNext(expect: Node, update: Node) = {
      UNSAFE.compareAndSwapObject(this, NEXT_OFFSET, expect, update)
    }

    @scala.inline
    def set(i : Int, elem : Any) = {
      UNSAFE.putObjectVolatile(elements, ELEMENTS_OFFSET + i * ELEMENTS_INDEX_STEP, elem)
    }

    @scala.inline
    def get(i : Int) = UNSAFE.getObjectVolatile(elements, ELEMENTS_OFFSET + i * ELEMENTS_INDEX_STEP)

//    def mkString(): String = {
//      var i = 0
//      var s = ""
//      while (i < atomicElements.length()) {
//        s += atomicElements.get(i)
//        i += 1
//      }
//      return s
//    }

    val elements = new Array[Any](NODE_SIZE)

    @volatile
    var next: Node = null

    @volatile
    var addHint = 0

    @volatile
    var deleteHint = 0
  }

  object Node {

    val NODE_SIZE = 1 << 6

    val NODE_SIZE_MIN_ONE = NODE_SIZE - 1

    val NEXT_OFFSET = UNSAFE.objectFieldOffset(classOf[Node].getDeclaredField("next"))

    val ELEMENTS_OFFSET = UNSAFE.arrayBaseOffset(classOf[Array[Any]])

    val ELEMENTS_INDEX_STEP = UNSAFE.arrayIndexScale(classOf[Array[Any]])

  }

}

object ConcurrentUnrolledQueue {

  val UNSAFE = {
    if (this.getClass.getClassLoader == null)
      sun.misc.Unsafe.getUnsafe()
    else
      try {
        val fld = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
        fld.setAccessible(true)
        fld.get(null).asInstanceOf[sun.misc.Unsafe]
      } catch {
        case e => throw new RuntimeException("Could not obtain access to sun.misc.Unsafe", e)
      }
  }

  val HEAD_OFFSET = UNSAFE.objectFieldOffset(classOf[ConcurrentUnrolledQueue[_]].getDeclaredField("head"))

  val TAIL_OFFSET = UNSAFE.objectFieldOffset(classOf[ConcurrentUnrolledQueue[_]].getDeclaredField("tail"))

}

