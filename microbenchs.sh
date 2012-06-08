#!/bin/bash

RUNS=20
ELEMENTS=20000000

if [ $# != 0 ]
then
  QUEUES=$@
else
  QUEUES=queue_implementations/*.scala
fi

print()
{
  color=$(echo $1 | tr '[A-Z]' '[a-z]')
  shift
  case $color in
    red)
      echo -e -n "\e[1;31m" ;;

    yellow)
      echo -e -n "\e[1;33m" ;;

    blue)
      echo -e -n "\e[1;34m" ;;

    purple)
      echo -e -n "\e[1;35m" ;;

    *)
                            ;;
  esac
  echo -e "${@}\e[0m"
}

set_queue_implementation()
{
  ln -s -f "$(pwd)/$1" src/main/scala/scala/concurrent/ConcurrentUnrolledQueue.scala
}

sbt()
{
  $(which sbt) "$@" | grep -v info | expand --tabs=4
}

bench_singlethread()
{
  print purple "    -> 1 thread, $ELEMENTS elements"
  sbt "bench $ELEMENTS microbench.enqueue $RUNS"
  sbt "bench $ELEMENTS microbench.dequeue $RUNS"
}

bench_multithread()
{
  for nthread in 2 4 8
  do
    print purple "    -> $nthread threads, $ELEMENTS elements"
    sbt "bench $ELEMENTS $nthread microbench.concurrentenqueue $RUNS"
    sbt "bench $ELEMENTS $nthread microbench.concurrentdequeue $RUNS"
  done
}

bench_reference_singlethread()
{
  print purple "    -> 1 thread, $ELEMENTS elements"
  sbt "bench $ELEMENTS microbench.linkedenqueue $RUNS"
  sbt "bench $ELEMENTS microbench.linkeddequeue $RUNS"
}

bench_reference_multithread()
{
  for nthread in 2 4 8
  do
    print purple "    -> $nthread threads, $ELEMENTS elements"
    sbt "bench $ELEMENTS $nthread microbench.concurrentlinkedenqueue $RUNS"
    sbt "bench $ELEMENTS $nthread microbench.concurrentlinkeddequeue $RUNS"
  done
}

print red "benchmarking reference ConcurrentLinkedQueue implementation"
bench_reference_singlethread
bench_reference_multithread

for queue in $QUEUES
do
  print red "benchmarking $queue"
  set_queue_implementation "$queue"
  bench_singlethread "$queue"
  bench_multithread "$queue"
done

