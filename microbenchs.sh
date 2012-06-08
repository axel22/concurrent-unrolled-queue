#!/bin/bash

RUNS=20
ELEMENTS_SINGLETHREAD=10000
ELEMENTS_MULTITHREAD=10000

if [ $# != 0 ]
then
  QUEUES=$@
else
  QUEUES=otherqueues/*.scala
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

run_singlethread()
{
  print purple "    -> benchmarking with 1 thread, $ELEMENTS elements, running $RUNS times"
  sbt "bench $ELEMENTS_SINGLETHREAD microbench.enqueue $RUNS"
}

run_multithread()
{
  for nthread in 2 3 4 5 6 7 8
  do
    print purple "    -> benchmarking with $nthread threads, $ELEMENTS elements, running $RUNS times"
    sbt "bench $ELEMENTS_MULTITHREAD $nthread microbench.concurrentenqueue $RUNS"
  done
}

for queue in $QUEUES
do
  print red "benchmarking $queue"
  set_queue_implementation "$queue"
  run_singlethread "$queue"
  run_multithread "$queue"
done

