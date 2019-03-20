#!/bin/bash

SCRIPTPATH=$(cd $(dirname "$0") && pwd)
BINPATH=$SCRIPTPATH/bin

cd $BINPATH

java -classpath .:../lib/jsonlib.jar turing/server/Server
