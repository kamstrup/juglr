#! /bin/bash

i=1
while [ $i -lt 1000 ]; do
	curl http://localhost:4567/actor/calc/ -d "{\"isPrime\" : $i}"
	echo # newline
	i=$[$i+1]
done
