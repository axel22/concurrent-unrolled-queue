A lock-free FIFO queue implementation
=====================================
Instead of a more standard linked list, the implementation is backed up by an unrolled linked list (a list whose nodes contain multiple elements). Doing so, we hope to improve the performance of both enqueue and dequeue implementations, because of less heap allocations and increased cache usage). The results can be found in report/concurrent\_unrolled\_queue.pdf


Testing
=======
The SBT script allows a "test" target which will run several test on the current queue implementation. You might want to try different implementations. They can be found under queue_implementations/* . The "current" implementation is defined under src/main/scala/scala/concurrent/ConcurrentUnrolledQueue.scala


Performance testing
===================
Performance testing can be done using the script microbenchs.sh [queue_implementation.scala ...]

