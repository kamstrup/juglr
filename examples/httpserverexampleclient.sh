#! /bin/bash

i=1
while [ $i -lt 1000 ]; do
	curl -XGET http://localhost:4567/calc -d "{\"isPrime\" : $i}"
	echo # newline
	i=$[$i+1]
done
