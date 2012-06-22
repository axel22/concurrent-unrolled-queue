package scala.concurrent

import java.util.concurrent.atomic._

class ConcurrentUnrolledQueue[A] {

  import ConcurrentUnrolledQueue._

  def enqueue(elem: A): Unit = {
    if (elem == null) {
      throw new NullPointerException("Queue cannot hold null values.")
    }

    val NODE_SIZE = Node.NODE_SIZE

    while(true) {
      val t = tail
      val n = t.next.get
      if (n == null) {
        var i = t.addHint
        while (i < NODE_SIZE && t.get(i) != null) {
          i += 1
        }

        if (i < NODE_SIZE) {
          if (t.compareAndSwapElem(i, null, elem)) {
            t.addHint = i + 1
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
      val nh = h.next.get
      val t = tail

      if (h == t) {
        if (nh == null) {
          throw new NoSuchElementException("first element of empty queue")
        }

        /* If no thread is adding elements, and there are threads removing, tail
         * will fall behind head. Therefore, we first need to advance tail before
         * actually removing an element, because we might have to remove the entire
         * node.
         */
        compareAndSwapTail(t, nh) // Tail is falling behind.  Try to advance it
      } else {
        var i = nh.deleteHint
        var v : Any = null

        while (i < Node.NODE_SIZE && { v = nh.get(i); v == DELETED }) {
          i += 1
        }

        if (v == null) {
          throw new NoSuchElementException("first element of empty queue")
        }

        if (i < Node.NODE_SIZE_MIN_ONE) {
          if (nh.compareAndSwapElem(i, v, DELETED)) {
            if (nh.deleteHint <= i) {
              nh.deleteHint = i + 1
            }

            return v.asInstanceOf[A]
          }
        } else if (i == Node.NODE_SIZE_MIN_ONE) { // if the element being removed is the last element of the node...
          if (compareAndSwapHead(h, nh)) {
            nh.set(Node.NODE_SIZE_MIN_ONE, DELETED)
            return v.asInstanceOf[A]
          }
        } else { // if (i == Node.NODE_SIZE)
          // All the elements of this node have been deleted : node has been deleted.
        }
      }
    }

    throw new Error("reached unreachable point") // should never happen, maybe throw an exception instead ?
  }

  def peek(): A = {
    while (true) {
      val h = head
      val nh = h.next.get
      val t = tail

      if (h == t) {
        if (nh == null) {
          throw new NoSuchElementException("first element of empty queue")
        }

        compareAndSwapTail(t, nh) // Tail is falling behind.  Try to advance it
      } else {
        var i = nh.deleteHint
        var v : Any = null

        while (i < Node.NODE_SIZE && { v = nh.get(i); v == DELETED }) {
          i += 1
        }

        if (i < Node.NODE_SIZE) {
          nh.deleteHint = i
          return v.asInstanceOf[A]
        } else {
          /* node has been deleted. */
        }
      }
    }

    throw new Error("reached unreachable point")
  }

  /* a simple size implementation. not very useful in a concurrent context */
  def size(): Int = {
    var count = 0
    var current = head
    while ({ current = current.next.get; current != null }) {
      val first = current.get(0)
      val last = current.get(Node.NODE_SIZE_MIN_ONE)
      if (first != null && first != DELETED && last != null && last != DELETED) {
        count += Node.NODE_SIZE
      } else {
        var i = current.deleteHint
        while (i < Node.NODE_SIZE) {
          val elem = current.get(i)
          if (elem == null) {
            return count
          } else if (elem != DELETED) {
            count += 1
          }
          i += 1
        }
      }
    }
    count
  }

  def isEmpty(): Boolean = {
    var current = head

    while ({ current = current.next.get; current != null }) {
      var i = current.deleteHint

      while (i < Node.NODE_SIZE) {
        val elem = current.get(i)

        if (elem == null) {
          return true;
        } else if (elem != DELETED) {
          return false
        }

        i += 1
      }
    }

    return true
  }

  def iterator(): Iterator[A] = {
    new CUQIterator(head)
  }

//  @scala.inline
  private def compareAndSwapHead(expect: Node, update: Node) = {
    UNSAFE.compareAndSwapObject(this, HEAD_OFFSET, expect, update)
  }

//  @scala.inline
  private def compareAndSwapTail(expect: Node, update: Node) = {
    UNSAFE.compareAndSwapObject(this, TAIL_OFFSET, expect, update)
//    tail.compareAndSet(expect, update)
  }

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

}

object ConcurrentUnrolledQueue {

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

//    @scala.inline
    def compareAndSwapElem(i: Int, expect: Any, update: Any) = {
//      UNSAFE.compareAndSwapObject(elements, ELEMENTS_OFFSET + i * ELEMENTS_INDEX_STEP, expect, update)
      elements.compareAndSet(i, expect, update)
    }

//    @scala.inline
    def compareAndSwapNext(expect: Node, update: Node) = {
//      UNSAFE.compareAndSwapObject(this, NEXT_OFFSET, expect, update)
      next.compareAndSet(expect, update)
    }

//    @scala.inline
    def set(i : Int, elem : Any) = {
//      UNSAFE.putObjectVolatile(elements, ELEMENTS_OFFSET + i * ELEMENTS_INDEX_STEP, elem)
      elements.set(i, elem)
    }

//    @scala.inline
    def get(i : Int) = {
//      UNSAFE.getObjectVolatile(elements, ELEMENTS_OFFSET + i * ELEMENTS_INDEX_STEP)
      elements.get(i)
    }

//    val elements = new Array[Any](NODE_SIZE)
    val elements = new AtomicReferenceArray[Any](NODE_SIZE)

//    @volatile
//    var next = null
    var next = new AtomicReference[Node](null)

    @volatile
    var addHint = 0

    @volatile
    var deleteHint = 0
  }

  object Node {

    val NODE_SIZE = 1 << 11

    val NODE_SIZE_MIN_ONE = NODE_SIZE - 1

//    val NEXT_OFFSET = UNSAFE.objectFieldOffset(classOf[Node].getDeclaredField("next"))

//    val ELEMENTS_OFFSET = UNSAFE.arrayBaseOffset(classOf[Array[Any]])

//    val ELEMENTS_INDEX_STEP = UNSAFE.arrayIndexScale(classOf[Array[Any]])

  }

  final class CUQIterator[A](private var current: Node) extends Iterator[A] {
    private var nextElem: Any = null
    private var index = current.deleteHint

    override def hasNext(): Boolean = {
      if (nextElem != null) {
        return true
      }

      while (current != null) {
        var v: Any = null
        while (index < Node.NODE_SIZE && { v = current.get(index); v == DELETED }) {
          index += 1
        }

        if (index < Node.NODE_SIZE) {
          if (v == null) {
            current.addHint = index
            return false
          }

          nextElem = v
          index += 1 // we would'nt want to give the same element back, wouldn't we ?
          return true
        }

        current = current.next.get
        index = current.deleteHint
      }

      return false
    }

    override def next(): A = {
      if (hasNext()) {
        val ret = nextElem
        nextElem = null
        return ret.asInstanceOf[A]
      } else {
        throw new NoSuchElementException("next on empty iterator")
      }
    }
  }

  private val UNSAFE = {
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

  private val HEAD_OFFSET = UNSAFE.objectFieldOffset(classOf[ConcurrentUnrolledQueue[_]].getDeclaredField("head"))

  private val TAIL_OFFSET = UNSAFE.objectFieldOffset(classOf[ConcurrentUnrolledQueue[_]].getDeclaredField("tail"))

  private val DELETED = new AnyRef()

}

