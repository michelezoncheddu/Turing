#!/bin/bash

SCRIPTPATH=$(cd $(dirname "$0") && pwd)
SRCPATH=$SCRIPTPATH/src

cd $SCRIPTPATH

mkdir lib
cd lib
curl http://central.maven.org/maven2/org/json/json/20180813/json-20180813.jar -o jsonlib.jar

cd $SRCPATH

javac -sourcepath . -classpath ../lib/jsonlib.jar turing/server/*.java -d ../bin/
javac -sourcepath . -classpath ../lib/jsonlib.jar turing/client/*.java -d ../bin/
