#!/bin/bash

RUNS=40
ELEMENTS_SINGLETHREAD=200
ELEMENTS_MULTITHREAD=20000

if [ $# != 0 ]
then
  QUEUES=$@
else
  QUEUES=otherqueues/*.scala
fi

CLEAN_SBT_OUTPUT="grep -v info" | expand --tabs=2

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
  print purple "    -> benchmarking with 1 thread, $ELEMENTS_SINGLETHREAD elements, running $RUNS times"
  sbt "bench $ELEMENTS_SINGLETHREAD microbench.enqueue $RUNS"
}

run_multithread()
{
  for nthread in 2 4 8
  do
    print purple "    -> benchmarking with $nthread threads, $ELEMENTS_MULTITHREAD / thread, running $RUNS times"
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

