#!/bin/bash

SCRIPTPATH=$(cd $(dirname "$0") && pwd)
BINPATH=$SCRIPTPATH/bin

cd $BINPATH

java -classpath .:../lib/json-20180813.jar -Djava.net.preferIPv4Stack=true turing/server/Server
