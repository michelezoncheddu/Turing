#!/bin/bash

SCRIPTPATH=$(cd $(dirname "$0") && pwd)
SRCPATH=$SCRIPTPATH/src

cd $SCRIPTPATH

mkdir -p lib
mkdir -p bin

cd lib
if [ ! -f json-20180813.jar ]; then
	curl http://central.maven.org/maven2/org/json/json/20180813/json-20180813.jar -o json-20180813.jar
fi

cd $SRCPATH

javac -sourcepath . -classpath ../lib/json-20180813.jar turing/server/*.java -d ../bin/
javac -sourcepath . -classpath ../lib/json-20180813.jar turing/client/*.java -d ../bin/
